package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class MoveFileMessage extends CommandMessage{
    private String from;
    private String to;
    private boolean directory;

    public MoveFileMessage(){super();} // (de)serialisation necessity
    public MoveFileMessage(String from, String to, boolean isDirectory, String actorId, String sessionId){
        super();
        this.from = from;
        this.to = to;
        directory = isDirectory;
        setCommandMessageType(Type.MOVE_FILE);
        setActorId(actorId);
        setSessionId(sessionId);

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
