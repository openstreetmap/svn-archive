// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.gui.tagging.presets;

import java.util.EventObject;

/**
 * Event fired when a tagging preset is modified.
 */
public class TaggingPresetModifiedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code TaggingPresetModifiedEvent}.
     * @param source object on which the Event initially occurred
     */
    public TaggingPresetModifiedEvent(Object source) {
        super(source);
    }
}
