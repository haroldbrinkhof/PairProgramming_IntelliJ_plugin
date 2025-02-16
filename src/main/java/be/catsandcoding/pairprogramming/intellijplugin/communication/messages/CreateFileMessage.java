package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class CreateFileMessage extends CommandMessage {
    private String fileName;
    private boolean directory;

    public CreateFileMessage(){ super();} // (de)serialisation necessity
    public CreateFileMessage(String fileName, boolean directory){
        super();
        this.fileName = fileName;
        this.directory = directory;
        setCommandMessageType(Type.NEW_FILE);
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDirectory(){
        return directory;
    }
}
