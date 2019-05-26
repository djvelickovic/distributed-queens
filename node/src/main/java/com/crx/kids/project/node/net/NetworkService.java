package com.crx.kids.project.node.net;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.common.util.Error;
import com.crx.kids.project.common.util.ErrorCode;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.Configuration;
import com.crx.kids.project.node.ThreadUtil;
import com.crx.kids.project.node.bootstrap.BootstrapService;
import com.crx.kids.project.node.messages.*;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.routing.RoutingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    @Autowired
    private BootstrapService bootstrapService;

    @Autowired
    private RoutingService routingService;

    @EventListener(ApplicationReadyEvent.class)
    public void connectToBootstrap() {
        Optional<CheckInResponse> checkInResponseOptional = bootstrapService.checkIn(Configuration.bootstrap, Configuration.myself);

        if (checkInResponseOptional.isPresent()) {
            logger.info("Checkin response {}", checkInResponseOptional.get());

            Configuration.id = checkInResponseOptional.get().getId();
            Network.firstKnownNode = checkInResponseOptional.get().getNodeInfo();


            // join network,
            if (Network.firstKnownNode != null) {
                int receiver = RoutingUtils.darah(Configuration.id);
                NewbieJoinMessage newbieJoinMessage = new NewbieJoinMessage(Configuration.id, receiver, Configuration.myself);
                // instead of calculating next hop, next hop is firsKnownNode

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(Network.firstKnownNode, "node/net/newbie-join"), newbieJoinMessage, CommonResponse.class);
                    CommonResponse commonResponse = response.getBody();
                    if (commonResponse.getType() == CommonType.OK) {
                        logger.info("Received ok response. Waiting for routing table.");
                    } else {
                        logger.info("Received error response. Exiting. {}", response);
                    }
                } catch (Exception e) {
                    logger.error("", e);
                    logger.error("Shutting down application. Unable to connect to network.");
                    System.exit(0);
                }
            }
        }
        else {
            logger.error("Shutting down application. No response from bootstrap.");
            System.exit(0);
        }
    }

    @Async
    public void newbieJoinAsync(NewbieJoinMessage newbieJoinMessage) {

        if (newbieJoinMessage.getReceiver() == Configuration.id) {
            // message is for me!


        }
        else { // dispatch message
            newbieJoinMessage.addTrace(new Trace(Configuration.id, null));

            // loop until next hop is determined
            FullNodeInfo nextHop = ThreadUtil.loopWithResult(300, (l) -> {
                Optional<FullNodeInfo> optionalNextHop = routingService.nextHop(Configuration.id, newbieJoinMessage.getReceiver(), () -> Network.neighbours);
                if (optionalNextHop.isPresent()) {
                    l.stop();
                    return optionalNextHop.get();
                }
                logger.warn("Unable to find next hope. Sleep for 300 ms!");
                return null;
            });

            Result<Void> result = dispatchJoinRequest(nextHop, newbieJoinMessage);
            // TODO: // watch result type, if type is for logout, then remove node from routing table, and redispatch message!
        }
    }


    private Result<Void> dispatchJoinRequest(NodeInfo nextHop, NewbieJoinMessage newbieJoinMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(nextHop, "node/net/newbie-join"), newbieJoinMessage, CommonResponse.class);

            logger.info("Response {}", response);
            return Result.of(null);
        }
        catch (Exception e) {
            logger.error("",e);
            return Result.error(Error.of(ErrorCode.COMMUNICATION_ERROR, e.getMessage()));
        }
    }


    public void newbieAccepted(NewbieAcceptedMessage newbieAcceptedMessage) {
        Network.neighbours.putAll(newbieAcceptedMessage.getRoutingTable());

        // TODO: send AlterRoutingTable message

        Network.neighbours.forEach( (receiver, nodeInfo) -> {
            AlterRoutingTableMessage alterRoutingTableMessage = new AlterRoutingTableMessage(Configuration.id, receiver, false, Configuration.myself);




        });
    }

    public Result<Void> sendAlterRoutingMessage(NodeInfo nextHop, AlterRoutingTableMessage alterRoutingTableMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<CommonResponse> response = restTemplate.postForEntity(NetUtil.url(nextHop, "node/net/neighbours"), alterRoutingTableMessage, CommonResponse.class);

            logger.info("Response {}", response);
            return Result.of(null);
        }
        catch (Exception e) {
            logger.error("",e);
            return Result.error(Error.of(ErrorCode.COMMUNICATION_ERROR, e.getMessage()));
        }
    }

    public Result<Void> alterRoutingTable(AlterRoutingTableMessage alterRoutingTableMessage) {
        logger.info("Altering routing table with {} : {}", alterRoutingTableMessage.getSender(), alterRoutingTableMessage.getNodeInfo());
        Network.neighbours.put(alterRoutingTableMessage.getSender(), alterRoutingTableMessage.getNodeInfo());
        return Result.of(null);    }
}
