package servent.message;

import app.Config;
import app.NodeInfo;
import app.snapshot_bitcake.BitcakeManager;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another node.
 * 
 * @author bmilojkovic
 *
 */
public class TransactionMessage extends BasicMessage {

	private static final long serialVersionUID = -333251402058492901L;

	private transient BitcakeManager bitcakeManager;

	private int amount;
	
	public TransactionMessage(NodeInfo sender, NodeInfo receiver, int amount, BitcakeManager bitcakeManager) {
		super(MessageType.TRANSACTION, sender, receiver);
		this.bitcakeManager = bitcakeManager;
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

}
