package node;

import node.cli.CLIParser;
import node.boundary.NodeListener;

import java.util.stream.Stream;


public class NodeMain {



	public static void main(String[] args) {
		// INIT LISTENERS
		// START CLI

		Stream.of(args).forEach(System.out::println);

		
//		NodeListener simpleListener = new NodeListener();
//		Thread listenerThread = new Thread(simpleListener);
//		listenerThread.start();
//
//		CLIParser cliParser = new CLIParser();
//		Thread cliThread = new Thread(cliParser);
//		cliThread.start();
		
	}
}
