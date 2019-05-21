package common.messages.bootstrap;

import common.NodeInfo;
import common.messages.BasicMessage;

public class BootstrapResponseMessage extends BasicMessage {

    private NodeInfo nodeInSystem;

    public BootstrapResponseMessage(NodeInfo sender, NodeInfo receiver, NodeInfo nodeInSystem) {
        super(sender, receiver);
        this.nodeInSystem = nodeInSystem;
    }

    public NodeInfo getNodeInSystem() {
        return nodeInSystem;
    }
}
