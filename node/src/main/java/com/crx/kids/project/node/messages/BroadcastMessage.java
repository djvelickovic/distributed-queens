package com.crx.kids.project.node.messages;

import java.util.Objects;

public class BroadcastMessage extends Message {
    private int id;

    public BroadcastMessage() {
    }

    public BroadcastMessage(int sender, int id) {
        super(sender, -1);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BroadcastMessage that = (BroadcastMessage) o;
        return id == that.id && sender == that.sender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sender);
    }
}
