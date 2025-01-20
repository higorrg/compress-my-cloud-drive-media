package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;

/**
 * Observe file by file of the process of listing files.
 */
public interface CloudClientItemObserver {
    void handleItem(CompressionFile compressionFile);
}
