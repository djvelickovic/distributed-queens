package bootstrap.handlers;

import bootstrap.BootstrapNodeService;
import common.Config;
import common.MessageUtil;
import common.NodeInfo;
import common.messages.bootstrap.BootstrapFlag;
import common.messages.bootstrap.BootstrapMessage;
import common.messages.bootstrap.BootstrapResponseMessage;
import common.util.Log;
import node.boundary.handler.MessageHandler;

import java.util.concurrent.ExecutorService;

public class BootstrapMessageHandler implements MessageHandler<BootstrapMessage> {

    private BootstrapNodeService bootstrapNodeService;

    public BootstrapMessageHandler(BootstrapNodeService bootstrapNodeService) {
        this.bootstrapNodeService = bootstrapNodeService;
    }

    @Override
    public void handle(BootstrapMessage message, ExecutorService executor) {
        executor.submit(() -> {
            if (message.getFlag() == BootstrapFlag.ACK) {
                Log.info("Node joined system "+message.sender());

                bootstrapNodeService.addNode(message.sender());
            }
            else if (message.getFlag() == BootstrapFlag.REGISTER) {
                NodeInfo activeNode = bootstrapNodeService.getRandomActiveNode();
                BootstrapResponseMessage bootstrapResponseMessage = new BootstrapResponseMessage(Config.bootstrap, message.sender(), activeNode);
                MessageUtil.sendMessage(bootstrapResponseMessage);
            }
            else if (message.getFlag() == BootstrapFlag.LEAVE) {
                Log.info("Node left system "+message.sender());
                bootstrapNodeService.addNode(message.sender());
            }
        });
    }
}
