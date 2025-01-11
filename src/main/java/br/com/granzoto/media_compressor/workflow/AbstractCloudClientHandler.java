package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientHandler;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;
import java.util.Objects;

public abstract class AbstractCloudClientHandler implements CloudClientHandler {

    protected CloudClient cloudClient;
    private CloudClientHandler next = null;

    @Override
    public void handleStart(CloudClient cloudClient) {
        this.cloudClient = cloudClient;
    }

    public void link(CloudClientHandler next){
        if (Objects.isNull(this.next)) {
            this.next = next;
        } else {
            this.next.link(next);
        }
    }

    protected void nextStartHandler(CloudClient cloudClient){
        if (!Objects.isNull(next)){
            this.next.handleStart(cloudClient);
        }
    }

    protected void nextItemHandler(CompressionFile compressionFile){
        if (!Objects.isNull(next)){
            this.next.handleItem(compressionFile);
        }
    }
    protected void nextEndHandler(List<CompressionFile> files){
        if (!Objects.isNull(next)){
            this.next.handleEnd(files);
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
    }

}
