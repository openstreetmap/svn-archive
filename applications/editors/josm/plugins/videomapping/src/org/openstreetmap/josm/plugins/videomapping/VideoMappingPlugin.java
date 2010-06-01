package org.openstreetmap.josm.plugins.videomapping;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.*;
import org.openstreetmap.josm.plugins.videomapping.actions.StartStopAction;
import org.openstreetmap.josm.plugins.videomapping.actions.VideoAddAction;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.*;

import static org.openstreetmap.josm.tools.I18n.*;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;

  /**
 * @author Matthias Meiﬂer
 * @ released under GPL
 * This Plugin allows you to link a video against a GPS track and playback both synchronously 
 */
public class VideoMappingPlugin extends Plugin implements LayerChangeListener{
	  private JMenu VMenu;
	  private GpxData GPSTrack;
	  private VideoAddAction VAdd;
	  private StartStopAction VStart;
	  

	public VideoMappingPlugin(PluginInformation info) {
		super(info);
		//Register for GPS menu
		VMenu = Main.main.menu.addMenu(" Video", KeyEvent.VK_V, Main.main.menu.defaultMenuPos,ht("/Plugin/Videomapping"));//TODO no more ugly " video" hack		 
		addMenuItems();
		//setup
		MapView.addLayerChangeListener(this);
		//further plugin informations are provided by build.xml properties
	}	
			
	//only use with GPS and own layers
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (newLayer instanceof GpxLayer)
		{
			VAdd.setEnabled(true);
			GPSTrack=((GpxLayer) newLayer).data;			
			VAdd.setGps(GPSTrack);
			//TODO append to GPS Layer menu
		}
		else
		{
			VAdd.setEnabled(false);
			if(newLayer instanceof PositionLayer)
			{
				enableControlMenus(true);
			}
			else
			{
				enableControlMenus(false);
			}
		}
		
	}

	public void layerAdded(Layer arg0) {
		activeLayerChange(null,arg0);
	}

	public void layerRemoved(Layer arg0) {	
	} //well ok we have a local copy of the GPS track....

	private void addMenuItems() {
		VAdd= new VideoAddAction(this);
		VStart = new StartStopAction();
		VMenu.add(VAdd);
		enableControlMenus(false);
		VMenu.add(VStart);
	}
	
	public void setMyLayer(PositionLayer layer)
	{		
		VStart.setLayer(layer);
		enableControlMenus(true);
	}
	
	private void enableControlMenus(boolean enabled)
	{
		VStart.setEnabled(enabled);
	}
  }
