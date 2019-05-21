package node.boundary.handler;

import common.messages.Message;

import java.util.concurrent.ExecutorService;

public interface MessageHandler<T extends Message> {

    void handle(T message, ExecutorService executor);
}
