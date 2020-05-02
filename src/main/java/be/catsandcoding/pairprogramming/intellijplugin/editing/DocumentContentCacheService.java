package be.catsandcoding.pairprogramming.intellijplugin.editing;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface DocumentContentCacheService {
    static DocumentContentCacheService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, DocumentContentCacheService.class);
    }

    void addToCache(BeforeChange contentData);

    Optional<BeforeChange> getFromCache(String filename, long modificationTimeStamp);

    void removeFromCache(BeforeChange toRemove);

    void clearCache();
}
