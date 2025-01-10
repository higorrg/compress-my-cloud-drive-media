package br.com.granzoto.media_compressor.compressor_strategy;

import br.com.granzoto.media_compressor.compressor_with_ffmpeg.FFmpegImageCompressorWithHost;
import br.com.granzoto.media_compressor.compressor_with_ffmpeg.FFmpegVideoCompressorWithHost;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompressorFactoryTest {

    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(CompressorFactory.class); // Captures logs for CompressorFactory
        logCaptor.clearLogs(); // Clear logs before each test
    }

    @Test
    void testGetCompressorForMimeType_Video() {
        // Act
        CompressorStrategy strategy = CompressorFactory.getCompressorForMimeType("video");

        // Assert
        assertNotNull(strategy, "Expected a valid CompressorStrategy for 'video'.");
        assertInstanceOf(FFmpegVideoCompressorWithHost.class, strategy, "Expected FFmpegVideoCompressorWithHost for 'video'.");
        assertTrue(logCaptor.getLogs().isEmpty(), "No log messages should be generated for valid MIME types.");
    }

    @Test
    void testGetCompressorForMimeType_Image() {
        // Act
        CompressorStrategy strategy = CompressorFactory.getCompressorForMimeType("image");

        // Assert
        assertNotNull(strategy, "Expected a valid CompressorStrategy for 'image'.");
        assertInstanceOf(FFmpegImageCompressorWithHost.class, strategy, "Expected FFmpegImageCompressorWithHost for 'image'.");
        assertTrue(logCaptor.getLogs().isEmpty(), "No log messages should be generated for valid MIME types.");
    }

    @Test
    void testGetCompressorForMimeType_InvalidMimeType() {
        // Act
        CompressorStrategy strategy = CompressorFactory.getCompressorForMimeType("audio");

        // Assert
        assertNull(strategy, "Expected null for unsupported MIME type 'audio'.");

        // Verify logs
        assertEquals(1, logCaptor.getLogs().size(), "Expected one log message for invalid MIME type.");
        assertEquals("Compressor strategy for MimeType not found: audio", logCaptor.getLogs().get(0));
    }

    @Test
    void testGetCompressorForMimeType_NullMimeType() {
        // Act
        CompressorStrategy strategy = CompressorFactory.getCompressorForMimeType(null);

        // Assert
        assertNull(strategy, "Expected null for null MIME type.");

        // Verify logs
        assertEquals(1, logCaptor.getLogs().size(), "Expected one log message for null MIME type.");
        assertEquals("Compressor strategy for MimeType not found: null", logCaptor.getLogs().get(0));
    }

    @Test
    void testGetCompressorForMimeType_EmptyMimeType() {
        // Act
        CompressorStrategy strategy = CompressorFactory.getCompressorForMimeType("");

        // Assert
        assertNull(strategy, "Expected null for empty MIME type.");

        // Verify logs
        assertEquals(1, logCaptor.getLogs().size(), "Expected one log message for empty MIME type.");
        assertEquals("Compressor strategy for MimeType not found: ", logCaptor.getLogs().get(0));
    }
}
