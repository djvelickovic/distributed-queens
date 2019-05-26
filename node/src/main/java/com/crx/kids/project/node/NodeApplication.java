package com.crx.kids.project.node;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.node.logic.BootstrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@SpringBootApplication
public class NodeApplication {

    private static final Logger logger = LoggerFactory.getLogger(NodeApplication.class);

    // bootstrap port
    // bootsrap address
    public static void main(String[] args) {
        BootstrapService bootstrapService = new BootstrapService();
        Optional<CheckInResponse> checkInResponseOptional = bootstrapService.checkIn();

        if (checkInResponseOptional.isPresent()) {
            logger.info("Checkin response {}", checkInResponseOptional.get());
            SpringApplication.run(NodeApplication.class, args);
        }
        else {
            logger.error("Shutting down application. No response from bootstrap.");
        }
    }
}
