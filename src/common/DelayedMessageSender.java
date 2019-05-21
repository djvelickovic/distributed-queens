package common;

import node.boundary.message.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This worker sends a message asynchronously. Doing this in a separate thread
 * has the added benefit of being able to delay without blocking main or somesuch.
 * 
 * @author bmilojkovic
 *
 */
public class DelayedMessageSender implements Runnable {

	private Message messageToSend;
	
	public DelayedMessageSender(Message messageToSend) {
		this.messageToSend = messageToSend;
	}
	
	public void run() {
		/*
		 * A random sleep before sending.
		 * It is important to take regular naps for health reasons.
		 */
		try {
			Thread.sleep((long)(Math.random() * 500) + 500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		NodeInfo receiverInfo = messageToSend.receiver();
		
		if (MessageUtil.MESSAGE_UTIL_PRINTING) {
			Log.info("Sending message " + messageToSend);
		}
		
		try {
			Socket sendSocket = new Socket(receiverInfo.getIp(), receiverInfo.getPort());

			ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
			oos.writeObject(messageToSend);
			oos.flush();

			sendSocket.close();
		} catch (IOException e) {
			Log.error("Couldn't send message: " + messageToSend.toString());
		}
	}
	
}
