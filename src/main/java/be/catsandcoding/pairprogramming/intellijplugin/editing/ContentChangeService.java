package be.catsandcoding.pairprogramming.intellijplugin.editing;

import be.catsandcoding.pairprogramming.intellijplugin.communication.ContentChangeMessage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface ContentChangeService {
    static ContentChangeService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ContentChangeService.class);
    }

    String getProjectRoot();
    void performChange(ContentChangeMessage msg) throws IOException;
}
