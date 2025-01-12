package br.com.granzoto.media_compressor;

import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client_for_google.GoogleDriveClient;
import br.com.granzoto.media_compressor.workflow.*;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

public class Main {

    public static void main(String... args) throws CloudClientListFilesException {
        setupLog();
        GoogleDriveClient.getInstance()
                .addHandler(new FileListToLogHandler())
                .addHandler(new FileListToCsvHandler())
                .addHandler(new DownloadHandler())
                .addHandler(new VideoCompressorHandler())
                .addHandler(new ImageCompressorHandler())
                .addHandler(new UploadHandler())
                .runFiles();
    }

    private static void setupLog() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

}
