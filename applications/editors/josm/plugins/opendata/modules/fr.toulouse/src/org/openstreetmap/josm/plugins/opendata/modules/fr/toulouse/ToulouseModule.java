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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.associations.Club3eAgeHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete.BureauxVoteHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete.MairieAnnexeHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete.MairieHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete.PolesTerritoriauxHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete.QuartiersHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete.SecteursHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture.BibliothequesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture.EquipementCulturelBalmaHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture.LudothequeHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture.MuseeHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture.TheatreHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance.CrechesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance.EcoleBalmaHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance.EcoleElementaireHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance.EcoleMaternelleHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance.PetiteEnfanceEtJeunesseBalmaHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.environnement.RecupEmballageHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.environnement.RecupVerreHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.environnement.StationEpurationHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.patrimoine.Parcelles1680Handler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.patrimoine.Parcelles1830Handler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.sport.InstallationSportiveBalmaHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.sport.InstallationSportiveToulouseHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.topographie.AltimetrieVoieHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.ChantiersLineairesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.ChantiersPonctuelsHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.HorodateurHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.MetroStationHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.PMRHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.PistesCyclablesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.ReseauTisseoHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.TramwayStationHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.VeloToulouseHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.Zone30Handler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme.CommuneHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme.NumerosRueHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme.SanisetteHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme.VoirieHandler;

public class ToulouseModule extends AbstractModule {

    public ToulouseModule(ModuleInformation info) {
        super(info);
        handlers.add(SanisetteHandler.class);
        handlers.add(NumerosRueHandler.class);
        handlers.add(CommuneHandler.class);
        handlers.add(VoirieHandler.class);
        handlers.add(Zone30Handler.class);
        handlers.add(HorodateurHandler.class);
        handlers.add(VeloToulouseHandler.class);
        handlers.add(AltimetrieVoieHandler.class);
        handlers.add(MetroStationHandler.class);
        handlers.add(TramwayStationHandler.class);
        handlers.add(Parcelles1680Handler.class);
        handlers.add(Parcelles1830Handler.class);
        handlers.add(PMRHandler.class);
        handlers.add(PistesCyclablesHandler.class);
        handlers.add(BureauxVoteHandler.class);
        handlers.add(Club3eAgeHandler.class);
        handlers.add(CrechesHandler.class);
        handlers.add(EcoleElementaireHandler.class);
        handlers.add(EcoleMaternelleHandler.class);
        handlers.add(LudothequeHandler.class);
        handlers.add(MairieHandler.class);
        handlers.add(MairieAnnexeHandler.class);
        handlers.add(BibliothequesHandler.class);
        handlers.add(MuseeHandler.class);
        handlers.add(PolesTerritoriauxHandler.class);
        handlers.add(QuartiersHandler.class);
        handlers.add(SecteursHandler.class);
        handlers.add(StationEpurationHandler.class);
        handlers.add(TheatreHandler.class);
        handlers.add(RecupEmballageHandler.class);
        handlers.add(RecupVerreHandler.class);
        handlers.add(ReseauTisseoHandler.class);
        handlers.add(EcoleBalmaHandler.class);
        handlers.add(PetiteEnfanceEtJeunesseBalmaHandler.class);
        handlers.add(EquipementCulturelBalmaHandler.class);
        handlers.add(InstallationSportiveBalmaHandler.class);
        handlers.add(ChantiersPonctuelsHandler.class);
        handlers.add(ChantiersLineairesHandler.class);
        handlers.add(InstallationSportiveToulouseHandler.class);
    }
}
