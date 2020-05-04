package be.catsandcoding.pairprogramming.intellijplugin.impl;

import be.catsandcoding.pairprogramming.intellijplugin.PairProgramming;
import be.catsandcoding.pairprogramming.intellijplugin.listener.BufferContentChangeListener;
import be.catsandcoding.pairprogramming.intellijplugin.listener.FileChangeListener;
import be.catsandcoding.pairprogramming.intellijplugin.listener.PairProgrammingBulkFileListener;
import be.catsandcoding.pairprogramming.intellijplugin.listener.filters.ForbiddenPathFileChangeListener;
import be.catsandcoding.pairprogramming.intellijplugin.listener.filters.MaximumFileSizeFileChangeListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.openapi.vfs.VirtualFileManager.VFS_CHANGES;

public class PairProgrammingImpl implements PairProgramming {
    private BufferContentChangeListener buffer;
    private MessageBusConnection bus;
    private final Project project;
    private List<String> pathsToDeny = new ArrayList<>(List.of("^.*/resources/.*$"));
    private long maxFileSizeInKB = 0;

    private final AtomicBoolean inWriteAction = new AtomicBoolean(false);

    public void markAsInWriteAction(){
        inWriteAction.set(true);
    }
    public void markAsOutOfWriteAction(){
        inWriteAction.set(false);
    }
    public boolean isInWriteAction(){
        return inWriteAction.get();
    }
    public PairProgrammingImpl(Project project) {
        this.project = project;
    }

    @Override
    public void installContentChangeListener(){
        this.buffer = new BufferContentChangeListener(project);
        EditorFactory.getInstance().getEventMulticaster()
                .addDocumentListener(buffer, project);
    }

    @Override
    public void removeContentChangeListener(){
        if(buffer != null) {
            EditorFactory.getInstance().getEventMulticaster()
                    .removeDocumentListener(buffer);
            buffer = null;
        }
    }

    @Override
    public void installFileChangeListener(){
        bus = ApplicationManager.getApplication().getMessageBus().connect(project);
        bus.subscribe(VFS_CHANGES, getListenerChain());
    }
    private PairProgrammingBulkFileListener getListenerChain(){
        return decorateOnFileListeningConditions(new FileChangeListener(project));

    }
    private PairProgrammingBulkFileListener decorateOnFileListeningConditions(PairProgrammingBulkFileListener base){
        base = applyForbiddenPathsIfRequired(base);
        base = applyMaximumFileSizeIfRequired(base);
        return base;
    }

    private PairProgrammingBulkFileListener applyMaximumFileSizeIfRequired(PairProgrammingBulkFileListener base) {
        return hasMaximumFileSize()? new MaximumFileSizeFileChangeListener(base, maxFileSizeInKB) : base;
    }

    private PairProgrammingBulkFileListener applyForbiddenPathsIfRequired(PairProgrammingBulkFileListener base) {
        return hasPathsToDeny()? new ForbiddenPathFileChangeListener(base, project, pathsToDeny) : base;
    }

    @Override
    public void removeFileChangeListener(){
        if(bus != null) {
            bus.disconnect();
            bus = null;
        }
    }

    public boolean hasPathsToDeny(){
        return pathsToDeny != null && pathsToDeny.size() > 0;
    }
    public boolean hasMaximumFileSize(){
        return maxFileSizeInKB > 0;
    }

    public void setMaxFileSizeInKB(long maxFileSizeInKB) {
        this.maxFileSizeInKB = maxFileSizeInKB;
    }

    public long getMaxFileSizeInKB(){
        return maxFileSizeInKB;
    }

    public void setPathsToDeny(List<String> pathsToDeny) {
        this.pathsToDeny = pathsToDeny;
    }

    public List<String> getPathsToDeny() {
        return pathsToDeny;
    }

    public void clearPathsToDeny(){
        pathsToDeny.clear();
    }
}
