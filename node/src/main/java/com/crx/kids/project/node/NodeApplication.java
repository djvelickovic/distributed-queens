package com.crx.kids.project.node;

import com.crx.kids.project.common.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@org.springframework.context.annotation.Configuration
@EnableAsync
public class NodeApplication {

    private static final Logger logger = LoggerFactory.getLogger(NodeApplication.class);

    // bootstrap port
    // bootsrap address
    public static void main(String[] args) {
        Configuration.bootstrap = new NodeInfo(System.getProperty("bootstrap.addr"), Integer.parseInt(System.getProperty("bootstrap.port")));
        Configuration.myself = new NodeInfo(System.getProperty("server.address"), Integer.parseInt(System.getProperty("server.port")));

        logger.info("I m node {}. first of my name..", Configuration.myself);
        logger.info("Bootstrap configuration {}..", Configuration.bootstrap);

        SpringApplication.run(NodeApplication.class, args);

    }
}
