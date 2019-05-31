package com.crx.kids.project.node.endpoints;


import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Methods;
import com.crx.kids.project.node.messages.PingMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "test")
public class TestEndpoint {



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
