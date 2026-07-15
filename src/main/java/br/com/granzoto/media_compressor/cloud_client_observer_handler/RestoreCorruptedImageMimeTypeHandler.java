package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientHandler;
import br.com.granzoto.media_compressor.cloud_client.CloudClientItemObserver;
import br.com.granzoto.media_compressor.cloud_client.CloudClientRestoreException;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Detects Drive files named like images (by extension) whose current {@code mimeType} is
 * {@code video/*} — the signature left behind by the id-collision bug fixed in this project,
 * where one file's compressed content got uploaded over an unrelated file with the same name.
 * Restores the earliest {@code image/*} revision found in the file's Drive revision history.
 *
 * @see br.com.granzoto.media_compressor.cloud_client.CloudClient#restoreOriginalRevision
 */
public class RestoreCorruptedImageMimeTypeHandler implements CloudClientHandler, CloudClientItemObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreCorruptedImageMimeTypeHandler.class.getName());
    private static final String VIDEO_MIME_SUPER_TYPE = "video";
    private static final String IMAGE_MIME_SUPER_TYPE = "image";
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "heic", "heif", "webp", "bmp", "tiff", "tif");

    private final boolean dryRun;
    private CloudClient cloudClient;

    public RestoreCorruptedImageMimeTypeHandler(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        if (!VIDEO_MIME_SUPER_TYPE.equals(compressionFile.mimeSuperType())) {
            return;
        }
        String extension = FilenameUtils.getExtension(compressionFile.name()).toLowerCase();
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            return;
        }

        try {
            boolean found = this.cloudClient.restoreOriginalRevision(compressionFile, IMAGE_MIME_SUPER_TYPE, this.dryRun);
            if (!found) {
                LOGGER.warn("No restorable image revision for {} ({})", compressionFile.name(), compressionFile.id());
            }
        } catch (CloudClientRestoreException e) {
            LOGGER.warn("Failed to restore {} ({}): {}", compressionFile.name(), compressionFile.id(), e.getMessage());
        }
    }

    @Override
    public void registerObserver(CloudClient cloudClient) {
        this.cloudClient = cloudClient;
        cloudClient.addItemObserver(this);
    }
}
