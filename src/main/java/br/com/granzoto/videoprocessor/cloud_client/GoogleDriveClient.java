package br.com.granzoto.videoprocessor.cloud_client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import br.com.granzoto.videoprocessor.cloud_client_for_google.GoogleDriveAuth;
import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.model.VideoCompressionFileFactory;

public class GoogleDriveClient implements CloudClient {

    private static final Logger LOGGER = Logger.getLogger(GoogleDriveClient.class.getName());

    private static final String APPLICATION_NAME = "Compress My Google Drive Videos";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static NetHttpTransport HTTP_TRANSPORT;
    private static GoogleDriveClient instance;

    private final Drive drive;

    public static synchronized GoogleDriveClient getInstance() {
        if (instance == null) {
            try {
                instance = new GoogleDriveClient();
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException("Fail to initialize Google Drive Client", e);
            }
        }
        return instance;
    }

    private GoogleDriveClient() throws GeneralSecurityException, IOException {
        this.drive = initializeDrive();
    }

    private Drive initializeDrive() throws GeneralSecurityException, IOException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                GoogleDriveAuth.getCredentials(HTTP_TRANSPORT, JSON_FACTORY))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public List<VideoCompressionFile> listFiles(String page) throws CloudClientListFilesException {
        List<VideoCompressionFile> result = new ArrayList<>();
        try {
            List<com.google.api.services.drive.model.File> googledDriveFiles = this.drive.files().list()
                    // .setQ("mimeType contains 'video/' and modifiedTime < '2024-01-04T00:00:00'")
                    .setQ("name = 'VID_20170328_215509.mp4'")
                    .setSpaces("drive")
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, size, parents)")
                    .setPageToken(page)
                    .execute()
                    .getFiles();
            googledDriveFiles.forEach(f -> {
                VideoCompressionFile file = VideoCompressionFileFactory.createFile(f);
                result.add(file);
            });
        } catch (IOException e) {
            throw new CloudClientListFilesException("Unable to get files from Google Drive", e);
        }
        return result;
    }

    @Override
    public void downloadVideo(VideoCompressionFile compressionFile, File inputFile)
            throws CloudClientDownloadException {
        LOGGER.info("Downloading : " + compressionFile.name());
        if (!inputFile.exists()) {
            try (OutputStream outputStream = new FileOutputStream(inputFile)) {
                this.drive.files().get(compressionFile.id()).executeMediaAndDownloadTo(outputStream);
                LOGGER.info("Download successfuly finished");
            } catch (IOException e) {
                throw new CloudClientDownloadException("Download from Google Drive failed", e);
            }
        } else {
            LOGGER.warning("Download file already exists. Skipping Download step.");
        }
    }

    @Override
    public void uploadVideo(File outputFile, VideoCompressionFile compressionFile) throws CloudClientUploadException {
        LOGGER.info("Uploading file name: " + outputFile.getName() + "; size: "
                + FileUtils.byteCountToDisplaySize(FileUtils.sizeOfAsBigInteger(outputFile)));

        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(outputFile.getName());
        if (!Objects.isNull(compressionFile.parentId())) {
            fileMetadata.setParents(Collections.singletonList(compressionFile.parentId()));
        }

        try {
            this.drive.files().create(fileMetadata,
                new com.google.api.client.http.FileContent("video/mp4", outputFile))
                .setFields("id")
                .setFields("parents")
                .execute();
            LOGGER.info("Uploaded successfuly finished");
        } catch (IOException e) {
            throw new CloudClientUploadException("Upload to Google Drive Failed", e);
        }
    }
}
