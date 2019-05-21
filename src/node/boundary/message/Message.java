package node.boundary.message;

import common.NodeInfo;

import java.io.Serializable;
import java.util.List;

public interface Message extends Serializable {

	NodeInfo sender();

	List<NodeInfo> route();

	NodeInfo receiver();

	int getMessageId();
}
