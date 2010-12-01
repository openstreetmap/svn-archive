package org.openstreetmap.josm.plugins.imagery.wms.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.io.FileImporter;

public class WMSLayerImporter extends FileImporter{

    public WMSLayerImporter() {
        super(new ExtensionFileFilter("wms", "wms", tr("WMS Files (*.wms)")));
    }

}
