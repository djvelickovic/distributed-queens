package servent.handler;

/**
 * Currently merely a decorative interface. Might get more
 * substantial in the future.
 * @author bmilojkovic
 *
 */
public abstract class MessageHandler implements Runnable {

    public void syncHandle() {

    }

    public void asyncHandle() {

    }

    @Override
    public void run() {
        asyncHandle();
    }
}
