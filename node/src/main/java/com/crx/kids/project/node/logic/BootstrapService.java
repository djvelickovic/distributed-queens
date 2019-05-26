package com.crx.kids.project.node.logic;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class BootstrapService {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapService.class);


    public Optional<CheckInResponse> checkIn() {
        String bootstrapAddress = System.getProperty("bootstrap.addr");
        String bootstrapPort = System.getProperty("bootstrap.port");

        NodeInfo bootstrap = new NodeInfo(bootstrapAddress, Integer.parseInt(bootstrapPort));
        Configuration.bootstrap = bootstrap;

        String url = "http://"+bootstrapAddress+":"+bootstrapPort+"/bootstrap/check-in";
        NodeInfo myself = new NodeInfo(System.getProperty("server.address"), Integer.parseInt(System.getProperty("server.port")));

        Configuration.myself = myself;

        logger.info("I m node {}. first of my name..", myself);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<CheckInResponse> response = restTemplate.postForEntity(url, myself, CheckInResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }

            logger.error("Couldn't get response from bootstrap. Status code: {}", response.getStatusCode().value());
            return Optional.empty();
        }
        catch (Exception e) {
            logger.error("Couldn't get response from bootstrap. Error: ",e);
            return Optional.empty();
        }
    }

}
