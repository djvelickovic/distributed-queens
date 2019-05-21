package servent.message;

import app.NodeInfo;

import java.io.Serializable;
import java.util.List;

public interface Message extends Serializable {

	NodeInfo sender();

	List<NodeInfo> route();

	NodeInfo receiver();
	
	/**
	 * Message type. Mainly used to decide which handler will work on this message.
	 */
	MessageType getMessageType();


	int getMessageId();
}
