package com.crx.kids.project.node.api;

import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.logic.QueensResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "control")
public class ControlPlaneEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ControlPlaneEndpoint.class);

    @Autowired
    private JobService jobService;

    @PostMapping(path = "start", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity start(@RequestBody DimensionsDTO dimensionsDTO) {
        if (dimensionsDTO.getDimension() == null || dimensionsDTO.getDimension() < 1) {
            return ResponseEntity.status(400).body(new ControlPlaneResponse("MISSING_PARAMETERS",""));
        }

        Result result = jobService.start(dimensionsDTO.getDimension());
        if (!result.isError()) {
            return ResponseEntity.ok(new ControlPlaneResponse("STARTED", "Started job for queens d = "+dimensionsDTO.getDimension()));
        }
        else {
            return ResponseEntity.ok(new ControlPlaneResponse("ERROR", "Job has been already started for queens d = "+dimensionsDTO.getDimension()));
        }
    }

    @GetMapping(path = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity status() {
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "pause", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity pause() {

        boolean pause = jobService.pause();
        if (pause) {
            return ResponseEntity.ok(new ControlPlaneResponse("PAUSED", ""));
        }
        else {
            return ResponseEntity.ok(new ControlPlaneResponse("ERROR", ""));
        }
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
