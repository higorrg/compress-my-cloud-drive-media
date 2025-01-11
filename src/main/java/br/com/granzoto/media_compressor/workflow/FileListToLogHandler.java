package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.apache.commons.io.FileUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

public class FileListToLogHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = Logger.getLogger(FileListToLogHandler.class.getName());

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        LOGGER.info("Listing media files from your Google Drive");
        this.nextStartHandler(cloudClient);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        LOGGER.info("");
        LOGGER.info(compressionFile.toString());
        this.nextItemHandler(compressionFile);
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        BigInteger totalSize = files.stream()
                .map(CompressionFile::size)
                .reduce(BigInteger.ZERO, BigInteger::add);
        LOGGER.info("");
        LOGGER.info("Total size: " + FileUtils.byteCountToDisplaySize(totalSize));
        LOGGER.info("Total items: " + files.size());
        LOGGER.info("");
        this.nextEndHandler(files);
    }
}
