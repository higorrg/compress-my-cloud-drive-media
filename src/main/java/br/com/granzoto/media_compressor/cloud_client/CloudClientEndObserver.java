package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;

public interface CloudClientEndObserver {
    void handleEnd(List<CompressionFile> files);
}
