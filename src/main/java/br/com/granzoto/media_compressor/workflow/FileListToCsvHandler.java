package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.model.CompressionFile;
import com.opencsv.CSVWriter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class FileListToCsvHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = Logger.getLogger(FileListToCsvHandler.class.getName());
    private CSVWriter writer;

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        System.out.println("List to CSV file started");
        File csvFile = Path.of("google-drive-files.csv").toFile();
        try {
            System.out.println("Writing CSV file " + csvFile.getAbsolutePath());
            writer = new CSVWriter(new FileWriter(csvFile));
            writeHeaderColumns();
            this.nextStartHandler(cloudClient);
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
        this.nextItemHandler(compressionFile);
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
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        try {
            writer.close();
            BigInteger totalSize = files.stream()
                    .map(CompressionFile::size)
                    .reduce(BigInteger.ZERO, BigInteger::add);
            LOGGER.info("");
            LOGGER.info("Total size: " + FileUtils.byteCountToDisplaySize(totalSize));
            LOGGER.info("Total items: " + files.size());
            LOGGER.info("");
            this.nextEndHandler(files);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
