package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class DeleteFileMessage extends CommandMessage{
    private String fileName;

    public DeleteFileMessage(){ // (de)serialisation necessity
        super();
    }
    public DeleteFileMessage(String fileName){
        super();
        this.fileName = fileName;
        setCommandMessageType(Type.DELETE_FILE);
    }

    public String getFileName() {
        return fileName;
    }
}
