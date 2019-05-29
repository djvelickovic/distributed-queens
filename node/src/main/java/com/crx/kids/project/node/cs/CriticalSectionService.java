package com.crx.kids.project.node.cs;

import com.crx.kids.project.node.Configuration;
import com.crx.kids.project.node.messages.BroadcastMessage;
import com.crx.kids.project.node.messages.SuzukiKasamiTokenMessage;
import com.crx.kids.project.node.net.Network;
import com.crx.kids.project.node.routing.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class CriticalSectionService {

    private static final Logger logger = LoggerFactory.getLogger(CriticalSectionService.class);

    // replace with map?!
    private static final Map<Integer, Consumer<SuzukiKasamiTokenMessage>> criticalSectionProceduresMap = new ConcurrentHashMap<>();

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

    public void submitProcedureForCriticalExecution(Consumer<SuzukiKasamiTokenMessage> criticalSectionProcedure) {
        //TODO: check if node already have token an it is idle
        int suzukiKasamiBroadcastId = initiateSuzukiKasamiBroadcast(Network.BROADCAST_CRITICAL_SECTION);
        criticalSectionProceduresMap.put(suzukiKasamiBroadcastId, criticalSectionProcedure);
        logger.info("Submitted critical section procedure for id {}", suzukiKasamiBroadcastId);
    }

    @Async
    public void handleSuzukiKasamiTokenMessage(SuzukiKasamiTokenMessage suzukiKasamiTokenMessage) {
        // TODO: calculate maps, check if token is old?
        Consumer<SuzukiKasamiTokenMessage> criticalProcedure = criticalSectionProceduresMap.get(-1);
        if (criticalProcedure == null) {
            logger.error("There is no critical section procedure for {} id", -1);
            return;
        }
        try {
            criticalProcedure.accept(suzukiKasamiTokenMessage);
            logger.info("CriticalSection procedure obtained and executed for {}", -1);
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
        }
        finally {
            CriticalSection.criticalSectionLock.writeLock().unlock();
        }
    }


}
