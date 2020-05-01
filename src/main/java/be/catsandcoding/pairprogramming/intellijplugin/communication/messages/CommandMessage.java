package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class CommandMessage {
    public enum Type { CONTENT_CHANGE, WHOLE_FILE_CONTENT_CHANGE,
                        DELETE_FILE,
                        NEW_FILE,
                        MOVE_FILE,
                        RENAME_FILE,
                        COPY_FILE, COPY_OUTSIDE_FILE
    }

    private Type commandMessageType;
    private String actorId;
    private String sessionId;

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Type getCommandMessageType(){
        return commandMessageType;
    }

    protected void setCommandMessageType(Type commandMessageType) {
        this.commandMessageType = commandMessageType;
    }
}
