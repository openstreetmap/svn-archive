// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.datasets;

import java.net.MalformedURLException;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.SncfConstants;
import org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.SncfLicense;

public abstract class SncfDataSetHandler extends FrenchDataSetHandler implements SncfConstants {
    
    public SncfDataSetHandler(String portalId) {
        init(portalId);
    }
    
    public SncfDataSetHandler(String portalId, String relevantTag) {
        super(relevantTag);
        init(portalId);
    }
    
    public SncfDataSetHandler(String portalId, boolean relevantUnion, String ... relevantTags) {
        super(relevantUnion, relevantTags);
        init(portalId);
    }

    public SncfDataSetHandler(String portalId, String ... relevantTags) {
        this(portalId, false, relevantTags);
    }
    
    public SncfDataSetHandler(String portalId, boolean relevantUnion, Tag ... relevantTags) {
        super(relevantUnion, relevantTags);
        init(portalId);
    }

    private final void init(String portalId) {
        setLicense(new SncfLicense());
        if (portalId != null && !portalId.isEmpty()) {
            try {
                setLocalPortalURL(PORTAL + portalId);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public String getLocalPortalIconName() {
        return ICON_24;
    }

    @Override
    public String getDataLayerIconName() {
        return ICON_16;
    }
}
