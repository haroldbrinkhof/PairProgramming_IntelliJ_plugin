package be.catsandcoding.pairprogramming.intellijplugin.communication;

public class CommandMessage {
    public enum Type { CONTENT_CHANGE }

    protected Type commandMessageType;

    public Type getCommandMessageType(){
        return commandMessageType;
    }

    protected void setCommandMessageType(Type commandMessageType) {
        this.commandMessageType = commandMessageType;
    }
}
