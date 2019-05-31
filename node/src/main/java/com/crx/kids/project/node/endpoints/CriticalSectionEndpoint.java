package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.messages.BroadcastMessage;
import com.crx.kids.project.node.messages.SuzukiKasamiTokenMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.services.CriticalSectionService;
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
@RequestMapping(path = "critical-section")
public class CriticalSectionEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(CriticalSectionEndpoint.class);

    @Autowired
    private RoutingService routingService;

    @Autowired
    private CriticalSectionService criticalSectionService;

    @PostMapping(path = "broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> broadcast(@RequestBody BroadcastMessage<Integer> criticalSectionBroadcast) {

        routingService.broadcastMessage(criticalSectionBroadcast, Methods.BROADCAST_CRITICAL_SECTION);
        criticalSectionService.handleSuzukiKasamiBroadcastMessage(criticalSectionBroadcast);

        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "token", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> token(@RequestBody SuzukiKasamiTokenMessage suzukiKasamiTokenMessage) {

        if (suzukiKasamiTokenMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(suzukiKasamiTokenMessage, Methods.CRITICAL_SECTION_TOKEN);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        logger.info("critical-section-token. Message {}", suzukiKasamiTokenMessage);
        criticalSectionService.handleSuzukiKasamiToken(suzukiKasamiTokenMessage.getCriticalSectionToken());

        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }
}
