package br.com.granzoto.media_compressor.model;

import java.io.File;
import java.math.BigInteger;

public record CompressionFile(String id, String name, BigInteger size, String folderPath, String mimeSuperType,
                              File originalFile, File compressedFile) {

}
