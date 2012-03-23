//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.transport.PassageNiveauHandler;

public class DataGouvFrModule extends AbstractModule {

	public DataGouvFrModule(ModuleInformation info) {
		super(info);
        handlers.add(new Etab1er2ndDegreHandler());
        handlers.add(new EtabAEFEHandler());
        handlers.add(new BibliothequesHandler());
        handlers.add(new EtabSupHandler());
        handlers.add(new AssainissementHandler());
        handlers.add(new RegistreParcellaireHandler());
        handlers.add(new GeoFlaHandler());
        handlers.add(new PassageNiveauHandler());
        handlers.add(new ROEHandler());
        handlers.add(new ForetsPubliquesHandler());
        handlers.add(new ReservesBiologiquesHandler());
        handlers.add(new EauxDeSurfaceHandler());
        handlers.add(new InventaireForestierNationalHandler());
    }
}
