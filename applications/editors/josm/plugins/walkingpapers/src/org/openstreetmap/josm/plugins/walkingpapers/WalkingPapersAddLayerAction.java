package org.openstreetmap.josm.plugins.walkingpapers;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.tools.OsmUrlToBounds;

public class WalkingPapersAddLayerAction extends JosmAction {

    public WalkingPapersAddLayerAction() {
        super(tr("Scanned Map..."), "walkingpapers", 
        	tr("Display a map that was previously scanned and uploaded to walking-papers.org"), null, false);
    }

    public void actionPerformed(ActionEvent e) {
        String wpid = JOptionPane.showInputDialog(Main.parent, 
        	tr("Image id from walking-papers.org"),
        	Main.pref.get("walkingpapers.last-used-id"));

        if (wpid == null || wpid.equals("")) return;

        // screen-scrape details about this id from walking-papers.org
        String wpUrl = "http://walking-papers.org/scan.php?id=" + wpid;

        Pattern locationPattern = 
        	Pattern.compile("<a id=\"print-location\" href=\"(http://www.openstreetmap.org/[^\"]+)\"");
        Pattern hiddenFieldPattern = 
        	Pattern.compile("<input name=\"(\\S+)\" type=\"hidden\" value=\"(.*)\" />");
        Matcher m;
        
        Bounds b = null;
        int minx = -1;
        int maxx = -1;
        int miny = -1;
        int maxy = -1;
        int minz = -1;
        int maxz = -1;

        try {
        	BufferedReader r = new BufferedReader(new InputStreamReader(new URL(wpUrl).openStream(), "utf-8"));
        	for (String line = r.readLine(); line != null; line = r.readLine()) {
        		m = locationPattern.matcher(line);
        		if (m.find()) {
        			String escapedUrl = m.group(1);
        			b = OsmUrlToBounds.parse(escapedUrl.replace("&amp;","&"));
        		} else {
        			m = hiddenFieldPattern.matcher(line);
        			if (m.find()) {
        				if ("maxrow".equals(m.group(1))) maxy = (int) Double.parseDouble(m.group(2));
        				else if ("maxcolumn".equals(m.group(1))) maxx = (int) Double.parseDouble(m.group(2));
        				else if ("minrow".equals(m.group(1))) miny = (int) Double.parseDouble(m.group(2));
        				else if ("mincolumn".equals(m.group(1))) minx = (int) Double.parseDouble(m.group(2));
        				else if ("minzoom".equals(m.group(1))) minz = Integer.parseInt(m.group(2));
        				else if ("maxzoom".equals(m.group(1))) maxz = Integer.parseInt(m.group(2));
        			}
        		}
        	}
        	r.close();
        	if ((b == null) || (minx < 0)) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Main.parent,tr("Could not read information from walking-papers.org for this id."));
            return;
        }
        
        // FIXME min/max values are not any good, they just indicate the centre tile x/y for the 
        // minimum and the maximum zoom lvl but not how many tiles there are...
        WalkingPapersLayer wpl = new WalkingPapersLayer(wpid, b, /* minx, maxx, miny, maxy, */ minz, maxz);
        Main.main.addLayer(wpl);

    }

}
