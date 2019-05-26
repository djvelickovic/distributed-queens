package com.crx.kids.project.node.net;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.Configuration;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.routing.RoutingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);

    /**
     * c1 | c2
     * -------
     * 14   12
     *  7    6
     *  4    3
     *  2 -  2
     *  1    1
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
            for (int i = receiverChain.size() - 1; i >= maxCommonNumber; i--) {
                nextHop = neighbours.get().getOrDefault(i, null);
                if (nextHop != null) {
                    return Optional.of(new FullNodeInfo(i, nextHop));
                }
            }
        }
        else { // switch on sender chain find best min
            for (int i = maxCommonNumberPosition; i < senderChain.size(); i++) {
                nextHop = neighbours.get().getOrDefault(i, null);
                if (nextHop != null) {
                    return Optional.of(new FullNodeInfo(i, nextHop));
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


    public int maxCommonNumber(List<Integer> list1, List<Integer> list2) {
        int minSize = min(list1.size(), list2.size());

        int maxCommonIndex = -1;

        logger.info("chains {}, {}", list1, list2);

        for (int i = 0; i < minSize; i ++ ){
            int n1 = list1.get(list1.size() - 1 - i);
            int n2 = list2.get(list2.size() - 1 - i);

            if (n1 == n2) {
                maxCommonIndex = 0;
            }
            if (n1 != n2) {
                break;
            }
        }

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

        return chain;
    }

}
