package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public final class ContentPosition {
    private int line;
    private int column;

    protected ContentPosition(){} // (de)serialisation necessity
    public ContentPosition(int line, int column){
        this.line = line;
        this.column = column;
    }

    public int getLine(){
        return line;
    }
    public int getColumn(){
        return column;
    }
}
