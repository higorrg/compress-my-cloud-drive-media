package br.com.granzoto.media_compressor.model;

import java.math.BigInteger;

public record CompressionFile(String id, String name, BigInteger size, String folderPath, String mimeSuperType) {

}
