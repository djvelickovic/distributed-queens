package com.crx.kids.project.node.net;

import com.crx.kids.project.common.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "net")
public class NetworkEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(NetworkEndpoint.class);

    @GetMapping(path = "neighbours", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getAllNeighbours() {
        Map<Integer, NodeInfo> neighbours = new HashMap<>(Network.neighbours);
        return ResponseEntity.ok().body(neighbours);
    }




}
