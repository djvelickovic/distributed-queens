package node;

import common.Config;
import common.NodeInfo;
import common.util.Log;

import java.util.Arrays;
import java.util.stream.Stream;


public class NodeMain {

	// arg 0 - listening address
    // arg 1 - listening port
    // arg 2 - bootstrap address
	// arg 3 - bootstrap port
	public static void main(String[] args) {
		// INIT LISTENERS
		// START CLI

		Log.debug("Arguments: " + Arrays.toString(args));

		Config.bootstrap = new NodeInfo(args[0], Integer.parseInt(args[1]));
		Config.bootstrap = new NodeInfo(args[0], Integer.parseInt(args[1]));

		Log.info("Node started...");

//		NodeListener simpleListener = new NodeListener();
//		Thread listenerThread = new Thread(simpleListener);
//		listenerThread.start();
//
//		CLIParser cliParser = new CLIParser();
//		Thread cliThread = new Thread(cliParser);
//		cliThread.start();
		
	}
}
