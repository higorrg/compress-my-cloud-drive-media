package br.com.granzoto.videoprocessor.video_compressor_for_ffmpeg;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import br.com.granzoto.videoprocessor.video_compressor.VideoCompressor;

public class FFmpegCompressorWithHost implements VideoCompressor {

    private static final Logger LOGGER = Logger.getLogger(FFmpegCompressorWithHost.class.getName());

    public static synchronized FFmpegCompressorWithHost getInstance() {
        return new FFmpegCompressorWithHost();
    }

    @Override
    public boolean executeCompression(File inputFile, File outputFile) {
        try {
            String[] cmd = { "ffmpeg",
                    "-y",
                    "-loglevel", "info",
                    "-i", inputFile.getAbsolutePath().replaceAll(" ", "\\ "),
                    // "-c:v", "libvvenc",
                    "-c:v", "h264",
                    "-c:a", "copy",
                    // "-crf", "28",
                    // "-vf", "scale=1280:-1",
                    // "-preset","fast",
                    // "-qp","21",
                    outputFile.getAbsolutePath().replaceAll(" ", "\\ ") };
            LOGGER.info(Arrays.toString(cmd));

            Process process = new ProcessBuilder().command(cmd).inheritIO().start();
            var exitCode = process.waitFor();
            System.out.println("");

            if (exitCode == 0) {
                LOGGER.info("Video compression success " + outputFile.getName());
            } else {
                LOGGER.warning("Vídeo not compressed. Exit code " + exitCode);
            }
        } catch (Exception e) {
            LOGGER.severe("Video compression failed: " + e.getMessage());
            return false;
        }

        return true;
    }

}
