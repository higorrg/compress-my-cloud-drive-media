package br.com.granzoto.media_compressor.main;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "MediaCompressor", mixinStandardHelpOptions = true, version = "1.0",
        description = "Compresses media files from Cloud Drives.")
public class Main implements Callable<Integer> {

    @Option(names = "--list", description = "Display files on console.")
    boolean listHandler;

    @Option(names = "--csv", description = "Create CSV file with file data.")
    boolean csvHandler;

    @Option(names = "--video", description = "Compress videos.")
    boolean videoCompressorHandler;

    @Option(names = "--image", description = "Compress images.")
    boolean imageCompressorHandler;

    @Option(names = "--pdf", description = "Compress PDF.")
    boolean pdfCompressorHandler;

    @Option(names = "--download", description = "Enable the DownloadHandler.")
    boolean downloadHandler;

    @Option(names = "--upload", description = "Enable the UploadHandler.")
    boolean uploadHandler;

    @Option(names = "--cloud-drive", description = "Connect to cloud drive. Options are: 'Google'", required = true)
    String cloudDriveName;

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws CloudClientListFilesException {
        var cloudClientFactory = new CloudClientFactory();
        CloudClient cloudClient = cloudClientFactory.getCloudClient(cloudDriveName);

        var handlerFactory = new HandlerFactory();
        handlerFactory.createCloudClientHandlers(cloudClient, Map.of(
                "listHandler", listHandler,
                "csvHandler", csvHandler,
                "downloadHandler", downloadHandler,
                "videoCompressorHandler", videoCompressorHandler,
                "imageCompressorHandler", imageCompressorHandler,
                "pdfCompressorHandler", pdfCompressorHandler,
                "uploadHandler", uploadHandler
        ));

        cloudClient.runFiles();
        return 0;
    }

}
