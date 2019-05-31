package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.endpoints.dto.StatusResponse;
import com.crx.kids.project.node.messages.*;
import com.crx.kids.project.node.messages.newbie.NewbieAcceptedMessage;
import com.crx.kids.project.node.messages.newbie.NewbieJoinMessage;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "job")
public class JobEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(JobEndpoint.class);

    @Autowired
    private RoutingService routingService;

    @Autowired
    private QueensService queensService;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobStealingService jobStealingService;


    @PostMapping(path = "queens", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> queens(@RequestBody QueensJobsMessage queensJobsMessage) {
        if (queensJobsMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(queensJobsMessage, Methods.QUEENS_JOBS);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        queensService.addJobsForDimension(queensJobsMessage.getDimension(), queensJobsMessage.getJobs());
        queensService.startWorkForDimension(queensJobsMessage.getDimension());

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "queens-pause", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> queens(@RequestBody BroadcastMessage<String> pauseMessage) {

        routingService.broadcastMessage(pauseMessage, Methods.QUEENS_PAUSE);
        QueensService.currentActiveDim = -1;

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }


    @PostMapping(path = "queens-status", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> queenStatus(@RequestBody StatusRequestMessage statusRequestMessage) {

        if (statusRequestMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(statusRequestMessage, Methods.QUEENS_STATUS);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        List<JobState> jobsStates = queensService.getJobsStates();

        StatusMessage statusMessage = new StatusMessage(Configuration.id, statusRequestMessage.getSender(), statusRequestMessage.getStatusRequestId(), jobsStates);
        routingService.dispatchMessage(statusMessage, Methods.QUEENS_STATUS_COLLECTOR);

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }


    @PostMapping(path = "queens-status-collector", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> queenStatusCollector(@RequestBody StatusMessage statusMessage) {

        if (statusMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(statusMessage, Methods.QUEENS_STATUS_COLLECTOR);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        jobService.putStatusMessage(statusMessage);

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "stealing-request", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> stealingRequest(@RequestBody JobStealingMessage stealingMessage) {

        if (stealingMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(stealingMessage, Methods.JOB_STEALING_REQUEST);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        jobStealingService.sendStolenJobs(stealingMessage.getSender(), stealingMessage.getDimension());

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }

    @PostMapping(path = "stealing-collector", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> stealingCollector(@RequestBody StolenJobsMessage stolenJobsMessage) {

        if (stolenJobsMessage.getReceiver() != Configuration.id) {
            routingService.dispatchMessage(stolenJobsMessage, Methods.JOB_STEALING_COLLECTOR);
            return ResponseEntity.ok(new CommonResponse(CommonType.OK));
        }

        jobStealingService.addStolenJobs(stolenJobsMessage.getSender(), stolenJobsMessage.getDimension(), stolenJobsMessage.getStolenJobs());

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }

}
