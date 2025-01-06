package br.com.granzoto.higor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.video_compressor.FFmpegCompressorWithHost;

public class VideoCompressionOld {

    private static final Logger LOGGER = Logger.getLogger(VideoCompressionOld.class.getName());
    private static final Path DOWNLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos", "Original_Videos");
    private static final Path UPLOAD_PATH = Path.of(FileUtils.getUserDirectoryPath(), "Vídeos", "Compressed_Videos");

    private Drive driveService;

    public VideoCompressionOld(Drive driveService) {
        this.driveService = driveService;
    }

    public void compressVideo(VideoCompressionFile myFile) throws IOException {
        var downloadedFile = this.downloadFile(myFile);
        var uploadFile = Path.of(UPLOAD_PATH.toString(), downloadedFile.getName()).toFile();
        if (this.executeFFmpegCompression(downloadedFile, uploadFile)){
            //     this.uploadFile(fileToUpload, myFile);
        }
    }

    private boolean executeFFmpegCompression(java.io.File downloadFile, java.io.File uploadFile) {
        return new FFmpegCompressorWithHost().executeCompression(downloadFile, uploadFile);
    }

    private java.io.File downloadFile(VideoCompressionFile myFile) throws IOException {
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

    public void uploadFile(java.io.File googleFile, VideoCompressionFile myFile) throws IOException {
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
