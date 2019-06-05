package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.common.CriticalSection;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.messages.*;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.services.NetworkService;
import com.crx.kids.project.node.services.RoutingService;
import com.crx.kids.project.node.services.StoppingService;
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

    @Autowired
    private StoppingService stoppingService;


    @PostMapping(path = "alter-neighbours", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> alterNeighbours(@RequestBody AlterRoutingTableMessage alterRoutingTableMessage) {

        return ResponseEntity.ok(
                routingService.handle(alterRoutingTableMessage, Methods.ALTER_NEIGHBOURS, routingService, () -> {
                    if (alterRoutingTableMessage.isDelete()) {
                        Network.neighbours.remove(alterRoutingTableMessage.getSender());
                    }
                    else {
                        Network.neighbours.put(alterRoutingTableMessage.getSender(), alterRoutingTableMessage.getNodeInfo());
                    }
                    return new CommonResponse(CommonType.OK);
                }));
    }

    @PostMapping(path = "newbie-join", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> newbieConnect(@RequestBody NewbieJoinMessage newbieJoinMessage) {

        return ResponseEntity.ok(
                routingService.handle(newbieJoinMessage, Methods.NEWBIE_JOIN, routingService, () -> {
                    networkService.newbieJoin(newbieJoinMessage);
                    return new CommonResponse(CommonType.OK);
                }));
    }

    @PostMapping(path = "newbie-accepted", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> newbieAccepted(@RequestBody NewbieAcceptedMessage newbieAcceptedMessage) {
        // NOTE: This should be direct message, so no routing will occur.

        return ResponseEntity.ok(
                routingService.handle(newbieAcceptedMessage, Methods.NEWBIE_ACCEPTED, routingService, () -> {
                    networkService.newbieAccepted(newbieAcceptedMessage);
                    routingService.initiateBroadcast(Methods.BROADCAST_JOIN);
                    return new CommonResponse(CommonType.OK);
                }));
    }

    @PostMapping(path = "join-broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> joinBroadcast(@RequestBody BroadcastMessage<String> discoveryBroadcastMessage) {
        if (routingService.broadcastMessage(discoveryBroadcastMessage, Methods.BROADCAST_JOIN)) {


            // or increment
            Network.maxNodeInSystem.incrementAndGet();
//                Network.maxNodeInSystem.updateAndGet(id -> {
//                    if (discoveryBroadcastMessage.getSender() > Network.maxNodeInSystem.get()) {
//                        logger.info("Discovered greater node in system. Replacing: {} with {}", Network.maxNodeInSystem, discoveryBroadcastMessage.getSender());
//                        return discoveryBroadcastMessage.getSender();
//                    }
//                    return id;
//                });

        }
        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "leave-broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> leaveBroadcast(@RequestBody BroadcastMessage<String> discoveryBroadcastMessage) {
        if (routingService.broadcastMessage(discoveryBroadcastMessage, Methods.BROADCAST_LEAVE)) {
            logger.warn("NODE LEFT BROADCAST: {}", Network.maxNodeInSystem.get());
            int node = Network.maxNodeInSystem.getAndDecrement();

            CriticalSection.suzukiKasamiCounterByNodes.remove(node);

            // alter table?!
//
//                if (discoveryBroadcastMessage.getSender() == Network.maxNodeInSystem) {
//                    logger.info("Max node in system left.");
//                    Network.neighbours.remove(Network.maxNodeInSystem);
//                    Network.maxNodeInSystem = Network.maxNodeInSystem - 1;
//                }
        }
        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "host-request", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> hostRequest(@RequestBody HostMessage hostMessage) {

        return ResponseEntity.ok(
                routingService.handle(hostMessage, Methods.HOST_REQUEST, routingService, () -> {
                    networkService.handleHostRequest(hostMessage);
                    return new CommonResponse(CommonType.OK);
                }));
    }

    @PostMapping(path = "max-leave", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> maxLeave(@RequestBody MaxLeaveMessage maxLeaveMessage) {

        return ResponseEntity.ok(
                routingService.handle(maxLeaveMessage, Methods.MAX_LEAVE, routingService, () -> {
                    networkService.maxLeave(maxLeaveMessage.getActiveJob());
                    return new CommonResponse(CommonType.OK);
                }));
    }

    @PostMapping(path = "host-ack", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> hostAck(@RequestBody PingMessage pingMessage) {

        return ResponseEntity.ok(
                routingService.handle(pingMessage, Methods.HOST_ACK, routingService, () -> {
                    networkService.ack();
                    return new CommonResponse(CommonType.OK);
                }));
    }
}
