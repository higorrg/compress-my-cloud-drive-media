package br.com.granzoto.media_compressor;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client_for_google.GoogleDriveClient;
import br.com.granzoto.media_compressor.cloud_client_observer.CloudClientListItemObserverForCsv;
import br.com.granzoto.media_compressor.cloud_client_observer.CloudClientListItemObserverForLog;
import br.com.granzoto.media_compressor.workflow.Workflow;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.LogManager;

public class Main {

    public static void main(String... args) throws CloudClientListFilesException {
        setupLog();
        CloudClient cloudClient = GoogleDriveClient.getInstance();
        cloudClient.addListObserver(new CloudClientListItemObserverForLog());
        cloudClient.addListObserver(new CloudClientListItemObserverForCsv());
        Workflow workflow = Workflow.getInstance(cloudClient);
        workflow.run();
//        Main.listOnly(cloudClient);
    }

//    private static void listOnly(CloudClient cloudClient) throws CloudClientListFilesException {
//        cloudClient.listFiles();
//    }

    private static void setupLog() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

}
