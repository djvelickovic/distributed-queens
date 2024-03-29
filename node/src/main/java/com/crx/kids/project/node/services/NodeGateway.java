package com.crx.kids.project.node.services;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.common.util.Error;
import com.crx.kids.project.common.util.ErrorCode;
import com.crx.kids.project.common.util.Result;
import com.crx.kids.project.node.messages.Message;
import com.crx.kids.project.node.messages.response.CommonResponse;
import com.crx.kids.project.node.messages.response.CommonType;
import com.crx.kids.project.node.utils.NetUtil;
import com.crx.kids.project.node.utils.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Service
public class NodeGateway {

    private static final Logger logger = LoggerFactory.getLogger(NodeGateway.class);
    private static final Random rnd = new Random();

    public Result send(Message message, NodeInfo nextHop, String path) {
        String url = NetUtil.url(nextHop, path);
        logger.debug("Url for sending: {}", url);

//        ThreadUtil.sleep(rnd.nextInt(100)+200);

        try {
            RestTemplate client = new RestTemplate();
            ResponseEntity<CommonResponse> response = client.postForEntity(url, message, CommonResponse.class);

            CommonResponse commonResponse = response.getBody();

            if (commonResponse == null || commonResponse.getType() == null) {
                logger.error("Protocol error. Response or response type from {} is null.", url);
                return Result.error(Error.of(ErrorCode.COMMUNICATION_ERROR, "Common response or response type are null."));
            }

            if (commonResponse.getType() == CommonType.OK) {
                return Result.of(null);
            }
            else if (commonResponse.getType() == CommonType.REDIRECT) {
                String reason = commonResponse.getComment();
                NodeInfo replacedHop = commonResponse.getRedirectNode();
                logger.warn("Node received REDIRECT response from {}. Reason: {}. Next hop is changed to {}", url, reason, replacedHop);

                // invoke another node
                return send(message, replacedHop, path);
            }
            else if (commonResponse.getType() == CommonType.ERROR) {
                logger.error("Node received ERROR response from {}. Reason: {}.", url, commonResponse.getComment());
                return Result.error(Error.of(ErrorCode.COMMUNICATION_ERROR, commonResponse.getComment()));
            }
            else {
                return Result.error(Error.of(ErrorCode.COMMUNICATION_ERROR, "Unhandled case!"));
            }
        }
        catch (Exception e){
            logger.error("Unable to communicate with {}. Error: {}", nextHop, e.getMessage());
            return Result.error(Error.of(ErrorCode.COMMUNICATION_ERROR, e.getMessage()));
        }
    }
}
