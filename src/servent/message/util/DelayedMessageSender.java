package servent.message.util;

import app.Config;
import app.NodeInfo;
import servent.message.Message;

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
		
		NodeInfo receiverInfo = messageToSend.getReceiverInfo();
		
		if (MessageUtil.MESSAGE_UTIL_PRINTING) {
			Config.timestampedStandardPrint("Sending message " + messageToSend);
		}
		
		try {
			/*
			 * Similar sync block to the one in FifoSenderWorker, except this one is
			 * related to Lai-Yang. We want to be sure that message color is red if we
			 * are red. Just setting the attribute when we were making the message may
			 * have been to early.
			 * All messages that declare their own stuff (eg. ResultMessage) will have
			 * to override setRedColor() because of this.
			 */
			synchronized (Config.snapshot) {

				Socket sendSocket = new Socket(receiverInfo.getIpAddress(), receiverInfo.getListenerPort());
				
				ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
				oos.writeObject(messageToSend);
				oos.flush();
				
				sendSocket.close();
				
				messageToSend.sendEffect();
			}
		} catch (IOException e) {
			Config.timestampedErrorPrint("ERROR "+ e.getMessage());
			Config.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());
		}
	}
	
}
