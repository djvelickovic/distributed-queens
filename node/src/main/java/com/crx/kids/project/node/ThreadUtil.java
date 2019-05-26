package com.crx.kids.project.node;

import java.util.function.Function;

public class ThreadUtil {

    public static <T> T loopWithResult(int timeout, Function<Loop, T> handler) {
        Loop loop = new Loop();
        while (true) {
            T result = handler.apply(loop);
            if (!loop.run) {
                return result;
            }
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static class Loop {
        private boolean run = true;

        public void stop() {
            this.run = false;
        }
    }

}
