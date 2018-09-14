// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.Action;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.OpenBrowser;

public class OpenLinkAction extends JosmAction {

    private URL url;

    public OpenLinkAction(URL url, String icon24Name, String title, String description) {
        super(title, null, description, null, false);
        putValue(Action.SMALL_ICON, OdUtils.getImageIcon(icon24Name));
        this.url = url;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Logging.info("Opening "+url);
            OpenBrowser.displayUrl(url.toURI());
        } catch (URISyntaxException e1) {
            try {
                Logging.error(e1.getLocalizedMessage());
                int index = e1.getIndex();
                if (index > -1) {
                    String s = url.toString().substring(index, index+1);
                    s = url.toString().replace(s, URLEncoder.encode(s, OdConstants.UTF8));
                    URI uri = new URI(s);
                    Logging.info("Opening "+uri);
                    OpenBrowser.displayUrl(uri);
                }
            } catch (IOException | URISyntaxException e2) {
                Logging.error(e2);
            }
        }
    }
}
