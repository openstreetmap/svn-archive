// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

public final class OsmDownloader {

    private OsmDownloader() {
        // Hide default constructor for utilities classes
    }

    public static void downloadOapi(String oapiReq) {
        if (oapiReq != null) {
            try {
                String oapiServer = Config.getPref().get(OdConstants.PREF_OAPI, OdConstants.DEFAULT_OAPI);
                Logging.info(oapiReq);
                String oapiReqEnc = URLEncoder.encode(oapiReq, OdConstants.UTF8);
                MainApplication.getMenu().openLocation.openUrl(false, oapiServer+"data="+oapiReqEnc);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadXapi(Collection<String> xapiReqs) {
        if (xapiReqs != null) {
            String xapiServer = Config.getPref().get(OdConstants.PREF_XAPI, OdConstants.DEFAULT_XAPI);
            for (String xapiReq : xapiReqs) {
                MainApplication.getMenu().openLocation.openUrl(false, xapiServer+xapiReq);
            }
        }
    }
}
