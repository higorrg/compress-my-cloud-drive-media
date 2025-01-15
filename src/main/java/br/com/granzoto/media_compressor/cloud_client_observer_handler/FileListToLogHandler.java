package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.cloud_client.*;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

public class FileListToLogHandler implements CloudClientHandler, CloudClientStartObserver, CloudClientItemObserver, CloudClientEndObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileListToLogHandler.class.getName());

    @Override
    public void handleStart(CloudClient cloudClient) {
        LOGGER.info("Listing media files from your Google Drive");
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        LOGGER.info("");
        LOGGER.info(compressionFile.toString());
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
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
