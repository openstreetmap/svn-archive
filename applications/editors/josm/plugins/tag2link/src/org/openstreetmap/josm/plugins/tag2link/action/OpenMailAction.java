//    JOSM tag2link plugin.
//    Copyright (C) 2011 Don-vip & FrViPofm
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
