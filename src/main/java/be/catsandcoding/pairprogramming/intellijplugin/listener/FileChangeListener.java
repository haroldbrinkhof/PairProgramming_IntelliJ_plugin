package be.catsandcoding.pairprogramming.intellijplugin.listener;

import be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FileChangeListener implements BulkFileListener {
    private final CommunicationService communicationService = ServiceManager.getService(CommunicationService.class);
    private final ProjectLocator projectLocator = ProjectLocator.getInstance();


    @Override
    public void before(@NotNull List<? extends VFileEvent> events) {

    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for(VFileEvent event: events){
            if (event instanceof VFileCreateEvent){
                handleFileEvent((VFileCreateEvent) event);
            } else if (event instanceof  VFilePropertyChangeEvent){
                handleFileEvent((VFilePropertyChangeEvent) event);
            } else if (event instanceof VFileCopyEvent){
                handleFileEvent((VFileCopyEvent) event);
            } else if (event instanceof  VFileDeleteEvent){
                handleFileEvent((VFileDeleteEvent) event);
            } else if (event instanceof  VFileMoveEvent){
                handleFileEvent((VFileMoveEvent) event);
            } /* else if (event instanceof VFileContentChangeEvent) {
                handleFileEvent((VFileContentChangeEvent) event);
            } */ else {
                handleFileEvent(event);
            }
            Project guess = projectLocator.guessProjectForFile(event.getFile());
            String projectName = guess == null?"":guess.getName();
            communicationService.showNotification("PROJECT: " + projectName);
        }

    }
    private void handleFileEvent(VFileDeleteEvent event){
        communicationService.showNotification("DELETE " + event.getFile().getName());
    }
    private void handleFileEvent(VFileMoveEvent event){
        communicationService.showNotification("MOVE" + event.getOldPath() + " to " + event.getNewPath());
    }
    private void handleFileEvent(VFileCopyEvent event){
        communicationService.showNotification("COPY" + event.getNewParent() + " " + event.getNewChildName());
    }
    private void handleFileEvent(VFilePropertyChangeEvent event){
        String property = event.getPropertyName();
        String newPath = event.getNewPath();
        String oldPath = event.getOldPath();
        communicationService.showNotification("PROPERTY CHANGE " + property + "\n" + newPath + " from " + oldPath);
    }
    private void handleFileEvent(VFileCreateEvent event){
        VirtualFile file = event.getFile();
        String fileName = file == null?"":file.getName();
        communicationService.showNotification("CREATE: " + fileName);

    }
    private void handleFileEvent(VFileEvent event){
        communicationService.showNotification( "Unknown event: " + event.getClass());

    }
}
