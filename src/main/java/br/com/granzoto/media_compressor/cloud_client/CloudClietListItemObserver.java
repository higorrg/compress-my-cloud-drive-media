package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;

public interface CloudClietListItemObserver {

    void notifyStart(CloudClient cloudClient);
    void notifyItem(CompressionFile compressionFile);
    void notifyEnd(List<CompressionFile> files);

}
