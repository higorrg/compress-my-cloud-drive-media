package br.com.granzoto.media_compressor;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client_for_google.GoogleDriveClient;
import br.com.granzoto.media_compressor.workflow.FileListToCsvHandler;
import br.com.granzoto.media_compressor.workflow.FileListToLogHandler;
import br.com.granzoto.media_compressor.workflow.*;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

public class Main {

    public static void main(String... args) throws CloudClientListFilesException {
        setupLog();
        CloudClient cloudClient = GoogleDriveClient.getInstance();
        cloudClient.addHandler(new FileListToLogHandler());
        cloudClient.addHandler(new FileListToCsvHandler());
        cloudClient.addHandler(new DownloadHandler());
        cloudClient.addHandler(new VideoCompressorHandler());
        cloudClient.addHandler(new ImageCompressorHandler());
        cloudClient.addHandler(new UploadHandler());
        cloudClient.runFiles();
    }

    private static void setupLog() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

}
