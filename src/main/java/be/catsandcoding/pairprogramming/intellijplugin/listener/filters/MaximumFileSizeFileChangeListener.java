package be.catsandcoding.pairprogramming.intellijplugin.listener.filters;

import be.catsandcoding.pairprogramming.intellijplugin.listener.PairProgrammingBulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;

public class MaximumFileSizeFileChangeListener implements PairProgrammingBulkFileListener {
    private final static int KILOBYTE = 1024;
    private final PairProgrammingBulkFileListener listener;
    private final long maxFileSizeInKB;

    private <E extends VFileEvent> void continueIfSmallEnough(E event){
        if(event != null && event.getFile() != null && event.getFile().getLength()/KILOBYTE <= maxFileSizeInKB){
            delegateEvent(listener, event);
        }
    }

    public MaximumFileSizeFileChangeListener(PairProgrammingBulkFileListener listener, long maxFileSizeInKB){
        this.listener = listener;
        this.maxFileSizeInKB = maxFileSizeInKB;
    }

    @Override
    public void handleFileEvent(VFileDeleteEvent event) {
        continueIfSmallEnough(event);
    }

    @Override
    public void handleFileEvent(VFileMoveEvent event) {
        continueIfSmallEnough(event);
    }

    @Override
    public void handleFileEvent(VFileCopyEvent event) {
        continueIfSmallEnough(event);
    }

    @Override
    public void handleFileEvent(VFilePropertyChangeEvent event) {
        continueIfSmallEnough(event);
    }

    @Override
    public void handleFileEvent(VFileCreateEvent event) {
        continueIfSmallEnough(event);
    }

    @Override
    public void handleFileEvent(VFileEvent event) {
        continueIfSmallEnough(event);
    }
}
