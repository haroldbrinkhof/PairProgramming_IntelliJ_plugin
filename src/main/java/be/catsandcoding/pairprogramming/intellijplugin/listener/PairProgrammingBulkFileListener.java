package be.catsandcoding.pairprogramming.intellijplugin.listener;

import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PairProgrammingBulkFileListener extends BulkFileListener {
    @Override
    default void after(@NotNull List<? extends VFileEvent> events) {
        for(VFileEvent event: events){
            delegateEvent(this, event);
        }
    }

    default void delegateEvent(PairProgrammingBulkFileListener listener, VFileEvent event) {
        if (event instanceof VFileCreateEvent){
            listener.handleFileEvent((VFileCreateEvent) event);
        } else if (event instanceof VFilePropertyChangeEvent){
            listener.handleFileEvent((VFilePropertyChangeEvent) event);
        } else if (event instanceof VFileCopyEvent){
            listener.handleFileEvent((VFileCopyEvent) event);
        } else if (event instanceof VFileDeleteEvent){
            listener.handleFileEvent((VFileDeleteEvent) event);
        } else if (event instanceof  VFileMoveEvent){
            listener.handleFileEvent((VFileMoveEvent) event);
        }
        // TODO: verify if interesting to implement
        /* else if (event instanceof VFileContentChangeEvent) {
            listener.handleFileEvent((VFileContentChangeEvent) event);
        } */
        else {
            listener.handleFileEvent(event);
        }
    }

    void handleFileEvent(VFileDeleteEvent event);
    void handleFileEvent(VFileMoveEvent event);
    void handleFileEvent(VFileCopyEvent event);
    void handleFileEvent(VFilePropertyChangeEvent event);
    void handleFileEvent(VFileCreateEvent event);
    void handleFileEvent(VFileEvent event);
}
