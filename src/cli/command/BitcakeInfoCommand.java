package cli.command;

import app.Config;
import app.snapshot_bitcake.SnapshotCollector;

public class BitcakeInfoCommand implements CLICommand {

	private SnapshotCollector collector;
	
	public BitcakeInfoCommand(SnapshotCollector collector) {
		this.collector = collector;
	}
	
	@Override
	public String commandName() {
		return "bitcake_info";
	}

	@Override
	public void execute(String args) {
		if (!Config.myServentInfo.isInitiator()){
			Config.timestampedStandardPrint("Servent is not initiator. Cannot do bitcake_info");
			return;
		}

		collector.startInitiatorCollecting();

	}

}
