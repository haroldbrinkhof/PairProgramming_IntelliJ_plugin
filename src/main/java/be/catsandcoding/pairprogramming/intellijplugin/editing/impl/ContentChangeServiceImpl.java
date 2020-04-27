package be.catsandcoding.pairprogramming.intellijplugin.editing.impl;

import be.catsandcoding.pairprogramming.intellijplugin.communication.CommandMessage;
import be.catsandcoding.pairprogramming.intellijplugin.communication.ContentChangeMessage;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.actionSystem.DocCommandGroupId;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import name.fraser.neil.plaintext.diff_match_patch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

    public void performChange(final ContentChangeMessage msg) throws IOException {
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
