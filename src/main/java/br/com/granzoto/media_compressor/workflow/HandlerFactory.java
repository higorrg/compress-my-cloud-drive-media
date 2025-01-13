package br.com.granzoto.media_compressor.workflow;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This factory is based on method reference using java.util.function.Consumer
 * in order to delay the instantiation of each handler to when it's actually
 * needed.
 * <br>
 * Meaning that, the {@code client.addHandler(new FileListToLogHandler())} instruction
 * will only actually happen, only when {@code handlerCreators.get("logHandler").accept(client)}
 * will call.
 */
public class HandlerFactory {
    private final Map<String, Consumer<CloudClient>> handlerCreators = new HashMap<>();

    public HandlerFactory() {
        handlerCreators.put("logHandler", client -> client.addHandler(new FileListToLogHandler()));
        handlerCreators.put("csvHandler", client -> client.addHandler(new FileListToCsvHandler()));
        handlerCreators.put("downloadHandler", client -> client.addHandler(new DownloadHandler()));
        handlerCreators.put("videoCompressorHandler", client -> client.addHandler(new VideoCompressorHandler()));
        handlerCreators.put("imageCompressorHandler", client -> client.addHandler(new ImageCompressorHandler()));
        handlerCreators.put("uploadHandler", client -> client.addHandler(new UploadHandler()));
    }

    public void configureCloudClientHandlers(CloudClient client, Map<String, Boolean> options) {
        options.forEach((option, enabled) -> {
            if (enabled && handlerCreators.containsKey(option)) {
                handlerCreators.get(option).accept(client);
            }
        });
    }
}
