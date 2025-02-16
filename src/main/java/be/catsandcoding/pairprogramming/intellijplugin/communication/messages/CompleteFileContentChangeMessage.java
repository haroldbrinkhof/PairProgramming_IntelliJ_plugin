package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class CompleteFileContentChangeMessage extends CommandMessage {
    private  String content;
    private  String fileName;
    private  String patch;
    private  long oldTimeStamp;
    private  long currentTimeStamp;
    private  String hash;

    public CompleteFileContentChangeMessage(){super();}
    public CompleteFileContentChangeMessage(String content, String fileName, String patch,
                                            long oldTimeStamp, long currentTimeStamp, String hash){
        super();
        this.content = content;
        this.fileName = fileName;
        this.patch = patch;
        this.oldTimeStamp = oldTimeStamp;
        this.currentTimeStamp = currentTimeStamp;
        this.hash = hash;
        setCommandMessageType(Type.WHOLE_FILE_CONTENT_CHANGE);

    }

    public String getFileName() {
        return fileName;
    }

    public String getPatch() {
        return patch;
    }

    public String getContent() {
        return content;
    }

    public long getOldTimeStamp() {
        return oldTimeStamp;
    }

    public long getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "CompleteFileContentChangeMessage{" +
                "content='" + content + '\'' +
                ", fileName='" + fileName + '\'' +
                ", patch='" + patch + '\'' +
                ", oldTimeStamp=" + oldTimeStamp +
                ", currentTimeStamp=" + currentTimeStamp +
                ", hash='" + hash + '\'' +
                '}';
    }
}
