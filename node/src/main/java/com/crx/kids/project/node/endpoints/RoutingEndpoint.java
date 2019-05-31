package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.messages.Message;
import com.crx.kids.project.node.services.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "routing")
public class RoutingEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(RoutingEndpoint.class);

    @Autowired
    private RoutingService routingService;

    @PostMapping(path = "/broadcast", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity broadcast(@RequestHeader(name = "crx-method") String method, @RequestHeader(name = "crx-from") Integer from, @RequestHeader(name = "crx-broadcast-id") String broadcastId, @RequestBody Object payload) {
        routingService.broadcastMessage();
        return ResponseEntity.ok().build();
    }


    @PostMapping(path = "/forward", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity forward(@RequestHeader(name = "crx-method") String method, @RequestBody Object payload) {
        return ResponseEntity.ok().build();
    }

}
