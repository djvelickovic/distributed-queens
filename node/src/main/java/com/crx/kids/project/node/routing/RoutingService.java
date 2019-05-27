package com.crx.kids.project.node.routing;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.Configuration;
import com.crx.kids.project.node.ThreadUtil;
import com.crx.kids.project.node.comm.NodeGateway;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.Message;
import com.crx.kids.project.node.messages.Trace;
import com.crx.kids.project.node.net.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;

@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);


    @Autowired
    private NodeGateway nodeGateway;

    /**
     *   c1-> c2
     *   -------
     *0.  1    1
     *1.  2 -  2
     *2.  4    3
     *3.  7    6
     *4. 14   12
     *
     * @param receiver
     * @return
     */
    public Optional<FullNodeInfo> nextHop(int sender, int receiver, Supplier<Map<Integer, NodeInfo>> neighbours) {
        if (Network.neighbours.isEmpty()) {
            return Optional.empty();
        }

        NodeInfo nextHop = Network.neighbours.getOrDefault(receiver, null);

        // first find from neighbours
        if (nextHop != null) {
            logger.info("{} is a neighbour", receiver);
            return Optional.of(new FullNodeInfo(receiver, nextHop));
        }

        // second try to utilize algorithm for finding next hop

        List<Integer> senderChain = chain(sender);
        List<Integer> receiverChain = chain(receiver);

        int maxCommonNumberPosition = maxCommonNumber(senderChain, receiverChain);
        int maxCommonNumber = senderChain.get(maxCommonNumberPosition);

        // switch to receiver chain, find best max
        if (maxCommonNumberPosition + 1 == senderChain.size()) {
            for (int i = receiverChain.size() - 1; i > maxCommonNumberPosition ; i--) { // >= or >
                nextHop = neighbours.get().getOrDefault(receiverChain.get(i), null);
                if (nextHop != null) {
                    return Optional.of(new FullNodeInfo(receiverChain.get(i), nextHop));
                }
            }
        }
        else { // switch on sender chain find best min
            for (int i = maxCommonNumberPosition; i < senderChain.size() - 2; i--) { // exclude myself?
                nextHop = neighbours.get().getOrDefault(senderChain.get(i), null);
                if (nextHop != null) {
                    return Optional.of(new FullNodeInfo(senderChain.get(i), nextHop));
                }
            }
        }

        // give any node, and see if it will have better luck,
        // TODO: reconsider to give empty response and to wait for node to be available
        logger.error("Unable to find next hop by algorithm. Giving random neighbour.");
        return neighbours.get()
                .entrySet()
                .stream()
                .map(e -> new FullNodeInfo(e.getKey(), e.getValue()))
                .findAny();
    }

    @Async
    public void dispatchMessage(Message message, String path) {
        try {
            message.addTrace(new Trace(Configuration.id, "Rerouted."));

            // loop until next hop is determined
            FullNodeInfo nextHop = ThreadUtil.loopWithResult(300, (l) -> {
                Optional<FullNodeInfo> optionalNextHop = nextHop(Configuration.id, message.getReceiver(), () -> Network.neighbours);
                if (optionalNextHop.isPresent()) {
                    l.stop();
                    return optionalNextHop.get();
                }
                logger.warn("Unable to find next hope. Retry!");
                return null;
            });

            logger.info("Dispatching message {} to {}", message, nextHop);

            Result result = nodeGateway.send(message, nextHop, path);

            if (result.isError()) {
                logger.error("Error dispatching message. Error: {}", result.getError());
            }
            else {
                logger.info("Message successfully dispatched.");
            }
        }
        catch (Exception e) {
            logger.error("Error while dispatching message", e);
        }
    }


    private int maxCommonNumber(List<Integer> list1, List<Integer> list2) {
        int minSize = min(list1.size(), list2.size());

        int maxCommonIndex = -1;

        logger.info("chains {}, {}", list1, list2);

        for (int i = 0; i < minSize; i ++ ){
            int n1 = list1.get(i);
            int n2 = list2.get(i);

            if (n1 == n2) {
                maxCommonIndex = i;
            }
            if (n1 != n2) {
                break;
            }
        }

        logger.info("Max common index {}", maxCommonIndex);


        return maxCommonIndex;
    }

    public int min(int n1, int n2) {
        return n1 < n2 ? n1 : n2;
    }

    public int max(int n1, int n2) {
        return n1 > n2 ? n1 : n2;
    }

    public List<Integer> chain(int nodeId) {
        List<Integer> chain = new ArrayList<>();

        while (nodeId != 1) {
            chain.add(nodeId);
            nodeId = RoutingUtils.darah(nodeId);
        }
        chain.add(1);
        Collections.reverse(chain);
        return chain;
    }

}
