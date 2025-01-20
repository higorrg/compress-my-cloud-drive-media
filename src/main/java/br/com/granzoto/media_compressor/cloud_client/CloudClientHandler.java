package br.com.granzoto.media_compressor.cloud_client;

/**
 * Represents a handler that wants to register itself on the CloudClient.
 *
 * @see br.com.granzoto.media_compressor.cloud_client.CloudClient
 */
public interface CloudClientHandler {
    void registerObserver(CloudClient cloudClient);
}
