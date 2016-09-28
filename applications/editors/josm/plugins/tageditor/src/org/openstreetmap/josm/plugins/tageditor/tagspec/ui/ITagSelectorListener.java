// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tageditor.tagspec.ui;

import org.openstreetmap.josm.data.osm.Tag;

public interface ITagSelectorListener {
    void itemSelected(Tag pair);
}
