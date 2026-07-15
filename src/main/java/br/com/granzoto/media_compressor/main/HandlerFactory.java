package br.com.granzoto.media_compressor.main;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client_observer_handler.*;
import br.com.granzoto.media_compressor.model.UserOptions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <p>
 * This factory is based on method reference using {@code java.util.function.Consumer}
 * in order to delay the instantiation of each handler to when it's actually
 * needed.
 * </p>
 * <p>
 * Meaning that, the {@code client.addHandler(new FileListToLogHandler())} instruction
 * will only actually happen, only when {@code handlerCreators.get("logHandler").accept(client)}
 * will call.
 * </p>
 * <p>
 * Using method references or lambdas with Consumer ensures that handlers are instantiated only when required
 * enabling lazy evaluation, which optimizes performance and resource usage.
 * </p>
 */
public class HandlerFactory {
    private final Map<String, Consumer<CloudClient>> handlerCreators = new HashMap<>();

    public HandlerFactory() {
        handlerCreators.put("listHandler", client -> client.addHandler(new FileListToLogHandler()));
        handlerCreators.put("csvHandler", client -> client.addHandler(new FileListToCsvHandler()));
        handlerCreators.put("downloadHandler", client -> client.addHandler(new DownloadHandler()));
        handlerCreators.put("videoCompressorHandler", client -> client.addHandler(new VideoCompressorHandler()));
        handlerCreators.put("imageCompressorHandler", client -> client.addHandler(new ImageCompressorHandler()));
        handlerCreators.put("pdfCompressorHandler", client -> client.addHandler(new PdfCompressorHandler()));
        handlerCreators.put("uploadHandler", client -> client.addHandler(new UploadHandler()));
        handlerCreators.put("restoreCorruptedImageMimeTypeHandler", client -> client.addHandler(
                new RestoreCorruptedImageMimeTypeHandler(!UserOptions.getInstance().isApplyRestore())));
    }

    /**
     * <p>
     * {@code options} must be a {@link LinkedHashMap} (not just any {@link Map}) because
     * handler registration order determines observer notification order: handlers are
     * notified per file in the exact order they're registered here, so e.g. downloading
     * must be registered before compressing, which must be registered before uploading.
     * An unordered map (like {@code Map.of(...)}, whose iteration order is unspecified
     * and randomized per JVM run) would make that order non-deterministic.
     * </p>
     */
    public void createCloudClientHandlers(CloudClient client, LinkedHashMap<String, Boolean> options) {
        options.forEach((option, enabled) -> {
            if (enabled && handlerCreators.containsKey(option)) {
                handlerCreators.get(option).accept(client);
            }
        });
    }
}
