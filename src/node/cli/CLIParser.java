package node.cli;

import common.Cancellable;
import common.Log;
import node.cli.command.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class CLIParser implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	private final List<CLICommand> commandList;
	
	public CLIParser() {
		this.commandList = new ArrayList<>();
		
		commandList.add(new ResultCommand());
		commandList.add(new PauseCommand());
		commandList.add(new StartCommand());
		commandList.add(new StopCommand());
		commandList.add(new StatusCommand());
	}
	
	@Override
	public void run() {

		try (Scanner sc = new Scanner(System.in)) {

			while (working) {
				String commandLine = sc.nextLine();

				int spacePos = commandLine.indexOf(" ");

				String commandName = null;
				String commandArgs = null;
				if (spacePos != -1) {
					commandName = commandLine.substring(0, spacePos);
					commandArgs = commandLine.substring(spacePos + 1, commandLine.length());
				} else {
					commandName = commandLine;
				}

				boolean found = false;

				for (CLICommand cliCommand : commandList) {
					if (cliCommand.commandName().equals(commandName)) {
						cliCommand.execute(commandArgs);
						found = true;
						break;
					}
				}

				if (!found) {
					Log.error("Unknown command: " + commandName);
				}
			}
		}
	}
	
	@Override
	public void stop() {
		this.working = false;
	}
}
