// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public final class OsmDownloader {

    private OsmDownloader() {
        // Hide default constructor for utilities classes
    }

    public static void downloadOapi(String oapiReq) {
        if (oapiReq != null) {
            try {
                String oapiServer = Main.pref.get(OdConstants.PREF_OAPI, OdConstants.DEFAULT_OAPI);
                Main.info(oapiReq);
                String oapiReqEnc = URLEncoder.encode(oapiReq, OdConstants.UTF8);
                Main.main.menu.openLocation.openUrl(false, oapiServer+"data="+oapiReqEnc);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadXapi(Collection<String> xapiReqs) {
        if (xapiReqs != null) {
            String xapiServer = Main.pref.get(OdConstants.PREF_XAPI, OdConstants.DEFAULT_XAPI);
            for (String xapiReq : xapiReqs) {
                Main.main.menu.openLocation.openUrl(false, xapiServer+xapiReq);
            }
        }
    }
}
