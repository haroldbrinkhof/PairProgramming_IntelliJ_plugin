package be.catsandcoding.pairprogramming.intellijplugin.editing.impl;

import be.catsandcoding.pairprogramming.intellijplugin.communication.*;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import name.fraser.neil.plaintext.diff_match_patch;
import org.jdesktop.swingx.util.OS;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;

public class ContentChangeServiceImpl implements ContentChangeService {
    private final Project project;
    private final VirtualFileManager virtualFileManager;
    private final FileDocumentManager fileDocumentManager;

    public ContentChangeServiceImpl(Project project) {
        this.project = project;
        this.virtualFileManager = VirtualFileManager.getInstance();
        this.fileDocumentManager = FileDocumentManager.getInstance();
    }

    public String getProjectRoot(){
        VirtualFile where = ModuleRootManager.getInstance(ModuleManager.getInstance(project).getModules()[0]).getContentRoots()[0];
        return where.getPath();
    }

    public void handle(final MoveFileMessage msg){

        final String from = getProjectRoot() + msg.getFrom().replace("World", "Copy");
        final String to = getProjectRoot() + msg.getTo().replace("World", "Copy");
        final VirtualFile fromLocal = virtualFileManager.refreshAndFindFileByUrl("file://" + from);
        final VirtualFile toLocal = virtualFileManager.refreshAndFindFileByUrl("file://" + to.substring(0, to.lastIndexOf("/") + 1));

        System.out.println("HANDLE MOVE: " + from + " => " + to);
        if(fromLocal != null && toLocal != null &&
                ProjectFileIndex.SERVICE.getInstance(project).isInContent(fromLocal) &&
                ProjectFileIndex.SERVICE.getInstance(project).isInContent(toLocal)) {
            WriteCommandAction.runWriteCommandAction(project,
                    () -> {
                        try {
                            fromLocal.move(this, toLocal);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
    public void handle(final CopyFileMessage msg){
        final String from = getProjectRoot() + msg.getFrom().replace("World", "Copy");
        final String to = getProjectRoot() + msg.getTo().replace("World", "Copy");

        System.out.println("HANDLE COPY: " + from + " => " + to);
        final VirtualFile fromLocal = virtualFileManager.refreshAndFindFileByUrl("file://" + from);
        final VirtualFile toLocal = virtualFileManager.refreshAndFindFileByUrl("file://" + to.substring(0, to.lastIndexOf("/") + 1));
        if(fromLocal != null && toLocal != null &&
                ProjectFileIndex.SERVICE.getInstance(project).isInContent(fromLocal) &&
                ProjectFileIndex.SERVICE.getInstance(project).isInContent(toLocal)){
            WriteCommandAction.runWriteCommandAction(project,
                    ()  -> {
                        try {
                            String target = to.substring(to.lastIndexOf("/") + 1);
                            fromLocal.copy(this, toLocal, target);
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void handle(final RenameFileMessage msg){
        final String from = getProjectRoot() + msg.getFrom().replace("World", "Copy");
        final String to = getProjectRoot() + msg.getTo().replace("World", "Copy");

        System.out.println("HANDLE CHANGE NAME: " + from + " => " + to);
        final VirtualFile fromLocal = virtualFileManager.refreshAndFindFileByUrl("file://" + from);
        if(fromLocal != null && ProjectFileIndex.SERVICE.getInstance(project).isInContent(fromLocal)){
            WriteCommandAction.runWriteCommandAction(project,
                    ()  -> {
                        try {
                            String target = to.substring(to.lastIndexOf("/") + 1);
                            fromLocal.rename(this, target);
                        } catch(IOException e){
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void handle(final DeleteFileMessage msg){
        String filename = getProjectRoot() + msg.getFileName().replace("World", "Copy");
        final VirtualFile file = virtualFileManager.refreshAndFindFileByUrl("file://" + filename);
        if(file == null || !ProjectFileIndex.SERVICE.getInstance(project).isInContent(file)) return ;
        WriteCommandAction.runWriteCommandAction(project,
                ()  -> {
                    try {
                        file.delete(this);
                    } catch(IOException e){
                        e.printStackTrace();
                    }
        });
    }

    public void handle(final CreateFileMessage msg){
        String filename = getProjectRoot() + msg.getFileName().replace("World", "Copy");
        System.out.println("CREATING FOR: " + filename);
        final VirtualFile file = virtualFileManager.refreshAndFindFileByUrl("file://" + filename);
        if(file != null) return;
        if(OS.isWindows()) filename = filename.replace("/","\\");

        final String fileToCreate = filename;
        WriteCommandAction.runWriteCommandAction(project,
                ()  -> {
                    try {

                        // Create the empty file with default permissions, etc.
                        Path path = Paths.get(fileToCreate);
                        if(msg.isDirectory()){
                            System.out.println("creating directory");
                            Files.createDirectory(path);
                        } else {
                            System.out.println("creating file");
                            Files.createFile(path);
                        }
                    } catch (FileAlreadyExistsException x) {
                        System.err.format("file named %s" +
                                " already exists%n", fileToCreate);
                    } catch (IOException x) {
                        // Some other sort of failure, such as permissions.
                        System.err.format("createFile error: %s%n", x);
                    }
                });

        virtualFileManager.refreshAndFindFileByUrl("file://" + filename);

    }

    public void handle(final ContentChangeMessage msg){
        String filename = getProjectRoot() + msg.getFileName().replace("World", "Copy");
        System.out.println("perfomChange: determine VirtualFile " + filename);
        final VirtualFile toChange = virtualFileManager.refreshAndFindFileByUrl("file://" + filename);
        if(toChange == null) return;

        System.out.println("perfomChange: determine Document");
        final Document contents = ReadAction.compute(() -> fileDocumentManager.getDocument(toChange));
        if(contents == null) return;

        System.out.println("changing contents of file: " + toChange.getPath());
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Patch> patch = (LinkedList<diff_match_patch.Patch>) dmp.patch_fromText(msg.getPatch());
        final Object[] results = dmp.patch_apply(patch,contents.getText());
        System.out.println("NEW TEXT: " + results[0].toString());
        WriteCommandAction.runWriteCommandAction(project, () -> {
                contents.setText(results[0].toString());
                fileDocumentManager.saveDocument(contents);
            } );
    }
}
