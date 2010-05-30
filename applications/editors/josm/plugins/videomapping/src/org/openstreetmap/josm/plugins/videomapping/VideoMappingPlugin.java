package org.openstreetmap.josm.plugins.videomapping;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.*;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.*;

import static org.openstreetmap.josm.tools.I18n.*;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;

  public class VideoMappingPlugin extends Plugin implements LayerChangeListener{
	  private JMenu VMenu;
	  private GpxData GPSTrack;
	  private VideoAction VAction;
	  

	public VideoMappingPlugin(PluginInformation info) {
		super(info);
		//Register for GPS menu
		VMenu = Main.main.menu.addMenu(" Video", KeyEvent.VK_V, Main.main.menu.defaultMenuPos,ht("/Plugin/Videomapping"));//TODO no more ugly " video" hack
		VMenu.setEnabled(false); //enabled only on GPS Layers
		VAction = new VideoAction();
		VMenu.add(VAction);
		//setup
		MapView.addLayerChangeListener(this);
		//plugin informations are provided by build.xml properties
	}
		
	
	//only used with GPS layers
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (newLayer instanceof GpxLayer)
		{
			VMenu.setEnabled(true);
			GPSTrack=((GpxLayer) newLayer).data;			
			VAction.setGps(GPSTrack);
			//TODO append to GPS Layer menu
			
		}
		else VMenu.setEnabled(false);
		
	}

	public void layerAdded(Layer arg0) {
		activeLayerChange(null,arg0);		
	}

	public void layerRemoved(Layer arg0) {	
	}



  }
