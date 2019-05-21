package common;

import common.messages.Message;
import common.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class MessageSender implements Runnable {

	private static final boolean TEST_DELAY = true;

	private Message messageToSend;
	private Consumer<Exception> exceptionHandler;

	public MessageSender(Message messageToSend, Consumer<Exception> exceptionHandler) {
		this.messageToSend = messageToSend;
		this.exceptionHandler = exceptionHandler;
	}

	public MessageSender(Message messageToSend) {
		this(messageToSend, e -> Log.error("Couldn't send message: " + messageToSend.toString()));
	}

	public void run() {
		/*
		 * A random sleep before sending.
		 * It is important to take regular naps for health reasons.
		 */
		if (TEST_DELAY) {
			try {
				Thread.sleep((long) (Math.random() * 500) + 500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		NodeInfo receiverInfo = messageToSend.receiver();

		Log.debug("Sending message " + messageToSend);

		try (Socket sendSocket = new Socket(receiverInfo.getIp(), receiverInfo.getPort());
			 ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream())) {

			oos.writeObject(messageToSend);
			oos.flush();
		} catch (IOException e) {
			exceptionHandler.accept(e);
		}
	}

}
