// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.cg41;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.environnement.ZonesInondablesBrayeHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.transport.ArretsBusHandler;

public class Cg41Module extends AbstractModule {

    public Cg41Module(ModuleInformation info) {
        super(info);
        handlers.add(ArretsBusHandler.class);
        handlers.add(ZonesInondablesBrayeHandler.class);
    }
}
