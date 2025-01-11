package br.com.granzoto.media_compressor.model;

import java.io.File;
import java.math.BigInteger;

public record CompressionFile(String id, String name, BigInteger size, String folderPath, String mimeType, String mimeSuperType,
                              File originalFile, File compressedFile) {

}
