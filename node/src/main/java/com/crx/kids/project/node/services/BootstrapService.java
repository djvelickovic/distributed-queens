package com.crx.kids.project.node.services;

import com.crx.kids.project.common.CheckInResponse;
import com.crx.kids.project.common.CheckOutRequest;
import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.common.Configuration;
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

    public void checkOut() {

        logger.info("Sending to bootstrap chechout request {}", Configuration.myself);

        String url = "http://"+Configuration.bootstrap.getIp()+":"+Configuration.bootstrap.getPort()+"/bootstrap/check-out";

        try {

            CheckOutRequest checkOutRequest = new CheckOutRequest();
            checkOutRequest.setId(Configuration.id);
            checkOutRequest.setNodeInfo(Configuration.myself);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity response = restTemplate.postForEntity(url, checkOutRequest, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Couldn't get response from bootstrap. Status code: {}", response.getStatusCode().value());
            }
            else {
                logger.info("Successfully chechkOut");
            }
        }
        catch (Exception e) {
            logger.error("Couldn't get response from bootstrap. Error: ",e);
        }
    }

    public void lock() {

        logger.info("Sending to bootstrap lock request {}", Configuration.myself);

        String url = "http://"+Configuration.bootstrap.getIp()+":"+Configuration.bootstrap.getPort()+"/bootstrap/lock";

        try {

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Couldn't get response from bootstrap. Status code: {}", response.getStatusCode().value());
            }
            else {
                logger.info("Successfully lock");
            }
        }
        catch (Exception e) {
            logger.error("Couldn't get response from bootstrap. Error: ",e);
        }
    }

    public void unlock() {

        logger.info("Sending to bootstrap unlock request {}", Configuration.myself);

        String url = "http://"+Configuration.bootstrap.getIp()+":"+Configuration.bootstrap.getPort()+"/bootstrap/unlock";

        try {

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Couldn't get response from bootstrap. Status code: {}", response.getStatusCode().value());
            }
            else {
                logger.info("Successfully lock");
            }
        }
        catch (Exception e) {
            logger.error("Couldn't get response from bootstrap. Error: ",e);
        }
    }

}
