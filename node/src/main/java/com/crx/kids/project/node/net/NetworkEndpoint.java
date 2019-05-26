package com.crx.kids.project.node.net;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.messages.AlterRoutingTableMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.messages.response.CommonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "net")
public class NetworkEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(NetworkEndpoint.class);

    @Autowired
    private NetworkService networkService;

    @GetMapping(path = "stats", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getAllNeighbours() {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.setNodes(Network.neighbours);
        statusResponse.setFirstSmallest(Network.firstSmallestNeighbour);
        statusResponse.setSecondSmallest(Network.secondSmallestNeighbour);
        return ResponseEntity.ok().body(statusResponse);
    }

    @PostMapping(path = "neighbours", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> alterNeighbours(@RequestBody AlterRoutingTableMessage alterRoutingTableMessage) {
        networkService.alterRoutingTable(alterRoutingTableMessage);

        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setType(CommonType.OK);
        return ResponseEntity.ok().body(commonResponse);
    }

    @PostMapping(path = "newbie-join", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> newbieConnect(@RequestBody NewbieJoinMessage newbieJoinMessage) {
        // async message!
        networkService.newbieJoinAsync(newbieJoinMessage);
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setType(CommonType.OK);
        return ResponseEntity.ok().body(commonResponse);
    }

    @PostMapping(path = "newbie-accepted", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> newbieAccepted(@RequestBody NewbieAcceptedMessage newbieAcceptedMessage) {
        networkService.newbieAccepted(newbieAcceptedMessage);
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setType(CommonType.OK);
        return ResponseEntity.ok().body(commonResponse);    }





}
