// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.openjfx;

import org.openstreetmap.josm.io.audio.AudioPlayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.openjfx.io.audio.JavaFxMediaPlayer;

/**
 * JAXB plugin brings OpenJFX (JavaFX) to other plugins.
 */
public class OpenJfxPlugin extends Plugin {

    /**
     * Constructs a new {@code OpenJfxPlugin}.
     * @param info plugin info
     */
    public OpenJfxPlugin(PluginInformation info) {
        super(info);
        AudioPlayer.setSoundPlayerClass(JavaFxMediaPlayer.class);
    }
}
