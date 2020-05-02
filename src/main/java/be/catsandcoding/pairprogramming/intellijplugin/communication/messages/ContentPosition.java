package be.catsandcoding.pairprogramming.intellijplugin.communication.messages;

public final class ContentPosition {
    private int line;
    private int column;

    public int getLine(){
        return line;
    }
    public int getColumn(){
        return column;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
