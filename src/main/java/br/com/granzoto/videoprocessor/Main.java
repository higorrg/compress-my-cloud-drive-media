package br.com.granzoto.videoprocessor;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.LogManager;

import org.apache.commons.io.FileUtils;
import org.slf4j.bridge.SLF4JBridgeHandler;

import br.com.granzoto.videoprocessor.cloud_client.CloudClient;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.videoprocessor.cloud_client_for_google.GoogleDriveClient;
import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.video_compressor.VideoCompressor;
import br.com.granzoto.videoprocessor.video_compressor_for_ffmpeg.FFmpegCompressorWithHost;
import br.com.granzoto.videoprocessor.workflow.Workflow;

public class Main {

    public static void main(String... args) throws CloudClientListFilesException {
        setupLog();
        CloudClient cloudClient = GoogleDriveClient.getInstance();
        VideoCompressor compressor = FFmpegCompressorWithHost.getInstance();
        Workflow workflow = Workflow.getInstance(cloudClient, compressor);
        workflow.run();
        // Main.listOnly(cloudClient);
    }

    private static void listOnly(CloudClient cloudClient) throws CloudClientListFilesException {
        System.out.println("List only in progress");
        List<VideoCompressionFile> cloudFiles = cloudClient.listFiles();
        if (cloudFiles != null && !cloudFiles.isEmpty()) {
            cloudFiles.forEach(myFile -> {
                System.out.println(myFile.name() + ";" + myFile.size());
            });
            BigInteger totalSize = cloudFiles.stream().map(VideoCompressionFile::size).reduce(BigInteger.ZERO,
                    BigInteger::add);
            System.out.println("Total size: " + FileUtils.byteCountToDisplaySize(totalSize));
            System.out.println("Total items: " + cloudFiles.size());

        }
    }

    private static void setupLog(){
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

}
