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
package org.openstreetmap.josm.plugins.mapdust.gui.value;


/**
 * Enumeration representing the MapDust plugin state. The plugin can be in the
 * following 2 states: online and offline.
 *
 * @author Bea
 */
public enum MapdustPluginState {

    /** The online state */
    ONLINE("online"),

    /** The offline state */
    OFFLINE("offline");

    /** The value of the state */
    private String value;

    /**
     * Builds a new <code>ActivationStatus</code> with the given value
     *
     * @param value The value of the object
     */
    private MapdustPluginState(String value) {
        this.value = value;
    }

    /**
     * Returns the value of the payment status
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a <code>MapdustPluginState</code> object for the given type. If
     * the type is invalid, the method return null.
     * @param type The type
     * @return A <code>MapdustPluginState</code> object.
     */
    public static MapdustPluginState getMapdustPluginState(String type) {
        MapdustPluginState[] pluginStates = values();
        if (pluginStates != null) {
            for (MapdustPluginState state : pluginStates) {
                if (state.getValue().equalsIgnoreCase(type)) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":'" + getValue() + "'";
    }

}
