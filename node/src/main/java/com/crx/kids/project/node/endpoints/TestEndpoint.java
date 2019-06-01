package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.dto.StatusResponse;
import com.crx.kids.project.node.messages.PingMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.services.CriticalSectionService;
import com.crx.kids.project.node.services.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "test")
public class TestEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(TestEndpoint.class);

    @Autowired
    private RoutingService routingService;

    @Autowired
    private CriticalSectionService criticalSectionService;


    @GetMapping(path = "stats", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getAllNeighbours() {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.setNodes(Network.neighbours);
        statusResponse.setFirstSmallest(Network.firstSmallestNeighbour);
        statusResponse.setSecondSmallest(Network.secondSmallestNeighbour);
        return ResponseEntity.ok().body(statusResponse);
    }

    @PostMapping(path = "ping", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> ping(@RequestBody PingMessage pingMessage) {
        if (pingMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(pingMessage, Methods.PING);
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


}
