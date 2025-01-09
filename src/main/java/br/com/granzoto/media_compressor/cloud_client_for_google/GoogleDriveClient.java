package br.com.granzoto.media_compressor.cloud_client_for_google;

import br.com.granzoto.media_compressor.cloud_client.*;
import br.com.granzoto.media_compressor.model.CompressionFile;
import br.com.granzoto.media_compressor.model.CompressionFileFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class GoogleDriveClient implements CloudClient {

    private static final Logger LOGGER = Logger.getLogger(GoogleDriveClient.class.getName());

    private static final String APPLICATION_NAME = "Compress My Google Drive Media";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final int PAGE_SIZE = 50;

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
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(transport, JSON_FACTORY,
                GoogleDriveAuth.getCredentials(transport, JSON_FACTORY))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public List<CompressionFile> listFiles() throws CloudClientListFilesException {
        LOGGER.info("Listing media files from your Google Drive");
        LOGGER.info("Name | MimeSuperType | Size");
        List<CompressionFile> result = new ArrayList<>();
        this.listFilesByPage(null, result);
        BigInteger totalSize = result.stream()
                .map(CompressionFile::size)
                .reduce(BigInteger.ZERO, BigInteger::add);
        LOGGER.info("");
        LOGGER.info("Total size: " + FileUtils.byteCountToDisplaySize(totalSize));
        LOGGER.info("Total items: " + result.size());
        LOGGER.info("");
        return result;
    }

    private void listFilesByPage(String page, List<CompressionFile> files)
            throws CloudClientListFilesException {
        try {
            FileList googledDriveFiles = this.drive.files().list()
                    .setQ("""
                            'me' in owners and
                            trashed = false and
                            (mimeType contains 'video/' or
                            mimeType contains 'image/')
                            """)
//                    .setQ("trashed = false and name = 'IMG_20200926_172840.jpg'")
                    .setSpaces("drive")
                    .setPageSize(PAGE_SIZE)
                    .setFields("nextPageToken, files(id, name, size, parents, mimeType)")
                    .setPageToken(page)
                    .execute();
            googledDriveFiles.getFiles().forEach(googleFile -> {
                CompressionFile compressionFile = CompressionFileFactory
                        .createCompressionFileFromGoogleFile(googleFile);
                files.add(compressionFile);
                LOGGER.info(compressionFile.toString());
            });
            String nextPageToken = googledDriveFiles.getNextPageToken();
            if (!Objects.isNull(nextPageToken)) {
                this.listFilesByPage(nextPageToken, files);
            }
        } catch (IOException e) {
            throw new CloudClientListFilesException("Unable to get files from Google Drive", e);
        }
    }

    @Override
    public void downloadFile(CompressionFile compressionFile, File inputFile)
            throws CloudClientDownloadException {
        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            LOGGER.info("Downloading file: " + compressionFile.name());
            this.drive.files().get(compressionFile.id()).executeMediaAndDownloadTo(outputStream);
            LOGGER.info("Download successfully finished");
        } catch (IOException e) {
            throw new CloudClientDownloadException("Download from Google Drive failed", e);
        }
    }

    @Override
    public void uploadFile(File outputFile, CompressionFile compressionFile) throws CloudClientUploadException {
        try {
            LOGGER.info("Uploading file: " + outputFile.getName() + "; size: "
                    + FileUtils.byteCountToDisplaySize(FileUtils.sizeOfAsBigInteger(outputFile)));
            var googleFile = new com.google.api.services.drive.model.File();
            var mediaContent = new FileContent("video/mp4", outputFile);
            this.drive.files().update(compressionFile.id(), googleFile, mediaContent)
                    .setFields("id, name, mimeType")
                    .execute();
            LOGGER.info("Upload successfully finished");
        } catch (IOException e) {
            throw new CloudClientUploadException("Upload to Google Drive failed", e);
        }
    }

    @Override
    public void delete(CompressionFile myFile) throws CloudClientDeleteException {
        try {
            this.drive.files().delete(myFile.id()).execute();
            LOGGER.info("File successfully deleted "+myFile.name());
        } catch (IOException e) {
            throw new CloudClientDeleteException("Delete Google Drive file failed: " + myFile.name(), e);
        }
    }
}
