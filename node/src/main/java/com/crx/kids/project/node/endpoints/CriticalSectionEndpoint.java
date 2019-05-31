package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Methods;
import com.crx.kids.project.node.messages.BroadcastMessage;
import com.crx.kids.project.node.messages.SuzukiKasamiTokenMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class CriticalSectionEndpoint {


    @PostMapping(path = "critical-section-broadcast", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> criticalSectionBroadcast(@RequestBody BroadcastMessage<Integer> criticalSectionBroadcast) {

        routingService.broadcastMessage(criticalSectionBroadcast, Methods.BROADCAST_CRITICAL_SECTION);
        criticalSectionService.handleSuzukiKasamiBroadcastMessage(criticalSectionBroadcast);

        return ResponseEntity.ok(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "critical-section-token", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
