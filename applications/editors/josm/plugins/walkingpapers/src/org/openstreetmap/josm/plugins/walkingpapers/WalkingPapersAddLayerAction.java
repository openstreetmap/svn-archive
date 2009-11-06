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
        	tr("Enter a walking-papers.org URL or ID (the bit after the ?id= in the URL)"),
        		Main.pref.get("walkingpapers.last-used-id"));

        if (wpid == null || wpid.equals("")) return;

        // Grab id= from the URL if we need to, otherwise get an ID
        String mungedWpId = this.getWalkingPapersId(wpid);

        if (mungedWpId == null || mungedWpId.equals("")) return;

        // screen-scrape details about this id from walking-papers.org
        String wpUrl = Main.pref.get("walkingpapers.base-url", "http://walking-papers.org/") + "scan.php?id=" + mungedWpId;

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
        	JOptionPane.showMessageDialog(Main.parent,tr("Could not read information from walking-papers.org the id \"{0}\"", mungedWpId));
        	return;
        }


        //http://walking-papers.org/scan.php?id=rmvdr3lq
        // The server is apparently broken and returning the WpId in the URL twice
        // which makes it return errors when we fetch it.  So, strip out one of
        // the dups.  This is a hack and needs to get removed when the server
        // is fixed.
        tile = tile.replaceFirst(mungedWpId+"/"+mungedWpId, mungedWpId);
        Main.pref.put("walkingpapers.last-used-id", mungedWpId);

        Bounds b = new Bounds(new LatLon(south, west), new LatLon(north, east));
        
        WalkingPapersLayer wpl = new WalkingPapersLayer(mungedWpId, tile, b, minz, maxz);
        Main.main.addLayer(wpl);

    }

    public static String getWalkingPapersId(String wpid) {
        if (!wpid.contains("id=")) {
            return wpid;
        } else {
            // To match e.g. http://walking-papers.org/scan.php?id=53h78bbx
            final Pattern pattern = Pattern.compile("\\?id=(\\S+)");
            final Matcher matcher = pattern.matcher(wpid);
            final boolean found   = matcher.find();

            if (found) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
