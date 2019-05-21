package bootstrap.handlers;

import bootstrap.BootstrapConfig;
import bootstrap.BootstrapNodeService;
import bootstrap.NodeState;
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
                bootstrapNodeService.changeNodeState(message.sender(), NodeState.ACTIVE);
            }
            else if (message.getFlag() == BootstrapFlag.REGISTER) {
                bootstrapNodeService.changeNodeState(message.sender(), NodeState.PENDING);
                NodeInfo activeNode = bootstrapNodeService.getRandomActiveNode();
                BootstrapResponseMessage bootstrapResponseMessage = new BootstrapResponseMessage(BootstrapConfig.bootstrap(), message.sender(), activeNode);
                MessageUtil.sendMessage(bootstrapResponseMessage, e -> {
                    bootstrapNodeService.changeNodeState(message.sender(), NodeState.INACTIVE);
                    Log.warn("Unable to reach node "+message.sender());
                });
            }
            else if (message.getFlag() == BootstrapFlag.HEARTBEAT) {
                Log.info("Node repond on heartbeat "+message.sender());
                bootstrapNodeService.changeNodeState(message.sender(), NodeState.ACTIVE);
            }
        });
    }
}
