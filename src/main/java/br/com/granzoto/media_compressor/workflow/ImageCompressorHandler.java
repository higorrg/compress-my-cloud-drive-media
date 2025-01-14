package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCompressorHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCompressorHandler.class.getName());
    public static final String IMAGE_MIME_TYPE = "image";

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        this.nextStartHandler(cloudClient);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        if (IMAGE_MIME_TYPE.equals(compressionFile.mimeSuperType()) && !compressionFile.compressedFile().exists()){
            boolean executeCompression = this.executeCompression(compressionFile.originalFile(), compressionFile.compressedFile());
            if (executeCompression) {
                this.nextItemHandler(compressionFile);
            } else if (compressionFile.compressedFile().exists()){
                if (!compressionFile.compressedFile().delete()){
                    LOGGER.warn("Fail deleting invalid compressed file {}. Will try to delete on JVM exit", compressionFile.compressedFile().getAbsolutePath());
                    compressionFile.compressedFile().deleteOnExit();
                } else {
                    LOGGER.info("Invalid compressed file successfully deleted {}", compressionFile.compressedFile().getAbsolutePath());
                }
            }
        } else {
            this.nextItemHandler(compressionFile);
        }
    }

    private boolean executeCompression(File inputFile, File outputFile) {
        try {
            String[] cmd = {"ffmpeg",
                    "-y",
                    "-loglevel", "quiet",
                    "-i", inputFile.getAbsolutePath().replaceAll(" ", "\\ "),
                    "-q:v", "2",
                    outputFile.getAbsolutePath().replaceAll(" ", "\\ ")};
            LOGGER.info(Arrays.toString(cmd));

            Process process = new ProcessBuilder().command(cmd).inheritIO().start();
            var exitCode = process.waitFor();

            if (exitCode == 0) {
                LOGGER.info("Image compression successfully finished {}", outputFile.getName());
                return true;
            } else {
                LOGGER.warn("Image compression failed: Exit code {}. File: {}", exitCode, inputFile.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Image compression failed: {}. File: {}", e.getMessage(), inputFile.getAbsolutePath());
            return false;
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        this.nextEndHandler(files);
    }

}
