package br.com.granzoto.media_compressor.cloud_client_for_google;

import br.com.granzoto.media_compressor.cloud_client.*;
import br.com.granzoto.media_compressor.workflow.FirstCloudClientHandler;
import br.com.granzoto.media_compressor.model.CompressionFile;
import br.com.granzoto.media_compressor.model.FolderInfo;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleDriveClient implements CloudClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveClient.class.getName());

    private static final String APPLICATION_NAME = "Compress My Google Drive Media";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final int PAGE_SIZE = 50;
    public static final String APPLICATION_VND_GOOGLE_APPS_FOLDER = "application/vnd.google-apps.folder";

    private static GoogleDriveClient instance;

    private final Drive drive;

    private final CloudClientHandler firstHandler = new FirstCloudClientHandler();

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
    public void addHandler(CloudClientHandler handler) {
        if (Objects.isNull(handler)){
            throw new IllegalArgumentException("Cloud client handler can't be null");
        }
        this.firstHandler.link(handler);
    }

    @Override
    public void runFiles() throws CloudClientListFilesException {
        this.firstHandler.handleStart(this);
        Map<String, FolderInfo> folderPaths = new HashMap<>();
        List<CompressionFile> files = new ArrayList<>();
        this.listFilesByPage(null, folderPaths, files);
        this.firstHandler.handleEnd(files);
    }

    private void listFilesByPage(String page, Map<String, FolderInfo> folderPaths, List<CompressionFile> files)
            throws CloudClientListFilesException {
        try {
            FileList googleDriveFiles = getFileList(page);

            for (var googleFile : googleDriveFiles.getFiles()) {
                if (APPLICATION_VND_GOOGLE_APPS_FOLDER.equals(googleFile.getMimeType())) {
                    String parentId = !Objects.isNull(googleFile.getParents()) ? googleFile.getParents().getFirst() : null;
                    folderPaths.put(googleFile.getId(), new FolderInfo(googleFile.getName(), parentId));
                } else {
                    CompressionFile compressionFile = CompressionFileFromGoogleFileFactory.createCompressionFile(googleFile, folderPaths);
                    files.add(compressionFile);
                    this.firstHandler.handleItem(compressionFile);
                }
            }

            String nextPageToken = googleDriveFiles.getNextPageToken();
            if (nextPageToken != null) {
                this.listFilesByPage(nextPageToken, folderPaths, files);
            }
        } catch (IOException e) {
            throw new CloudClientListFilesException("Unable to get files from Google Drive", e);
        }
    }

    private FileList getFileList(String page) throws IOException {
        return this.drive.files().list()
                .setQ("""
                        'me' in owners and
                        trashed = false and
                        (mimeType='application/vnd.google-apps.folder' or
                        mimeType contains 'video/' or
                        mimeType contains 'image/'or
                        mimeType = 'application/pdf') and
                        modifiedTime < '2024-01-04T00:00:00'
                        """)
                .setSpaces("drive")
                .setPageSize(PAGE_SIZE)
                .setFields("nextPageToken, files(id, name, parents, size, mimeType)")
                .setOrderBy("folder")
                .setPageToken(page)
                .execute();
    }

    @Override
    public void downloadFile(CompressionFile compressionFile)
            throws CloudClientDownloadException {
        try (OutputStream outputStream = new FileOutputStream(compressionFile.originalFile())) {
            LOGGER.info("Downloading file: {}", compressionFile.name());
            this.drive.files().get(compressionFile.id()).executeMediaAndDownloadTo(outputStream);
            LOGGER.info("Download successfully finished");
        } catch (IOException e) {
            throw new CloudClientDownloadException("Download from Google Drive failed", e);
        }
    }

    /**
     * It's important to note that set the name is not mandatory on upload,
     * but it's done to assure what @{@link br.com.granzoto.media_compressor.model.FileExtensionFixer}
     * meant to fix.
     * @see br.com.granzoto.media_compressor.model.FileExtensionFixer
     * @param compressionFile The entity that represents the cloud drive been passed through the chain
     * @throws CloudClientUploadException Wrap IOException
     */
    @Override
    public void uploadFile(CompressionFile compressionFile) throws CloudClientUploadException {
        try {
            LOGGER.info("Uploading file: {}; size: {}", compressionFile.compressedFile().getName(), FileUtils.byteCountToDisplaySize(FileUtils.sizeOfAsBigInteger(compressionFile.compressedFile())));
            var googleFile = new com.google.api.services.drive.model.File();
            googleFile.setName(compressionFile.name());
            var mediaContent = new FileContent(compressionFile.mimeType(), compressionFile.compressedFile());
            this.drive.files().update(compressionFile.id(), googleFile, mediaContent)
                    .setFields("id, name, mimeType")
                    .execute();
            LOGGER.info("Upload successfully finished");
        } catch (IOException e) {
            throw new CloudClientUploadException("Upload to Google Drive failed", e);
        }
    }

}
