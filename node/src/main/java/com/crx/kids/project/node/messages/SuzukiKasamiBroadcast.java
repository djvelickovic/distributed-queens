package com.crx.kids.project.node.messages;

import java.util.Objects;
import java.util.UUID;

public class SuzukiKasamiBroadcast extends BroadcastMessage<String> {
    private Integer csId;

    public SuzukiKasamiBroadcast() {
    }

    public SuzukiKasamiBroadcast(int sender, Integer csId) {
        super(sender, UUID.randomUUID().toString());
        this.csId = csId;
    }

    public Integer getCsId() {
        return csId;
    }

    public void setCsId(Integer csId) {
        this.csId = csId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuzukiKasamiBroadcast that = (SuzukiKasamiBroadcast) o;
        return id.equals(that.id) && sender == that.sender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sender);
    }
}
