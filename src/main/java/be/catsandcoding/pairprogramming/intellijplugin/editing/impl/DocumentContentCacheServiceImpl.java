package be.catsandcoding.pairprogramming.intellijplugin.editing.impl;

import be.catsandcoding.pairprogramming.intellijplugin.editing.DocumentContentCacheService;
import be.catsandcoding.pairprogramming.intellijplugin.editing.BeforeChange;
import com.intellij.openapi.project.Project;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class DocumentContentCacheServiceImpl implements DocumentContentCacheService {
    public DocumentContentCacheServiceImpl(Project project) {

    }

    private final CopyOnWriteArrayList<BeforeChange> cache = new CopyOnWriteArrayList<>();

    public void addToCache(BeforeChange contentData){
        cache.addIfAbsent(contentData);
    }

    public Optional<BeforeChange> getFromCache(String filename, long modificationTimeStamp){
        int index = cache.indexOf(new BeforeChange(null,filename, modificationTimeStamp));
        if(index == -1) return Optional.empty();

        return Optional.ofNullable(cache.get(index));
    }

    public void removeFromCache(BeforeChange toRemove){
        cache.remove(toRemove);
    }

    public void clearCache(){
        cache.clear();
    }

}
