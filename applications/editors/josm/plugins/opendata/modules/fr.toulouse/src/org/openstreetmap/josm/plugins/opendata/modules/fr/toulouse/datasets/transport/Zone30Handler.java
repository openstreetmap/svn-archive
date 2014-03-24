// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme.VoirieHandler;

public class Zone30Handler extends VoirieHandler {
    
    public Zone30Handler() {
        super(12548, "Street", "maxspeed=30");
        setWikiPage("Zones 30");
        setCategory(CAT_TRANSPORT);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Zone30");
    }
    
    @Override
    public void updateDataSet(DataSet ds) {
        super.updateDataSet(ds);
        
        for (Way w : ds.getWays()) {
            w.put("zone:maxspeed", "FR:30");
            w.put("maxspeed", "30");
            replace(w, "SensUnique", "oneway", new String[]{"oui", "non"}, new String[]{"yes", "no"});
            replace(w, "Annee", "start_date");
            replace(w, "Longueur", "length");
            w.remove("code_insee");
            w.remove("commune");
            w.remove("MotDir");
        }
    }

    @Override
    protected String getStreetId(Way w) {
        return w.get("code_insee")+"/"+w.get("name");
    }
}
