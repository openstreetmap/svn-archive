// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.javafx;

import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * OpenJFX plugin brings OpenJFX (JavaFX) to other plugins.
 */
public class JavaFxPluginOsx extends JavaFxPlugin {

    /**
     * Constructs a new {@code OpenJfxPlugin}.
     * @param info plugin info
     */
    public JavaFxPluginOsx(PluginInformation info) {
        super(info, ".dylib");
    }
}
