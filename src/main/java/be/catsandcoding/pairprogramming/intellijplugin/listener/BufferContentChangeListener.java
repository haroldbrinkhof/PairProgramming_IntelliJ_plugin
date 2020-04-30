package be.catsandcoding.pairprogramming.intellijplugin.listener;

import be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationService;
import be.catsandcoding.pairprogramming.intellijplugin.communication.ContentChangeMessage;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ChangedContentCacheService;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import be.catsandcoding.pairprogramming.intellijplugin.communication.ContentPosition;
import be.catsandcoding.pairprogramming.intellijplugin.editing.PriorToChangeContentData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import name.fraser.neil.plaintext.diff_match_patch;

public class BufferContentChangeListener implements DocumentListener {
    private final Logger log = Logger.getLogger("keystrokes");
    private Handler fh;
    private final CommunicationService communicationService = ServiceManager.getService(CommunicationService.class);
    private final ContentChangeService contentChangeService;
    private final ChangedContentCacheService changedContentCacheService;
    private final Project project;

    public BufferContentChangeListener(Project project) {
        this.project = project;
        contentChangeService = ServiceManager.getService(project, ContentChangeService.class);
        changedContentCacheService = ServiceManager.getService(project, ChangedContentCacheService.class);
        //if(preChangeDataCacheService == null) throw new IllegalStateException("service is null");
        try {
            fh = new FileHandler("C:/_development/keystrokes.log");

            log.addHandler(fh);
        } catch(Exception e){
            throw new IllegalStateException("couldn't create log",e);
        }

    }
    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        PriorToChangeContentData contentData = new PriorToChangeContentData(event.getDocument().getText(),
                getFileName(event.getDocument()), event.getDocument().getModificationStamp());

        changedContentCacheService.addToCache(contentData);
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {

        if(event.isWholeTextReplaced()){
            log.info("whole text has been replaced" + event.getNewFragment());
        } else {
            Document document = event.getDocument();
            long modificationStamp = document.getModificationStamp();
            long previousModificationStamp = event.getOldTimeStamp();
            String fileName = getFileName(document);


            int offset = event.getOffset();
            int newLength = event.getNewLength();

            int firstLine = document.getLineNumber(offset);
            int firstColumn = offset - document.getLineStartOffset(firstLine);
            int lastLine = newLength == 0 ? firstLine : document.getLineNumber(offset + newLength - 1);
            int lastColumn = (offset + newLength) - document.getLineStartOffset(lastLine);

            String newFragment = event.getNewFragment().toString();
            String oldFragment = event.getOldFragment().toString();

            Optional<PriorToChangeContentData> contentBeforeChangeOpt = changedContentCacheService.getFromCache(fileName, previousModificationStamp);
            String patch = "";
            if(contentBeforeChangeOpt.isPresent()){

                diff_match_patch dmp = new diff_match_patch();
                LinkedList<diff_match_patch.Diff> diff = dmp.diff_main( contentBeforeChangeOpt.get().getContent(),
                                                    event.getDocument().getText(), true);
                dmp.diff_cleanupEfficiency(diff);
                LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diff);
                patch = dmp.patch_toText(patches);
                // our next edit of this file will have a different predecessor
                changedContentCacheService.removeFromCache(contentBeforeChangeOpt.get());
            }

            if(fileName.length() > 0 && !newFragment.trim().equals("IntellijIdeaRulezzz") && (!newFragment.isEmpty() || !oldFragment.isEmpty())) {
                try {
                    ContentChangeMessage.Action action;
                    if(newFragment.isEmpty() && !oldFragment.isEmpty()){
                        action = ContentChangeMessage.Action.DELETE;
                    } else if (!newFragment.isEmpty() && oldFragment.isEmpty()){
                        action = ContentChangeMessage.Action.INSERT;
                    } else if (!newFragment.isEmpty() && !oldFragment.isEmpty()){
                        action = ContentChangeMessage.Action.REPLACE;
                    } else {
                        throw new UnsupportedOperationException("unknown Action");
                    }
                    ContentPosition start = new ContentPosition(firstLine, firstColumn);
                    ContentPosition end = new ContentPosition(lastLine, lastColumn);
                    ContentChangeMessage cc = new ContentChangeMessage(action,
                            start, end,
                            oldFragment, newFragment,
                            communicationService.getIdentity(),
                            contentChangeService.getProjectRoot(),
                            fileName.replace(contentChangeService.getProjectRoot(),""),
                            patch,
                            previousModificationStamp, modificationStamp);

                    //String response = communicationService.sendMessage(fileName + " at " + firstLine + ":" + column + " - " + lastLine +
                    //        " old:|" + showSpecialChars(oldFragment) + "| --> new:|" + showSpecialChars(newFragment) + "|" );
                    //response += communicationService.sendMessage("old timestamp: " + previousModificationStamp + " new timestamp: " + modificationStamp);
                    //if(!response.isEmpty()) communicationService.sendMessage("MESSAGE RECEIVED: " + response);
                    communicationService.sendMessage(new ObjectMapper().writeValueAsString(cc));
                } catch(java.io.IOException e){
                    // this is not a place where we wish to throw an exception on every change to the text
                }
                log.info(oldFragment + " --> " + newFragment);
            }
        }


    }

    @NotNull
    private String getFileName(Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        return (file != null && file.isValid())?file.getPath():"";
    }

    private String showSpecialChars(String input){
        if(input == null) return "";
        return input.replace("\n","%n")
                .replace("\r","%r")
                .replace(" ", ".")
                .replace("\t", "%t");


    }

    @Override
    public void bulkUpdateStarting(@NotNull Document document) {

    }

    @Override
    public void bulkUpdateFinished(@NotNull Document document) {

    }
}
