package app;

import cli.CLIParser;
import servent.SimpleServentListener;


public class NodeMain {

	public static void main(String[] args) {
		// INIT LISTENERS
		// START CLI
		
		SimpleServentListener simpleListener = new SimpleServentListener(snapshotCollector, spezialettiKearnsCollector);
		Thread listenerThread = new Thread(simpleListener);
		listenerThread.start();
		
		CLIParser cliParser = new CLIParser(simpleListener, snapshotCollector);
		Thread cliThread = new Thread(cliParser);
		cliThread.start();
		
	}
}
