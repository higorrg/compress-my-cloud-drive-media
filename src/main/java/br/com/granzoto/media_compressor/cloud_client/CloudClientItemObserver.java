package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;

public interface CloudClientItemObserver {
    void handleItem(CompressionFile compressionFile);
}
