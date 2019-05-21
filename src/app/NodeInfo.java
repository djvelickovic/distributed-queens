package app;

import java.io.Serializable;
import java.util.List;

public class NodeInfo implements Serializable {

	private static final long serialVersionUID = 5304170042791281555L;
	private final String ip;
	private final int port;

	public NodeInfo(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
