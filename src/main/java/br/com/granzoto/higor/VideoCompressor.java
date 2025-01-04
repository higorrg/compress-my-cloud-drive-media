package br.com.granzoto.higor;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.apache.commons.io.FileUtils;
import org.bytedeco.ffmpeg.ffmpeg;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Loader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class VideoCompressor {

    private static final Logger LOGGER = Logger.getLogger(VideoCompressor.class.getName());
    private static final Path DOWNLOAD_PATH = Path.of(System.getProperty("user.home"), "Vídeos", "Original_Videos");
    private static final Path UPLOAD_PATH = Path.of(System.getProperty("user.home"), "Vídeos", "Compressed_Videos");

    private Drive driveService;

    public VideoCompressor(Drive driveService) {
        this.driveService = driveService;
    }

    public boolean compressVideo(File file) throws IOException {
        var downloadFile = this.downloadFile(file);
        return this.executeFFmpegCompression(downloadFile);
    }

    private java.io.File executeFFmpegCompression(java.io.File downloadFile) {
        try {
            var uploadFile = Path.of(UPLOAD_PATH.toString(), downloadFile.getName()).toFile();
            avutil.av_log_set_level(avutil.AV_LOG_INFO);

            ProcessBuilder builder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", downloadFile.getAbsolutePath(),
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    uploadFile.getAbsolutePath());
            Process process = builder.start();
            process.waitFor();

            if (process.exitValue() == 0){
                return uploadFile;
            }
        } catch (Exception e) {
            System.err.println("Error compressing video: " + e.getMessage());            
        }

    }

    private java.io.File downloadFile(File file) throws IOException {
        LOGGER.info("Downloading name: " + file.getName() + "; size: "
                + FileUtils.byteCountToDisplaySize(BigInteger.valueOf(Long.valueOf(file.get("size").toString()))));        
        var downloadFile = Path.of(DOWNLOAD_PATH.toString(), file.getName()).toFile();
        OutputStream outputStream = new FileOutputStream(DOWNLOAD_PATH.toFile());
        driveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
        LOGGER.info("Download finish");
        return downloadFile;
    }

    public void uploadFile(String fileName, Path filePath, String parentId) throws IOException {
        LOGGER.info("Uploading file name: "+fileName+"; size: "+FileUtils.byteCountToDisplaySize(FileUtils.sizeOfAsBigInteger(filePath.toFile())));

        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        if (parentId != null) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }
        
        java.io.File fileContent = filePath.toFile();
        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata,
                new com.google.api.client.http.FileContent("video/mp4", fileContent))
                .setFields("id")
                .execute();

        LOGGER.info("Uploaded file ID: " + uploadedFile.getId());
    }

    public static void main(String[] args) {
        try {
            authenticateGoogleDrive();

            String folderId = null; // Set your folder ID here if needed
            List<File> videoFiles = listVideoFiles(folderId);

            Path tempDir = Files.createTempDirectory("video_compressor");

            for (File file : videoFiles) {
                System.out.println("Processing file: " + file.getName());
                Path originalPath = tempDir.resolve(file.getName());
                Path compressedPath = tempDir.resolve("compressed_" + file.getName());

                // Download file
                downloadFile(file, originalPath);

                // Compress video
                if (compressVideo(originalPath.toString(), compressedPath.toString(), 1280,
                        720)) {
                    // Upload compressed file
                    uploadFile(file.getName(), compressedPath, folderId);

                    // Delete original file
                    driveService.files().delete(file.getId()).execute();
                    System.out.println("Replaced original with compressed version: " +
                            file.getName());
                } else {
                    System.err.println("Failed to compress file: " + file.getName());
                }

                // Clean up local files
                Files.deleteIfExists(originalPath);
                Files.deleteIfExists(compressedPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
