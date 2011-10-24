package org.openstreetmap.josm.plugins.tag2link.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.tag2link.Tag2LinkConstants;
import org.openstreetmap.josm.plugins.tag2link.data.Link;

@SuppressWarnings("serial")
public class OpenMailAction extends JosmAction implements Tag2LinkConstants {
	
    private String url;
    
    public OpenMailAction(Link link) {
        super(tr(link.name), MAIL_ICON_24, tr("Launch your default software for sending an email to the selected contact address"), null, false);
        this.url = link.url;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	if (Desktop.isDesktopSupported()) {
    		try {
    			System.out.println("Sending "+url);
				Desktop.getDesktop().mail(new URI(url));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
    	}
    }
}
