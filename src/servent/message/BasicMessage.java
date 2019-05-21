package servent.message;

import app.Config;
import app.NodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A default message implementation. This should cover most situations.
 * If you want to add stuff, remember to think about the modificator methods.
 * If you don't override the modificators, you might drop stuff.
 * @author bmilojkovic
 *
 */
public class BasicMessage implements Message {

	private static final long serialVersionUID = -9075856313609777945L;

	private static AtomicInteger messageCounter = new AtomicInteger(0);

	private final MessageType type;
	private final NodeInfo sender;
	private final NodeInfo receiver;
	private final List<NodeInfo> routeList;

	private final int messageId;
	
	public BasicMessage(MessageType type, NodeInfo sender, NodeInfo receiver) {
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;

		this.routeList = new ArrayList<>();
		
		this.messageId = messageCounter.getAndIncrement();
	}
	
	@Override
	public MessageType getMessageType() {
		return type;
	}

	@Override
	public NodeInfo getOriginalSenderInfo() {
		return originalSenderInfo;
	}

	@Override
	public NodeInfo getReceiverInfo() {
		return receiverInfo;
	}

	@Override
	public List<NodeInfo> getRoute() {
		return routeList;
	}
	
	@Override
	public int getMessageId() {
		return messageId;
	}

	
	/**
	 * Used when resending a message. It will not change the original owner
	 * (so equality is not affected), but will add us to the route list, so
	 * message path can be retraced later.
	 */
	@Override
	public Message makeMeASender() {

	}
	
	/**
	 * Change the message received based on ID. The receiver has to be our neighbor.
	 * Use this when you want to send a message to multiple neighbors, or when resending.
	 */
	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (Config.myServentInfo.getNeighbors().contains(newReceiverId)) {
			NodeInfo newReceiverInfo = Config.getInfoById(newReceiverId);
			
			Message toReturn = new BasicMessage(getMessageType(), getOriginalSenderInfo(),
					newReceiverInfo, snapshotIdentifier, getRoute(), getMessageId());
			
			return toReturn;
		} else {
			Config.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
			return null;
		}
	}
	
	/**
	 * Comparing messages is based on their unique id and the original sender id.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicMessage) {
			BasicMessage other = (BasicMessage)obj;
			
			if (getMessageId() == other.getMessageId() &&
				getOriginalSenderInfo().getId() == other.getOriginalSenderInfo().getId()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are gonna keep this object
	 * in a set or a map. So, this is based on message id and original sender id also.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getMessageId(), getOriginalSenderInfo().getId());
	}
}
