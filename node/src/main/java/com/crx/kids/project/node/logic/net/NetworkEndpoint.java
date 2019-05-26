package com.crx.kids.project.node.logic.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "net")
public class NetworkEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(NetworkEndpoint.class);

    @GetMapping(path = "neighbours", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getAllNeighbours() {
        logger.info(System.getProperty("bootstrap.addr"));
        return ResponseEntity.ok().build();
    }


}
