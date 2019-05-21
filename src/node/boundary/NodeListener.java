package node.boundary;

import common.Cancellable;
import common.ConfigUtil;
import common.Log;
import common.MessageUtil;
import node.NodeConfig;
import node.boundary.handler.MessageHandler;
import node.boundary.message.Message;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NodeListener implements Runnable, Cancellable {

	private volatile boolean working = true;

	public NodeListener() {

	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService executorService = Executors.newWorkStealingPool();


	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(NodeConfig.nodePort(), 2000);
			/*
			 * If there is no connection after 5s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(5000);
		} catch (IOException e) {
			Log.error("Couldn't open listener socket on: " + NodeConfig.nodePort());
			System.exit(0);
		}

		while (working) {
			try {
				Message clientMessage;

				/*
				 * This blocks for up to 1s, after which SocketTimeoutException is thrown.
				 */
				Socket clientSocket = listenerSocket.accept();

				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);



				MessageHandler messageHandler = (e) -> {};
//
//				/*
//				 * Each message type has it's own handler.
//				 * If we can get away with stateless handlers, we will,
//				 * because that way is much simpler and less error prone.
//				 */
//				switch (clientMessage.getMessageType()) {
//					case TRANSACTION:
//						messageHandler = new TransactionHandler(clientMessage, snapshotCollector.getBitcakeManager());
//						break;
//					case SPAN:
//						messageHandler = new SpanHandler(clientMessage, snapshotCollector);
//						break;
//					case RESULT:
//						messageHandler = new ResultHandler(clientMessage, snapshotCollector);
//						break;
//					case BORDER:
//						messageHandler = new BorderHandler(clientMessage, snapshotCollector);
//						break;
//					case REGION:
//						messageHandler = new RegionHandler(clientMessage, spezialettiKearnsCollector);
//						break;
//				}

				messageHandler.handle(executorService);

			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				ConfigUtil.timedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
