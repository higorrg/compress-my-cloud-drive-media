package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientHandler;
import br.com.granzoto.media_compressor.cloud_client.CloudClientItemObserver;
import br.com.granzoto.media_compressor.cloud_client.CloudClientEndObserver;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientRestoreException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientStartObserver;
import br.com.granzoto.media_compressor.model.CompressionFile;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class RestoreCorruptedImageMimeTypeHandlerTest {

    @Test
    void handleItem_RestoresImageNamedFileWithVideoMimeType() {
        var fakeClient = new RestoreCallRecordingCloudClient();
        var handler = new RestoreCorruptedImageMimeTypeHandler(true);
        handler.registerObserver(fakeClient);

        handler.handleItem(compressionFile("IMG_20200926_173947.jpg", "video/mp4", "video"));

        assertEquals(1, fakeClient.calls.size(), "A restore should have been attempted.");
        assertEquals("image", fakeClient.calls.get(0).expectedMimeSuperType());
        assertTrue(fakeClient.calls.get(0).dryRun());
    }

    @Test
    void handleItem_PassesThroughApplyFlagWhenNotDryRun() {
        var fakeClient = new RestoreCallRecordingCloudClient();
        var handler = new RestoreCorruptedImageMimeTypeHandler(false);
        handler.registerObserver(fakeClient);

        handler.handleItem(compressionFile("IMG_20200926_173947.jpg", "video/mp4", "video"));

        assertFalse(fakeClient.calls.get(0).dryRun());
    }

    @Test
    void handleItem_IgnoresGenuineVideoFiles() {
        var fakeClient = new RestoreCallRecordingCloudClient();
        var handler = new RestoreCorruptedImageMimeTypeHandler(true);
        handler.registerObserver(fakeClient);

        handler.handleItem(compressionFile("clip.mp4", "video/mp4", "video"));

        assertTrue(fakeClient.calls.isEmpty(), "A file whose name also looks like a video should not be touched.");
    }

    @Test
    void handleItem_IgnoresFilesThatAreNotCurrentlyVideo() {
        var fakeClient = new RestoreCallRecordingCloudClient();
        var handler = new RestoreCorruptedImageMimeTypeHandler(true);
        handler.registerObserver(fakeClient);

        handler.handleItem(compressionFile("photo.jpg", "image/jpeg", "image"));

        assertTrue(fakeClient.calls.isEmpty(), "A file that is not currently mimeType video/* isn't corrupted, so it should be left alone.");
    }

    private static CompressionFile compressionFile(String name, String mimeType, String mimeSuperType) {
        return new CompressionFile("some-id", name, BigInteger.ZERO, "SomeFolder", mimeType, mimeSuperType, null, null);
    }

    private record RestoreCall(String expectedMimeSuperType, boolean dryRun) {
    }

    private static class RestoreCallRecordingCloudClient implements CloudClient {

        final java.util.List<RestoreCall> calls = new java.util.ArrayList<>();

        @Override
        public void addHandler(CloudClientHandler handler) {
        }

        @Override
        public void addStartObserver(CloudClientStartObserver observer) {
        }

        @Override
        public void addItemObserver(CloudClientItemObserver observer) {
        }

        @Override
        public void addEndObserver(CloudClientEndObserver observer) {
        }

        @Override
        public void runFiles() throws CloudClientListFilesException {
        }

        @Override
        public void downloadFile(CompressionFile compressionFile) {
        }

        @Override
        public void uploadFile(CompressionFile compressionFile) {
        }

        @Override
        public boolean restoreOriginalRevision(CompressionFile compressionFile, String expectedMimeSuperType, boolean dryRun)
                throws CloudClientRestoreException {
            calls.add(new RestoreCall(expectedMimeSuperType, dryRun));
            return true;
        }
    }
}
