/* Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.mapdust.util.retry;


/**
 * A <code>RetryAgent</code> attempts several times to execute a given
 * operation. It can run in several modes (see {@link RetryMode} for details),
 * but no matter what mode it is in, it will run at least once and it will stop
 * trying after the first success. This class has two abstract methods which
 * have to be implemented specifically by any instance: the target method and
 * the cleanup method.
 *
 * @param <T> the type of the object returned by the target method
 * @author rrainn
 */
public abstract class RetryAgent<T> {

    /** The <code>RetrySetup</code> object  */
    private RetrySetup setup;

    /**
     * Builds a new <code>RetryAgent</code> that will run as specified by a
     * default setup.
     */
    public RetryAgent() {
        this(RetrySetup.DEFAULT);
    }

    /**
     * Builds a new <code>RetryAgent</code> that will run as specified by the
     * given <code>RetrySetup</code>.
     *
     * @param setup the metadata specifying the running parameters for this
     * <code>RetryAgent</code>
     */
    public RetryAgent(RetrySetup setup) {
        this.setup = setup;
    }

    /**
     * The target operation of the <code>RetryAgent</code>. This method will be
     * called several times (until it returns successfully, or until the number
     * of attempts has been exhausted).
     *
     * @return what this method returns must be defined by the implementer
     * @throws Exception the cases in which this method returns this mode of
     * exception must be defined by the implementer
     */
    protected abstract T target() throws Exception;

    /**
     * The cleanup operation of the <code>RetryAgent</code>. This method will be
     * called after each call of the target method.
     *
     * @throws Exception the cases in which this method returns this mode of
     * exception must be defined by the implementer
     */
    protected abstract void cleanup() throws Exception;

    /**
     * Launches the <code>RetryAgent</code>'s execution. This involves at least
     * one run of the target and cleanup methods.
     *
     * @return whatever the target method is returning
     * @throws Exception in case the target method failed on every attempt and
     * the running conditions have been exhausted
     */
    public T run() throws Exception {
        T result;
        if (setup.getMode() == RetryMode.COUNTED) {
            result = runCounted();
        } else if (setup.getMode() == RetryMode.TIMED) {
            result = runTimed();
        } else {
            throw new RuntimeException("Unsupported retry mode: '"
                    + setup.getMode() + "'");
        }
        return result;
    }

    /**
     * Launches the <code>RetryAgent</code>'s  counted execution.
     *
     * @return whatever the target method is returning
     * @throws Exception in case the target method failed on every attempt and
     * the running conditions have been exhausted
     */
    private T runCounted() throws Exception {
        T result = null;
        boolean success = false;
        int attempts = setup.getStopCondition();
        int delay = setup.getBaseDelay();
        do {
            attempts--;
            try {
                result = target();
                success = true;
            } catch (Exception e) {
                if (attempts <= 0) {
                    throw e;
                }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e1) {
                        // LOG.error(e1.getMessage(), e1);
                        // throw e;
                    }
                    delay = delay * 3 / 2;

            } finally {
                try {
                    cleanup();
                } catch (Exception e) {
                    /* if it can't be cleaned up, there's nothing to do */
                    // LOG.error("Could not clean up", e);
                }
            }
        } while (!success && attempts > 0);
        return result;
    }

    /**
     * Launches the <code>RetryAgent</code>'s  timed execution.
     *
     * @return whatever the target method is returning
     * @throws Exception in case the target method failed on every attempt and
     * the running conditions have been exhausted
     */
    private T runTimed() throws Exception {
        T result = null;
        boolean success = false;
        int delay = setup.getBaseDelay();
        int maxTime = setup.getStopCondition();
        do {
            long time = System.currentTimeMillis();
            try {
                result = target();
                success = true;
            } catch (Exception e) {
                // LOG.debug("Attempt failed after "
                // + (setup.getStopCondition() - maxTime) + " ms", e);
                if (maxTime - delay <= 0) {
                    throw e;
                }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e1) {
                        // LOG.error(e1.getMessage(), e1);
                        throw e;
                    }
                    delay = delay * 3 / 2;

            } finally {
                try {
                    cleanup();
                } catch (Exception e) {
                    /* if it can't be cleaned up, there's nothing to do */
                    // LOG.error("Could not clean up", e);
                }
            }
            time = System.currentTimeMillis() - time;
            maxTime -= time;
        } while (!success && maxTime > 0);
        return result;
    }
}
