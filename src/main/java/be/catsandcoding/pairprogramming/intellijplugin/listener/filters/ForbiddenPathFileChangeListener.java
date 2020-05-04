package be.catsandcoding.pairprogramming.intellijplugin.listener.filters;

import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import be.catsandcoding.pairprogramming.intellijplugin.listener.PairProgrammingBulkFileListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.events.*;

import java.util.List;

public class ForbiddenPathFileChangeListener implements PairProgrammingBulkFileListener {
    private final List<String> pathsToDeny;
    private final PairProgrammingBulkFileListener listener;
    private final ContentChangeService contentChangeService;

    private <E extends VFileEvent> void continueIfNoneMatch(E event){
        if(event != null && noneMatches(event.getPath())) {
            delegateEvent(listener, event);
        }
    }

    private boolean noneMatches(String path) {
        String filePath = contentChangeService.getProjectIndependentPath(path);
        for(String filter: pathsToDeny){
            if(filePath.matches(filter)) {
                return false;
            }
        }
        return true;
    }

    public ForbiddenPathFileChangeListener(PairProgrammingBulkFileListener listener, Project project, List<String> pathsToDeny){
        this.listener = listener;
        this.pathsToDeny = pathsToDeny;
        contentChangeService  = ContentChangeService.getInstance(project);
    }

    @Override
    public void handleFileEvent(VFileDeleteEvent event) {
        continueIfNoneMatch(event);
    }

    @Override
    public void handleFileEvent(VFileMoveEvent event) {
        continueIfNoneMatch(event);
    }

    @Override
    public void handleFileEvent(VFileCopyEvent event) {
        continueIfNoneMatch(event);
    }

    @Override
    public void handleFileEvent(VFilePropertyChangeEvent event) {
        continueIfNoneMatch(event);
    }

    @Override
    public void handleFileEvent(VFileCreateEvent event) {
        continueIfNoneMatch(event);
    }

    @Override
    public void handleFileEvent(VFileEvent event) {
        continueIfNoneMatch(event);
    }
}
