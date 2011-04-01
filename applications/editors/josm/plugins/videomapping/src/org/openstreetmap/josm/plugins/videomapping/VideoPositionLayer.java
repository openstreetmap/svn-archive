package org.openstreetmap.josm.plugins.videomapping;


import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.videomapping.video.GPSVideoPlayer;

//Basic rendering and GPS layer interaction
public class VideoPositionLayer extends Layer implements MouseListener,MouseMotionListener {

	public VideoPositionLayer(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getInfoComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action[] getMenuEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMergable(Layer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mergeFrom(Layer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paint(Graphics2D arg0, MapView arg1, Bounds arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
    
}
