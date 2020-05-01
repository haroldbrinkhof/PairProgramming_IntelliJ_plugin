package be.catsandcoding.pairprogramming.intellijplugin.impl;

import be.catsandcoding.pairprogramming.intellijplugin.PairProgramming;
import be.catsandcoding.pairprogramming.intellijplugin.listener.BufferContentChangeListener;
import be.catsandcoding.pairprogramming.intellijplugin.listener.FileChangeListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.openapi.vfs.VirtualFileManager.VFS_CHANGES;

public class PairProgrammingImpl implements PairProgramming {
    private BufferContentChangeListener buffer;
    private FileChangeListener files;
    private MessageBusConnection bus;
    private final Project project;

    private AtomicBoolean inWriteAction = new AtomicBoolean(false);

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
        this.files = new FileChangeListener(project);
        bus = ApplicationManager.getApplication().getMessageBus().connect(project);
        bus.subscribe(VFS_CHANGES, files);
    }

    @Override
    public void removeFileChangeListener(){
        if(bus != null) {
            bus.disconnect();
            bus = null;
        }
    }

}
