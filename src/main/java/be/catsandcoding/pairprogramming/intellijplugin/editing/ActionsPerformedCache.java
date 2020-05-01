package be.catsandcoding.pairprogramming.intellijplugin.editing;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ActionsPerformedCache {
    static ActionsPerformedCache getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ActionsPerformedCache.class);
    }

    void registerAction(ActionPerformed action);
    boolean alreadyPerformedPrior(ActionPerformed action);
}
