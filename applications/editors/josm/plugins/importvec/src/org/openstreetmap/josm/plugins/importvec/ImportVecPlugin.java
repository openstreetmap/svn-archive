package org.openstreetmap.josm.plugins.importvec;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class ImportVecPlugin extends Plugin {

    public ImportVecPlugin(PluginInformation info) {
        super(info);
	ExtensionFileFilter.importers.add(new SvgImporter());
    }

}
