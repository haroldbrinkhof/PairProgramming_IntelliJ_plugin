package be.catsandcoding.pairprogramming.intellijplugin.communication;

public class CommandMessage {
    public enum Type { CONTENT_CHANGE, DELETE_FILE, NEW_FILE, MOVE_FILE, RENAME_FILE, COPY_FILE }

    protected Type commandMessageType;

    public Type getCommandMessageType(){
        return commandMessageType;
    }

    protected void setCommandMessageType(Type commandMessageType) {
        this.commandMessageType = commandMessageType;
    }
}
