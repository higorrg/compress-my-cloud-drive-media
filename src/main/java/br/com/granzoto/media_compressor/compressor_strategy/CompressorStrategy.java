package br.com.granzoto.media_compressor.compressor_strategy;

public interface CompressorStrategy {

    boolean executeCompression(java.io.File inputFile, java.io.File outputFile);

}