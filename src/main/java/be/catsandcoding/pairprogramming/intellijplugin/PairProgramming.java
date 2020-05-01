package be.catsandcoding.pairprogramming.intellijplugin;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface PairProgramming {
    static PairProgramming getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, PairProgramming.class);
    }

    void installContentChangeListener();
    void removeContentChangeListener();
    void installFileChangeListener();
    void removeFileChangeListener();
    void markAsInWriteAction();
    void markAsOutOfWriteAction();
    boolean isInWriteAction();

}
