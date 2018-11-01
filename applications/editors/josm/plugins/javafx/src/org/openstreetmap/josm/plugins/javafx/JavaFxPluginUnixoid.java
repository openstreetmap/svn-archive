// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.javafx;

import java.util.Arrays;

import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * OpenJFX plugin brings OpenJFX (JavaFX) to other plugins.
 */
public class JavaFxPluginUnixoid extends JavaFxPlugin {

    /**
     * Constructs a new {@code JavaFxPluginUnixoid}.
     * @param info plugin info
     */
    public JavaFxPluginUnixoid(PluginInformation info) {
        super(info, ".so", Arrays.asList());
    }
}
