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
package org.openstreetmap.josm.plugins.mapdust.util;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * This class is used for reading the configurations for the connection to the
 * MapDust service, from a configuration file.
 *
 * @author Bea
 */
public class Configuration {

    /** The instance of this singleton object type */
    private static final Configuration INSTANCE = new Configuration();

    /** The resource bundle containing properties needed at runtime */
    private static ResourceBundle properties;

    /** The MapDust URL */
    private String mapdustUrl;

    /** The MapDust api key */
    private String mapdustKey;

    /** The MapDust bug details url */
    private String mapdustBugDetailsUrl;

    /**
     * Returns the only instance of this singleton object type.
     *
     * @return the instance of the <code>Configuration</code>
     */
    public static Configuration getInstance() {
        return INSTANCE;
    }

    /**
     * Reads the configuration parameters.
     */
    private Configuration() {
        try {
            properties = ResourceBundle.getBundle("mapdust");
        } catch (RuntimeException e) {
            throw new MissingResourceException(
                    "Could not instantiate resource bundle ", "", "mapdust");
        }
        try {
            mapdustUrl = properties.getString("mapdust.url");
        } catch (RuntimeException e) {
            throw new MissingResourceException("Could not read mapdust.url ",
                    "", "mapdust");
        }
        try {
            mapdustKey = properties.getString("mapdust.key");
        } catch (RuntimeException e) {
            throw new MissingResourceException("Could not read mapdust.key ",
                    "", "mapdust");
        }
        try {
            mapdustBugDetailsUrl = properties.getString("mapdust.site");
        } catch (RuntimeException e) {
            throw new MissingResourceException("Could not read mapdust.site ",
                    "", "mapdust");
        }
    }

    /**
     * Returns the MapDust service URL
     *
     * @return the mapdustUrl
     */
    public String getMapdustUrl() {
        return mapdustUrl;
    }

    /**
     * Returns the MapDust API key
     *
     * @return the mapdustKey
     */
    public String getMapdustKey() {
        return mapdustKey;
    }

    /**
     * Returns the MapDust bug details URL
     *
     * @return the mapdustBugDetailsUrl
     */
    public String getMapdustBugDetailsUrl() {
        return mapdustBugDetailsUrl;
    }

}
