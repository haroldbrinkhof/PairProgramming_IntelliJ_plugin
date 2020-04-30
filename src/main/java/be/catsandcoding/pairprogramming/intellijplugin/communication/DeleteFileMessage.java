package be.catsandcoding.pairprogramming.intellijplugin.communication;

public class DeleteFileMessage extends CommandMessage{
    private String fileName;

    public DeleteFileMessage(){
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
