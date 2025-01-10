package br.com.granzoto.media_compressor.cloud_client_observer;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClietListItemObserver;
import br.com.granzoto.media_compressor.model.CompressionFile;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CloudClientListItemObserverForCsv implements CloudClietListItemObserver {

    private CSVWriter writer;

    @Override
    public void notifyStart(CloudClient cloudClient) {
        System.out.println("List only in progress");
        File csvFile = Path.of("/tmp/google-drive-files.csv").toFile();
        try {
            writer = new CSVWriter(new FileWriter(csvFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Writing CSV file " + csvFile.getAbsolutePath());
    }

    @Override
    public void notifyItem(CompressionFile compressionFile) {
        String[] line = new String[5];
        line[0] = compressionFile.id();
        line[1] = compressionFile.name();
        line[2] = compressionFile.folderPath();
        line[3] = compressionFile.mimeSuperType();
        line[4] = compressionFile.size().toString();
        writer.writeNext(line);
    }

    @Override
    public void notifyEnd(List<CompressionFile> files) {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
