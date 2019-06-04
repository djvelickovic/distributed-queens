package com.crx.kids.project.bootstrap;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.CheckOutRequest;
import com.crx.kids.project.common.NodeInfo;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NodeService {

    private static final AtomicInteger counter = new AtomicInteger();

    private static final Set<NodeInfo> nodes = ConcurrentHashMap.newKeySet();

    public CheckInResponse checkIn(NodeInfo nodeInfo) {

        if (nodes.contains(nodeInfo)) {
            throw new RuntimeException("Node has already subscribed to system: "+ nodeInfo);
        }

        int id = counter.incrementAndGet();

        CheckInResponse checkInResponse = new CheckInResponse(id, nodes.stream().findFirst().orElse(null));
        nodes.add(nodeInfo);

        return checkInResponse;
    }

    public void checkOut(CheckOutRequest checkOutRequest) {
        counter.decrementAndGet();
        nodes.remove(checkOutRequest.getNodeInfo());
    }
}
