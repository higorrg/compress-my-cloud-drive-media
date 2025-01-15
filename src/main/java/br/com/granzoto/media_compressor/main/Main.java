package br.com.granzoto.media_compressor.main;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.model.UserOptions;
import picocli.CommandLine;

import java.util.Map;

import static java.lang.System.out;

public class Main {

    public static void main(String... args) throws CloudClientListFilesException {
        try {
            new CommandLine(UserOptions.getInstance()).parseArgs(args);
            int exitCode = new Main().run();
            System.exit(exitCode);
        } catch (CommandLine.ParameterException e) {
            System.err.println(e.getMessage());
            e.getCommandLine().usage(out, CommandLine.Help.Ansi.ON);
            System.exit(2);
        }
    }

    public Integer run() throws CloudClientListFilesException {
        var cloudClientFactory = new CloudClientFactory();
        CloudClient cloudClient = cloudClientFactory.getCloudClient(UserOptions.getInstance().getCloudDriveName());

        var handlerFactory = new HandlerFactory();
        handlerFactory.createCloudClientHandlers(cloudClient, Map.of(
                "listHandler", UserOptions.getInstance().isListHandler(),
                "csvHandler", UserOptions.getInstance().isCsvHandler(),
                "downloadHandler", UserOptions.getInstance().isDownloadHandler(),
                "videoCompressorHandler", UserOptions.getInstance().isVideoCompressorHandler(),
                "imageCompressorHandler", UserOptions.getInstance().isImageCompressorHandler(),
                "pdfCompressorHandler", UserOptions.getInstance().isPdfCompressorHandler(),
                "uploadHandler", UserOptions.getInstance().isUploadHandler()
        ));

        cloudClient.runFiles();
        return 0;
    }

}
