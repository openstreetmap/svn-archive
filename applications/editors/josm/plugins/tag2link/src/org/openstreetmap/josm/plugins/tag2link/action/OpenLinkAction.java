//    JOSM tag2link plugin.
//    Copyright (C) 2011-2012 Don-vip & FrViPofm
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

import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.tag2link.Tag2LinkConstants;
import org.openstreetmap.josm.plugins.tag2link.data.Link;
import org.openstreetmap.josm.plugins.tag2link.data.LinkPost;
import org.openstreetmap.josm.tools.OpenBrowser;

/**
 * Action allowing to open a general link.
 * @author Don-vip
 *
 */
@SuppressWarnings("serial")
public class OpenLinkAction extends JosmAction implements Tag2LinkConstants {

    private Link link;
    
    /**
     * Constructs a new {@code OpenLinkAction}.
     * @param link The link to open
     */
    public OpenLinkAction(Link link) {
        super(tr(link.name), ICON_24, tr("Launch browser with information about the selected object"), null, false);
        this.link = link;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (link instanceof LinkPost) {
            try {
                LinkPost lp = (LinkPost) link;
                System.out.println("Sending POST request to "+lp.url);
                HttpURLConnection conn = (HttpURLConnection) new URL(lp.url).openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                for (String header : lp.headers.keySet()) {
                    conn.setRequestProperty(header, lp.headers.get(header));
                }
                String data = "";
                for (String param : lp.params.keySet()) {
                    if (!data.isEmpty()) {
                        data += "&";
                    }
                    data += param+"="+lp.params.get(param);
                }
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                osw.write(data);
                osw.flush();
                osw.close();
                
                String filename = "output.pdf";// FIXME: should work for PDF files only (not even tested)
                FileOutputStream fos = new FileOutputStream(filename);
                InputStream is = conn.getInputStream();
                byte[] buffer = new byte[2048];
                int n = -1;
                while ((n = is.read(buffer)) > -1) {
                    fos.write(buffer, 0, n);
                }
                is.close();
                fos.close();
                
                System.out.println("Opening "+filename);
                String result = OpenBrowser.displayUrl("file://"+filename);
                if (result != null) {
                    System.err.println(result);
                }
                
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Opening "+link.url);
            String result = OpenBrowser.displayUrl(link.url);
            if (result != null) {
                System.err.println(result);
            }
        }
    }
}
