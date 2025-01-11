package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientHandler;
import br.com.granzoto.media_compressor.compressor_strategy.CompressorStrategy;
import br.com.granzoto.media_compressor.compressor_with_ffmpeg.FFmpegImageCompressorWithHost;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;
import java.util.logging.Logger;

public class ImageCompressorHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = Logger.getLogger(ImageCompressorHandler.class.getName());
    public static final String IMAGE_MIME_TYPE = "image";

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        this.nextStartHandler(cloudClient);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        if (IMAGE_MIME_TYPE.equals(compressionFile.mimeSuperType()) && !compressionFile.compressedFile().exists()){
            CompressorStrategy imageCompressor = new FFmpegImageCompressorWithHost();
            boolean executeCompression = imageCompressor.executeCompression(compressionFile.originalFile(), compressionFile.compressedFile());
            if (executeCompression) {
                this.nextItemHandler(compressionFile);
            } else {
                LOGGER.warning("Image compression failed");
            }
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        this.nextEndHandler(files);
    }

}
