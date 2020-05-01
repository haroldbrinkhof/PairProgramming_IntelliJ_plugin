package be.catsandcoding.pairprogramming.intellijplugin.listener;

import be.catsandcoding.pairprogramming.intellijplugin.PairProgramming;
import be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationService;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.CompleteFileContentChangeMessage;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.ContentChangeMessage;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ChangedContentCacheService;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.ContentPosition;
import be.catsandcoding.pairprogramming.intellijplugin.editing.PriorToChangeContentData;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Optional;
import name.fraser.neil.plaintext.diff_match_patch;

public class BufferContentChangeListener implements DocumentListener {
    private final CommunicationService communicationService = ServiceManager.getService(CommunicationService.class);
    private final ContentChangeService contentChangeService;
    private final ChangedContentCacheService changedContentCacheService;
    private final Project project;

    public BufferContentChangeListener(Project project) {
        this.project = project;
        contentChangeService = ServiceManager.getService(project, ContentChangeService.class);
        changedContentCacheService = ServiceManager.getService(project, ChangedContentCacheService.class);

    }
    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        PriorToChangeContentData contentData = new PriorToChangeContentData(event.getDocument().getText(),
                getFileName(event.getDocument()), event.getDocument().getModificationStamp());

        changedContentCacheService.addToCache(contentData);
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        if(PairProgramming.getInstance(project).isInWriteAction()) return;
        if(event.isWholeTextReplaced()){
            Document document = event.getDocument();
            String hash = DigestUtils.md5Hex(document.getText()).toUpperCase();

            String fileName = getFileName(document);
            CompleteFileContentChangeMessage completeChange = new CompleteFileContentChangeMessage(
                    document.getText(),
                    contentChangeService.getProjectRoot(),
                    contentChangeService.getProjectIndependentPath(fileName),
                    "", event.getOldTimeStamp(),
                    document.getModificationStamp(),hash);

            communicationService.sendMessage(completeChange);

            System.out.println("whole text has been replaced" + event.getNewFragment());
        }

        else {
            Document document = event.getDocument();
            final long modificationStamp = document.getModificationStamp();
            final long previousModificationStamp = event.getOldTimeStamp();
            final String fileName = getFileName(document);


            final int offset = event.getOffset();
            final int newLength = event.getNewLength();

            final int firstLine = document.getLineNumber(offset);
            final int firstColumn = offset - document.getLineStartOffset(firstLine);
            final int lastLine = newLength == 0 ? firstLine : document.getLineNumber(offset + newLength - 1);
            final int lastColumn = (offset + newLength) - document.getLineStartOffset(lastLine);

            String newFragment = event.getNewFragment().toString();
            String oldFragment = event.getOldFragment().toString();

            if(isIntellijMagicalMarker(newFragment)) return;
            if(fileName.length() == 0) return;
            if(newFragment.isEmpty() && oldFragment.isEmpty()) return;

            String patch = generatePatchIfPossible(event, previousModificationStamp);

            ContentChangeMessage.Action action = determineAction(oldFragment.isEmpty(), newFragment.isEmpty());
            ContentPosition start = new ContentPosition(firstLine, firstColumn);
            ContentPosition end = new ContentPosition(lastLine, lastColumn);
            String hash = DigestUtils.md5Hex(document.getText()).toUpperCase();

            ContentChangeMessage cc = new ContentChangeMessage(action,
                    start, end,
                    oldFragment, newFragment,
                    contentChangeService.getProjectRoot(),
                    contentChangeService.getProjectIndependentPath(fileName),
                    patch,
                    previousModificationStamp, modificationStamp,
                    hash);
            communicationService.sendMessage(cc);
        }


    }

    private boolean isIntellijMagicalMarker(String newFragment) {
        return newFragment.trim().equals("IntellijIdeaRulezzz");
    }

    private ContentChangeMessage.Action determineAction(boolean oldEmpty, boolean newEmpty){
        if(newEmpty && !oldEmpty){
            return ContentChangeMessage.Action.DELETE;
        } else if (!newEmpty && oldEmpty){
            return ContentChangeMessage.Action.INSERT;
        } else if (!newEmpty && !oldEmpty) {
            return ContentChangeMessage.Action.REPLACE;
        }
        throw new UnsupportedOperationException("unknown Action");

    }
    private String generatePatchIfPossible(@NotNull DocumentEvent event, long previousModificationStamp) {
        String fileName = getFileName(event.getDocument());
        Optional<PriorToChangeContentData> contentBeforeChangeOpt = changedContentCacheService.getFromCache(fileName, previousModificationStamp);
        String patch = "";
        if(contentBeforeChangeOpt.isPresent()){

            diff_match_patch dmp = new diff_match_patch();
            LinkedList<diff_match_patch.Diff> diff = dmp.diff_main( contentBeforeChangeOpt.get().getContent(),
                                                event.getDocument().getText(), false);
            dmp.diff_cleanupEfficiency(diff);
            LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diff);
            patch = dmp.patch_toText(patches);
            // our next edit of this file will have a different predecessor
            changedContentCacheService.removeFromCache(contentBeforeChangeOpt.get());
        }
        return patch;
    }

    @NotNull
    private String getFileName(Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        return (file != null && file.isValid())?file.getPath():"";
    }


    @Override
    public void bulkUpdateStarting(@NotNull Document document) {

    }

    @Override
    public void bulkUpdateFinished(@NotNull Document document) {

    }
}
