// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets;

import java.net.MalformedURLException;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.DataGouvFrConstants;

public abstract class DataGouvDataSetHandler extends FrenchDataSetHandler implements DataGouvFrConstants {
    
    public DataGouvDataSetHandler() {
        init(null, null);
    }

    public DataGouvDataSetHandler(String portalPath) {
        init(portalPath, null);
    }

    public DataGouvDataSetHandler(String portalPath, Projection singleProjection) {
        init(portalPath, singleProjection);
    }

    public DataGouvDataSetHandler(String portalPath, Projection singleProjection, String relevantTag) {
        super(relevantTag);
        init(portalPath, singleProjection);
    }

    public DataGouvDataSetHandler(String portalPath, String relevantTag) {
        super(relevantTag);
        init(portalPath, null);
    }

    private void init(String portalPath, Projection singleProjection) {
        setNationalPortalPath(portalPath);
        setSingleProjection(singleProjection);
        setLicense(License.LOOL);
    }

    @Override
    public String getSource() {
        return SOURCE_DATAGOUVFR;
    }
    
    protected final void setDownloadFileName(String filename) {
        try {
            setDataURL(FRENCH_PORTAL+"var/download/"+filename);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
