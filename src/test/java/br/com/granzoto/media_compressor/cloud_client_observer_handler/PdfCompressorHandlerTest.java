package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.model.CompressionFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises {@link PdfCompressorHandler} against a real Ghostscript ({@code gs}) subprocess,
 * the same way {@code MediaPipelineIdCollisionIntegrationTest} exercises the video/image handlers
 * against real ffmpeg subprocesses. The fixture PDF is itself rendered by Ghostscript from a tiny
 * PostScript program, rather than hand-rolled bytes, so the compression step runs on a genuine PDF.
 */
class PdfCompressorHandlerTest {

    @Test
    void handleItem_CompressesPdfFile(@TempDir Path tempDir) throws Exception {
        java.io.File originalFile = renderPdfFixture(tempDir);
        java.io.File compressedFile = tempDir.resolve("compressed/document.pdf").toFile();

        new PdfCompressorHandler().handleItem(compressionFile(originalFile, compressedFile, "application/pdf"));

        assertTrue(compressedFile.exists(), "Ghostscript should have produced a compressed PDF.");
        assertTrue(isPdf(Files.readAllBytes(compressedFile.toPath())), "The compressed output must still be a valid PDF.");
    }

    @Test
    void handleItem_IgnoresNonPdfMimeType(@TempDir Path tempDir) throws Exception {
        java.io.File originalFile = renderPdfFixture(tempDir);
        java.io.File compressedFile = tempDir.resolve("compressed/document.pdf").toFile();

        new PdfCompressorHandler().handleItem(compressionFile(originalFile, compressedFile, "image/jpeg"));

        assertFalse(compressedFile.exists(), "A file whose mime-type isn't application/pdf should never be sent to Ghostscript.");
    }

    @Test
    void handleItem_SkipsWhenCompressedFileAlreadyExists(@TempDir Path tempDir) throws Exception {
        java.io.File originalFile = renderPdfFixture(tempDir);
        java.io.File compressedFile = tempDir.resolve("compressed/document.pdf").toFile();
        FileUtils.writeStringToFile(compressedFile, "already compressed", StandardCharsets.UTF_8);

        new PdfCompressorHandler().handleItem(compressionFile(originalFile, compressedFile, "application/pdf"));

        assertEquals("already compressed", FileUtils.readFileToString(compressedFile, StandardCharsets.UTF_8),
                "An existing compressed file should be left untouched instead of being recompressed.");
    }

    private static CompressionFile compressionFile(java.io.File originalFile, java.io.File compressedFile, String mimeType) {
        return new CompressionFile("some-id", originalFile.getName(), BigInteger.ZERO, "SomeFolder", mimeType, null,
                originalFile, compressedFile);
    }

    private static java.io.File renderPdfFixture(Path dir) throws IOException, InterruptedException {
        Path script = dir.resolve("fixture.ps");
        Files.writeString(script, """
                %!
                << /PageSize [200 200] >> setpagedevice
                0 0 1 setrgbcolor
                0 0 200 200 rectfill
                showpage
                """);

        Path out = dir.resolve("fixture.pdf");
        Process process = new ProcessBuilder(
                "gs", "-q", "-sDEVICE=pdfwrite", "-dNOPAUSE", "-dBATCH",
                "-sOutputFile=" + out, "-f", script.toString())
                .start();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode, "Ghostscript fixture generation failed");

        return out.toFile();
    }

    private static boolean isPdf(byte[] bytes) {
        return bytes.length >= 5 && "%PDF-".equals(new String(bytes, 0, 5, StandardCharsets.US_ASCII));
    }
}
