package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class ContentChangeMessage extends CommandMessage {
    public enum Action {
        DELETE, REPLACE, INSERT, NEW
    }


    private ContentPosition startPosition;
    private ContentPosition endPosition;

    private String oldContent;
    private String newContent;

    private Action action;
    private String fileName;
    private String patch;
    private long oldTimeStamp;
    private long currentTimeStamp;
    private String hash;

    protected ContentChangeMessage(){super();} // (de)serialisation necessity

    public ContentChangeMessage(Action action, ContentPosition startPosition, ContentPosition endPosition,
                                String oldContent, String newContent,
                                String fileName, String patch, long oldTimeStamp, long currentTimeStamp,
                                String hash){
        super();
        this.action = action;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.fileName = fileName;
        this.patch = patch;
        this.oldTimeStamp = oldTimeStamp;
        this.currentTimeStamp = currentTimeStamp;
        this.hash = hash;

        setCommandMessageType(Type.CONTENT_CHANGE);
    }

    public long getOldTimeStamp() {
        return oldTimeStamp;
    }

    public long getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public Action getAction() {
        return action;
    }
    public ContentPosition getStartPosition(){
        return startPosition;
    }
    public ContentPosition getEndPosition(){
        return endPosition;
    }
    public String getOldContent(){
        return oldContent;
    }
    public String getNewContent(){
        return newContent;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPatch() {
        return patch;
    }

    public String getHash(){
        return hash;
    }
}
