package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
public class DownloadHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadHandler.class);

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
                LOGGER.info("File already exists. Skipping download. {}", compressionFile.originalFile().getAbsolutePath());
            }
            this.nextItemHandler(compressionFile);
        } catch (CloudClientDownloadException e) {
            LOGGER.warn("Download from Cloud Drive failed");
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        this.nextEndHandler(files);
    }
}
