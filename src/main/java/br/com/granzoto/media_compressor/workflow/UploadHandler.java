package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientUploadException;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;
import java.util.logging.Logger;

public class UploadHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = Logger.getLogger(UploadHandler.class.getName());

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
                LOGGER.info("Compression file doesn't exist. Skipping upload. "+compressionFile.compressedFile().getAbsolutePath());
            }
            this.nextItemHandler(compressionFile);
        } catch (CloudClientUploadException e) {
            LOGGER.warning("Upload to Cloud Drive Failed");
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        this.nextEndHandler(files);
    }

}
