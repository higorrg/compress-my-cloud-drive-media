package br.com.granzoto.videoprocessor.video_compressor;

public interface VideoCompressor {

    boolean executeCompression(java.io.File inputFile, java.io.File outputFile);

}