package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;
import br.com.granzoto.media_compressor.model.FolderInfo;
import br.com.granzoto.media_compressor.workflow.FirstCloudClientHandler;

import java.util.*;

public abstract class AbstractCloudClient implements CloudClient {

    private final CloudClientHandler firstHandler = new FirstCloudClientHandler();

    @Override
    public void addHandler(CloudClientHandler handler) {
        if (!Objects.isNull(handler)) {
            this.firstHandler.link(handler);
        }
    }

    protected void handleNextItem(CompressionFile compressionFile){
        this.firstHandler.handleItem(compressionFile);
    }

    @Override
    public void runFiles() throws CloudClientListFilesException {
        this.firstHandler.handleStart(this);
        Map<String, FolderInfo> folderPaths = new HashMap<>();
        List<CompressionFile> files = new ArrayList<>();
        this.listFilesByPage(null, folderPaths, files);
        this.firstHandler.handleEnd(files);
    }

    protected abstract void listFilesByPage(String page, Map<String, FolderInfo> folderPaths, List<CompressionFile> files)
            throws CloudClientListFilesException;

}
