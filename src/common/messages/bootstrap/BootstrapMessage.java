package common.messages.bootstrap;

import common.NodeInfo;
import common.messages.BasicMessage;

public class BootstrapMessage extends BasicMessage {

    private BootstrapFlag flag;

    public BootstrapMessage(NodeInfo sender, NodeInfo receiver, BootstrapFlag bootstrapFlag) {
        super(sender, receiver);
        this.flag = bootstrapFlag;
    }

    public BootstrapFlag getFlag() {
        return flag;
    }
}
