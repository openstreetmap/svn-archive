package org.openstreetmap.josm.plugins.videomapping.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.*;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.plugins.videomapping.PositionLayer;
import org.openstreetmap.josm.plugins.videomapping.VideoMappingPlugin;
import org.openstreetmap.josm.tools.DateUtils;
import org.openstreetmap.josm.tools.Shortcut;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.*;

public class VideoAddAction extends JosmAction {
	private VideoMappingPlugin plugin;
	private GpxData gps;
	private DataSet ds; //all extracted GPS points
	private List<WayPoint> ls;

	public VideoAddAction(VideoMappingPlugin plugin) {
		super("Sync Video","videomapping","Sync a video against this GPS track",null,true);
		this.plugin=plugin;
	}

	// Choose a file
	public void actionPerformed(ActionEvent arg0) {
		
		copyGPSLayer();
		PositionLayer l = new PositionLayer("test",ls);
		Main.main.addLayer(l);
		plugin.setMyLayer(l);

	}
		
	
	public void setGps(GpxData gps) {
		this.gps = gps;
	}
	
	//makes a local flat copy for interaction
	private void copyGPSLayer()
	{ 
		ls = new LinkedList<WayPoint>();
        for (GpxTrack trk : gps.tracks) {
            for (GpxTrackSegment segment : trk.getSegments()) {
                ls.addAll(segment.getWayPoints());
            }
        }
        Collections.sort(ls); //sort basing upon time
	}

}


