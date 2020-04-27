package be.catsandcoding.pairprogramming.intellijplugin.editing.impl;

import be.catsandcoding.pairprogramming.intellijplugin.editing.ChangedContentCacheService;
import be.catsandcoding.pairprogramming.intellijplugin.editing.PriorToChangeContentData;
import com.intellij.openapi.project.Project;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangedContentCacheServiceImpl implements ChangedContentCacheService {
    public ChangedContentCacheServiceImpl(Project project) {
    }

    private final CopyOnWriteArrayList<PriorToChangeContentData> cache = new CopyOnWriteArrayList<>();

    public boolean addToCache(PriorToChangeContentData contentData){
        return cache.addIfAbsent(contentData);
    }

    public Optional<PriorToChangeContentData> getFromCache(String filename, long modificationTimeStamp){
        int index = cache.indexOf(new PriorToChangeContentData(null,filename, modificationTimeStamp));
        if(index == -1) return Optional.empty();

        return Optional.ofNullable(cache.get(index));
    }

    public boolean removeFromCache(PriorToChangeContentData toRemove){
        return cache.remove(toRemove);
    }

    public void clearCache(){
        cache.clear();
    }
}
