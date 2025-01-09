package br.com.granzoto.media_compressor;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client_for_google.GoogleDriveClient;
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
        Workflow workflow = Workflow.getInstance(cloudClient);
//        workflow.run();
        Main.listOnly(cloudClient);
//        Main.listToCsv(cloudClient);
        // Main.deleteAll(cloudClient);
    }

    private static void listOnly(CloudClient cloudClient) throws CloudClientListFilesException {
        cloudClient.listFiles();
    }

//    private static void uploadCompressed(CloudClient cloudClient)
//            throws CloudClientListFilesException, CloudClientUploadException {
//        GoogleDriveClient.getInstance().uploadFile(null, null);
//    }

//    private static void deleteAll(CloudClient cloudClient) throws CloudClientListFilesException {
//        System.out.println("List only in progress");
//        List<CompressionFile> cloudFiles = cloudClient.listFiles();
//        List<CompressionFile> errorFiles = new ArrayList<>();
//        if (cloudFiles != null && !cloudFiles.isEmpty()) {
//            cloudFiles.forEach(myFile -> {
//                try {
//                    cloudClient.delete(myFile);
//                    System.out.println("Deleted: " + myFile.name() + ";" + myFile.size());
//                } catch (CloudClientDeleteException e) {
//                    errorFiles.add(myFile);
//                }
//            });
//            System.out.println("Not deleted files:");
//            errorFiles.forEach(myFile -> {
//                System.out.println(myFile.name() + ";" + myFile.size());
//            });
//        }
//    }

//    private static void listToCsv(CloudClient cloudClient) throws CloudClientListFilesException {
//        System.out.println("List only in progress");
//        List<CompressionFile> files = cloudClient.listFiles();
//        File csvFile = Path.of("/tmp/google-drive-files.csv").toFile();
//        System.out.println("Writing CSV file " + csvFile.getAbsolutePath());
//        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {
//            files.forEach(file -> {
//                String[] line = new String[5];
//                line[0] = file.id();
//                line[1] = file.name();
//                line[2] = file.mimeSuperType();
//                line[4] = file.size().toString();
//                writer.writeNext(line);
//                System.out.println(line);
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    private static void setupLog() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

}
