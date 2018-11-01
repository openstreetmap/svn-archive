// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.javafx;

import java.util.Arrays;

import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * OpenJFX plugin brings OpenJFX (JavaFX) to other plugins.
 */
public class JavaFxPluginWindows extends JavaFxPlugin {

    /**
     * Constructs a new {@code JavaFxPluginWindows}.
     * @param info plugin info
     */
    public JavaFxPluginWindows(PluginInformation info) {
        super(info, ".dll", Arrays.asList("fxplugins.dll"));
    }
}
