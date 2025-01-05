package br.com.granzoto.higor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.bytedeco.ffmpeg.global.avutil;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Download;
import com.google.api.services.drive.model.File;

public class VideoCompressor {

    private static final Logger LOGGER = Logger.getLogger(VideoCompressor.class.getName());
    private static final Path DOWNLOAD_PATH = Path.of(System.getProperty("user.home"), "Vídeos", "Original_Videos");
    private static final Path UPLOAD_PATH = Path.of(System.getProperty("user.home"), "Vídeos", "Compressed_Videos");

    private Drive driveService;

    public VideoCompressor(Drive driveService) {
        this.driveService = driveService;
    }

    public void compressVideo(File file) throws IOException {
        var downloadedFile = this.downloadFile(file);
        var fileToUpload = this.executeFFmpegCompression(downloadedFile);
        if (fileToUpload.exists()) {
            this.uploadFile(fileToUpload, null);
        }
    }

    private java.io.File executeFFmpegCompression(java.io.File downloadFile) {

        var uploadFile = Path.of(UPLOAD_PATH.toString(), downloadFile.getName()).toFile();
        try {
            avutil.av_log_set_level(avutil.AV_LOG_INFO);

            ProcessBuilder builder = new ProcessBuilder(
                    "ffmpeg",
                    "-loglevel", "info",
                    "-i", "\""+downloadFile.getAbsolutePath()+"\"",
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-vf", "scale=1280:-1",
                    "\""+uploadFile.getAbsolutePath()+"\"");
            LOGGER.info(builder.command().toString());
            Process process = builder.start();
            process.waitFor();

            if (process.exitValue() == 0) {
                LOGGER.info("Video compression success " + uploadFile.getName());
            } else {
                LOGGER.warning("Vídeo not compressed. Exit code " + process.exitValue());
            }
        } catch (Exception e) {
            LOGGER.severe("Video compression failed: " + e.getMessage());
        }

        return uploadFile;

    }

    private java.io.File downloadFile(File file) throws IOException {
        LOGGER.info("Downloading name: " + file.getName() + "; size: "
                + FileUtils.byteCountToDisplaySize(BigInteger.valueOf(Long.valueOf(file.get("size").toString()))));
        var downloadFile = Path.of(DOWNLOAD_PATH.toString(), file.getName()).toFile();
        if (!downloadFile.exists()) {
            OutputStream outputStream = new FileOutputStream(downloadFile);
            // driveService.files().download(file.getId()).executeAndDownloadTo(outputStream);
            driveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
            LOGGER.info("Download finish");
        } else {
            LOGGER.warning("Download file already exists. Skipping Download step.");
        }
        return downloadFile;
    }

    public void uploadFile(java.io.File file, String parentId) throws IOException {
        LOGGER.info("Uploading file name: " + file.getName() + "; size: "
                + FileUtils.byteCountToDisplaySize(FileUtils.sizeOfAsBigInteger(file)));

        File fileMetadata = new File();
        fileMetadata.setName(file.getName());
        if (parentId != null) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }

        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata,
                new com.google.api.client.http.FileContent("video/mp4", file))
                .setFields("id")
                .execute();

        LOGGER.info("Uploaded file ID: " + uploadedFile.getId());
    }

    // public static void main(String[] args) {
    // try {
    // authenticateGoogleDrive();

    // String folderId = null; // Set your folder ID here if needed
    // List<File> videoFiles = listVideoFiles(folderId);

    // Path tempDir = Files.createTempDirectory("video_compressor");

    // for (File file : videoFiles) {
    // System.out.println("Processing file: " + file.getName());
    // Path originalPath = tempDir.resolve(file.getName());
    // Path compressedPath = tempDir.resolve("compressed_" + file.getName());

    // // Download file
    // downloadFile(file, originalPath);

    // // Compress video
    // if (compressVideo(originalPath.toString(), compressedPath.toString(), 1280,
    // 720)) {
    // // Upload compressed file
    // uploadFile(file.getName(), compressedPath, folderId);

    // // Delete original file
    // driveService.files().delete(file.getId()).execute();
    // System.out.println("Replaced original with compressed version: " +
    // file.getName());
    // } else {
    // System.err.println("Failed to compress file: " + file.getName());
    // }

    // // Clean up local files
    // Files.deleteIfExists(originalPath);
    // Files.deleteIfExists(compressedPath);
    // }

    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
}
