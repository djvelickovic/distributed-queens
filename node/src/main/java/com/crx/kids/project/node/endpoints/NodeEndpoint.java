package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.messages.AlterRoutingTableMessage;
import com.crx.kids.project.node.messages.BroadcastMessage;
import com.crx.kids.project.node.messages.GhostMessage;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.services.NetworkService;
import com.crx.kids.project.node.services.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "node")
public class NodeEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(NodeEndpoint.class);

    @Autowired
    private NetworkService networkService;

    @Autowired
    private RoutingService routingService;


    @PostMapping(path = "alter-neighbours", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> alterNeighbours(@RequestBody AlterRoutingTableMessage alterRoutingTableMessage) {

        return ResponseEntity.ok(
                routingService.handle(alterRoutingTableMessage, Methods.ALTER_NEIGHBOURS, () -> {
                    networkService.alterRoutingTable(alterRoutingTableMessage);
                    return new CommonResponse(CommonType.OK);
                }, ghostId -> {
                    return new CommonResponse(CommonType.OK);
                }));
    }

    @PostMapping(path = "newbie-join", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> newbieConnect(@RequestBody NewbieJoinMessage newbieJoinMessage) {

        return ResponseEntity.ok(
                routingService.handle(newbieJoinMessage, Methods.NEWBIE_JOIN, () -> {
                    networkService.newbieJoin(newbieJoinMessage);
                    return new CommonResponse(CommonType.OK);
                }, ghostId -> {
                    return new CommonResponse(CommonType.OK);
                }));
    }

    @PostMapping(path = "newbie-accepted", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> newbieAccepted(@RequestBody NewbieAcceptedMessage newbieAcceptedMessage) {
        // NOTE: This should be direct message, so no routing will occur.

        return ResponseEntity.ok(
                routingService.handle(newbieAcceptedMessage, Methods.NEWBIE_ACCEPTED, () -> {
                    networkService.newbieAccepted(newbieAcceptedMessage);
                    routingService.initiateBroadcast(Methods.BROADCAST_JOIN);
                    return new CommonResponse(CommonType.OK);
                }, ghostId -> {
                    return new CommonResponse(CommonType.OK);
                }));
    }

    @PostMapping(path = "join-broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> joinBroadcast(@RequestBody BroadcastMessage<String> discoveryBroadcastMessage) {
        if (routingService.broadcastMessage(discoveryBroadcastMessage, Methods.BROADCAST_JOIN)) {
            try {
                Network.maxNodeLock.writeLock().lock();
                if (discoveryBroadcastMessage.getSender() > Network.maxNodeInSystem) {
                    logger.info("Discovered greater node in system. Replacing: {} with {}", Network.maxNodeInSystem, discoveryBroadcastMessage.getSender());
                    Network.maxNodeInSystem = discoveryBroadcastMessage.getSender();
                }
            } finally {
                Network.maxNodeLock.writeLock().unlock();
            }
        }
        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "leave-broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> leaveBroadcast(@RequestBody BroadcastMessage<String> discoveryBroadcastMessage) {
        if (routingService.broadcastMessage(discoveryBroadcastMessage, Methods.BROADCAST_LEAVE)) {
            try {
                Network.maxNodeLock.writeLock().lock();
                if (discoveryBroadcastMessage.getSender() == Network.maxNodeInSystem) {
                    logger.info("Max node in system left.");
                    Network.maxNodeInSystem = Network.maxNodeInSystem - 1;
                }
            } finally {
                Network.maxNodeLock.writeLock().unlock();
            }
        }

        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "host-request", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> hostRequest(@RequestBody GhostMessage ghostMessage) {

        return ResponseEntity.ok(
                routingService.handle(ghostMessage, Methods.HOST_REQUEST, () -> {
                    networkService.handleHostRequest(ghostMessage);
                    return new CommonResponse(CommonType.OK);
                }, ghostId -> {
                    return new CommonResponse(CommonType.OK);
                }));
    }
}
