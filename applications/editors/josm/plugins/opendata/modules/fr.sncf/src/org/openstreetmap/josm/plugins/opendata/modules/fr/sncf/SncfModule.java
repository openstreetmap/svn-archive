// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.sncf;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.datasets.EquipementsHandler;

public class SncfModule extends AbstractModule {

    public SncfModule(ModuleInformation info) {
        super(info);
        handlers.add(EquipementsHandler.class);
    }
}
