package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.model.CompressionFile;

import java.io.File;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfCompressorHandler extends AbstractCloudClientHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfCompressorHandler.class.getName());
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
                LOGGER.info("PDF compression successfully finished {}", outputFile.getName());
                return true;
            } else {
                LOGGER.warn("PDF compression failed: Exit code {}. File: {}", exitCode, inputFile.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("PDF compression failed: {}. File: {}", e.getMessage(), inputFile.getAbsolutePath());
            return false;
        }
    }
}


