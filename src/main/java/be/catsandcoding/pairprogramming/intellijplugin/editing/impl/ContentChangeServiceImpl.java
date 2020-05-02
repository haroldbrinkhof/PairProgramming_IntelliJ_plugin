package be.catsandcoding.pairprogramming.intellijplugin.editing.impl;

import be.catsandcoding.pairprogramming.intellijplugin.PairProgramming;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.*;
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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Optional;

public class ContentChangeServiceImpl implements ContentChangeService {
    private final Project project;
    private final VirtualFileManager virtualFileManager;
    private final FileDocumentManager fileDocumentManager;
    private final PairProgramming pairProgramming;

    public ContentChangeServiceImpl(Project project) {
        this.project = project;
        this.pairProgramming = PairProgramming.getInstance(project);
        this.virtualFileManager = VirtualFileManager.getInstance();
        this.fileDocumentManager = FileDocumentManager.getInstance();
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

    @Override
    public String transformToProjectPath(String path) {
        return getProjectRoot() + path;
    }

    @Override
    public boolean isPartOfThisProject(VirtualFile fromLocal) {
        return ProjectFileIndex.SERVICE.getInstance(project).isInContent(fromLocal);
    }

    @Override
    public boolean isPartOfThisProject(String path) {
        VirtualFile virtualFile = getVirtualFileFromPath(path);
        return isPartOfThisProject(virtualFile);
    }

    private VirtualFile getVirtualFileFromPath(String path) {
        return virtualFileManager.refreshAndFindFileByUrl("file://" + path);
    }


    @Override
    public void handle(final MoveFileMessage msg){
        final String from = transformToProjectPath(msg.getFrom());
        final String to = transformToProjectPath(msg.getTo());
        final VirtualFile fromLocal = getVirtualFileFromPath(from);
        final VirtualFile toLocal = getVirtualFileFromPath(pathWithoutFilename(to));

        if(fromLocal != null && toLocal != null &&
                isPartOfThisProject(fromLocal) &&
                isPartOfThisProject(toLocal)) {
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

    @NotNull
    private String pathWithoutFilename(String to) {
        return to.substring(0, to.lastIndexOf("/") + 1);
    }

    @Override
    public void handle(final CopyOutsideFileMessage msg){
        String to = transformToProjectPath(msg.getTo());

        tryToCreate(to, msg.isDirectory());
        Optional<Document> documentOpt = getDocumentForContentChange(to, null);
        documentOpt.ifPresent(contents -> writeDocumentContent(contents, msg.getContent()));
    }

    @Override
    public void handle(final CopyFileMessage msg){
        final String from = transformToProjectPath(msg.getFrom());
        final String to = transformToProjectPath(msg.getTo());

        final VirtualFile fromLocal = getVirtualFileFromPath(from);
        final VirtualFile toLocal = getVirtualFileFromPath(pathWithoutFilename(to));
        if(fromLocal != null && toLocal != null &&
                isPartOfThisProject(fromLocal) &&
                isPartOfThisProject(toLocal)){
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

        final VirtualFile fromLocal = getVirtualFileFromPath(from);
        if(fromLocal != null && isPartOfThisProject(fromLocal)){
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
        final VirtualFile file = getVirtualFileFromPath(filename);
        if(file == null || !isPartOfThisProject(file)) return ;
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
        final VirtualFile file = getVirtualFileFromPath(filename);
        if(file != null) return;

        final String fileToCreate = filename;
        WriteCommandAction.runWriteCommandAction(project, ()  -> tryToCreate(fileToCreate, msg.isDirectory()) );

        getVirtualFileFromPath(filename);

    }

    private void tryToCreate(String fileToCreate, boolean isDirectory) {
        try {

            Path path = Paths.get(safeguardForWindowsIfNecessary(fileToCreate));
            if(isDirectory){
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

    @Override
    public void handle(final CompleteFileContentChangeMessage msg){
        final Optional<Document> contentsOpt = getDocumentForContentChange(msg.getFileName(), msg.getHash());
        contentsOpt.ifPresent( contents -> writeDocumentContent(contents, msg.getContent()));
    }

    @Override
    public void handle(final ContentChangeMessage msg){
        final Optional<Document> contentsOpt = getDocumentForContentChange(msg.getFileName(), msg.getHash());
        contentsOpt.ifPresent(contents -> {
            String result = applyPatchToContents(msg, contents);
            writeDocumentContent(contents, result);
        });
    }

    private void writeDocumentContent(Document contents, String result) {
        pairProgramming.markAsInWriteAction();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            contents.setText(result);
            fileDocumentManager.saveDocument(contents);
        });
        pairProgramming.markAsOutOfWriteAction();
    }

    @NotNull
    private Optional<Document> getDocumentForContentChange(String filenameToChange, String hash) {
        if(pairProgramming.isInWriteAction()) {
            return Optional.empty();
        }
        String filename = transformToProjectPath(filenameToChange);
        final VirtualFile toChange = getVirtualFileFromPath(filename);
        if(toChange == null) {
            return Optional.empty();
        }

        final Document contents = ReadAction.compute(() -> fileDocumentManager.getDocument(toChange));
        if(contents == null) {
            return Optional.empty();
        }
        if(isChangeUnnecessary(contents, hash)) {
            return Optional.empty();
        }
        return Optional.of(contents);
    }

    private boolean isChangeUnnecessary(Document document, String hash){
        return DigestUtils.md5Hex(document.getText()).toUpperCase().equals(hash);
    }

    @NotNull
    private String applyPatchToContents(ContentChangeMessage msg, Document contents) {
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Patch> patch = (LinkedList<diff_match_patch.Patch>) dmp.patch_fromText(msg.getPatch());
        return dmp.patch_apply(patch,contents.getText())[0].toString();
    }
}
