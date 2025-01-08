package br.com.granzoto.media_compressor.compressor_strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.com.granzoto.media_compressor.compressor_with_ffmpeg.FFmpegImageCompressorWithHost;
import br.com.granzoto.media_compressor.compressor_with_ffmpeg.FFmpegVideoCompressorWithHost;

public class CompressorFactory {

    private static final Logger LOGGER = Logger.getLogger(CompressorFactory.class.getName());

    private static final Map<String, CompressorStrategy> compressorList = new HashMap<>(2);

    static {
        compressorList.put("video", FFmpegVideoCompressorWithHost.getInstance());
        compressorList.put("image", FFmpegImageCompressorWithHost.getInstance());
    }


    public static CompressorStrategy getCompressorForMimeType(String mimeType) throws IllegalArgumentException{
        if (compressorList.containsKey(mimeType)){
            return compressorList.get(mimeType);
        } else {
            LOGGER.warning("Compressor strategy for MimeType not found: "+mimeType);
            return null;
        }
    }
}
