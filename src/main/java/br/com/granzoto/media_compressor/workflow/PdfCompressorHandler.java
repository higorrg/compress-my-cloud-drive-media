package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.model.CompressionFile;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

public class PdfCompressorHandler extends AbstractCloudClientHandler {
    private static final Logger LOGGER = Logger.getLogger(PdfCompressorHandler.class.getName());
    public static final String PDF_MIME_TYPE = "application/pdf";

    @Override
    public void handleItem(CompressionFile compressionFile) {
        if (PDF_MIME_TYPE.equals(compressionFile.mimeType()) && !compressionFile.compressedFile().exists()){
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
            String[] cmd = {"gs",
                    "-sDEVICE=pdfwrite",
                    "-dCompatibilityLevel=1.4",
                    "-dPDFSETTINGS=/screen",
                    "-dNOPAUSE",
                    "-dBATCH",
                    "-sOutputFile=" + outputFile.getAbsolutePath().replaceAll(" ", "\\ "),
                    inputFile.getAbsolutePath().replaceAll(" ", "\\ ")};
            LOGGER.info(Arrays.toString(cmd));

            Process process = new ProcessBuilder().command(cmd).inheritIO().start();
            var exitCode = process.waitFor();

            if (exitCode == 0) {
                LOGGER.info("PDF compression successfully finished " + outputFile.getName());
                return true;
            } else {
                LOGGER.warning("PDF compression failed: Exit code " + exitCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("PDF compression failed: " + e.getMessage());
            return false;
        }
    }
}


