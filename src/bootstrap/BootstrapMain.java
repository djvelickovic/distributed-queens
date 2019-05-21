package bootstrap;

import bootstrap.handlers.BootstrapMessageHandler;
import common.CommonListener;
import common.messages.bootstrap.BootstrapMessage;
import common.util.Log;

import java.util.stream.Stream;


public class BootstrapMain {


    // arg 0 - properties path
    // arg 1 - listening port
	public static void main(String[] args) {
		// INIT LISTENERS
		// START CLI

		Stream.of(args).forEach(System.out::println);

		BootstrapConfig.readConfig(args[0]);
		BootstrapConfig.setBootstrapIp(args[1]);
		BootstrapConfig.setBootstrapPort(args[2]);

		Log.debug(BootstrapConfig.bootstrapPort()+"");
		Log.debug(BootstrapConfig.testDelay()+"");

		Log.info("Bootstrap service started...");

		BootstrapNodeService bootstrapNodeService = new BootstrapNodeService();

		BootstrapMessageHandler bootstrapMessageHandler = new BootstrapMessageHandler(bootstrapNodeService);

		new Thread(new NodeCleanerWorker(bootstrapNodeService)).start();

		CommonListener commonListener = new CommonListener(BootstrapConfig.bootstrapIp(), BootstrapConfig.bootstrapPort());

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
