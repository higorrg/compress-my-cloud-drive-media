package br.com.granzoto.higor;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.*;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String APPLICATION_NAME = "Compress My Google Drive Videos";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static void main(String... args) throws IOException, GeneralSecurityException {
        Main.listFiles(null);
    }

    public static void listFiles(String nextPageToken) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                GoogleDriveAuth.getCredentials(HTTP_TRANSPORT, JSON_FACTORY))
                .setApplicationName(APPLICATION_NAME)
                .build();

        FileList result = service.files().list()
                // .setQ("mimeType contains 'video/' and modifiedTime < '2024-01-04T00:00:00'")
                .setQ("name = 'Cópia de 20200127_163453.mp4'")
                .setSpaces("drive")
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, size, parents)")
                .setPageToken(nextPageToken)
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            LOGGER.info("No files found.");
        } else {
            files.forEach(file -> {
                // file.entrySet().forEach(e -> {
                // LOGGER.info(e.getKey()+": "+e.getValue());
                // });

                LOGGER.info(file.getName() + " (" + FileUtils
                        .byteCountToDisplaySize(BigInteger.valueOf(Long.valueOf(file.get("size").toString()))) + ")");
                try {
                    new VideoCompressor(service).compressVideo(file);
                } catch (IOException e) {
                    LOGGER.severe(e.getMessage());
                    e.printStackTrace();
                }
                LOGGER.info("-------------------------------------------------\n");
            });
            // listFiles(result.getNextPageToken());
        }
    }
}
