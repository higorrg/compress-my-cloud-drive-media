package br.com.granzoto.media_compressor.main;

import br.com.granzoto.media_compressor.cloud_client.CloudClient;
import br.com.granzoto.media_compressor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.media_compressor.model.UserOptions;
import picocli.CommandLine;

import java.util.LinkedHashMap;

import static java.lang.System.out;

public class Main {

    public static void main(String... args) throws CloudClientListFilesException {
        CommandLine commandLine = new CommandLine(UserOptions.getInstance());
        try {
            commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException e) {
            System.err.println(e.getMessage());
            e.getCommandLine().usage(out, CommandLine.Help.Ansi.ON);
            System.exit(2);
            return;
        }

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(out, CommandLine.Help.Ansi.ON);
            System.exit(0);
            return;
        }
        if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(out, CommandLine.Help.Ansi.ON);
            System.exit(0);
            return;
        }

        int exitCode = new Main().run();
        System.exit(exitCode);
    }

    public Integer run() throws CloudClientListFilesException {
        var cloudClientFactory = new CloudClientFactory();
        CloudClient cloudClient = cloudClientFactory.getCloudClient(UserOptions.getInstance().getCloudDriveName());

        var handlerFactory = new HandlerFactory();

        // LinkedHashMap preserves insertion order, so handlers are registered as
        // observers in this exact order: list/csv, then download, then compress, then upload.
        var handlerOptions = new LinkedHashMap<String, Boolean>();
        handlerOptions.put("listHandler", UserOptions.getInstance().isListHandler());
        handlerOptions.put("csvHandler", UserOptions.getInstance().isCsvHandler());
        handlerOptions.put("downloadHandler", UserOptions.getInstance().isDownloadHandler());
        handlerOptions.put("videoCompressorHandler", UserOptions.getInstance().isVideoCompressorHandler());
        handlerOptions.put("imageCompressorHandler", UserOptions.getInstance().isImageCompressorHandler());
        handlerOptions.put("pdfCompressorHandler", UserOptions.getInstance().isPdfCompressorHandler());
        handlerOptions.put("uploadHandler", UserOptions.getInstance().isUploadHandler());
        handlerOptions.put("restoreCorruptedImageMimeTypeHandler", UserOptions.getInstance().isRestoreCorruptedImageMimeTypeHandler());
        handlerFactory.createCloudClientHandlers(cloudClient, handlerOptions);

        cloudClient.runFiles();
        return 0;
    }

}
