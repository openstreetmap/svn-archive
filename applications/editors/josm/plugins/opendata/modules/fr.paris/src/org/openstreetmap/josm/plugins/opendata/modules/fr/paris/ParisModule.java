// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.environnement.ArbresRemarquablesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.environnement.MobilierVoiePubliqueHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme.EclairagePublicHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme.SanisettesHandler;

public class ParisModule extends AbstractModule {

    public ParisModule(ModuleInformation info) {
        super(info);
        handlers.add(SanisettesHandler.class);
        handlers.add(ArbresRemarquablesHandler.class);
        //handlers.add(VolumesBatisHandler.class); // Disabled as the projection cannot be transformed
        //handlers.add(VolumesNonBatisHandler.class); // Disabled as the projection cannot be transformed
        //handlers.add(ElectriciteHandler.class); // Disabled (useless for OSM)
        handlers.add(EclairagePublicHandler.class);
        handlers.add(MobilierVoiePubliqueHandler.class);
    }
}
