package br.com.granzoto.media_compressor.cloud_client_observer;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClietListItemObserver;
import br.com.granzoto.media_compressor.cloud_client_for_google.GoogleDriveClient;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.apache.commons.io.FileUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

public class CloudClientListItemObserverForLog implements CloudClietListItemObserver {

    private static final Logger LOGGER = Logger.getLogger(CloudClientListItemObserverForLog.class.getName());

    @Override
    public void notifyStart(CloudClient cloudClient) {
        LOGGER.info("Listing media files from your Google Drive");
    }

    @Override
    public void notifyItem(CompressionFile compressionFile) {
        LOGGER.info(compressionFile.toString());
    }

    @Override
    public void notifyEnd(List<CompressionFile> files) {
        BigInteger totalSize = files.stream()
                .map(CompressionFile::size)
                .reduce(BigInteger.ZERO, BigInteger::add);
        LOGGER.info("");
        LOGGER.info("Total size: " + FileUtils.byteCountToDisplaySize(totalSize));
        LOGGER.info("Total items: " + files.size());
        LOGGER.info("");
    }
}
