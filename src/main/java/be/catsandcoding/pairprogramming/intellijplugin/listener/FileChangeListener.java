package be.catsandcoding.pairprogramming.intellijplugin.listener;

import be.catsandcoding.pairprogramming.intellijplugin.communication.*;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.*;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
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
    private final ContentChangeService contentChangeService;
    private final Project project;

    public FileChangeListener(Project project){
        this.project = project;
        contentChangeService = ServiceManager.getService(project, ContentChangeService.class);

    }
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
            }
            // TODO: verify if interesting to implement
            /* else if (event instanceof VFileContentChangeEvent) {
                handleFileEvent((VFileContentChangeEvent) event);
            } */
            else {
                handleFileEvent(event);
            }
            Project guess = projectLocator.guessProjectForFile(event.getFile());
            String projectName = guess == null?"":guess.getName();
            communicationService.showNotification("PROJECT: " + projectName);
        }

    }
    private void handleFileEvent(VFileDeleteEvent event){
        System.out.println("DELETE: " + event.getFile().getPath());
        String fileName = event.getFile().getPath();
        DeleteFileMessage dfMsg = new DeleteFileMessage(contentChangeService.getProjectIndependentPath(fileName),
                communicationService.getIdentity(),communicationService.getSessionId());
        communicationService.sendMessage(dfMsg);
        communicationService.showNotification("DELETE " + event.getFile().getName());
    }
    private void handleFileEvent(VFileMoveEvent event){
        System.out.println("MOVE: " + event.getOldPath() + " to " + event.getNewPath());
        String oldPath = event.getOldPath();
        String newPath = event.getNewPath();
        MoveFileMessage mvMsg = new MoveFileMessage(contentChangeService.getProjectIndependentPath(oldPath),
                contentChangeService.getProjectIndependentPath(newPath), event.getFile().isDirectory(),
                communicationService.getIdentity(),communicationService.getSessionId());

        communicationService.sendMessage(mvMsg);
        communicationService.showNotification("MOVE " + event.getOldPath() + " to " + event.getNewPath());
    }
    private void handleFileEvent(VFileCopyEvent event){
        String oldPath = event.getFile().getPath();
        String newPath = event.getNewParent().getPath() + "/" + event.getNewChildName();
        System.out.println("COPY " + oldPath + " => " + newPath);
        CopyFileMessage cpMsg = new CopyFileMessage(contentChangeService.getProjectIndependentPath(oldPath),
                contentChangeService.getProjectIndependentPath(newPath), event.getFile().isDirectory(),
                communicationService.getIdentity(),communicationService.getSessionId());
        communicationService.sendMessage(cpMsg);
        communicationService.showNotification("COPY" + event.getNewParent() + " " + event.getNewChildName());
    }
    private void handleFileEvent(VFilePropertyChangeEvent event){
        String property = event.getPropertyName();
        String newPath = event.getNewPath();
        String oldPath = event.getOldPath();
        if(!oldPath.equals(newPath)){
            RenameFileMessage rnMsg = new RenameFileMessage(contentChangeService.getProjectIndependentPath(oldPath),
                    contentChangeService.getProjectIndependentPath(newPath), event.getFile().isDirectory(),
                    communicationService.getIdentity(),communicationService.getSessionId());
            communicationService.sendMessage(rnMsg);
            System.out.println("CHANGE NAME: " + oldPath + " => " + newPath);
            communicationService.showNotification("PROPERTY CHANGE " + property + "\n" + newPath + " from " + oldPath);
        }
    }
    private void handleFileEvent(VFileCreateEvent event){
        VirtualFile file = event.getFile();
        String fileName = event.getFile().getPath();
        CreateFileMessage cfMsg = new CreateFileMessage(contentChangeService.getProjectIndependentPath(fileName),
                file.isDirectory(), communicationService.getIdentity(),communicationService.getSessionId());

        communicationService.sendMessage(cfMsg);
        System.out.println("CREATE: " + fileName);
        communicationService.showNotification("CREATE: " + fileName);

    }
    private void handleFileEvent(VFileEvent event){
        communicationService.showNotification( "Unknown event: " + event.getClass());
    }
}
