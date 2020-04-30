package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class MoveFileMessage extends CommandMessage{
    private String from;
    private String to;
    private boolean directory;

    public MoveFileMessage(){super();} // (de)serialisation necessity
    public MoveFileMessage(String from, String to, boolean isDirectory){
        super();
        this.from = from;
        this.to = to;
        directory = isDirectory;
        setCommandMessageType(Type.MOVE_FILE);
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
