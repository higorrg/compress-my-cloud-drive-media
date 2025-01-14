package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;
import java.util.logging.Logger;

public class DownloadHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = Logger.getLogger(DownloadHandler.class.getName());

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        this.nextStartHandler(cloudClient);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        try {
            if (!compressionFile.originalFile().exists()) {
                this.cloudClient.downloadFile(compressionFile);
            } else {
                LOGGER.info("File already exists. Skipping download. "+compressionFile.originalFile().getAbsolutePath());
            }
            this.nextItemHandler(compressionFile);
        } catch (CloudClientDownloadException e) {
            LOGGER.warning("Download from Cloud Drive failed");
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        this.nextEndHandler(files);
    }
}
