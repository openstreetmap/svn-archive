package org.openstreetmap.josm.plugins.videomapping;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.*;
import org.openstreetmap.josm.plugins.videomapping.video.GPSVideoPlayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
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
	  private List<WayPoint> ls;
	  private JosmAction VAdd,VStart,Vbackward,Vforward,Vloop;
	  private GPSVideoPlayer video;
	private PositionLayer layer;
	  

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

	//register main controls
	private void addMenuItems() {
		VAdd= new JosmAction("Sync Video","videomapping","Sync a video against this GPS track",null,false) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				copyGPSLayer();
				layer = new PositionLayer("test",ls);
				Main.main.addLayer(layer);
				enableControlMenus(true);
				video = new GPSVideoPlayer(new File("C:\\temp\\test.mpg"), layer.l);
			}
		};
		VStart = new JosmAction("play/pause", "audio-playpause", "starts/pauses video playback",
				Shortcut.registerShortcut("videomapping:startstop","",KeyEvent.VK_SPACE, Shortcut.GROUP_MENU), false) {
			
			public void actionPerformed(ActionEvent e) {								
				video.play();				
				video.jump(605000);
				layer.l.jump(9*60+20);
				layer.pause();
			}
		};
		Vbackward = new JosmAction("backward", "audio-prev", "jumps n sec back",
				Shortcut.registerShortcut("videomapping:backward","",KeyEvent.VK_NUMPAD4, Shortcut.GROUP_MENU), false) {
			
			public void actionPerformed(ActionEvent e) {
				layer.backward();
							
			}
		};
		Vforward= new JosmAction("forward", "audio-next", "jumps n sec forward",
				Shortcut.registerShortcut("videomapping:forward","",KeyEvent.VK_NUMPAD6, Shortcut.GROUP_MENU), false) {
			
			public void actionPerformed(ActionEvent e) {
				layer.forward();
							
			}
		};
		Vloop= new JosmAction("loop", "clock", "loops n sec around current position",
				Shortcut.registerShortcut("videomapping:loop","",KeyEvent.VK_NUMPAD5, Shortcut.GROUP_MENU), false) {
			
			public void actionPerformed(ActionEvent e) {
				layer.loop();
							
			}
		};
		VMenu.add(VAdd);
		enableControlMenus(false);
		VMenu.add(VStart);
		VMenu.add(Vbackward);
		VMenu.add(Vforward);
		VMenu.add(Vloop);
	}
	
	
	
	//we can only move on our layer
	private void enableControlMenus(boolean enabled)
	{
		VStart.setEnabled(enabled);
		Vbackward.setEnabled(enabled);
		Vforward.setEnabled(enabled);
		Vloop.setEnabled(enabled);
	}
	
	//make a flat copy
	private void copyGPSLayer()
	{ 
		ls = new LinkedList<WayPoint>();
        for (GpxTrack trk : GPSTrack.tracks) {
            for (GpxTrackSegment segment : trk.getSegments()) {
                ls.addAll(segment.getWayPoints());
            }
        }
        Collections.sort(ls); //sort basing upon time
	}
  }
