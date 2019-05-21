package node;

import common.util.Log;

import java.util.stream.Stream;


public class NodeMain {


    // arg 0 - listening port
    // arg 1 - properties path
	public static void main(String[] args) {
		// INIT LISTENERS
		// START CLI

		Stream.of(args).forEach(System.out::println);

        NodeConfig.readConfig(args[1]);
        NodeConfig.setNodePort(args[0]);

		Log.info(NodeConfig.bootstrap()+"");
		Log.info(NodeConfig.nodePort()+"");

//		NodeListener simpleListener = new NodeListener();
//		Thread listenerThread = new Thread(simpleListener);
//		listenerThread.start();
//
//		CLIParser cliParser = new CLIParser();
//		Thread cliThread = new Thread(cliParser);
//		cliThread.start();
		
	}
}
