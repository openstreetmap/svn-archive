// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.opendata.core.io.NetworkReader;
import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.associations.Club3eAgeHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete.BureauxVoteDecoupageHandler;
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
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.services.MarchesPleinVentHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.services.PointsLumineuxHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.sport.InstallationSportiveBalmaHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.sport.InstallationSportiveToulouseHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.topographie.AltimetrieVoieHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.AiresPietonnesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.ChantiersLineairesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.ChantiersPonctuelsHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.HorodateurHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.MetroStationHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.PMRHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.PistesCyclablesHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.ReseauTisseoHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport.StationsAutoPartageHandler;
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
        handlers.add(BureauxVoteDecoupageHandler.class);
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
        handlers.add(StationsAutoPartageHandler.class);
        handlers.add(MarchesPleinVentHandler.class);
        handlers.add(AiresPietonnesHandler.class);
        handlers.add(PointsLumineuxHandler.class);
    }
    
    public static final DataSet data = new DataSet();
    
    private static final Collection<Relation> getBoundaries(int admin_level) {
        Collection<Relation> result = new TreeSet<>(new Comparator<Relation>() {
            @Override
            public int compare(Relation o1, Relation o2) {
                if (o1.hasKey("name") && o2.hasKey("name")) {
                    return o1.get("name").compareTo(o2.get("name"));
                } else if (o1.hasKey("ref") && o2.hasKey("ref")) {
                    return o1.get("ref").compareTo(o2.get("ref"));
                } else {
                    return o1.get("description").compareTo(o2.get("description"));
                }
            }
        });
        synchronized (data) {
            for (Relation r : data.getRelations()) {
                if (r.hasTag("admin_level", Integer.toString(admin_level)) && 
                        (r.hasKey("name") || r.hasKey("ref") || r.hasKey("description"))) {
                    result.add(r);
                }
            }
        }
        return result;
    }
    
    public static final void downloadData() {
        synchronized (data) {
            if (data.allPrimitives().isEmpty()) {
                for (final ToulouseDataSetHandler handler : new ToulouseDataSetHandler[]{
                        new CommuneHandler(), new SecteursHandler(), new QuartiersHandler()}) {
                    Main.worker.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DataSet ds = new NetworkReader(handler.getDataURL().toString(), handler, false).
                                        parseOsm(NullProgressMonitor.INSTANCE);
                                handler.updateDataSet(ds);
                                synchronized (data) {
                                    data.mergeFrom(ds);
                                }
                            } catch (OsmTransferException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }
    
    public static final Collection<Relation> getMunicipalities() {
        return getBoundaries(8);
    }
    
    public static final Collection<Relation> getSectors() {
        return getBoundaries(10);
    }

    public static final Collection<Relation> getNeighbourhoods() {
        return getBoundaries(11);
    }
}
