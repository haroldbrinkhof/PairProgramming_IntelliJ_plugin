package be.catsandcoding.pairprogramming.intellijplugin.editing;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ChangedContentCacheService {
    static ChangedContentCacheService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ChangedContentCacheService.class);
    }

    void addToCache(PriorToChangeContentData contentData);

    Optional<PriorToChangeContentData> getFromCache(String filename, long modificationTimeStamp);

    void removeFromCache(PriorToChangeContentData toRemove);

    void clearCache();
}
