package br.com.granzoto.media_compressor.compressor_with_ffmpeg;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import br.com.granzoto.media_compressor.compressor_strategy.CompressorStrategy;

public class FFmpegImageCompressorWithHost implements CompressorStrategy {

    private static final Logger LOGGER = Logger.getLogger(FFmpegImageCompressorWithHost.class.getName());

    public static synchronized FFmpegImageCompressorWithHost getInstance() {
        return new FFmpegImageCompressorWithHost();
    }

    @Override
    public boolean executeCompression(File inputFile, File outputFile) {
        try {
            String[] cmd = { "ffmpeg",
                    "-y",
                    "-loglevel", "info",
                    "-i", inputFile.getAbsolutePath().replaceAll(" ", "\\ "),
                    "-q:v", "2",
                    outputFile.getAbsolutePath().replaceAll(" ", "\\ ") };
            LOGGER.info(Arrays.toString(cmd));

            Process process = new ProcessBuilder().command(cmd).inheritIO().start();
            var exitCode = process.waitFor();
            System.out.println("");

            if (exitCode == 0) {
                LOGGER.info("Image compression success " + outputFile.getName());
                return true;
            } else {
                LOGGER.warning("Image not compressed. Exit code " + exitCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Image compression failed: " + e.getMessage());
            return false;
        }
    }

}
