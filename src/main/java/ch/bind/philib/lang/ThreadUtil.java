/*
 * Copyright (c) 2011 Philipp Meinen <philipp@bind.ch>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
