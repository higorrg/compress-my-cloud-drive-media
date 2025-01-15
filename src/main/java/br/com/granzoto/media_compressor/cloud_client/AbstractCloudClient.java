package br.com.granzoto.media_compressor.cloud_client;

import br.com.granzoto.media_compressor.model.CompressionFile;
import br.com.granzoto.media_compressor.model.FolderInfo;

import java.util.*;

public abstract class AbstractCloudClient implements CloudClient {

    private final List<CloudClientStartObserver> startObservers = new ArrayList<>();
    private final List<CloudClientItemObserver> itemObservers = new ArrayList<>();
    private final List<CloudClientEndObserver> endObservers = new ArrayList<>();
    private final List<CompressionFile> compressionFiles = new ArrayList<>();

    @Override
    public void addHandler(CloudClientHandler handler) {
        if (!Objects.isNull(handler)) {
            handler.registerObserver(this);
        }
    }

    @Override
    public void addStartObserver(CloudClientStartObserver observer) {
        if (!Objects.isNull(observer)) {
            this.startObservers.add(observer);
        }
    }

    @Override
    public void addItemObserver(CloudClientItemObserver observer) {
        if (!Objects.isNull(observer)) {
            this.itemObservers.add(observer);
        }
    }

    @Override
    public void addEndObserver(CloudClientEndObserver observer) {
        if (!Objects.isNull(observer)) {
            this.endObservers.add(observer);
        }
    }

    protected void notifyStart() {
        this.startObservers.forEach(observer -> observer.handleStart(this));
    }

    protected void notifyItem(CompressionFile compressionFile) {
        this.compressionFiles.add(compressionFile);
        this.itemObservers.forEach(observer -> observer.handleItem(compressionFile));
    }

    protected void notifyEnd(List<CompressionFile> compressionFiles) {
        this.endObservers.forEach(observer -> observer.handleEnd(compressionFiles));
    }

    @Override
    public void runFiles() throws CloudClientListFilesException {
        this.notifyStart();
        Map<String, FolderInfo> folderPaths = new HashMap<>();
        this.listFilesByPage(null, folderPaths, this.compressionFiles);
        this.notifyEnd(this.compressionFiles);
    }

    protected abstract void listFilesByPage(String page, Map<String, FolderInfo> folderPaths, List<CompressionFile> files)
            throws CloudClientListFilesException;

}
