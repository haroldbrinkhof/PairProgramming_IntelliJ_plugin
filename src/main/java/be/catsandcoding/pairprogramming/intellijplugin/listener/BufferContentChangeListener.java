package be.catsandcoding.pairprogramming.intellijplugin.listener;

import be.catsandcoding.pairprogramming.intellijplugin.PairProgramming;
import be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationService;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.CompleteFileContentChangeMessage;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.ContentChangeMessage;
import be.catsandcoding.pairprogramming.intellijplugin.editing.DocumentContentCacheService;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.ContentPosition;
import be.catsandcoding.pairprogramming.intellijplugin.editing.BeforeChange;
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
    private final DocumentContentCacheService documentContentCacheService;
    private final Project project;

    public BufferContentChangeListener(Project project) {
        this.project = project;
        contentChangeService = ServiceManager.getService(project, ContentChangeService.class);
        documentContentCacheService = ServiceManager.getService(project, DocumentContentCacheService.class);

    }
    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        BeforeChange contentData = new BeforeChange(event.getDocument().getText(),
                getFileName(event.getDocument()), event.getDocument().getModificationStamp());

        documentContentCacheService.addToCache(contentData);
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        if(PairProgramming.getInstance(project).isInWriteAction()) { return; }

        Document document = event.getDocument();
        String fileName = getFileName(document);
        if(fileName.length() == 0) return;

        final long modificationStamp = document.getModificationStamp();
        final String documentText = document.getText();
        String hash = DigestUtils.md5Hex(documentText).toUpperCase();

        if(event.isWholeTextReplaced()){
            CompleteFileContentChangeMessage completeChange = new CompleteFileContentChangeMessage(
                    documentText, contentChangeService.getProjectIndependentPath(fileName),
                    "", event.getOldTimeStamp(), modificationStamp,hash);

            communicationService.sendMessage(completeChange);
        } else {
            ContentChangeData ccd = new ContentChangeData(event);
            if(isIntellijMagicalMarker(ccd.newFragment)) { return; }
            if(ccd.newFragment.isEmpty() && ccd.oldFragment.isEmpty()) { return; }

            ContentChangeMessage cc = new ContentChangeMessage(ccd.action, ccd.start, ccd.end,
                    ccd.oldFragment, ccd.newFragment, contentChangeService.getProjectIndependentPath(fileName),
                    ccd.patch, ccd.previousModificationStamp, modificationStamp, hash);

            communicationService.sendMessage(cc);
        }
    }

    private class ContentChangeData {
        public final String newFragment;
        public final String oldFragment;
        final long previousModificationStamp;
        final ContentPosition start = new ContentPosition();
        final ContentPosition end = new ContentPosition();
        final String patch;
        final ContentChangeMessage.Action action;

        public ContentChangeData(DocumentEvent event){
            this.newFragment = event.getNewFragment().toString();
            this.oldFragment = event.getOldFragment().toString();
            this.previousModificationStamp = event.getOldTimeStamp();
            this.patch = generatePatchIfPossible(event, previousModificationStamp);
            this.action = determineAction(oldFragment.isEmpty(), newFragment.isEmpty());
            fillInContentPositions(event, start, end);
        }
    }

    private void fillInContentPositions(@NotNull DocumentEvent event, @NotNull ContentPosition start, @NotNull ContentPosition end){
        final Document document = event.getDocument();
        final int offset = event.getOffset();
        final int newLength = event.getNewLength();
        final int firstLine = document.getLineNumber(offset);
        final int firstColumn = offset - document.getLineStartOffset(firstLine);
        final int lastLine = newLength == 0 ? firstLine : document.getLineNumber(offset + newLength - 1);
        final int lastColumn = (offset + newLength) - document.getLineStartOffset(lastLine);
        start.setLine(firstLine);
        start.setColumn(firstColumn);
        end.setLine(lastLine);
        end.setColumn(lastColumn);
    }

    private boolean isIntellijMagicalMarker(@NotNull String newFragment) {
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
        Optional<BeforeChange> contentBeforeChangeOpt = documentContentCacheService.getFromCache(fileName, previousModificationStamp);
        String patch = "";
        if(contentBeforeChangeOpt.isPresent()){
            patch = generatePatchFromBothTexts(event.getDocument().getText(), contentBeforeChangeOpt.get().getContent());
            documentContentCacheService.removeFromCache(contentBeforeChangeOpt.get());
        }
        return patch;
    }

    private String generatePatchFromBothTexts(@NotNull String newText, String oldText) {
        String patch;
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Diff> diff = dmp.diff_main( oldText,
                                            newText, false);
        dmp.diff_cleanupEfficiency(diff);
        LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(diff);
        patch = dmp.patch_toText(patches);
        return patch;
    }

    @NotNull
    private String getFileName(@NotNull Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        return (file != null && file.isValid())?file.getPath():"";
    }


    @Override
    public void bulkUpdateStarting(@NotNull Document document) {
        // not using this
    }

    @Override
    public void bulkUpdateFinished(@NotNull Document document) {
        // not using this
    }
}
