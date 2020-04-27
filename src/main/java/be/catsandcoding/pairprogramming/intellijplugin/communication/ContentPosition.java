package be.catsandcoding.pairprogramming.intellijplugin.communication;

public final class ContentPosition {
    private int line;
    private int column;

    protected ContentPosition(){}
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
