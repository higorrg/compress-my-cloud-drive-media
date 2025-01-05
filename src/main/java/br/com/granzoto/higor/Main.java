package br.com.granzoto.higor;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String APPLICATION_NAME = "Compress My Google Drive Videos";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static NetHttpTransport HTTP_TRANSPORT;

    private static Drive service;

    private static List<String> compressedFileNames = new ArrayList<>();

    public static void main(String... args) throws IOException, GeneralSecurityException {
         HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
         service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                GoogleDriveAuth.getCredentials(HTTP_TRANSPORT, JSON_FACTORY))
                .setApplicationName(APPLICATION_NAME)
                .build();
        Main.listFiles(null);
    }

    public static void listFiles(String nextPageToken) throws IOException, GeneralSecurityException {

        FileList result = service.files().list()
                // .setQ("mimeType contains 'video/' and modifiedTime < '2024-01-04T00:00:00'")
                .setQ("name = 'Cópia de 20200127_163453.mp4'")
                .setSpaces("drive")
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, size, parents)")
                .setPageToken(nextPageToken)
                .execute();
        List<File> googleFileList = result.getFiles();
        if (googleFileList == null || googleFileList.isEmpty()) {
            LOGGER.info("No files found.");
        } else {
            googleFileList.forEach(googleFile -> {
                // file.entrySet().forEach(e -> {
                // System.out.println(e.getKey()+": "+e.getValue());
                // });
                CompressMyFile myFile = CompressMyFileFromGoogleFactory.createFile(googleFile);
                LOGGER.info(myFile.name() + " (" + FileUtils.byteCountToDisplaySize(myFile.size()) + ")");
                if (!compressedFileNames.contains(myFile.name())) {
                    try {
                        new VideoCompressor(service).compressVideo(myFile);
                        compressedFileNames.add(myFile.name());
                    } catch (IOException e) {
                        LOGGER.severe(e.getMessage());
                        e.printStackTrace();
                    }
                    LOGGER.info("-------------------------------------------------\n");
                } else {
                    LOGGER.info(myFile.name() + " already compressed. Skipping file.");
                }
            });
            // listFiles(result.getNextPageToken());
        }
    }
}
