package br.com.granzoto.higor;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class VideoCompressor {

   private static Drive driveService;

   public static List<File> listVideoFiles() throws IOException {
       String query = "mimeType contains 'video/'";

       FileList result = driveService.files().list()
               .setQ(query)
               .setSpaces("drive")
               .setFields("files(id, name)")
               .execute();

       return result.getFiles();
   }
//
//    public static boolean compressVideo(String inputPath, String outputPath, int width, int height) {
//        try {
//            // Initialize FFmpeg components
//            avformat.av_register_all();
//            avcodec.avcodec_register_all();
//            avutil.av_log_set_level(avutil.AV_LOG_QUIET);
//
//            // Execute FFmpeg compression
//            ProcessBuilder builder = new ProcessBuilder(
//                    "ffmpeg",
//                    "-i", inputPath,
//                    "-vf", "scale=" + width + ":-1",
//                    "-c:v", "libx264",
//                    "-c:a", "aac",
//                    outputPath
//            );
//            Process process = builder.start();
//            process.waitFor();
//
//            return process.exitValue() == 0;
//        } catch (Exception e) {
//            System.err.println("Error compressing video: " + e.getMessage());
//            return false;
//        }
//    }
//
//    public static void downloadFile(File file, Path downloadPath) throws IOException {
//        OutputStream outputStream = Files.newOutputStream(downloadPath);
//        driveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
//    }
//
//    public static void uploadFile(String fileName, Path filePath, String parentId) throws IOException {
//        File fileMetadata = new File();
//        fileMetadata.setName(fileName);
//        if (parentId != null) {
//            fileMetadata.setParents(Collections.singletonList(parentId));
//        }
//
//        java.io.File fileContent = filePath.toFile();
//        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata,
//                new com.google.api.client.http.FileContent("video/mp4", fileContent))
//                .setFields("id")
//                .execute();
//
//        System.out.println("Uploaded file ID: " + uploadedFile.getId());
//    }
//
//    public static void main(String[] args) {
//        try {
//            authenticateGoogleDrive();
//
//            String folderId = null; // Set your folder ID here if needed
//            List<File> videoFiles = listVideoFiles(folderId);
//
//            Path tempDir = Files.createTempDirectory("video_compressor");
//
//            for (File file : videoFiles) {
//                System.out.println("Processing file: " + file.getName());
//                Path originalPath = tempDir.resolve(file.getName());
//                Path compressedPath = tempDir.resolve("compressed_" + file.getName());
//
//                // Download file
//                downloadFile(file, originalPath);
//
//                // Compress video
//                if (compressVideo(originalPath.toString(), compressedPath.toString(), 1280, 720)) {
//                    // Upload compressed file
//                    uploadFile(file.getName(), compressedPath, folderId);
//
//                    // Delete original file
//                    driveService.files().delete(file.getId()).execute();
//                    System.out.println("Replaced original with compressed version: " + file.getName());
//                } else {
//                    System.err.println("Failed to compress file: " + file.getName());
//                }
//
//                // Clean up local files
//                Files.deleteIfExists(originalPath);
//                Files.deleteIfExists(compressedPath);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}

