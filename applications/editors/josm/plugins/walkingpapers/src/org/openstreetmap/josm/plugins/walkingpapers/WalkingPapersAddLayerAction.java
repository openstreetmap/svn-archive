package org.openstreetmap.josm.plugins.walkingpapers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;

@SuppressWarnings("serial")
public class WalkingPapersAddLayerAction extends JosmAction {

    public WalkingPapersAddLayerAction() {
        super(tr("Scanned Map..."), "walkingpapers", 
        	tr("Display a map that was previously scanned and uploaded to walking-papers.org"), null, false);
    }

    public void actionPerformed(ActionEvent e) {
        String wpid = JOptionPane.showInputDialog(Main.parent, 
        	tr("Image id from walking-papers.org (the bit after the ?id= in the URL)"),
        		Main.pref.get("walkingpapers.last-used-id"));

        if (wpid == null || wpid.equals("")) return;

        // screen-scrape details about this id from walking-papers.org
        String wpUrl = "http://walking-papers.org/scan.php?id=" + wpid;

        Pattern spanPattern = Pattern.compile("<span class=\"(\\S+)\">(\\S+)</span>");
        Matcher m;
        
        double north = 0;
        double south = 0;
        double east = 0;
        double west = 0;
        int minz = -1;
        int maxz = -1;
        String tile = null;

        try {
        	BufferedReader r = new BufferedReader(new InputStreamReader(new URL(wpUrl).openStream(), "utf-8"));
        	for (String line = r.readLine(); line != null; line = r.readLine()) {
        		m = spanPattern.matcher(line);
        		if (m.find()) {
        			if ("tile".equals(m.group(1))) tile = m.group(2);
        			else if ("north".equals(m.group(1))) north = Double.parseDouble(m.group(2));
        			else if ("south".equals(m.group(1))) south = Double.parseDouble(m.group(2));
        			else if ("east".equals(m.group(1))) east = Double.parseDouble(m.group(2));
        			else if ("west".equals(m.group(1))) west = Double.parseDouble(m.group(2));
        			else if ("minzoom".equals(m.group(1))) minz = Integer.parseInt(m.group(2));
        			else if ("maxzoom".equals(m.group(1))) maxz = Integer.parseInt(m.group(2));
        		}
        	}
        	r.close();
        	if ((tile == null) || (north == 0 && south == 0) || (east == 0 && west == 0)) throw new Exception();
        } catch (Exception ex) {
        	JOptionPane.showMessageDialog(Main.parent,tr("Could not read information from walking-papers.org for this id."));
        	return;
        }

        Main.pref.put("walkingpapers.last-used-id", wpid);

        Bounds b = new Bounds(new LatLon(south, west), new LatLon(north, east));
        
        WalkingPapersLayer wpl = new WalkingPapersLayer(wpid, tile, b, minz, maxz);
        Main.main.addLayer(wpl);

    }

}
