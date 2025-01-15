package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientHandler;
import br.com.granzoto.media_compressor.cloud_client.CloudClientItemObserver;
import br.com.granzoto.media_compressor.cloud_client.CloudClientUploadException;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadHandler implements CloudClientHandler, CloudClientItemObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadHandler.class.getName());
    private CloudClient cloudClient;

    @Override
    public void handleItem(CompressionFile compressionFile) {
        try {
            if (compressionFile.compressedFile().exists()) {
                this.cloudClient.uploadFile(compressionFile);
            } else {
                LOGGER.info("Compression file doesn't exist. Skipping upload. {}", compressionFile.compressedFile().getAbsolutePath());
            }
        } catch (CloudClientUploadException e) {
            LOGGER.warn("Upload to Cloud Drive Failed {}", compressionFile.originalFile().getAbsolutePath());
        }
    }

    @Override
    public void registerObserver(CloudClient cloudClient) {
        this.cloudClient = cloudClient;
        cloudClient.addItemObserver(this);
    }
}
