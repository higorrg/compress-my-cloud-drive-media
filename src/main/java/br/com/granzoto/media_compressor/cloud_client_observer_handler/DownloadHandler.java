package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.cloud_client.*;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DownloadHandler implements CloudClientHandler, CloudClientItemObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadHandler.class);
    private CloudClient cloudClient;

    @Override
    public void handleItem(CompressionFile compressionFile) {
        try {
            if (!compressionFile.originalFile().exists()) {
                FileUtils.createParentDirectories(compressionFile.originalFile());
                this.cloudClient.downloadFile(compressionFile);
            } else {
                LOGGER.info("File already exists. Skipping download. {}", compressionFile.originalFile().getAbsolutePath());
            }
        } catch (CloudClientDownloadException | IOException e) {
            LOGGER.warn("Download from Cloud Drive failed {}", compressionFile.originalFile().getAbsolutePath());
        }
    }

    @Override
    public void registerObserver(CloudClient cloudClient) {
        this.cloudClient = cloudClient;
        cloudClient.addItemObserver(this);
    }
}
