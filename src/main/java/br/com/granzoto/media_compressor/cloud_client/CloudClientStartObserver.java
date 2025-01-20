package br.com.granzoto.media_compressor.cloud_client;

/**
 * Observe the start of the process of listing files.
 * Kind of a header section.
 */
public interface CloudClientStartObserver {
    void handleStart(CloudClient cloudClient);
}
