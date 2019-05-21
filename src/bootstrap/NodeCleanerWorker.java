package bootstrap;

import common.util.Cancellable;
import common.util.Log;

public class NodeCleanerWorker implements Runnable, Cancellable {

    private volatile boolean run = true;
    private BootstrapNodeService bootstrapNodeService;

    public NodeCleanerWorker(BootstrapNodeService bootstrapNodeService) {
        this.bootstrapNodeService = bootstrapNodeService;
    }

    public void run() {
        while (run) {
            try {
                Log.debug("Cleaner worker started cycle.");

                bootstrapNodeService.removeInactiveNodes();
                bootstrapNodeService.sendHeartbeatMessages();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            catch (Exception e) {
                Log.error("error while cleaning nodes: "+e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        run = false;
    }
}
