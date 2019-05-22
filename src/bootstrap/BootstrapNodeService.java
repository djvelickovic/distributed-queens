package bootstrap;

import common.NodeInfo;
import common.util.Log;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BootstrapNodeService {

    public Set<NodeInfo> nodes;

    public BootstrapNodeService() {
        nodes = ConcurrentHashMap.newKeySet();
    }

    public void addNode(NodeInfo nodeInfo) {
        nodes.add(nodeInfo);
    }

    public NodeInfo getRandomActiveNode() {
        return nodes.stream()
                .findFirst()
                .orElse(null);
    }


    public void removeNode(NodeInfo nodeInfo) {
        Log.debug("Removing inactive nodes.");

        nodes.remove(nodeInfo);
    }
}
