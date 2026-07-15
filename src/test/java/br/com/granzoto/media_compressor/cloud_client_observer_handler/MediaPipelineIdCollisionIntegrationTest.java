package br.com.granzoto.media_compressor.cloud_client_observer_handler;

import br.com.granzoto.media_compressor.cloud_client.AbstractCloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientDownloadException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.cloud_client.CloudClientUploadException;
import br.com.granzoto.media_compressor.cloud_client_for_google.CompressionFileFromGoogleFileFactory;
import br.com.granzoto.media_compressor.model.CompressionFile;
import br.com.granzoto.media_compressor.model.FolderInfo;
import com.google.api.services.drive.model.File;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduces the real-world report of images ending up with a video mime-type on Google Drive:
 * a video and an image that resolve to the same name/folder used to share the same local
 * "compressed" file, so whichever handler ran second would find an "already compressed" file
 * (belonging to the other item) and skip its own compression, uploading the wrong bytes to its
 * own Drive id. Files are built through the real {@link CompressionFileFromGoogleFileFactory},
 * same as {@code GoogleDriveClient.listFilesByPage} does, and run through the real handlers,
 * including real ffmpeg subprocesses, wired together the same way {@code HandlerFactory}/
 * {@code Main} do; only the network-facing download/upload is replaced by an in-memory
 * {@link AbstractCloudClient}.
 */
class MediaPipelineIdCollisionIntegrationTest {

    private static final String FOLDER_ID = "folder-id";
    private static final String FOLDER_NAME = "MediaPipelineIdCollisionIntegrationTest";
    private static final String COLLIDING_NAME = "clip.mp4";

    private CompressionFile videoFile;
    private CompressionFile imageFile;

    @Test
    void differentDriveFilesWithSameNameAndFolder_KeepTheirOwnContentAndMimeType(@TempDir Path fixturesDir) throws Exception {
        byte[] realMp4Bytes = renderMp4Fixture(fixturesDir);
        byte[] realJpegBytes = renderJpegFixture(fixturesDir);
        assertTrue(isMp4(realMp4Bytes), "Test fixture setup: mp4 fixture must actually be an MP4 container.");
        assertTrue(isJpeg(realJpegBytes), "Test fixture setup: jpeg fixture must actually be a JPEG.");

        Map<String, FolderInfo> folderPaths = new HashMap<>();
        folderPaths.put(FOLDER_ID, new FolderInfo(FOLDER_NAME, null));

        // Same name and same folder: on Google Drive, this can legitimately happen even between
        // unrelated files of different types (duplicate uploads, extension-less names backfilled
        // inconsistently, etc.), which is exactly the precondition that triggered the bug.
        File googleVideoFile = new File();
        googleVideoFile.setId("video-id");
        googleVideoFile.setName(COLLIDING_NAME);
        googleVideoFile.setMimeType("video/mp4");
        googleVideoFile.setParents(List.of(FOLDER_ID));

        File googleImageFile = new File();
        googleImageFile.setId("image-id");
        googleImageFile.setName(COLLIDING_NAME);
        googleImageFile.setMimeType("image/jpeg");
        googleImageFile.setParents(List.of(FOLDER_ID));

        videoFile = CompressionFileFromGoogleFileFactory.createCompressionFile(googleVideoFile, folderPaths);
        imageFile = CompressionFileFromGoogleFileFactory.createCompressionFile(googleImageFile, folderPaths);

        assertNotEquals(videoFile.compressedFile().getPath(), imageFile.compressedFile().getPath(),
                "Precondition: the two Drive files share name and folder, so only differing ids/mime-types can keep their local paths apart.");

        var recordingClient = new RecordingCloudClient(
                List.of(videoFile, imageFile),
                Map.of("video-id", realMp4Bytes, "image-id", realJpegBytes));

        recordingClient.addHandler(new DownloadHandler());
        recordingClient.addHandler(new VideoCompressorHandler());
        recordingClient.addHandler(new ImageCompressorHandler());
        recordingClient.addHandler(new UploadHandler());

        recordingClient.runFiles();

        byte[] uploadedForVideoId = recordingClient.uploadedContentById.get("video-id");
        byte[] uploadedForImageId = recordingClient.uploadedContentById.get("image-id");

        assertNotNull(uploadedForVideoId, "The video file should have been uploaded.");
        assertNotNull(uploadedForImageId, "The image file should have been uploaded.");

        assertTrue(isMp4(uploadedForVideoId), "The video's own Drive id must keep receiving real video content.");
        assertFalse(isMp4(uploadedForImageId),
                "Regression: the image's Drive id must never receive the video's MP4 content/mime-type.");
        assertTrue(isJpeg(uploadedForImageId), "The image's own Drive id must keep receiving real image content.");
    }

    @AfterEach
    void cleanUp() throws IOException {
        if (videoFile != null) {
            FileUtils.deleteDirectory(videoFile.originalFile().getParentFile());
            FileUtils.deleteDirectory(videoFile.compressedFile().getParentFile());
        }
        if (imageFile != null) {
            FileUtils.deleteDirectory(imageFile.originalFile().getParentFile());
            FileUtils.deleteDirectory(imageFile.compressedFile().getParentFile());
        }
    }

    private static byte[] renderMp4Fixture(Path dir) throws IOException, InterruptedException {
        Path out = dir.resolve("fixture.mp4");
        runFfmpeg("-f", "lavfi", "-i", "color=c=red:s=64x64:d=1", "-t", "1", "-r", "5", out.toString());
        return Files.readAllBytes(out);
    }

    private static byte[] renderJpegFixture(Path dir) throws IOException, InterruptedException {
        Path out = dir.resolve("fixture.jpg");
        runFfmpeg("-f", "lavfi", "-i", "color=c=blue:s=64x64", "-frames:v", "1", out.toString());
        return Files.readAllBytes(out);
    }

    private static void runFfmpeg(String... args) throws IOException, InterruptedException {
        var cmd = new java.util.ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-y");
        cmd.add("-loglevel");
        cmd.add("quiet");
        cmd.addAll(List.of(args));
        Process process = new ProcessBuilder(cmd).start();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode, "ffmpeg fixture generation failed for " + cmd);
    }

    private static boolean isJpeg(byte[] bytes) {
        return bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8;
    }

    private static boolean isMp4(byte[] bytes) {
        return bytes.length >= 8 && "ftyp".equals(new String(bytes, 4, 4, StandardCharsets.US_ASCII));
    }

    private static class RecordingCloudClient extends AbstractCloudClient {

        private final List<CompressionFile> itemsToStream;
        private final Map<String, byte[]> fixtureBytesById;
        final Map<String, byte[]> uploadedContentById = new LinkedHashMap<>();

        RecordingCloudClient(List<CompressionFile> itemsToStream, Map<String, byte[]> fixtureBytesById) {
            this.itemsToStream = itemsToStream;
            this.fixtureBytesById = fixtureBytesById;
        }

        @Override
        protected void listFilesByPage(String page, Map<String, FolderInfo> folderPaths, List<CompressionFile> files)
                throws CloudClientListFilesException {
            itemsToStream.forEach(this::notifyItem);
        }

        @Override
        public void downloadFile(CompressionFile compressionFile) throws CloudClientDownloadException {
            try {
                FileUtils.writeByteArrayToFile(compressionFile.originalFile(), fixtureBytesById.get(compressionFile.id()));
            } catch (IOException e) {
                throw new CloudClientDownloadException("Failed writing test fixture", e);
            }
        }

        @Override
        public void uploadFile(CompressionFile compressionFile) throws CloudClientUploadException {
            try {
                uploadedContentById.put(compressionFile.id(), FileUtils.readFileToByteArray(compressionFile.compressedFile()));
            } catch (IOException e) {
                throw new CloudClientUploadException("Failed reading compressed test file", e);
            }
        }

        @Override
        public boolean restoreOriginalRevision(CompressionFile compressionFile, String expectedMimeSuperType, boolean dryRun) {
            throw new UnsupportedOperationException("Not exercised by this test");
        }
    }
}
