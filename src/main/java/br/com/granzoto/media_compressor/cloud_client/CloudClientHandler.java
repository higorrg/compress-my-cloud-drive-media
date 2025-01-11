package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;

public interface CloudClientHandler {
    void handleStart(CloudClient cloudClient);
    void handleItem(CompressionFile compressionFile);
    void handleEnd(List<CompressionFile> files);
    void link(CloudClientHandler next);
}
