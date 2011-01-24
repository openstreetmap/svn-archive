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
 * Defined metadata for the running method used by a {@link RetryAgent}.
 *
 * @author rrainn
 */
public class RetrySetup {

    /** A default configuration for the {@link RetryAgent} */
    public static final RetrySetup DEFAULT = new RetrySetup(RetryMode.COUNTED,
            1, 500);

    /** The retry mode */
    private final RetryMode mode;

    /** The stop condition */
    private final int stopCondition;

    /** The delay */
    private final int baseDelay; // milliseconds

    /**
     * Builds a new <code>RetrySetup</code> with the specified values.
     *
     * @param mode the method for computing time intervals between attempts
     * @param stopCondition the value at which to cease attempts
     * @param baseDelay the base delay between attempts
     * @see RetrySetup#getMode()
     * @see RetrySetup#getStopCondition()
     * @see RetrySetup#getBaseDelay()
     */
    public RetrySetup(RetryMode mode, int stopCondition, int baseDelay) {
        this.mode = mode;
        this.stopCondition = stopCondition;
        this.baseDelay = baseDelay;
    }

    /**
     * Returns the method for computing time intervals between attempts.
     *
     * @see RetryMode
     * @return the retry mode
     */
    public RetryMode getMode() {
        return mode;
    }

    /**
     * Returns the value at which new attempts are ceased, intrerpreted either
     * as 'maximum number of attempts' (for <code>COUNTED</code> mode) or as
     * 'maximum number of milliseconds' (for <code>TIMED</code> mode).
     *
     * @return the stop condition
     */
    public int getStopCondition() {
        return stopCondition;
    }

    /**
     * Returns the base delay in milliseconds between attempts. Each delay will
     * be computed based on this basic value, which will be augmented after each
     * attempt.
     *
     * @return the base delay between attempts, in milliseconds
     */
    public int getBaseDelay() {
        return baseDelay;
    }
}
