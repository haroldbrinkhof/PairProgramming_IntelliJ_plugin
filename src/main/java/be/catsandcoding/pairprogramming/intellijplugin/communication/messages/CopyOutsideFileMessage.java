package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class CopyOutsideFileMessage extends CopyFileMessage {
    private String content;

    public CopyOutsideFileMessage(){super();}
    public CopyOutsideFileMessage(String to, String content){
        super("",to,false);
        this.content = content;
        setCommandMessageType(Type.COPY_OUTSIDE_FILE);

    }

    public String getContent() {
        return content;
    }
}
