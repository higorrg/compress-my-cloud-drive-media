package br.com.granzoto.media_compressor.model;

import com.google.common.base.Strings;
import picocli.CommandLine;

@CommandLine.Command(name = "MediaCompressor", mixinStandardHelpOptions = true, version = "1.0",
        description = "Compresses media files from Cloud Drives.")
public class UserOptions {

    private static final UserOptions instance = new UserOptions();

    @CommandLine.Option(names = "--list", description = "Display files on console.")
    private boolean listHandler;

    @CommandLine.Option(names = "--csv", description = "Create CSV file with file data.")
    private boolean csvHandler;

    @CommandLine.Option(names = "--video", description = "Compress videos.")
    private boolean videoCompressorHandler;

    @CommandLine.Option(names = "--image", description = "Compress images.")
    private boolean imageCompressorHandler;

    @CommandLine.Option(names = "--pdf", description = "Compress PDF.")
    private boolean pdfCompressorHandler;

    @CommandLine.Option(names = "--download", description = "Enable the DownloadHandler.")
    private boolean downloadHandler;

    @CommandLine.Option(names = "--download-folder", description = "Folder to download files, like ~/Downloads for example.")
    private String downloadPath;

    @CommandLine.Option(names = "--upload", description = "Enable the UploadHandler.")
    private boolean uploadHandler;

    @CommandLine.Option(names = "--cloud-drive", description = "Connect to cloud drive. Options are: 'Google'", required = true)
    private String cloudDriveName;

    private UserOptions() {
        super();
    }

    public static synchronized UserOptions getInstance() {
        return instance;
    }

    public boolean isListHandler() {
        return listHandler;
    }

    public boolean isCsvHandler() {
        return csvHandler;
    }

    public boolean isVideoCompressorHandler() {
        return videoCompressorHandler;
    }

    public boolean isImageCompressorHandler() {
        return imageCompressorHandler;
    }

    public boolean isPdfCompressorHandler() {
        return pdfCompressorHandler;
    }

    public boolean isDownloadHandler() {
        return downloadHandler;
    }

    public boolean isUploadHandler() {
        return uploadHandler;
    }

    public String getCloudDriveName() {
        return cloudDriveName;
    }

    public String getDownloadPath() {
        if (Strings.isNullOrEmpty(this.downloadPath)) {
            return System.getProperty("java.io.tmpdir");
        }
        return downloadPath;
    }
}
