package servent;

import app.Config;
import app.Cancellable;
import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.SpezialettiKearnsCollector;
import servent.handler.MessageHandler;
import servent.handler.NullHandler;
import servent.handler.TransactionHandler;
import servent.handler.snapshot.BorderHandler;
import servent.handler.snapshot.RegionHandler;
import servent.handler.snapshot.ResultHandler;
import servent.handler.snapshot.SpanHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;

	private SnapshotCollector snapshotCollector;
	private SpezialettiKearnsCollector spezialettiKearnsCollector;

	public SimpleServentListener(SnapshotCollector snapshotCollector, SpezialettiKearnsCollector spezialettiKearnsCollector) {
		this.snapshotCollector = snapshotCollector;
		this.spezialettiKearnsCollector = spezialettiKearnsCollector;
	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();

	private Queue<Message> newSnapshotMessages = new LinkedList<>();

	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(Config.myServentInfo.getListenerPort(), 2000);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(5000);
		} catch (IOException e) {
			Config.timestampedErrorPrint("Couldn't open listener socket on: " + Config.myServentInfo.getListenerPort());
			System.exit(0);
		}


		while (working) {
			try {
				Message clientMessage;

				/*
				 * Lai-Yang stuff. Process any red messages we got before we got the marker.
				 * The marker contains the collector id, so we need to process that as our first
				 * red message.
				 *
				 * If snapshot id of message in messages list is equal to node's, then it means that node has received marker
				 * so collected messages can be processed.
				 */
				if (newSnapshotMessages.size() > 0 && Config.snapshot.snapshotIdentifier().equals(newSnapshotMessages.peek().snapshotIdentifier())) {
					clientMessage = newSnapshotMessages.poll();
					Config.timestampedErrorPrint("Polled red message ( "+clientMessage.getMessageType()+" ) for snapshot "+clientMessage.snapshotIdentifier());

				} else {
					/*
					 * This blocks for up to 1s, after which SocketTimeoutException is thrown.
					 */
					Socket clientSocket = listenerSocket.accept();

					//GOT A MESSAGE! <3
					clientMessage = MessageUtil.readMessage(clientSocket);
					if (clientMessage.getMessageType() != MessageType.TRANSACTION) {
						Config.timestampedErrorPrint(clientMessage+"");

					}

				}
				synchronized (Config.snapshot) {
					// if messages are of different snapshot id then the node's, store messages
					if (!Config.snapshot.hasParent() && !Config.snapshot.snapshotIdentifier().equals(clientMessage.snapshotIdentifier())) {
						/*
						 * If the message is red, we are white, and the message isn't a marker,
						 * then store it. We will get the marker soon, and then we will process
						 * this message. The point is, we need the marker to know who to send
						 * our info to, so this is the simplest way to work around that.
						 */
						if (clientMessage.getMessageType() == MessageType.SPAN) {
							//TODO: In logs there is no `adding red messages ...` log. Maybe there are some losses.
							//TODO: Sending span messages and adding red messages must be under same lock.
						}
						else {
							Config.timestampedErrorPrint("Adding red messages ( "+clientMessage.getMessageType()+" ) for snapshot "+clientMessage.snapshotIdentifier());
							newSnapshotMessages.add(clientMessage);
							continue;
						}
					}
				}



				MessageHandler messageHandler = new NullHandler(clientMessage);

				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch (clientMessage.getMessageType()) {
					case TRANSACTION:
						messageHandler = new TransactionHandler(clientMessage, snapshotCollector.getBitcakeManager());
						break;
					case SPAN:
						messageHandler = new SpanHandler(clientMessage, snapshotCollector);
						break;
					case RESULT:
						messageHandler = new ResultHandler(clientMessage, snapshotCollector);
						break;
					case BORDER:
						messageHandler = new BorderHandler(clientMessage, snapshotCollector);
						break;
					case REGION:
						messageHandler = new RegionHandler(clientMessage, spezialettiKearnsCollector);
						break;
				}

				messageHandler.syncHandle();
				threadPool.submit(messageHandler);

			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				Config.timedStandardPrint("Waiting...");
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
