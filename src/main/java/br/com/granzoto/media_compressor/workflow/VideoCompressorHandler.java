package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.compressor_strategy.CompressorStrategy;
import br.com.granzoto.media_compressor.compressor_with_ffmpeg.FFmpegVideoCompressorWithHost;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.util.List;
import java.util.logging.Logger;

public class VideoCompressorHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = Logger.getLogger(VideoCompressorHandler.class.getName());
    public static final String VIDEO_MIME_TYPE = "video";

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        this.nextStartHandler(cloudClient);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        if (VIDEO_MIME_TYPE.equals(compressionFile.mimeSuperType()) && !compressionFile.compressedFile().exists()){
            CompressorStrategy videoCompressor = new FFmpegVideoCompressorWithHost();
            boolean executeCompression = videoCompressor.executeCompression(compressionFile.originalFile(), compressionFile.compressedFile());
            if (executeCompression) {
                this.nextItemHandler(compressionFile);
            } else {
                LOGGER.warning("Video compression failed");
            }
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        this.nextEndHandler(files);
    }

}
