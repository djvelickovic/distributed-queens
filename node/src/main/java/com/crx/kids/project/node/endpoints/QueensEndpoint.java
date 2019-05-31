package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.common.Methods;
import com.crx.kids.project.node.messages.*;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.services.QueensService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/queens")
public class QueensEndpoint {



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

    @PostMapping(path = "queens-results-collector", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommonResponse> queenStatusCollector(@RequestBody QueensResultsMessage queensResultsMessage) {

        routingService.broadcastMessage(queensResultsMessage, Methods.QUEENS_RESULTS_COLLECTOR);

//        jobService.putStatusMessage(statusMessage);

        return ResponseEntity.ok().body(new CommonResponse(CommonType.OK));
    }


}
