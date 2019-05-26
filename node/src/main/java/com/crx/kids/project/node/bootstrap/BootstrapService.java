package com.crx.kids.project.node.bootstrap;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class BootstrapService {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapService.class);


    public Optional<CheckInResponse> checkIn(NodeInfo bootstrap, NodeInfo myself) {

        logger.info("Sending to bootstrap request {}", myself);

        String url = "http://"+bootstrap.getIp()+":"+bootstrap.getPort()+"/bootstrap/check-in";

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
