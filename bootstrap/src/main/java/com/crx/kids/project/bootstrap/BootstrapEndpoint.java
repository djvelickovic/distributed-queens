package com.crx.kids.project.bootstrap;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.CheckOutRequest;
import com.crx.kids.project.common.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class BootstrapEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapEndpoint.class);

    private static final AtomicBoolean lock = new AtomicBoolean(false);
    @Autowired
    private NodeService nodeService;

    @PostMapping(path = "check-in", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity checkIn(@RequestBody NodeInfo checkIn) {

        while (lock.get()) {
            logger.warn("Checkin lock hit!");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        CheckInResponse checkInResponse = nodeService.checkIn(checkIn);
        logger.info("New node {} is subscribed to system {}", checkIn, checkInResponse);
        return ResponseEntity.ok(checkInResponse);
    }

    @PostMapping(path = "check-out", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity checkOut(@RequestBody CheckOutRequest checkOut) {

        while (lock.get()) {
            logger.warn("Checkout lock hit!");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Node is unsubscribing from system {}", checkOut);
        nodeService.checkOut(checkOut);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "lock", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity lock() {
        logger.warn("Locking bootstrap");
        lock.set(true);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "unlock", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity unlock() {
        logger.warn("Unlocking bootstrap");
        lock.set(false);
        return ResponseEntity.ok().build();
    }
}
