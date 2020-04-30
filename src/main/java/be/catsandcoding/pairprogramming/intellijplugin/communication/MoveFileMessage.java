package be.catsandcoding.pairprogramming.intellijplugin.communication;

public class MoveFileMessage extends CommandMessage{
    private String from;
    private String to;
    private boolean directory;

    public MoveFileMessage(){super();}
    public MoveFileMessage(String from, String to, boolean isDirectory){
        setCommandMessageType(Type.MOVE_FILE);
        this.from = from;
        this.to = to;
        directory = isDirectory;
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
