// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.administration.GeoFlaHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.agriculture.RegistreParcellaireHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.culture.BibliothequesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.diplomatie.EtabAEFEHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie.AssainissementHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie.ForetsPubliquesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie.InventaireForestierNationalHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie.ReservesBiologiquesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.education.Etab1er2ndDegreHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.education.EtabSupHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.hydrologie.EauxDeSurfaceHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.hydrologie.ROEHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.transport.AeroportsHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.transport.PassageNiveauHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.transport.Route500Handler;

public class DataGouvFrModule extends AbstractModule {

    public DataGouvFrModule(ModuleInformation info) {
        super(info);
        handlers.add(Etab1er2ndDegreHandler.class);
        handlers.add(EtabAEFEHandler.class);
        handlers.add(BibliothequesHandler.class);
        handlers.add(EtabSupHandler.class);
        handlers.add(AssainissementHandler.class);
        handlers.add(RegistreParcellaireHandler.class);
        handlers.add(GeoFlaHandler.class);
        handlers.add(PassageNiveauHandler.class);
        handlers.add(ROEHandler.class);
        handlers.add(ForetsPubliquesHandler.class);
        handlers.add(ReservesBiologiquesHandler.class);
        handlers.add(EauxDeSurfaceHandler.class);
        handlers.add(InventaireForestierNationalHandler.class);
        handlers.add(AeroportsHandler.class);
        handlers.add(Route500Handler.class);
    }
}
