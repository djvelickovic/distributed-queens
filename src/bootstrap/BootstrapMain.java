package bootstrap;

import bootstrap.handlers.BootstrapMessageHandler;
import common.CommonListener;
import common.Config;
import common.NodeInfo;
import common.messages.bootstrap.BootstrapMessage;
import common.util.Log;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class BootstrapMain {


    // arg 0 - address
    // arg 1 - listening port
	public static void main(String[] args) {
		// INIT LISTENERS
		// START CLI

		Log.debug("Arguments: " + Arrays.toString(args));

		Config.bootstrap = new NodeInfo(args[0], Integer.parseInt(args[1]));

		Log.info("Bootstrap service started...");

		BootstrapNodeService bootstrapNodeService = new BootstrapNodeService();
		BootstrapMessageHandler bootstrapMessageHandler = new BootstrapMessageHandler(bootstrapNodeService);
		CommonListener commonListener = new CommonListener(Config.bootstrap.getIp(), Config.bootstrap.getPort());

		commonListener.setMessageHandler((m, e) -> {
			if (m instanceof BootstrapMessage) {
				bootstrapMessageHandler.handle((BootstrapMessage) m, e);
			}
			else {
				Log.error("Message cannot be handled! Message: "+m.toString());
			}
		});

		new Thread(commonListener).start();


//		NodeListener simpleListener = new NodeListener();
//		Thread listenerThread = new Thread(simpleListener);
//		listenerThread.start();
//
//		CLIParser cliParser = new CLIParser();
//		Thread cliThread = new Thread(cliParser);
//		cliThread.start();
		
	}
}
