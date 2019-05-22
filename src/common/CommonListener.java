package common;

import common.messages.Message;
import common.util.Cancellable;
import common.util.Log;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class CommonListener implements Runnable, Cancellable {


	private final ExecutorService executorService = Executors.newWorkStealingPool();

	private volatile boolean working = true;

	private BiConsumer<Message, ExecutorService> messageHandler;
	private String address;
	private int port;

	public CommonListener(String address, int port) {
		messageHandler = (m, e) -> Log.error("Message handler is not specified!");
		this.address = address;
		this.port = port;
	}

	public void setMessageHandler(BiConsumer<Message, ExecutorService> messageHandler) {
		this.messageHandler = messageHandler;
	}

	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(port, 2000, InetAddress.getByName(address));
			/*
			 * If there is no connection after 5s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(5000);
		} catch (IOException e) {
			Log.error("Couldn't open listener socket on: " + address+":"+port);
			System.exit(0);
		}

		while (working) {
			try {
				Socket clientSocket = listenerSocket.accept();

				Message clientMessage = MessageUtil.readMessage(clientSocket);

				messageHandler.accept(clientMessage, executorService);
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
