package ch.bind.philib.lang;

public final class ThreadUtil {

    private static final PhiLog LOG = new PhiLog(ThreadUtil.class);

    public static final long DEFAULT_WAIT_TIME_MS = 1000L;

    public static boolean interruptAndJoin(Thread t) {
        return interruptAndJoin(t, DEFAULT_WAIT_TIME_MS);
    }

    public static boolean interruptAndJoin(Thread t, long waitTime) {
        if (t == null)
            return true;
        if (!t.isAlive())
            return true;

        t.interrupt();
        try {
            t.join(waitTime);
        } catch (InterruptedException e) {
            LOG.warn("interrupted while waiting for a thread to finish: " + e.getMessage(), e);
        }
        if (t.isAlive()) {
            LOG.warn("thread is still alive: " + t.getName());
            return false;
        } else {
            return true;
        }
    }
}
