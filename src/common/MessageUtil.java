package common;

import common.util.Log;
import common.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.function.Consumer;

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
			Log.error("Error in reading socket on " + socket.getInetAddress() + ":" + socket.getPort());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		if (MESSAGE_UTIL_PRINTING) {
			Log.info("Got message " + clientMessage);
		}
				
		return clientMessage;
	}
	
	public static void sendMessage(Message message, Consumer<Exception> exceptionHandler) {
		Thread delayedSender = new Thread(new MessageSender(message, exceptionHandler));
		delayedSender.start();
	}

	public static void sendMessage(Message message) {
		Thread delayedSender = new Thread(new MessageSender(message));
		delayedSender.start();
	}
}
