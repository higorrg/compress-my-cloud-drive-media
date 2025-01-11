package br.com.granzoto.media_compressor.compressor_with_ffmpeg;

import br.com.granzoto.media_compressor.compressor_strategy.CompressorStrategy;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

public class FFmpegVideoCompressorWithHost implements CompressorStrategy {

    private static final Logger LOGGER = Logger.getLogger(FFmpegVideoCompressorWithHost.class.getName());

    @Override
    public boolean executeCompression(File inputFile, File outputFile) {
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
                LOGGER.warning("Video compression failed: Exit code " + exitCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Video compression failed: " + e.getMessage());
            return false;
        }
    }

}
