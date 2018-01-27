// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.eventbus.gui.download;

import java.util.EventObject;
import java.util.Objects;

import org.openstreetmap.josm.gui.download.DownloadSource;

/**
 * Event fired when a download source is added.
 */
public class DownloadSourceAddedEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final DownloadSource<?> downloadSource;

    /**
     * Constructs a new {@code DownloadSourceAddedEvent}.
     * @param source object on which the Event initially occurred
     * @param downloadSource new download source
     */
    public DownloadSourceAddedEvent(Object source, DownloadSource<?> downloadSource) {
        super(source);
        this.downloadSource = Objects.requireNonNull(downloadSource);
    }

    /**
     * Returns the added download source.
     * @return the added download source
     */
    public DownloadSource<?> getDownloadSource() {
        return downloadSource;
    }
}
