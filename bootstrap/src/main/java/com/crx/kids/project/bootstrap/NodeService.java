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
    private static final Queue<Integer> queue = new ConcurrentLinkedQueue<>();

    private static final Set<NodeInfo> nodes = ConcurrentHashMap.newKeySet();

    public CheckInResponse checkIn(NodeInfo nodeInfo) {
        Integer id = queue.poll();

        if (id == null) {
            id = counter.incrementAndGet();
        }

        CheckInResponse checkInResponse = new CheckInResponse(id, nodes.stream().findFirst().orElse(null));
        nodes.add(nodeInfo);

        return checkInResponse;
    }

    public void checkOut(CheckOutRequest checkOutRequest) {
        queue.add(checkOutRequest.getId()); // check if already exist?
        nodes.remove(checkOutRequest.getNodeInfo());
    }
}
