// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.NeptuneReader;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class ReseauTisseoHandler extends ToulouseDataSetHandler {

    private static final URL neptuneSchemaUrl = ReseauTisseoHandler.class.getResource(TOULOUSE_NEPTUNE_XSD);
    
    public ReseauTisseoHandler() {
        super(14022, "network=fr_tisseo");
        NeptuneReader.registerSchema(neptuneSchemaUrl);
        setName("Réseau Tisséo (Métro, Bus, Tram)");
        setCategory(CAT_TRANSPORT);
        getArchiveHandler().setSkipXsdValidation(true);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsZipFilename(filename, "14022-reseau-tisseo-metro-bus-tram-") || filename.toLowerCase().endsWith(OdConstants.XML_EXT);
    }
    
    @Override
    public boolean acceptsFile(File file) {
        return acceptsFilename(file.getName()) 
                && (file.getName().toLowerCase().endsWith(OdConstants.ZIP_EXT) || NeptuneReader.acceptsXmlNeptuneFile(file, neptuneSchemaUrl));
    }

    @Override
    public String getSource() {
        return SOURCE_TISSEO;
    }

    @Override
    public URL getWikiURL() {
        try {
            return new URL("http://wiki.openstreetmap.org/wiki/Toulouse/Transports_en_commun#Réseau_Tisséo");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (OsmPrimitive p : ds.allPrimitives()) {
            p.put("operator", "Tisséo");
            p.put("network", "fr_tisseo");
        }
    }
}
