package be.catsandcoding.pairprogramming.intellijplugin.editing;

import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.CommandMessage;

import java.util.Objects;

public class ActionPerformed {
    private CommandMessage.Type type;
    private String path;

    public ActionPerformed(CommandMessage.Type type, String path){
        this.type = type;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionPerformed that = (ActionPerformed) o;
        return type == that.type &&
                path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path);
    }

    @Override
    public String toString() {
        return "ActionPerformed{" +
                "type=" + type +
                ", path='" + path + '\'' +
                '}';
    }
}
