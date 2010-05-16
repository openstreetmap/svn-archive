package org.openstreetmap.josm.plugins.videomapping;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;


import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.*;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.tools.DateUtils;
import org.openstreetmap.josm.tools.Shortcut;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.*;

public class VideoAction extends JosmAction {

	private GpxData gps;
	private DataSet ds; //all extracted GPS points
	private List<WayPoint> ls;

	public VideoAction() {
		super("Sync Video","videomapping","Sync a video against this GPS track",null,true);
	}

	// Choose a file
	public void actionPerformed(ActionEvent arg0) {
	
		copyGPSLayer();
		Main.main.addLayer(new PositionLayer("test",ds));
	}
		
	
	public void setGps(GpxData gps) {
		this.gps = gps;
	}
	
	//makes a private flat copy for interaction
	private void copyGPSLayer()
	{
		//TODO we assume that GPS points are in the correct order! 
		ds = new DataSet();
            for (GpxTrack trk : gps.tracks) {
                for (GpxTrackSegment segment : trk.getSegments()) {
                    Way w = new Way();
                    for (WayPoint p : segment.getWayPoints()) {
                        Node n = new Node(p.getCoor());
                        String timestr = p.getString("time");
                        if(timestr != null)
                            n.setTimestamp(DateUtils.fromString(timestr));
                        ds.addPrimitive(n);
                        w.addNode(n);
                        //ls.add
                    }
                    ds.addPrimitive(w);
                }
            }
	}

}
