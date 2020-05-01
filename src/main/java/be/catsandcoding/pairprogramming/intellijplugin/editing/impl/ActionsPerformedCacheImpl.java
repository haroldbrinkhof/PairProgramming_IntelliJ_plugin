package be.catsandcoding.pairprogramming.intellijplugin.editing.impl;

import be.catsandcoding.pairprogramming.intellijplugin.editing.ActionPerformed;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ActionsPerformedCache;
import com.intellij.openapi.project.Project;
import net.sf.cglib.asm.$ClassWriter;

import java.util.concurrent.ConcurrentHashMap;

public class ActionsPerformedCacheImpl implements ActionsPerformedCache {
    public static final long THRESHOLD = 50L;
    private final ConcurrentHashMap<ActionPerformed, Long> actions = new ConcurrentHashMap<>();

    public ActionsPerformedCacheImpl(Project project) {

    }

    public void registerAction(ActionPerformed action){
        System.out.println("registering action: " + action);
        actions.put(action, System.currentTimeMillis());
    }

    public boolean alreadyPerformedPrior(ActionPerformed action){
        long between = 0L;
        if(actions.contains(action)){
            between = System.currentTimeMillis() - actions.get(action);
            System.out.println("action already performed " + action);
        }
        System.out.println("milliseconds between prior action: " + between + " " + action);
        return actions.contains(action) && (System.currentTimeMillis() - actions.get(action) <= THRESHOLD );
    }
}
