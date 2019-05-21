package servent.handler;

import app.Config;
import app.snapshot_bitcake.BitcakeManager;
import servent.message.Message;
import servent.message.TransactionMessage;

public class TransactionHandler extends MessageHandler {

	private TransactionMessage clientMessage;
	private BitcakeManager bitcakeManager;
	
	public TransactionHandler(Message clientMessage, BitcakeManager bitcakeManager) {
		this.clientMessage = (TransactionMessage) clientMessage;
		this.bitcakeManager = bitcakeManager;
	}

	@Override
	public void asyncHandle() {
		bitcakeManager.addSomeBitcakes(clientMessage.getAmount());
		synchronized (Config.snapshot) {
			if (Config.snapshot.snapshotIdentifier().equals(clientMessage.snapshotIdentifier())) {
				bitcakeManager.recordGetTransaction(clientMessage.getOriginalSenderInfo().getId(), clientMessage.getAmount());
			}
		}
	}

}
