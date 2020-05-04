package be.catsandcoding.pairprogramming.intellijplugin.listener;

import be.catsandcoding.pairprogramming.intellijplugin.communication.*;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.*;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.*;

import java.io.IOException;

public class FileChangeListener implements PairProgrammingBulkFileListener {
    private final CommunicationService communicationService = ServiceManager.getService(CommunicationService.class);
    private final ContentChangeService contentChangeService;

    public FileChangeListener(Project project){
        contentChangeService = ContentChangeService.getInstance(project);
    }

    @Override
    public void handleFileEvent(VFileDeleteEvent event){
        System.out.println("DELETE: " + event.getFile().getPath());
        String fileName = event.getFile().getPath();
        DeleteFileMessage dfMsg = new DeleteFileMessage(contentChangeService.getProjectIndependentPath(fileName));
        communicationService.sendMessage(dfMsg);
        communicationService.showNotification("DELETE " + event.getFile().getName());
    }

    @Override
    public void handleFileEvent(VFileMoveEvent event){
        System.out.println("MOVE: " + event.getOldPath() + " to " + event.getNewPath());
        String oldPath = event.getOldPath();
        String newPath = event.getNewPath();
        MoveFileMessage mvMsg = new MoveFileMessage(contentChangeService.getProjectIndependentPath(oldPath),
                contentChangeService.getProjectIndependentPath(newPath), event.getFile().isDirectory());

        communicationService.sendMessage(mvMsg);
        communicationService.showNotification("MOVE " + event.getOldPath() + " to " + event.getNewPath());
    }

    @Override
    public void handleFileEvent(VFileCopyEvent event){
        String oldPath = event.getFile().getPath();
        String newPath = event.getNewParent().getPath() + "/" + event.getNewChildName();
        System.out.println("COPY " + oldPath + " => " + newPath);

        if(contentChangeService.isPartOfThisProject(oldPath)) {
            CopyFileMessage cpMsg = new CopyFileMessage(contentChangeService.getProjectIndependentPath(oldPath),
                    contentChangeService.getProjectIndependentPath(newPath), event.getFile().isDirectory());
            communicationService.sendMessage(cpMsg);
        } else {
            try {
                String content = VfsUtil.loadText(event.getFile());
                CopyOutsideFileMessage cpoMsg;
                cpoMsg = new CopyOutsideFileMessage(contentChangeService.getProjectIndependentPath(newPath), content);
                communicationService.sendMessage(cpoMsg);
            } catch(IOException e) {
                e.printStackTrace();
            }

        }
        communicationService.showNotification("COPY" + event.getNewParent() + " " + event.getNewChildName());
    }

    @Override
    public void handleFileEvent(VFilePropertyChangeEvent event){
        System.out.println("VFilePropertyChangeEvent");

        String property = event.getPropertyName();
        String newPath = event.getNewPath();
        String oldPath = event.getOldPath();
        if(!oldPath.equals(newPath)){
            RenameFileMessage rnMsg = new RenameFileMessage(contentChangeService.getProjectIndependentPath(oldPath),
                    contentChangeService.getProjectIndependentPath(newPath), event.getFile().isDirectory());
            communicationService.sendMessage(rnMsg);
            System.out.println("CHANGE NAME: " + oldPath + " => " + newPath);
            communicationService.showNotification("PROPERTY CHANGE " + property + "\n" + newPath + " from " + oldPath);
        } else {
            System.out.println("no match propertychange");
        }
    }

    @Override
    public void handleFileEvent(VFileCreateEvent event){
        VirtualFile file = event.getFile();
        if(file == null) return;

        String fileName = file.getPath();
        CreateFileMessage cfMsg = new CreateFileMessage(contentChangeService.getProjectIndependentPath(fileName),
                file.isDirectory());

        communicationService.sendMessage(cfMsg);
        System.out.println("CREATE: " + fileName);
        communicationService.showNotification("CREATE: " + fileName);

    }

    @Override
    public void handleFileEvent(VFileEvent event){
        System.out.println("Unhandled event: " + event.getClass());
        communicationService.showNotification( "Unhandled event: " + event.getClass());
    }
}
