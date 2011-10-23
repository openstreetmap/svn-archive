package org.openstreetmap.josm.plugins.tag2link.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.tag2link.Tag2LinkConstants;
import org.openstreetmap.josm.plugins.tag2link.data.Link;
import org.openstreetmap.josm.tools.OpenBrowser;

@SuppressWarnings("serial")
public class OpenLinkAction extends JosmAction implements Tag2LinkConstants {

    private String url;
    
    public OpenLinkAction(Link link) {
        super(tr(link.name), ICON_24, tr("Launch browser with information about the selected object"), null, false);
        this.url = link.url;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	if (url.matches("mailto:.*")) {
        	if (Desktop.isDesktopSupported()) {
        		try {
        			System.out.println("Sending "+url);
					Desktop.getDesktop().mail(new URI(url));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
        	}
    	}
    	System.out.println("Opening "+url);
        OpenBrowser.displayUrl(url);
    }
}
