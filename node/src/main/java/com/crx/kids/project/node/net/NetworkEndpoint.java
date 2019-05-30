package com.crx.kids.project.node.net;

import com.crx.kids.project.node.Configuration;
import com.crx.kids.project.node.cs.CriticalSection;
import com.crx.kids.project.node.cs.CriticalSectionService;
import com.crx.kids.project.node.logic.QueensService;
import com.crx.kids.project.node.messages.*;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.routing.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "net")
public class NetworkEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(NetworkEndpoint.class);

    @Autowired
    private NetworkService networkService;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private CriticalSectionService criticalSectionService;

    @Autowired
    private QueensService queensService;

    @GetMapping(path = "stats", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getAllNeighbours() {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.setNodes(Network.neighbours);
        statusResponse.setFirstSmallest(Network.firstSmallestNeighbour);
        statusResponse.setSecondSmallest(Network.secondSmallestNeighbour);
        return ResponseEntity.ok().body(statusResponse);
    }

    @PostMapping(path = "alter-neighbours", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> alterNeighbours(@RequestBody AlterRoutingTableMessage alterRoutingTableMessage) {
        if (alterRoutingTableMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(alterRoutingTableMessage, Network.ALTER_NEIGHBOURS);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        networkService.alterRoutingTable(alterRoutingTableMessage);
        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "newbie-join", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> newbieConnect(@RequestBody NewbieJoinMessage newbieJoinMessage) {

        if (newbieJoinMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(newbieJoinMessage, Network.NEWBIE_JOIN);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        networkService.newbieJoin(newbieJoinMessage);
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setType(CommonType.OK);
        return ResponseEntity.ok().body(commonResponse);
    }

    @PostMapping(path = "newbie-accepted", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> newbieAccepted(@RequestBody NewbieAcceptedMessage newbieAcceptedMessage) {
        // NOTE: This should be direct message, so no routing will occur.
        if (newbieAcceptedMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(newbieAcceptedMessage, Network.NEWBIE_ACCEPTED);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        networkService.newbieAccepted(newbieAcceptedMessage);

        routingService.initiateBroadcast(Network.BROADCAST_JOIN);

        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setType(CommonType.OK);
        return ResponseEntity.ok().body(commonResponse);
    }

    @PostMapping(path = "join-broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> joinBroadcast(@RequestBody BroadcastMessage<String> discoveryBroadcastMessage) {
        routingService.broadcastMessage(discoveryBroadcastMessage, Network.BROADCAST_JOIN);

        try {
            Network.maxNodeLock.writeLock().lock();
            if (discoveryBroadcastMessage.getSender() > Network.maxNodeInSystem) {
                logger.info("Discovered greater node in system. Replacing: {} with {}", Network.maxNodeInSystem, discoveryBroadcastMessage.getSender());
                Network.maxNodeInSystem = discoveryBroadcastMessage.getSender();
            }
        }
        finally {
            Network.maxNodeLock.writeLock().unlock();
        }

        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "leave-broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> leaveBroadcast(@RequestBody BroadcastMessage<String> discoveryBroadcastMessage) {
        routingService.broadcastMessage(discoveryBroadcastMessage, Network.BROADCAST_LEAVE);

        try {
            Network.maxNodeLock.writeLock().lock();
            if (discoveryBroadcastMessage.getSender() == Network.maxNodeInSystem) {
                logger.info("Max node in system left.");
                Network.maxNodeInSystem = Network.maxNodeInSystem - 1;
            }
        }
        finally {
            Network.maxNodeLock.writeLock().unlock();
        }

        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "critical-section-broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> criticalSectionBroadcast(@RequestBody BroadcastMessage<Integer> criticalSectionBroadcast) {

        routingService.broadcastMessage(criticalSectionBroadcast, Network.BROADCAST_CRITICAL_SECTION);
        criticalSectionService.handleSuzukiKasamiBroadcastMessage(criticalSectionBroadcast);

        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "critical-section-token", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> token(@RequestBody SuzukiKasamiTokenMessage suzukiKasamiTokenMessage) {

        if (suzukiKasamiTokenMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(suzukiKasamiTokenMessage, Network.CRITICAL_SECTION_TOKEN);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        logger.info("critical-section-token. Message {}", suzukiKasamiTokenMessage);
        criticalSectionService.handleSuzukiKasamiToken(suzukiKasamiTokenMessage.getCriticalSectionToken());

        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "ping", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> ping(@RequestBody PingMessage pingMessage) {
        if (pingMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(pingMessage, Network.PING);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        logger.info("Ping message received {}", pingMessage);
        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }

    @GetMapping(path = "critical", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> critical() {
        criticalSectionService.submitProcedureForCriticalExecution((token) -> {
            logger.warn("Working...");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.warn("EXECUTED UNDER CRITICAL SECTION! Q: {}, Nodes: {}", token.getQueue(), token.getSuzukiKasamiNodeMap());
        });

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }


    @PostMapping(path = "queens", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> queens(@RequestBody QueensJobsMessage queensJobsMessage) {
        if (queensJobsMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(queensJobsMessage, Network.QUEENS_JOBS);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        queensService.addJobsForDimension(queensJobsMessage.getDimension(), queensJobsMessage.getJobs());
        queensService.startWorkForDimension(queensJobsMessage.getDimension());

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }
}
