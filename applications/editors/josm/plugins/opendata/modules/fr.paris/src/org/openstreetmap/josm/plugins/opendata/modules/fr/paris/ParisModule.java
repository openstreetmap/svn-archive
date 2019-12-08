// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.environnement.ArbresRemarquablesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.environnement.MobilierVoiePubliqueHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme.EclairagePublicHandler;

public class ParisModule extends AbstractModule {

    public ParisModule(ModuleInformation info) {
        super(info);
        handlers.add(ArbresRemarquablesHandler.class);
        handlers.add(EclairagePublicHandler.class);
        handlers.add(MobilierVoiePubliqueHandler.class);
    }
}
