package be.catsandcoding.pairprogramming.intellijplugin.editing;

import java.util.Objects;

public class PriorToChangeContentData {
    private final String content;
    private final long modificationTimeStamp;
    private final String filename;

    public PriorToChangeContentData(String content, String filename, long modificationTimeStamp){
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
        PriorToChangeContentData that = (PriorToChangeContentData) o;
        return modificationTimeStamp == that.modificationTimeStamp &&
                filename.equals(that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modificationTimeStamp, filename);
    }
}
