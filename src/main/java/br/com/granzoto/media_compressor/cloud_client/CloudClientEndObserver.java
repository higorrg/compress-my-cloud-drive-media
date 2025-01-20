package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;

/**
 * Observe the end of the process of listing files.
 * Kind of a footer section.
 */
public interface CloudClientEndObserver {
    void handleEnd(List<CompressionFile> files);
}
