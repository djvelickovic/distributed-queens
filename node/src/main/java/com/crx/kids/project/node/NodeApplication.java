package com.crx.kids.project.node;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.bootstrap.BootstrapService;
import com.crx.kids.project.node.net.Network;
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
        Configuration.bootstrap = new NodeInfo(System.getProperty("bootstrap.addr"), Integer.parseInt(System.getProperty("bootstrap.port")));
        Configuration.myself = new NodeInfo(System.getProperty("server.address"), Integer.parseInt(System.getProperty("server.port")));

        logger.info("I m node {}. first of my name..", Configuration.myself);
        logger.info("Bootstrap configuration {}..", Configuration.bootstrap);

        BootstrapService bootstrapService = new BootstrapService();
        Optional<CheckInResponse> checkInResponseOptional = bootstrapService.checkIn(Configuration.bootstrap, Configuration.myself);

        if (checkInResponseOptional.isPresent()) {
            logger.info("Checkin response {}", checkInResponseOptional.get());

            Configuration.id = checkInResponseOptional.get().getId();
            Network.firstKnownNode = checkInResponseOptional.get().getNodeInfo();

            SpringApplication.run(NodeApplication.class, args);
        }
        else {
            logger.error("Shutting down application. No response from bootstrap.");
        }
    }
}
