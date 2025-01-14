package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientUploadException;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadHandler.class.getName());

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        this.nextStartHandler(cloudClient);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        try {
            if (compressionFile.compressedFile().exists()) {
                this.cloudClient.uploadFile(compressionFile);
            } else {
                LOGGER.info("Compression file doesn't exist. Skipping upload. {}", compressionFile.compressedFile().getAbsolutePath());
            }
            this.nextItemHandler(compressionFile);
        } catch (CloudClientUploadException e) {
            LOGGER.warn("Upload to Cloud Drive Failed");
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        this.nextEndHandler(files);
    }

}
