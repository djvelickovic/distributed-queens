package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.common.Configuration;
import com.crx.kids.project.node.endpoints.dto.ControlPlaneResponse;
import com.crx.kids.project.node.endpoints.dto.DimensionsDTO;
import com.crx.kids.project.node.services.JobService;
import com.crx.kids.project.node.services.CriticalSectionService;
import com.crx.kids.project.node.entities.QueensResult;
import com.crx.kids.project.node.messages.BroadcastMessage;
import com.crx.kids.project.node.common.Network;
import com.crx.kids.project.node.services.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "control")
public class ControlPlaneEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ControlPlaneEndpoint.class);

    @Autowired
    private JobService jobService;

    @Autowired
    private CriticalSectionService criticalSectionService;

    @Autowired
    private RoutingService routingService;

    @PostMapping(path = "start", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity start(@RequestBody DimensionsDTO dimensionsDTO) {
        if (dimensionsDTO.getDimension() == null || dimensionsDTO.getDimension() < 1) {
            return ResponseEntity.status(400).body(new ControlPlaneResponse("MISSING_PARAMETERS",""));
        }

        criticalSectionService.submitProcedureForCriticalExecution(i -> {
            Result result = jobService.start(dimensionsDTO.getDimension());

            if (!result.isError()) {
                logger.info("Started job for queens d = {}.", dimensionsDTO.getDimension());
//                routingService.broadcastMessage(null, null);
            }
            else {
                logger.warn("Job has been already started for queens  d = {}, Error {}", dimensionsDTO.getDimension(), result.getError());
            }
        });

        return ResponseEntity.ok(new ControlPlaneResponse("OK", ""));
    }

    @GetMapping(path = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity status() {
        return ResponseEntity.ok(jobService.getStatus());
    }

    @PostMapping(path = "pause", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity pause() {

        criticalSectionService.submitProcedureForCriticalExecution(i -> {
            boolean pause = jobService.pause();
            BroadcastMessage<String> pauseBroadcastMessage = new BroadcastMessage<>(Configuration.id, UUID.randomUUID().toString());
            routingService.broadcastMessage(pauseBroadcastMessage, Network.QUEENS_PAUSE);
            if (pause) {
                logger.info("All jobs paused.");
            }
            else {
                logger.info("Failed to pause jobs.");
            }
        });
        return ResponseEntity.ok(new ControlPlaneResponse("OK", ""));
    }

    @PostMapping(path = "stop", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity stop() {

        boolean stop = jobService.stop();
        if (stop) {
            return ResponseEntity.ok(new ControlPlaneResponse("STOPPED", ""));
        }
        else {
            return ResponseEntity.ok(new ControlPlaneResponse("ERROR", ""));
        }
    }


    @PostMapping(path = "result", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity result(@RequestBody DimensionsDTO dimensionsDTO) {
        if (dimensionsDTO.getDimension() == null || dimensionsDTO.getDimension() < 1) {
            return ResponseEntity.status(400).body(new ControlPlaneResponse("MISSING_PARAMETERS",""));
        }

        Result<QueensResult> queensResultResult = jobService.result(dimensionsDTO.getDimension());

        if (!queensResultResult.isError()) {
            return ResponseEntity.ok(new ControlPlaneResponse("STARTED", "Started job for queens d = "+dimensionsDTO.getDimension()));
        }
        else {
            return ResponseEntity.ok(new ControlPlaneResponse("ERROR", "Job has been already started for queens d = "+dimensionsDTO.getDimension()));
        }
    }
}
