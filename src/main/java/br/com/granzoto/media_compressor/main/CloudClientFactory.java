package br.com.granzoto.media_compressor.main;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client_for_google.GoogleDriveClient;

public class CloudClientFactory {

    public CloudClient getCloudClient(String instanceType) {
        return switch (instanceType.toLowerCase()) {
            case "google" -> GoogleDriveClient.getInstance();
//            case "onedrive" -> OneDriveClient.getInstance();
//            case "dropbox" -> DropboxClient.getInstance();
            default -> throw new IllegalArgumentException("Unsupported cloud client type: " + instanceType);
        };
    }
}
