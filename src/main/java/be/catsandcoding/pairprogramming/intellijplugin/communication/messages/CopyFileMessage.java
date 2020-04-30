package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class CopyFileMessage extends CommandMessage {
    private String from;
    private String to;
    private boolean directory;

    public CopyFileMessage(){super();} // (de)serialisation necessity
    public CopyFileMessage(String from, String to, boolean isDirectory){
        super();
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
