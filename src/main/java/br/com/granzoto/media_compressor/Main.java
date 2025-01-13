package br.com.granzoto.media_compressor;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.workflow.CloudClientFactory;
import br.com.granzoto.media_compressor.workflow.HandlerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.LogManager;

@Command(name = "MediaCompressor", mixinStandardHelpOptions = true, version = "1.0",
        description = "Compresses media files from Cloud Drives.")
public class Main implements Callable<Integer> {

    @Option(names = "--log", description = "Display log on console.")
    boolean logHandler;

    @Option(names = "--csv", description = "Create CSV file with file data.")
    boolean csvHandler;

    @Option(names = "--video-compressor", description = "Compress videos.")
    boolean videoCompressorHandler;

    @Option(names = "--image-compressor", description = "Compress images.")
    boolean imageCompressorHandler;

    @Option(names = "--download", description = "Enable the DownloadHandler.")
    boolean downloadHandler;

    @Option(names = "--upload", description = "Enable the UploadHandler.")
    boolean uploadHandler;

    @Option(names = "--cloud-drive", description = "Connect to cloud drive.", defaultValue = "google", required = true)
    String driveInstance;

    public static void main(String... args) {
        setupLog();
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    private static void setupLog() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    @Override
    public Integer call() throws CloudClientListFilesException {
        var cloudClientFactory = new CloudClientFactory();
        CloudClient cloudClient = cloudClientFactory.getCloudClient(driveInstance);

        var handlerFactory = new HandlerFactory();
        handlerFactory.createCloudClientHandlers(cloudClient, Map.of(
                "logHandler", logHandler,
                "csvHandler", csvHandler,
                "downloadHandler", downloadHandler,
                "videoCompressorHandler", videoCompressorHandler,
                "imageCompressorHandler", imageCompressorHandler,
                "uploadHandler", uploadHandler
        ));

        cloudClient.runFiles();
        return 0;
    }

}
