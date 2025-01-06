package br.com.granzoto.videoprocessor.model;

import java.math.BigInteger;

public record VideoCompressionFile(String id, String name, BigInteger size, String parentId) {

}
