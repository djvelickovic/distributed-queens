package com.crx.kids.project.bootstrap;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.CheckOutRequest;
import com.crx.kids.project.common.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BootstrapEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapEndpoint.class);

    @Autowired
    private NodeService nodeService;

    @PostMapping(path = "check-in", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity checkIn(@RequestBody NodeInfo checkIn) {
        CheckInResponse checkInResponse = nodeService.checkIn(checkIn);
        logger.info("New node {} is subscribed to system {}", checkIn, checkInResponse);
        return ResponseEntity.ok(checkInResponse);
    }

    @PostMapping(path = "check-out", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity checkOut(@RequestBody CheckOutRequest checkOut) {
        logger.info("Node is unsubscribing from system {}", checkOut);
        nodeService.checkOut(checkOut);
        return ResponseEntity.ok().build();
    }
}
