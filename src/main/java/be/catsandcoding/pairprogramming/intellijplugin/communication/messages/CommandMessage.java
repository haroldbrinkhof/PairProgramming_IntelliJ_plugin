package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class CommandMessage {
    public enum Type { CONTENT_CHANGE, DELETE_FILE, NEW_FILE, MOVE_FILE, RENAME_FILE, COPY_FILE }

    private Type commandMessageType;
    private String actorId;
    private String sessionId;

    public String getActorId() {
        return actorId;
    }

    protected void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getSessionId() {
        return sessionId;
    }

    protected void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Type getCommandMessageType(){
        return commandMessageType;
    }

    protected void setCommandMessageType(Type commandMessageType) {
        this.commandMessageType = commandMessageType;
    }
}
