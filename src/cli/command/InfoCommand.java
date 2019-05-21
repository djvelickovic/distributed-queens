package cli.command;

import app.Config;

public class InfoCommand implements CLICommand {

	@Override
	public String commandName() {
		return "info";
	}

	@Override
	public void execute(String args) {
		Config.timestampedStandardPrint("My info: " + Config.myServentInfo);
		Config.timestampedStandardPrint("Neighbors:");
		String neighbors = "";
		for (Integer neighbor : Config.myServentInfo.getNeighbors()) {
			neighbors += neighbor + " ";
		}
		
		Config.timestampedStandardPrint(neighbors);
	}

}
