package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.model.CompressionFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class VideoCompressorHandler extends AbstractCloudClientHandler {

    private static final Logger LOGGER = Logger.getLogger(VideoCompressorHandler.class.getName());
    public static final String VIDEO_MIME_TYPE = "video";

    @Override
    public void handleStart(CloudClient cloudClient) {
        super.handleStart(cloudClient);
        this.nextStartHandler(cloudClient);
    }

    @Override
    public void handleItem(CompressionFile compressionFile) {
        if (VIDEO_MIME_TYPE.equals(compressionFile.mimeSuperType()) && !compressionFile.compressedFile().exists()){
            boolean executeCompression = this.executeCompression(compressionFile.originalFile(), compressionFile.compressedFile());
            if (executeCompression) {
                this.nextItemHandler(compressionFile);
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
                    "-c:v", "h264",
                    "-c:a", "copy",
                    outputFile.getAbsolutePath().replaceAll(" ", "\\ ")};
            LOGGER.info(Arrays.toString(cmd));

            Process process = new ProcessBuilder().command(cmd).inheritIO().start();
            var exitCode = process.waitFor();
            System.out.println();

            if (exitCode == 0) {
                LOGGER.info("Video compression successfully finished " + outputFile.getName());
                return true;
            } else {
                LOGGER.warning("Video compression failed: Exit code " + exitCode+". File: "+inputFile.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Video compression failed: " + e.getMessage()+". File: "+inputFile.getAbsolutePath());
            return false;
        }
    }

    @Override
    public void handleEnd(List<CompressionFile> files) {
        super.handleEnd(files);
        this.nextEndHandler(files);
    }

}
