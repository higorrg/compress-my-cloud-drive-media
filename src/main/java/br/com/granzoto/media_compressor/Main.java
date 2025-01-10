package br.com.granzoto.media_compressor;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client_for_google.GoogleDriveClient;
import br.com.granzoto.media_compressor.cloud_client_observer.CloudClientListItemObserverForLog;
import br.com.granzoto.media_compressor.model.CompressionFile;
import br.com.granzoto.media_compressor.workflow.Workflow;
import com.opencsv.CSVWriter;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.LogManager;

public class Main {

    public static void main(String... args) throws CloudClientListFilesException {
        setupLog();
        CloudClient cloudClient = GoogleDriveClient.getInstance();
        cloudClient.addListObserver(new CloudClientListItemObserverForLog());
        Workflow workflow = Workflow.getInstance(cloudClient);
//        workflow.run();
        Main.listOnly(cloudClient);
    }

    private static void listOnly(CloudClient cloudClient) throws CloudClientListFilesException {
        cloudClient.listFiles();
    }

    private static void setupLog() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

}
