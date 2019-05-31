package com.crx.kids.project.node.services;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.CriticalSection;
import com.crx.kids.project.node.endpoints.Methods;
import com.crx.kids.project.node.entities.CriticalSectionToken;
import com.crx.kids.project.node.messages.BroadcastMessage;
import com.crx.kids.project.node.messages.SuzukiKasamiTokenMessage;
import com.crx.kids.project.node.common.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Service
public class CriticalSectionService {

    private static final Logger logger = LoggerFactory.getLogger(CriticalSectionService.class);

    // replace with map?!
    private static final Queue<Consumer<CriticalSectionToken>> criticalSectionProcedures = new ConcurrentLinkedQueue<>();

    @Autowired
    private RoutingService routingService;



    public int initiateSuzukiKasamiBroadcast(String path) {
        int suzukiKasamiId;
        try {
            CriticalSection.criticalSectionLock.writeLock().lock();
            suzukiKasamiId = CriticalSection.suzukiKasamiCounter.incrementAndGet();
            CriticalSection.suzukiKasamiCounterByNodes.put(Configuration.id, suzukiKasamiId);
        }
        finally {
            CriticalSection.criticalSectionLock.writeLock().unlock();
        }
        logger.info("Creating and broadcasting Suzuki-Kasami {} message to neighbours. Counter: {}", path, suzukiKasamiId);
        routingService.broadcastMessage(new BroadcastMessage<>(Configuration.id, suzukiKasamiId), path);
        return suzukiKasamiId;
    }

    @Async
    public void submitProcedureForCriticalExecution(Consumer<CriticalSectionToken> criticalSectionProcedure) {
        //TODO: check if node already have token an it is idle
        int suzukiKasamiBroadcastId = initiateSuzukiKasamiBroadcast(Methods.BROADCAST_CRITICAL_SECTION);
//        criticalSectionProceduresMap.put(suzukiKasamiBroadcastId, criticalSectionProcedure);
        criticalSectionProcedures.add(criticalSectionProcedure);
        logger.info("Submitted critical section procedure for id {}", suzukiKasamiBroadcastId);

        if (CriticalSection.tokenIdle.compareAndSet(true, false)) {
            handleSuzukiKasamiToken(CriticalSection.token);
        }
    }

    @Async
    public void handleSuzukiKasamiToken(CriticalSectionToken token) {
        if (token == null) {
            logger.error("Received token is null!");
            return;
        }


        // TODO: calculate maps, check if token is old?


        Consumer<CriticalSectionToken> criticalProcedure = criticalSectionProcedures.poll();
        if (criticalProcedure == null) {
            logger.error("There is no critical section procedure. Setting token idle");
            CriticalSection.token = token;
            CriticalSection.tokenIdle.set(true);
            return;
        }

        try {
            criticalProcedure.accept(token);
            logger.info("CriticalSection procedure obtained and executed");

//            token.getSuzukiKasamiNodeMap().put(Configuration.id, CriticalSection.suzukiKasamiCounterByNodes.get(Configuration.id));
            token.getSuzukiKasamiNodeMap().compute(Configuration.id, (nodeId, value) -> {
                if (value == null) {
                    return 1;
                }
                return value + 1;
            });


            updateToken(token);
        }
        catch (Exception e) {
            logger.error("Unexpected error while executing critical procedure ", e);
        }
    }


    @Async
    public void handleSuzukiKasamiBroadcastMessage(BroadcastMessage<Integer> criticalSectionBroadcast) {
        try {
            CriticalSection.criticalSectionLock.writeLock().lock();

            CriticalSection.suzukiKasamiCounterByNodes.compute(criticalSectionBroadcast.getSender(), (sender, counter) -> {
                if (counter == null) {
                    logger.info("Setting first REQUEST message for Suzuki-Kasami and node {}. Current: {}, Received: {}", criticalSectionBroadcast.getSender(), counter, criticalSectionBroadcast.getId());

                    return criticalSectionBroadcast.getId();
                }
                if (counter >= criticalSectionBroadcast.getId()) {
                    logger.info("Received old REQUEST message for Suzuki-Kasami. Requesting node: {}, Current: {}, Received: {}", criticalSectionBroadcast.getSender(), counter, criticalSectionBroadcast.getId());
                    return counter;
                }
                else {
                    logger.info("Received new REQUEST message for Suzuki-Kasami. Requesting node: {}, Current: {}, Received: {}", criticalSectionBroadcast.getSender(), counter, criticalSectionBroadcast.getId());
                    return criticalSectionBroadcast.getId();
                }
            });

            // if token is idle, send it
            if (CriticalSection.tokenIdle.compareAndSet(true, false)) {
                updateToken(CriticalSection.token);
            }
        }
        finally {
            CriticalSection.criticalSectionLock.writeLock().unlock();
        }
    }

    public void updateToken(CriticalSectionToken token) {


        IntStream.rangeClosed(1, Network.maxNodeInSystem).forEach(i -> {
            int rn = CriticalSection.suzukiKasamiCounterByNodes.getOrDefault(i, 0);
            int ln = token.getSuzukiKasamiNodeMap().getOrDefault(i, 0);

            logger.debug("TOKEN: {}.  rn = {}, ln = {}", i, rn, ln);

//            if (rn == ln + 1) { // rn > ln
            if (rn > ln) {

                if (token.getQueue().stream().noneMatch(node -> node == i)) {
                    logger.info("TOKEN: Adding to queue: {}", i);
                    token.getQueue().add(i);
                }
            }
        });


        logger.info("TOKEN: {}", token);

        if (token.getQueue().isEmpty()) {
            logger.warn("There are no waiter for critical section in Queue. Setting token idle");
            CriticalSection.token = token;
            CriticalSection.tokenIdle.set(true);

        }
        else {
            CriticalSection.tokenIdle.set(false);

            Integer nextNode = token.getQueue().poll();

            if (Configuration.id.equals(nextNode)) {
                handleSuzukiKasamiToken(token);
            }
            else {
                CriticalSection.token = null;
                SuzukiKasamiTokenMessage suzukiKasamiTokenMessage = new SuzukiKasamiTokenMessage(Configuration.id, nextNode, token);
                routingService.dispatchMessage(suzukiKasamiTokenMessage, Methods.CRITICAL_SECTION_TOKEN);
            }
        }
    }

}
