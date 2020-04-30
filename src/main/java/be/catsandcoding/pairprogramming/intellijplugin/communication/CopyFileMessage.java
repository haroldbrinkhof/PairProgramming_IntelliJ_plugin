package be.catsandcoding.pairprogramming.intellijplugin.communication;

public class CopyFileMessage extends CommandMessage {
    private String from;
    private String to;
    private boolean directory;

    public CopyFileMessage(){super();}
    public CopyFileMessage(String from, String to, boolean isDirectory){
        this.from = from;
        this.to = to;
        directory = isDirectory;
        setCommandMessageType(Type.COPY_FILE);
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean isDirectory() {
        return directory;
    }
}
