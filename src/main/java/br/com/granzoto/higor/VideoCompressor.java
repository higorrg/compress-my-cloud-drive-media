package br.com.granzoto.higor;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class VideoCompressor {

    private static final Logger LOGGER = Logger.getLogger(VideoCompressor.class.getName());
    private static final Path DOWNLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos", "Original_Videos");
    private static final Path UPLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos", "Compressed_Videos");

    private Drive driveService;

    public VideoCompressor(Drive driveService) {
        this.driveService = driveService;
    }

    public void compressVideo(CompressMyFile myFile) throws IOException {
        var downloadedFile = this.downloadFile(myFile);
        var fileToUpload = this.executeFFmpegCompression(downloadedFile);
        // if (fileToUpload.exists()) {
        //     this.uploadFile(fileToUpload, myFile);
        // }
    }

    private java.io.File executeFFmpegCompression(java.io.File downloadFile) {
        return this.executeFFmpegCompressionOnHost(downloadFile);
    }

    private java.io.File executeFFmpegCompressionOnJava(java.io.File downloadFile) {
        var inputPath = Path.of(DOWNLOAD_PATH.toString(), downloadFile.getName()).toFile();
        var outputPath = Path.of(UPLOAD_PATH.toString(), downloadFile.getName()).toFile();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, grabber.getImageWidth(),
                        grabber.getImageHeight(), grabber.getAudioChannels())) {

            grabber.start();

            if (grabber.getAudioChannels() > 0) {
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // AAC codec
                // recorder.setSampleRate(grabber.getSampleRate());
                // recorder.setAudioBitrate(grabber.getAudioBitrate());
            }

            // Configure video
            // recorder.setPixelFormat(avutil.AV_PIX_FMT_RGB32);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            //recorder.setVideoBitrate(8000 * 1024); // Adjust bitrate (2 Mbps example)
            //recorder.setFrameRate(grabber.getFrameRate());
            recorder.setFormat("mp4");

            // Adjust video scaling
            int targetWidth = 1280;
            int targetHeight = (grabber.getImageHeight() * targetWidth) / grabber.getImageWidth();
            // int targetWidth = grabber.getImageWidth();
            // int targetHeight = grabber.getImageHeight();

            recorder.setImageWidth(targetWidth);
            recorder.setImageHeight(targetHeight);

            // Start the recorder
            recorder.start();

            // Read and write frames
            while (grabber.grab() != null) {
                recorder.record(grabber.grab());
            }

            // Stop grabber and recorder
            recorder.stop();
            grabber.stop();

            LOGGER.info("Compression completed: " + outputPath);
        } catch (Exception e) {
            LOGGER.severe("Compression failed, output file will be deleted.");
            outputPath.deleteOnExit();
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            return Path.of(FileUtils.getTempDirectory().getPath(), "FileDontExists.tmp").toFile();
        }

        return outputPath;
    }

    private java.io.File executeFFmpegCompressionOnHost(java.io.File downloadFile) {

        var uploadFile = Path.of(UPLOAD_PATH.toString(), downloadFile.getName()).toFile();
        try {
            String[] cmd = {"ffmpeg",
                    "-y",
                    "-loglevel", "info",
                    "-i", downloadFile.getAbsolutePath().replaceAll(" ", "\\ "),
                    "-c:v", "h264",
                    "-c:a", "aac",
                    "-crf", "23",
                    "-pix_fmt", "yuv420p",
                    // "-vf", "scale=1280:-1",
                    uploadFile.getAbsolutePath().replaceAll(" ", "\\ ")};
            LOGGER.info(Arrays.toString(cmd));

            Process process = new ProcessBuilder().command(cmd).inheritIO().start();
            var exitCode = process.waitFor();
            System.out.println("");

            if (exitCode == 0) {
                LOGGER.info("Video compression success " + uploadFile.getName());
            } else {
                LOGGER.warning("Vídeo not compressed. Exit code " + exitCode);
            }
        } catch (Exception e) {
            LOGGER.severe("Video compression failed: " + e.getMessage());
            return Path.of(FileUtils.getTempDirectory().getPath(), "FileDontExists.tmp").toFile();
        }

        return uploadFile;

    }

    private java.io.File downloadFile(CompressMyFile myFile) throws IOException {
        LOGGER.info("Downloading : " + myFile.name());
        var downloadFile = Path.of(DOWNLOAD_PATH.toString(), myFile.name()).toFile();
        if (!downloadFile.exists()) {
            OutputStream outputStream = new FileOutputStream(downloadFile);
            driveService.files().get(myFile.id()).executeMediaAndDownloadTo(outputStream);
            LOGGER.info("Download finish");
        } else {
            LOGGER.warning("Download file already exists. Skipping Download step.");
        }
        return downloadFile;
    }

    public void uploadFile(java.io.File googleFile, CompressMyFile myFile) throws IOException {
        LOGGER.info("Uploading file name: " + googleFile.getName() + "; size: "
                + FileUtils.byteCountToDisplaySize(FileUtils.sizeOfAsBigInteger(googleFile)));

        File fileMetadata = new File();
        fileMetadata.setName(googleFile.getName());
        if (!Objects.isNull(myFile.parentId())) {
            fileMetadata.setParents(Collections.singletonList(myFile.parentId()));
        }

        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata,
                new com.google.api.client.http.FileContent("video/mp4", googleFile))
                .setFields("id")
                .setFields("parents")
                .execute();

        LOGGER.info("Uploaded file ID: " + uploadedFile.getId());
    }

}
