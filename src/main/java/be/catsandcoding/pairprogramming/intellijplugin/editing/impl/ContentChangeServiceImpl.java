package be.catsandcoding.pairprogramming.intellijplugin.editing.impl;

import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.*;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ActionPerformed;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ActionsPerformedCache;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.jdesktop.swingx.util.OS;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;

public class ContentChangeServiceImpl implements ContentChangeService {
    private final Project project;
    private final VirtualFileManager virtualFileManager;
    private final FileDocumentManager fileDocumentManager;
    private final ActionsPerformedCache actionsPerformedCache;

    public ContentChangeServiceImpl(Project project) {
        this.project = project;
        this.virtualFileManager = VirtualFileManager.getInstance();
        this.fileDocumentManager = FileDocumentManager.getInstance();
        this.actionsPerformedCache = ActionsPerformedCache.getInstance(project);
    }

    @Override
    public String getProjectRoot(){
        VirtualFile where = ModuleRootManager.getInstance(ModuleManager.getInstance(project).getModules()[0]).getContentRoots()[0];
        return where.getPath();
    }

    @Override
    public String getProjectIndependentPath(String path){
        return path.replace(getProjectRoot(), "");
    }

    public String transformToProjectPath(String path) {
        return getProjectRoot() + path;
    }

    @Override
    public void handle(final MoveFileMessage msg){
        final String from = transformToProjectPath(msg.getFrom());
        final String to = transformToProjectPath(msg.getTo());
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

    @Override
    public void handle(final CopyFileMessage msg){
        final String from = transformToProjectPath(msg.getFrom());
        final String to = transformToProjectPath(msg.getTo());

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

    @Override
    public void handle(final RenameFileMessage msg){
        final String from = transformToProjectPath(msg.getFrom());
        final String to = transformToProjectPath(msg.getTo());

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

    @Override
    public void handle(final DeleteFileMessage msg){
        String filename = transformToProjectPath(msg.getFileName());
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

    @Override
    public void handle(final CreateFileMessage msg){
        String filename = transformToProjectPath(msg.getFileName());
        System.out.println("CREATING FOR: " + filename);
        final VirtualFile file = virtualFileManager.refreshAndFindFileByUrl("file://" + filename);
        if(file != null) return;

        final String fileToCreate = filename;
        WriteCommandAction.runWriteCommandAction(project, ()  -> tryToCreate(msg, fileToCreate) );

        virtualFileManager.refreshAndFindFileByUrl("file://" + filename);

    }

    private void tryToCreate(CreateFileMessage msg, String fileToCreate) {
        try {

            Path path = Paths.get(safeguardForWindowsIfNecessary(fileToCreate));
            if(msg.isDirectory()){
                Files.createDirectory(path);
            } else {
                Files.createFile(path);
            }
        } catch (FileAlreadyExistsException x) {
            System.err.format("file named %s" +
                    " already exists%n", fileToCreate);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    private String safeguardForWindowsIfNecessary(String fileToCreate) {
        return OS.isWindows()?fileToCreate.replace("/","\\"):fileToCreate;
    }
    private boolean isChangeUnnecessary(Document document, String hash){
        return DigestUtils.md5Hex(document.getText()).toUpperCase().equals(hash);
    }

    @Override
    public void handle(final CompleteFileContentChangeMessage msg){
        String filename = transformToProjectPath(msg.getFileName());
        System.out.println("perfomChange: determine VirtualFile " + filename);
        final VirtualFile toChange = virtualFileManager.refreshAndFindFileByUrl("file://" + filename);
        if(toChange == null) return;

        final Document contents = ReadAction.compute(() -> fileDocumentManager.getDocument(toChange));
        if(contents == null) return;
        if(isChangeUnnecessary(contents, msg.getHash())) return;

        System.out.println("NEW TEXT: " + msg.getContent());
        WriteCommandAction.runWriteCommandAction(project, () -> {
            contents.setText(msg.getContent());
            fileDocumentManager.saveDocument(contents);
        } );
    }

    @Override
    public void handle(final ContentChangeMessage msg){
        if(actionsPerformedCache.alreadyPerformedPrior(new ActionPerformed(msg.getCommandMessageType(), msg.getFileName()))) return;
        String filename = transformToProjectPath(msg.getFileName());
        System.out.println("perfomChange: determine VirtualFile " + filename);
        final VirtualFile toChange = virtualFileManager.refreshAndFindFileByUrl("file://" + filename);
        if(toChange == null) return;

        System.out.println("perfomChange: determine Document");
        final Document contents = ReadAction.compute(() -> fileDocumentManager.getDocument(toChange));
        if(contents == null) return;
        if(isChangeUnnecessary(contents, msg.getHash())) return;

        System.out.println("changing contents of file: " + toChange.getPath());
        final String result = applyPatchToContents(msg, contents);

        System.out.println("NEW TEXT: " + result);
        WriteCommandAction.runWriteCommandAction(project, () -> {
                contents.setText(result);
                fileDocumentManager.saveDocument(contents);
            } );
        actionsPerformedCache.registerAction(new ActionPerformed(msg.getCommandMessageType(),msg.getFileName()));
    }

    private String applyPatchToContents(ContentChangeMessage msg, Document contents) {
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Patch> patch = (LinkedList<diff_match_patch.Patch>) dmp.patch_fromText(msg.getPatch());
        return dmp.patch_apply(patch,contents.getText())[0].toString();
    }
}
