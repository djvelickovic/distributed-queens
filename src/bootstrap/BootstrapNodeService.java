package bootstrap;

import common.MessageUtil;
import common.NodeInfo;
import common.messages.bootstrap.BootstrapFlag;
import common.messages.bootstrap.BootstrapMessage;
import common.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BootstrapNodeService {

    public Map<NodeInfo, NodeState> nodes;

    public BootstrapNodeService() {
        nodes = new ConcurrentHashMap<>();
    }

    public void changeNodeState(NodeInfo nodeInfo, NodeState nodeState) {
        if (nodeState == NodeState.ACTIVE) {
            Log.info("New node in system. Node: "+ nodeInfo);
        }
        nodes.put(nodeInfo, nodeState);
    }

    public NodeInfo getRandomActiveNode() {
        return nodes.entrySet().stream()
                .filter(e -> e.getValue().equals(NodeState.ACTIVE))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public void sendHeartbeatMessages() {
        Log.debug("Sending heartbeat messages.");

        nodes.forEach((node, state) -> {
            BootstrapMessage pingMessage = new BootstrapMessage(BootstrapConfig.bootstrap(), node, BootstrapFlag.HEARTBEAT);
            MessageUtil.sendMessage(pingMessage, e -> {
                Log.warn("Node is inactive. Unable to send heartbeat message: "+ node);
                nodes.compute(node, (k, v) -> NodeState.INACTIVE);
            });
        });
    }

    public void removeInactiveNodes() {
        Log.debug("Removing inactive nodes.");

        nodes.entrySet().stream()
                .filter(e -> e.getValue().equals(NodeState.INACTIVE))
                .map(Map.Entry::getKey)
                .forEach(nodes::remove);
    }
}
