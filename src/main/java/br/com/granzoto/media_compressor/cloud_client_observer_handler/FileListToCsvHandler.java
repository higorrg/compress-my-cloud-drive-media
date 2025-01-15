package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.cloud_client.*;
import br.com.granzoto.media_compressor.model.CompressionFile;
import com.opencsv.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;

public class FileListToCsvHandler implements CloudClientHandler, CloudClientStartObserver, CloudClientItemObserver, CloudClientEndObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileListToCsvHandler.class.getName());
    private CSVWriter writer;

    @Override
    public void handleStart(CloudClient cloudClient) {
        System.out.println("List to CSV file started");
        File csvFile = Path.of("google-drive-files.csv").toFile();
        try {
            System.out.println("Writing CSV file " + csvFile.getAbsolutePath());
            writer = new CSVWriter(new FileWriter(csvFile));
            writeHeaderColumns();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeHeaderColumns() {
        String[] header = new String[6];
        header[0] = "Id";
        header[1] = "Name";
        header[2] = "Folder";
        header[3] = "MimeType";
        header[4] = "MimeSuperType";
        header[5] = "Size";
        writer.writeNext(header);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        writeLineItem(compressionFile);
    }

    private void writeLineItem(CompressionFile compressionFile) {
        String[] line = new String[6];
        line[0] = compressionFile.id();
        line[1] = compressionFile.name();
        line[2] = compressionFile.folderPath();
        line[3] = compressionFile.mimeType();
        line[4] = compressionFile.mimeSuperType();
        line[5] = compressionFile.size().toString();
        writer.writeNext(line);
        LOGGER.info(Arrays.asList(line).toString());
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        try {
            writer.close();
            LOGGER.info("");
            this.printSubtotalByFolder(files);
            this.printSubtotalByMimeType(files);
            this.printSubtotalByMimeSuperType(files);
            this.printTotal(files);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printSubtotalByFolder(List<CompressionFile> files) {
        LOGGER.info("Group by Folder");
        Map<String, BigInteger> groupBy = new HashMap<>();
        files.forEach(f -> groupBy.merge(f.folderPath(), f.size(), BigInteger::add));

        List<Map.Entry<String, BigInteger>> sorted = new ArrayList<>(groupBy.entrySet());
        sorted.sort(Map.Entry.comparingByValue());

        sorted.forEach(e -> LOGGER.info("{}: {}", e.getKey(), FileUtils.byteCountToDisplaySize(e.getValue())));
        LOGGER.info("");
    }

    private void printSubtotalByMimeType(List<CompressionFile> files) {
        LOGGER.info("Group by MimeType");
        Map<String, BigInteger> groupBy = new HashMap<>();
        files.forEach(f -> groupBy.merge(f.mimeType(), f.size(), BigInteger::add));

        List<Map.Entry<String, BigInteger>> sorted = new ArrayList<>(groupBy.entrySet());
        sorted.sort(Map.Entry.comparingByValue());

        sorted.forEach(e -> LOGGER.info("{}: {}", e.getKey(), FileUtils.byteCountToDisplaySize(e.getValue())));
        LOGGER.info("");
    }

    private void printSubtotalByMimeSuperType(List<CompressionFile> files) {
        LOGGER.info("Group by MimeSuperType");
        Map<String, BigInteger> groupBy = new HashMap<>();
        files.forEach(f -> groupBy.merge(f.mimeSuperType(), f.size(), BigInteger::add));

        List<Map.Entry<String, BigInteger>> sorted = new ArrayList<>(groupBy.entrySet());
        sorted.sort(Map.Entry.comparingByValue());

        sorted.forEach(e -> LOGGER.info("{}: {}", e.getKey(), FileUtils.byteCountToDisplaySize(e.getValue())));
        LOGGER.info("");
    }


    private void printTotal(List<CompressionFile> files) {
        BigInteger totalSize = files.stream()
                .map(CompressionFile::size)
                .reduce(BigInteger.ZERO, BigInteger::add);
        LOGGER.info("");
        LOGGER.info("Total size: {}", FileUtils.byteCountToDisplaySize(totalSize));
        LOGGER.info("Total items: {}", files.size());
        LOGGER.info("");
    }

    @Override
    public void registerObserver(CloudClient cloudClient) {
        cloudClient.addStartObserver(this);
        cloudClient.addItemObserver(this);
        cloudClient.addEndObserver(this);
    }
}
