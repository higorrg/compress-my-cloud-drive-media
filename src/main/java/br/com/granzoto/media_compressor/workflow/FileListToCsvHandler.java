package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.model.CompressionFile;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class FileListToCsvHandler extends AbstractCloudClientHandler {

    private CSVWriter writer;

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        System.out.println("List to CSV file started");
        File csvFile = Path.of("google-drive-files.csv").toFile();
        try {
            System.out.println("Writing CSV file " + csvFile.getAbsolutePath());
            writer = new CSVWriter(new FileWriter(csvFile));
            this.nextStartHandler(cloudClient);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        String[] line = new String[5];
        line[0] = compressionFile.id();
        line[1] = compressionFile.name();
        line[2] = compressionFile.folderPath();
        line[3] = compressionFile.mimeSuperType();
        line[4] = compressionFile.size().toString();
        writer.writeNext(line);
        this.nextItemHandler(compressionFile);
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        try {
            writer.close();
            this.nextEndHandler(files);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
