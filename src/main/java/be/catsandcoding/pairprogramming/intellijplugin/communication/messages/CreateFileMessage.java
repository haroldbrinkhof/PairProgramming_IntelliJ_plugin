package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public class CreateFileMessage extends CommandMessage {
    private String fileName;
    private boolean directory;

    public CreateFileMessage(){ super();} // (de)serialisation necessity
    public CreateFileMessage(String fileName, boolean directory, String actorId, String sessionId){
        super();
        this.fileName = fileName;
        this.directory = directory;
        setCommandMessageType(Type.NEW_FILE);
        setActorId(actorId);
        setSessionId(sessionId);
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDirectory(){
        return directory;
    }
}
