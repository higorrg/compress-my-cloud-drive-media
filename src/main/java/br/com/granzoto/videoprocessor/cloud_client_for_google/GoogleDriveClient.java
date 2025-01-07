package br.com.granzoto.videoprocessor.cloud_client_for_google;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

import br.com.granzoto.videoprocessor.cloud_client.CloudClient;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientUploadException;
import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.model.VideoCompressionFileFactory;

public class GoogleDriveClient implements CloudClient {

    private static final Logger LOGGER = Logger.getLogger(GoogleDriveClient.class.getName());

    private static final String APPLICATION_NAME = "Compress My Google Drive Videos";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final int PAGE_SIZE = 50;

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
    public List<VideoCompressionFile> listFiles() throws CloudClientListFilesException {
        List<VideoCompressionFile> result = new ArrayList<>();
        String pageToken = null;
        this.listFilesByPage(pageToken, result);
        return result;
    }

    private List<VideoCompressionFile> listFilesByPage(String page, List<VideoCompressionFile> files)
            throws CloudClientListFilesException {
        try {
            FileList googledDriveFiles = this.drive.files().list()
                    .setQ("'me' in owners and trashed = false and mimeType contains 'video/' and modifiedTime < '2024-01-04T00:00:00'")
                    // .setQ("trashed = false and name = '100_0323.MOV'")
                    .setSpaces("drive")
                    .setPageSize(PAGE_SIZE)
                    .setFields("nextPageToken, files(id, name, size, parents)")
                    .setPageToken(page)
                    .execute();
            googledDriveFiles.getFiles().forEach(googleFile -> {
                VideoCompressionFile compressionFile = VideoCompressionFileFactory
                        .createVideoCompressionFileFromGoogleFile(googleFile);
                files.add(compressionFile);
            });
            String nextPageToken = googledDriveFiles.getNextPageToken();
            if (!Objects.isNull(nextPageToken)) {
                this.listFilesByPage(nextPageToken, files);
            }
        } catch (IOException e) {
            throw new CloudClientListFilesException("Unable to get files from Google Drive", e);
        }
        return files;
    }

    @Override
    public void downloadVideo(VideoCompressionFile compressionFile, File inputFile)
            throws CloudClientDownloadException {
        LOGGER.info("Downloading file: " + compressionFile.name());
        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            this.drive.files().get(compressionFile.id()).executeMediaAndDownloadTo(outputStream);
            LOGGER.info("Download successfuly finished");
        } catch (IOException e) {
            throw new CloudClientDownloadException("Download from Google Drive failed", e);
        }
    }

    @Override
    public void uploadVideo(File outputFile, VideoCompressionFile compressionFile) throws CloudClientUploadException {
        LOGGER.info("Uploading file: " + outputFile.getName() + "; size: "
                + FileUtils.byteCountToDisplaySize(FileUtils.sizeOfAsBigInteger(outputFile)));

        try {
            var googleFile = VideoCompressionFileFactory.createGoogleFileFromVideoCompressionFile(compressionFile);
            this.drive.files().update(compressionFile.id(), googleFile).execute();
            LOGGER.info("Uploaded successfuly finished");
        } catch (IOException e) {
            throw new CloudClientUploadException("Upload to Google Drive Failed", e);
        }
    }
}
