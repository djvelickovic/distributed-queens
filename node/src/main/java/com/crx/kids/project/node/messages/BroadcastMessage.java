package com.crx.kids.project.node.messages;

import java.util.Objects;

public class BroadcastMessage<T> extends Message {
    protected T id;

    public BroadcastMessage() {
    }

    public BroadcastMessage(int sender, T key) {
        super(sender, -1);
        this.id = key;
    }

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BroadcastMessage that = (BroadcastMessage) o;
        return id.equals(that.id) && sender == that.sender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sender);
    }
}
