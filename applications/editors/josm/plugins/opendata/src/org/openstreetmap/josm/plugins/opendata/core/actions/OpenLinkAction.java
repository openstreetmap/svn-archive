//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
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
package org.openstreetmap.josm.plugins.opendata.core.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.tools.OpenBrowser;

@SuppressWarnings("serial")
public class OpenLinkAction extends JosmAction implements OdConstants {

    private URL url;
    
    public OpenLinkAction(URL url, String icon24Name, String title, String description) {
        super(title, icon24Name, description, null, false);
        this.url = url;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
        	System.out.println("Opening "+url);
			OpenBrowser.displayUrl(url.toURI());
		} catch (URISyntaxException e1) {
			try {
				System.err.println(e1.getLocalizedMessage());
				int index = e1.getIndex();
				if (index > -1) {
					String s = url.toString().substring(index, index+1);
					s = url.toString().replace(s, URLEncoder.encode(s, UTF8));
					URI uri = new URI(s);
		        	System.out.println("Opening "+uri);
					OpenBrowser.displayUrl(uri);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
    }
}
