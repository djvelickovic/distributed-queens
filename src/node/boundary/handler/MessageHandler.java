package node.boundary.handler;

import java.util.concurrent.ExecutorService;

public interface MessageHandler {
    void handle(ExecutorService executor);
}
