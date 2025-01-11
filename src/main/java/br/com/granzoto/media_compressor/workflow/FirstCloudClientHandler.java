package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;

public class FirstCloudClientHandler extends AbstractCloudClientHandler {

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        this.nextStartHandler(cloudClient);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        this.nextItemHandler(compressionFile);
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        this.nextEndHandler(files);
    }
}
