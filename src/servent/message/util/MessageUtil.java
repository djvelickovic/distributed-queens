package servent.message.util;

import app.Config;
import servent.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MessageUtil {

	/**
	 * Normally this should be true, because it helps with debugging.
	 * Flip this to false to disable printing every message send / receive.
	 */
	public static final boolean MESSAGE_UTIL_PRINTING = true;
	
	public static Message readMessage(Socket socket) {
		Message clientMessage = null;
			
		try {
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
	
			clientMessage = (Message) ois.readObject();
			
			socket.close();
		} catch (IOException e) {
			Config.timestampedErrorPrint("Error in reading socket on " +
					socket.getInetAddress() + ":" + socket.getPort());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		if (MESSAGE_UTIL_PRINTING) {
			Config.timestampedStandardPrint("Got message " + clientMessage);
		}
				
		return clientMessage;
	}
	
	public static void sendMessage(Message message) {
		Thread delayedSender = new Thread(new DelayedMessageSender(message));
		
		delayedSender.start();
	}
}
