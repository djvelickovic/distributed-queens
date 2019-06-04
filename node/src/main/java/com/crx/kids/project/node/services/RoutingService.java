package com.crx.kids.project.node.services;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.messages.BroadcastMessage;
import com.crx.kids.project.node.messages.FullNodeInfo;
import com.crx.kids.project.node.messages.Message;
import com.crx.kids.project.node.messages.Trace;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.utils.RoutingUtils;
import com.crx.kids.project.node.utils.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    private static final Set<BroadcastMessage> broadcastMessages = ConcurrentHashMap.newKeySet();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

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
    private Optional<FullNodeInfo> nextHop(int sender, int receiver, Supplier<Map<Integer, NodeInfo>> neighbours) {
        if (Network.neighbours.isEmpty()) {
            return Optional.empty();
        }

        NodeInfo nextHop = Network.neighbours.getOrDefault(receiver, null);

        // first find from neighbours
        if (nextHop != null) {
            logger.debug("Next hop determined directly by routing table: {}", nextHop);
            return Optional.of(new FullNodeInfo(receiver, nextHop));
        }

        // second try to utilize algorithm for finding next hop

        List<Integer> senderChain = RoutingUtils.chain(sender);
        List<Integer> receiverChain = RoutingUtils.chain(receiver);

        int maxCommonNumberPosition = maxCommonNumber(senderChain, receiverChain);
//        int maxCommonNumber = senderChain.get(maxCommonNumberPosition);

        // switch to receiver chain, find best max
        if (maxCommonNumberPosition + 1 == senderChain.size()) {
            for (int i = receiverChain.size() - 1; i > maxCommonNumberPosition ; i--) { // >= or >
                nextHop = neighbours.get().getOrDefault(receiverChain.get(i), null);
                if (nextHop != null) {
                    logger.debug("Next hop determined by algorithm: {}", nextHop);
                    return Optional.of(new FullNodeInfo(receiverChain.get(i), nextHop));
                }
            }
        }
        else { // switch on sender chain find best min
            for (int i = maxCommonNumberPosition; i < senderChain.size(); i++) { // exclude myself?
                nextHop = neighbours.get().getOrDefault(senderChain.get(i), null);
                if (nextHop != null) {
                    logger.debug("Next hop determined by algorithm: {}", nextHop);
                    return Optional.of(new FullNodeInfo(senderChain.get(i), nextHop));
                }
            }
        }

        // give any node, and see if it will have better luck,
        // TODO: reconsider to give empty response and to wait for node to be available
//        logger.error("Unable to find next hop by algorithm. Giving random neighbour.");
        return neighbours.get()
                .entrySet()
                .stream()
                .map(e -> new FullNodeInfo(e.getKey(), e.getValue()))
                .peek(nodeInfo -> logger.warn("Next hop determined by random calculation from routing table: {}", nodeInfo))
                .findAny();
    }

    private boolean containTraceFor(int nodeId, Message message) {
        return message.getTrace().stream().anyMatch(t -> t.getNodeId() == nodeId);
    }

    @Async
    public void initiateBroadcast(String path) {
        logger.info("Creating and broadcasting {} message to neighbours.", path);
        broadcastMessage(new BroadcastMessage<>(Configuration.id, UUID.randomUUID().toString()), path);
    }


    public boolean broadcastMessage(BroadcastMessage message, String path) {
        if (!broadcastMessages.add(message)) {
            return false;
        }

        executor.submit(() -> {

            try {
                logger.info("Broadcasting message {}. Action: {}", message, path);

                message.addTrace(new Trace(Configuration.id, "Broadcast."));

                List<Result> broadcastResults = Network.neighbours.entrySet().stream()
                        .filter(e -> !containTraceFor(e.getKey(), message))
                        .map(Map.Entry::getValue)
                        .map(nodeInfo -> nodeGateway.send(message, nodeInfo, path))
                        .collect(Collectors.toList());

                broadcastResults.stream()
                        .filter(Result::isError)
                        .map(Result::getError)
                        .forEach(e -> logger.error("Error broadcasting message. Error: {}", e));

                broadcastResults.stream()
                        .filter(r -> !r.isError())
                        .map(Result::getError)
                        .forEach(e -> logger.debug("Message successfully broadcast."));

            } catch (Exception e) {
                logger.error("Error while broadcasting message", e);
            }
        });

        return true;
    }

    @Async
    public void dispatchMessage(Message message, String path) {
        dispatchMessageNonAsync(message, path);
    }

    public void dispatchMessageNonAsync(Message message, String path) {
        try {
            message.addTrace(new Trace(Configuration.id, "Rerouted."));

            // loop until next hop is determined
            FullNodeInfo nextHop = ThreadUtil.loopWithResult(300, (l) -> {
                Optional<FullNodeInfo> optionalNextHop = nextHop(Configuration.id, message.getReceiver(), () -> Network.neighbours);
                if (optionalNextHop.isPresent()) {
                    l.stop();
                    return optionalNextHop.get();
                }
                logger.error("Unable to find next hope. Retry!");
                return null;
            });

            logger.info("Dispatching message {} to {}", message, nextHop);

            Result result = nodeGateway.send(message, nextHop, path);

            if (result.isError()) {
                logger.error("Error dispatching message. Error: {}", result.getError());
            }
            else {
                logger.debug("Message successfully dispatched.");
            }
        }
        catch (Exception e) {
            logger.error("Error while dispatching message", e);
        }
    }



    private int maxCommonNumber(List<Integer> list1, List<Integer> list2) {
        int minSize = RoutingUtils.min(list1.size(), list2.size());

        int maxCommonIndex = -1;

        logger.debug("Routing chains {}, {}", list1, list2);

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

        logger.debug("Max common index {}", maxCommonIndex);

        return maxCommonIndex;
    }

    public CommonResponse handle(Message message, String method, Supplier<CommonResponse> myselfHandler, Function<Integer, CommonResponse> ghostHandler) {
        int receiver = message.getReceiver();
        if (receiver == Configuration.id) {
            return myselfHandler.get();
        }

        Optional<Integer> ghostIdOptional = Network.ghostRoutingTables.keySet().stream().filter(ghostId -> ghostId == receiver).findAny();

        if (!ghostIdOptional.isPresent()) {
            // todo: route
            executor.submit(() -> dispatchMessage(message, method));
            return new CommonResponse(CommonType.OK);
        }

        logger.error("GHOST: alter message for {}", ghostIdOptional.get());

        return ghostHandler.apply(ghostIdOptional.get());
    }


}
