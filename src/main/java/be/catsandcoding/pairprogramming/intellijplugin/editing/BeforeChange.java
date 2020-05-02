package be.catsandcoding.pairprogramming.intellijplugin.editing;

import java.util.Objects;

public class BeforeChange {
    private final String content;
    private final long modificationTimeStamp;
    private final String filename;

    public BeforeChange(String content, String filename, long modificationTimeStamp){
        this.content = content;
        this.filename = filename;
        this.modificationTimeStamp = modificationTimeStamp;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeforeChange that = (BeforeChange) o;
        return modificationTimeStamp == that.modificationTimeStamp &&
                filename.equals(that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modificationTimeStamp, filename);
    }
}
