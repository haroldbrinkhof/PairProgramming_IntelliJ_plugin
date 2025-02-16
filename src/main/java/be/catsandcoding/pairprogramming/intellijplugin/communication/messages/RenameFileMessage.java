package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class RenameFileMessage extends CommandMessage {
    private String from;
    private String to;
    private boolean directory;

    public RenameFileMessage(){super();} // (de)serialisation necessity
    public RenameFileMessage(String from, String to, boolean isDirectory){
        super();
        this.from = from;
        this.to = to;
        directory = isDirectory;
        setCommandMessageType(Type.RENAME_FILE);
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
