package br.com.granzoto.media_compressor.cloud_client_for_google;

import br.com.granzoto.media_compressor.cloud_client.AbstractCloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientRestoreException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientUploadException;
import br.com.granzoto.media_compressor.model.CompressionFile;
import br.com.granzoto.media_compressor.model.FolderInfo;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Revision;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GoogleDriveClient extends AbstractCloudClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveClient.class.getName());
    private static final String APPLICATION_NAME = "Compress My Google Drive Media";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final int PAGE_SIZE = 1000;
    private static final String APPLICATION_VND_GOOGLE_APPS_FOLDER = "application/vnd.google-apps.folder";
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
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        this.drive = new Drive.Builder(transport, JSON_FACTORY,
                GoogleDriveAuth.getCredentials(transport, JSON_FACTORY))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    protected void listFilesByPage(String page, Map<String, FolderInfo> folderPaths, List<CompressionFile> files)
            throws CloudClientListFilesException {
        try {
            FileList googleDriveFiles = getFileList(page);

            for (var googleFile : googleDriveFiles.getFiles()) {
                if (APPLICATION_VND_GOOGLE_APPS_FOLDER.equals(googleFile.getMimeType())) {
                    String parentId = !Objects.isNull(googleFile.getParents()) ? googleFile.getParents().getFirst() : null;
                    folderPaths.put(googleFile.getId(), new FolderInfo(googleFile.getName(), parentId));
                } else {
                    CompressionFile compressionFile = CompressionFileFromGoogleFileFactory.createCompressionFile(googleFile, folderPaths);
                    this.notifyItem(compressionFile);
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
                .setQ("trashed = false")
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
     * but it's done here to assure what {@link br.com.granzoto.media_compressor.model.FileExtensionFixer}
     * meant to fix.
     *
     * @param compressionFile The entity that represents the cloud drive been passed through the chain
     * @throws CloudClientUploadException Wrap IOException
     * @see br.com.granzoto.media_compressor.model.FileExtensionFixer
     */
    @Override
    public void uploadFile(CompressionFile compressionFile) throws CloudClientUploadException {
        try {
            LOGGER.info("Uploading file: {}; size: {}", compressionFile.compressedFile().getName(), FileUtils.byteCountToDisplaySize(FileUtils.sizeOfAsBigInteger(compressionFile.compressedFile())));
            var googleFile = new com.google.api.services.drive.model.File();
            googleFile.setName(compressionFile.name());
            googleFile.setMimeType(compressionFile.mimeType());
            var mediaContent = new FileContent(compressionFile.mimeType(), compressionFile.compressedFile());
            this.drive.files().update(compressionFile.id(), googleFile, mediaContent)
                    .setFields("id, name, mimeType")
                    .execute();
            LOGGER.info("Upload successfully finished");
        } catch (IOException e) {
            throw new CloudClientUploadException("Upload to Google Drive failed", e);
        }
    }

    /**
     * Google Drive keeps a limited revision history per file (by default, 30 days or the last
     * 100 revisions, whichever is reached first, unless a revision was pinned with "keep
     * forever"), so a matching revision isn't guaranteed to still exist.
     */
    @Override
    public boolean restoreOriginalRevision(CompressionFile compressionFile, String expectedMimeSuperType, boolean dryRun)
            throws CloudClientRestoreException {
        try {
            var revisionList = this.drive.revisions().list(compressionFile.id())
                    .setFields("revisions(id, mimeType, modifiedTime, size)")
                    .execute();
            List<Revision> revisions = revisionList.getRevisions();
            if (revisions == null) {
                LOGGER.warn("No revision history available for {} ({})", compressionFile.name(), compressionFile.id());
                return false;
            }

            String expectedMimePrefix = expectedMimeSuperType + "/";
            Optional<Revision> matchingRevision = revisions.stream()
                    .filter(revision -> revision.getMimeType() != null && revision.getMimeType().startsWith(expectedMimePrefix))
                    .min(Comparator.comparing(revision -> revision.getModifiedTime().getValue()));

            if (matchingRevision.isEmpty()) {
                LOGGER.warn("No {} revision found for {} ({}); current mimeType is {}",
                        expectedMimeSuperType, compressionFile.name(), compressionFile.id(), compressionFile.mimeType());
                return false;
            }

            Revision revision = matchingRevision.get();
            if (dryRun) {
                LOGGER.info("[DRY-RUN] Would restore {} ({}) from revision {} (mimeType={}, modifiedTime={})",
                        compressionFile.name(), compressionFile.id(), revision.getId(), revision.getMimeType(), revision.getModifiedTime());
                return true;
            }

            try (var revisionContent = new ByteArrayOutputStream()) {
                this.drive.revisions().get(compressionFile.id(), revision.getId()).executeMediaAndDownloadTo(revisionContent);

                var googleFile = new com.google.api.services.drive.model.File();
                googleFile.setMimeType(revision.getMimeType());
                var mediaContent = new ByteArrayContent(revision.getMimeType(), revisionContent.toByteArray());
                this.drive.files().update(compressionFile.id(), googleFile, mediaContent)
                        .setFields("id, name, mimeType")
                        .execute();
                LOGGER.info("Restored {} ({}) from revision {} (mimeType={})",
                        compressionFile.name(), compressionFile.id(), revision.getId(), revision.getMimeType());
                return true;
            }
        } catch (IOException e) {
            throw new CloudClientRestoreException("Failed to restore original revision for " + compressionFile.id(), e);
        }
    }

}
