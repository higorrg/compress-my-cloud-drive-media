package br.com.granzoto.videoprocessor;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.io.FileUtils;

import br.com.granzoto.videoprocessor.cloud_client.CloudClient;
import br.com.granzoto.videoprocessor.cloud_client.CloudClientListFilesException;
import br.com.granzoto.videoprocessor.cloud_client_for_google.GoogleDriveClient;
import br.com.granzoto.videoprocessor.model.VideoCompressionFile;
import br.com.granzoto.videoprocessor.video_compressor.VideoCompressor;
import br.com.granzoto.videoprocessor.video_compressor_for_ffmpeg.FFmpegCompressorWithHost;
import br.com.granzoto.videoprocessor.workflow.Workflow;
import br.com.granzoto.videoprocessor.workflow.WorkflowCompressionStepException;
import br.com.granzoto.videoprocessor.workflow.WorkflowDownloadStepException;
import br.com.granzoto.videoprocessor.workflow.WorkflowUploadStepException;

public class Main {

    public static void main(String... args) throws CloudClientListFilesException {
        CloudClient cloudClient = GoogleDriveClient.getInstance();
        // VideoCompressor compressor = FFmpegCompressorWithHost.getInstance();
        // Workflow workflow = Workflow.getInstance(cloudClient, compressor);
        // workflow.run();
        Main.listOnly(cloudClient);
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

}
