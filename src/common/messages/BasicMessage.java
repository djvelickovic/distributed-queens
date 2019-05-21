package common.messages;

import common.NodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BasicMessage implements Message {

	private static final long serialVersionUID = -9075856313609777945L;

	private static AtomicInteger messageCounter = new AtomicInteger(0);

	private final NodeInfo sender;
	private final NodeInfo receiver;
	private final List<NodeInfo> routeList;

	private final int messageId;
	
	public BasicMessage(NodeInfo sender, NodeInfo receiver) {
		this.sender = sender;
		this.receiver = receiver;

		this.routeList = new ArrayList<>();
		
		this.messageId = messageCounter.getAndIncrement();
	}


	@Override
	public NodeInfo sender() {
		return sender;
	}

	@Override
	public List<NodeInfo> route() {
		return routeList;
	}

	@Override
	public NodeInfo receiver() {
		return receiver;
	}

	@Override
	public int getMessageId() {
		return messageId;
	}
}
