package bootstrap;

import common.Log;
import node.NodeConfig;

import java.util.stream.Stream;


public class BootstrapMain {


    // arg 0 - properties path
    // arg 1 - listening port
	public static void main(String[] args) {
		// INIT LISTENERS
		// START CLI

		Stream.of(args).forEach(System.out::println);

		BootstrapConfig.readConfig(args[0]);
		BootstrapConfig.setBootstrapPort(args[1]);

		Log.info(BootstrapConfig.bootstrapPort()+"");
		Log.info(BootstrapConfig.testDelay()+"");

//		NodeListener simpleListener = new NodeListener();
//		Thread listenerThread = new Thread(simpleListener);
//		listenerThread.start();
//
//		CLIParser cliParser = new CLIParser();
//		Thread cliThread = new Thread(cliParser);
//		cliThread.start();
		
	}
}
