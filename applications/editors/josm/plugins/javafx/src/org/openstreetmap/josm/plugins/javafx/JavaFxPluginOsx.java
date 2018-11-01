// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.javafx;

import java.util.Arrays;

import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * OpenJFX plugin brings OpenJFX (JavaFX) to other plugins.
 */
public class JavaFxPluginOsx extends JavaFxPlugin {

    /**
     * Constructs a new {@code JavaFxPluginOsx}.
     * @param info plugin info
     */
    public JavaFxPluginOsx(PluginInformation info) {
        super(info, ".dylib", Arrays.asList());
    }
}
