package be.catsandcoding.pairprogramming.intellijplugin.editing;

import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ContentChangeService {
    static ContentChangeService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ContentChangeService.class);
    }

    String getProjectRoot();
    String getProjectIndependentPath(String path);
    void handle(ContentChangeMessage msg);
    void handle(DeleteFileMessage msg);
    void handle(CreateFileMessage msg);
    void handle(RenameFileMessage msg);
    void handle(CopyFileMessage msg);
    void handle(MoveFileMessage msg);
    void handle(CompleteFileContentChangeMessage msg);
}
