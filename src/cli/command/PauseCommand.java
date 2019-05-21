package cli.command;

import app.Config;

public class PauseCommand implements CLICommand {

	@Override
	public String commandName() {
		return "pause";
	}

	@Override
	public void execute(String args) {
		int timeToSleep = -1;
		
		try {
			timeToSleep = Integer.parseInt(args);
			
			if (timeToSleep < 0) {
				throw new NumberFormatException();
			}

			Config.timestampedStandardPrint("Pausing for " + timeToSleep + " ms");
			try {
				Thread.sleep(timeToSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} catch (NumberFormatException e) {
			Config.timestampedErrorPrint("Pause command should have one int argument, which is time in ms.");
		}
	}

}
