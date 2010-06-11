package org.openstreetmap.josm.plugins.videomapping;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

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

//Here we manage properties and start the other classes
public class VideoMappingPlugin extends Plugin implements LayerChangeListener{
	  private JMenu VMenu,VDeinterlacer;
	  private GpxData GPSTrack;
	  private List<WayPoint> ls;
	  private JosmAction VAdd,VRemove,VStart,Vbackward,Vforward,Vloop;
	  private JRadioButtonMenuItem VIntBob,VIntNone,VIntLinear;
	  private JCheckBoxMenuItem VCenterIcon;
	  private JMenuItem VJumpLength,VLoopLength;
	  private GPSVideoPlayer player;
	  private PositionLayer layer;
	  

	public VideoMappingPlugin(PluginInformation info) {
		super(info);
		//Register for GPS menu
		VMenu = Main.main.menu.addMenu(" Video", KeyEvent.VK_V, Main.main.menu.defaultMenuPos,ht("/Plugin/Videomapping"));//TODO no more ugly " video" hack
		addMenuItems();
		loadSettings();
		enableControlMenus(false);
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
		VAdd= new JosmAction("Import Video","videomapping","Sync a video against this GPS track",null,false) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				copyGPSLayer();
				enableControlMenus(true);
				layer = new PositionLayer("test",ls);
				Main.main.addLayer(layer);
				/*JFileChooser fc = new JFileChooser("Open Video file");
				fc.getSelectedFile();*/
				player = new GPSVideoPlayer(new File("C:\\temp\\test.mpg"), layer.player);
				//TODO Check here if we can sync by hand
				layer.setGPSPlayer(player);
				VAdd.setEnabled(false);
				VRemove.setEnabled(true);				
			}			
		};
		VRemove= new JosmAction("Remove Video","videomapping","Removes current video from Layer",null,false) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				player.removeVideo();
			}
		};
		VStart = new JosmAction("play/pause", "audio-playpause", "starts/pauses video playback",
				Shortcut.registerShortcut("videomapping:startstop","",KeyEvent.VK_SPACE, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {								
				//video.play();				
				//video.jump(605000);
				//layer.l.jump(9*60+20);
				//layer.pause();
				player.play((9*60+20)*1000);
			}
		};
		Vbackward = new JosmAction("backward", "audio-prev", "jumps n sec back",
				Shortcut.registerShortcut("videomapping:backward","",KeyEvent.VK_NUMPAD4, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {
				player.backward();
							
			}
		};
		Vforward= new JosmAction("forward", "audio-next", "jumps n sec forward",
				Shortcut.registerShortcut("videomapping:forward","",KeyEvent.VK_NUMPAD6, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {
				player.forward();
							
			}
		};
		Vloop= new JosmAction("loop", "clock", "loops n sec around current position",
				Shortcut.registerShortcut("videomapping:loop","",KeyEvent.VK_NUMPAD5, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {
				player.loop();
							
			}
		};
		
		//now the options menu
		VCenterIcon = new JCheckBoxMenuItem(new JosmAction("Keep centered", null, "Follows the video icon automaticly",null, false) {
			
			public void actionPerformed(ActionEvent e) {
				player.setAutoCenter(VCenterIcon.isSelected()); 
							
			}
		});
		
		VJumpLength = new JMenuItem(new JosmAction("Jump length", null, "Set the length of a jump",null, false) {
			
			public void actionPerformed(ActionEvent e) {
				Object[] possibilities = {"200", "500", "1000", "2000", "10000"};
				String s = (String)JOptionPane.showInputDialog(Main.parent,"Jump in video for x ms","Jump length",JOptionPane.QUESTION_MESSAGE,null,possibilities,"1000");
				player.setJumpLength(Integer.getInteger(s));
							
			}
		});
		
		VLoopLength = new JMenuItem(new JosmAction("Loop length", null, "Set the length around a looppoint",null, false) {
			
			public void actionPerformed(ActionEvent e) {
				Object[] possibilities = {"500", "1000", "3000", "5000", "10000"};
				String s = (String)JOptionPane.showInputDialog(Main.parent,"Jump in video for x ms","Loop length",JOptionPane.QUESTION_MESSAGE,null,possibilities,"5000");
				player.setLoopLength(Integer.getInteger(s));
							
			}
		});
		
		VDeinterlacer= new JMenu("Deinterlacer");
		VIntNone= new JRadioButtonMenuItem(new JosmAction("none", null, "no deinterlacing",null, false) {
			
			public void actionPerformed(ActionEvent e) {
				player.setDeinterlacer(null);
			}
		});
		VIntBob= new JRadioButtonMenuItem(new JosmAction("bob", null, "deinterlacing using line doubling",null, false) {
			
			public void actionPerformed(ActionEvent e) {
				player.setDeinterlacer("bob");
			}
		});
		VIntLinear= new JRadioButtonMenuItem(new JosmAction("linear", null, "deinterlacing using linear interpolation",null, false) {
			
			public void actionPerformed(ActionEvent e) {
				player.setDeinterlacer("bob");
			}
		});
		VDeinterlacer.add(VIntNone);
		VDeinterlacer.add(VIntBob);
		VDeinterlacer.add(VIntLinear);
		
		VMenu.add(VAdd);		
		VMenu.add(VStart);
		VMenu.add(Vbackward);
		VMenu.add(Vforward);
		VMenu.add(Vloop);
		VMenu.addSeparator();
		VMenu.add(VCenterIcon);
		VMenu.add(VJumpLength);
		VMenu.add(VLoopLength);
		VMenu.add(VDeinterlacer);
	}
	
	
	
	//we can only work on our own layer
	private void enableControlMenus(boolean enabled)
	{
		VStart.setEnabled(enabled);
		Vbackward.setEnabled(enabled);
		Vforward.setEnabled(enabled);
		Vloop.setEnabled(enabled);
	}
	
	//load all properties
	private void loadSettings() {
		VIntNone.setSelected(true);
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
