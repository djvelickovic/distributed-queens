package com.crx.kids.project.node.endpoints;

import com.crx.kids.project.node.common.Jobs;
import com.crx.kids.project.node.endpoints.dto.ControlPlaneResponse;
import com.crx.kids.project.node.endpoints.dto.DimensionsDTO;
import com.crx.kids.project.node.services.CriticalSectionService;
import com.crx.kids.project.node.services.JobService;
import com.crx.kids.project.node.services.RoutingService;
import com.crx.kids.project.node.services.StoppingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(path = "control")
public class ControlEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ControlEndpoint.class);

    @Autowired
    private JobService jobService;

    @Autowired
    private CriticalSectionService criticalSectionService;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private StoppingService stoppingService;

    @PostMapping(path = "start", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity start(@RequestBody DimensionsDTO dimensionsDTO) {
        if (dimensionsDTO.getDimension() == null || dimensionsDTO.getDimension() < 1) {
            return ResponseEntity.status(400).body(new ControlPlaneResponse("MISSING_PARAMETERS",""));
        }

        if (Jobs.currentActiveDim.get() == dimensionsDTO.getDimension()) {
            return ResponseEntity.ok(new ControlPlaneResponse("ERROR", "Calculating for dimension "+dimensionsDTO.getDimension()+" has been already started."));

        }
        criticalSectionService.submitProcedureForCriticalExecution(i -> {
            jobService.initiateJobForDimension(dimensionsDTO.getDimension());
        });

        return ResponseEntity.ok(new ControlPlaneResponse("OK", ""));
    }

    @GetMapping(path = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity status() {
        return ResponseEntity.ok(jobService.getStatus());
    }

    @PostMapping(path = "pause", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity pause() {

        if (Jobs.currentActiveDim.get() == -1) {
            return ResponseEntity.ok(new ControlPlaneResponse("ERROR", "There are no active job."));
        }

        criticalSectionService.submitProcedureForCriticalExecution(i -> {
            boolean pause = jobService.pause();

            if (pause) {
                logger.info("All jobs paused.");
            }
            else {
                logger.info("Failed to pause jobs.");
            }
        });
        return ResponseEntity.ok(new ControlPlaneResponse("OK", "Requesting job pausing."));
    }

    @PostMapping(path = "stop", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity stop() {

        stoppingService.initiateStoppingPrcedure();

        return ResponseEntity.ok(new ControlPlaneResponse("INITIATED", "Initiated stopping procedure."));
    }


    @PostMapping(path = "result", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity result(@RequestBody DimensionsDTO dimensionsDTO) {
        if (dimensionsDTO.getDimension() == null || dimensionsDTO.getDimension() < 1) {
            return ResponseEntity.status(400).body(new ControlPlaneResponse("MISSING_PARAMETERS",""));
        }

        Optional<List<Integer[]>> queensResultResult = jobService.result(dimensionsDTO.getDimension());

        if (queensResultResult.isPresent()) {
            return ResponseEntity.ok(transformTable(queensResultResult.get()));
        }

        return ResponseEntity.ok("Job for dimension "+dimensionsDTO.getDimension()+" has not been finished or started.");
    }

    @PostMapping(path = "result-simple", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity resultSimple(@RequestBody DimensionsDTO dimensionsDTO) {
        if (dimensionsDTO.getDimension() == null || dimensionsDTO.getDimension() < 1) {
            return ResponseEntity.status(400).body(new ControlPlaneResponse("MISSING_PARAMETERS",""));
        }

        Optional<List<Integer[]>> queensResultResult = jobService.result(dimensionsDTO.getDimension());

        if (queensResultResult.isPresent()) {
            return ResponseEntity.ok(simpleTransform(queensResultResult.get()));
        }

        return ResponseEntity.ok("Job for dimension "+dimensionsDTO.getDimension()+" has not been finished or started.");
    }

    private String simpleTransform(List<Integer[]> queens) {
        StringBuilder sb = new StringBuilder();
        sb.append("QUEENS: "+queens.size()+"\n");
        queens.forEach(q -> {
            sb.append(Arrays.toString(q));
            sb.append("\n");
        });
        return sb.toString();
    }

    private String transformTable(List<Integer[]> queens) {

        StringBuilder sb = new StringBuilder();
        AtomicInteger cnt = new AtomicInteger(0);
        queens.forEach(q -> {
            sb.append("QUEENS: "+cnt.incrementAndGet()+"\n\n");


            sb.append(" ");
            for (int j = 0; j < q.length; j++) {
                sb.append(" ");
                sb.append(j+1);
            }
            sb.append("\n");
            for (int i = 0; i < q.length; i++) {
                sb.append(i+1);
                for (int j = 0; j < q.length; j++) {
                    if (q[i] == j) {
                        sb.append(" Q");
                    } else {
                        sb.append(" _");
                    }
                }
                sb.append("\n");
            }
            sb.append("\n\n");
        });
        return sb.toString();
    }
}
