package org.openstreetmap.josm.plugins.videomapping;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
 * @author Matthias Meißer (digi_c at arcor dot de)
 * @ released under GPL
 * This Plugin allows you to link a video against a GPS track and playback both synchronously 
 */

//Here we manage properties and start the other classes
public class VideoMappingPlugin extends Plugin implements LayerChangeListener{
	  private JMenu VMenu,VDeinterlacer;
	  private GpxData GPSTrack;
	  private List<WayPoint> ls;
	  private JosmAction VAdd,VRemove,VStart,Vbackward,Vforward,VJump,Vfaster,Vslower,Vloop;
	  private JRadioButtonMenuItem VIntBob,VIntNone,VIntLinear;
	  private JCheckBoxMenuItem VCenterIcon,VSubTitles;
	  private JMenuItem VJumpLength,VLoopLength;
	  private GPSVideoPlayer player;
	  private PositionLayer layer;
	  private final String VM_DEINTERLACER="videomapping.deinterlacer"; //where we store settings
	  private final String VM_MRU="videomapping.mru";
	  private final String VM_AUTOCENTER="videomapping.autocenter";
	  private final String VM_JUMPLENGTH="videomapping.jumplength";
	  private final String VM_LOOPLENGTH="videomapping.looplength";
	  private boolean autocenter;
	  private String deinterlacer;
	  private Integer jumplength,looplength;
	  private String mru;
	  //TODO What more to store during sessions? Size/Position
	  
	public VideoMappingPlugin(PluginInformation info) {
		super(info);
		MapView.addLayerChangeListener(this);
		//Register for GPS menu
		VMenu = Main.main.menu.addMenu(" Video", KeyEvent.VK_V, Main.main.menu.defaultMenuPos,ht("/Plugin/Videomapping"));//TODO no more ugly " video" hack
		addMenuItems();
		enableControlMenus(true);
		loadSettings();
		applySettings();
		//further plugin informations are provided by build.xml properties
	}
			
	//only use with GPS and own layers
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		System.out.println(newLayer);
		if (newLayer instanceof GpxLayer)
		{
			VAdd.setEnabled(true);
			GPSTrack=((GpxLayer) newLayer).data;			
			//TODO append to GPS Layer menu
		}
		else
		{/*
			VAdd.setEnabled(false);
			if(newLayer instanceof PositionLayer)
			{
				enableControlMenus(true);
			}
			else
			{
				enableControlMenus(false);
			}*/
		}
		
	}

	public void layerAdded(Layer arg0) {
		activeLayerChange(null,arg0);
	}

	public void layerRemoved(Layer arg0) {	
	} //well ok we have a local copy of the GPS track....

	//register main controls
	private void addMenuItems() {
		VAdd= new JosmAction(tr("Import Video"),"videomapping",tr("Sync a video against this GPS track"),null,false) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {					
					JFileChooser fc = new JFileChooser("C:\\TEMP\\");
					//fc.setSelectedFile(new File(mru));
					if(fc.showOpenDialog(Main.main.parent)!=JFileChooser.CANCEL_OPTION)
					{
						saveSettings();
						ls=copyGPSLayer(GPSTrack);
						enableControlMenus(true);
						layer = new PositionLayer(fc.getSelectedFile().getName(),ls);
						Main.main.addLayer(layer);
						player = new GPSVideoPlayer(fc.getSelectedFile(), layer.player);
						//TODO Check here if we can sync allready now
						layer.setGPSPlayer(player);
						layer.addObserver(player);
						VAdd.setEnabled(false);
						VRemove.setEnabled(true);
						player.setSubtitleAction(VSubTitles);
					}
				}
		
		};
		VRemove= new JosmAction(tr("Remove Video"),"videomapping",tr("removes current video from layer"),null,false) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				player.removeVideo();
			}
		};
		
		VStart = new JosmAction(tr("play/pause"), "audio-playpause", tr("starts/pauses video playback"),
				Shortcut.registerShortcut("videomapping:startstop","",KeyEvent.VK_SPACE, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {								
				if(player.playing()) player.pause(); else player.play();
			}
		};
		Vbackward = new JosmAction(tr("backward"), "audio-prev", tr("jumps n sec back"),
				Shortcut.registerShortcut("videomapping:backward","",KeyEvent.VK_NUMPAD4, Shortcut.GROUP_DIRECT), false) {
			
			/**
					 * 
					 */
					private static final long serialVersionUID = -1060444361541900464L;

			public void actionPerformed(ActionEvent e) {
				player.backward();
							
			}
		};
		Vbackward = new JosmAction(tr("jump"), null, tr("jumps to the entered gps time"),null, false) {			
			public void actionPerformed(ActionEvent e) {
				String s =JOptionPane.showInputDialog(tr("please enter GPS timecode"),"10:07:57");
				SimpleDateFormat format= new SimpleDateFormat("hh:mm:ss");
				Date t;
				try {
					t = format.parse(s);
					if (t!=null)
						{							
							player.jumpToGPSTime(t.getTime());
						}						
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
							
			}
		};
		Vforward= new JosmAction(tr("forward"), "audio-next", tr("jumps n sec forward"),
				Shortcut.registerShortcut("videomapping:forward","",KeyEvent.VK_NUMPAD6, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {
				player.forward();
							
			}
		};
		Vfaster= new JosmAction(tr("faster"), "audio-faster", tr("faster playback"),
				Shortcut.registerShortcut("videomapping:faster","",KeyEvent.VK_PLUS, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {
				player.faster();
							
			}
		};
		Vslower= new JosmAction(tr("slower"), "audio-slower", tr("slower playback"),
				Shortcut.registerShortcut("videomapping:slower","",KeyEvent.VK_MINUS, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {
				player.slower();
							
			}
		};
		Vloop= new JosmAction(tr("loop"), "clock", tr("loops n sec around current position"),
				Shortcut.registerShortcut("videomapping:loop","",KeyEvent.VK_NUMPAD5, Shortcut.GROUP_DIRECT), false) {
			
			public void actionPerformed(ActionEvent e) {
				player.loop();
							
			}
		};
		
		//now the options menu
		VCenterIcon = new JCheckBoxMenuItem(new JosmAction(tr("Keep centered"), "cursor/crosshair", tr("follows the video icon automaticly"),null, false) {
			
			public void actionPerformed(ActionEvent e) {
				autocenter=VCenterIcon.isSelected();
				applySettings();
				saveSettings();
							
			}
		});
		//now the options menu
		VSubTitles = new JCheckBoxMenuItem(new JosmAction(tr("Subtitles"), "cursor/crosshair", tr("Show subtitles in video"),null, false) {
			
			public void actionPerformed(ActionEvent e) {
				player.toggleSubtitles();
							
			}
		});
		
		VJumpLength = new JMenuItem(new JosmAction(tr("Jump length"), null, tr("Set the length of a jump"),null, false) {
			
			public void actionPerformed(ActionEvent e) {
				Object[] possibilities = {"200", "500", "1000", "2000", "10000"};
				String s = (String)JOptionPane.showInputDialog(Main.parent,tr("Jump in video for x ms"),tr("Jump length"),JOptionPane.QUESTION_MESSAGE,null,possibilities,jumplength);
				jumplength=Integer.getInteger(s);
				applySettings();
				saveSettings();			
			}
		});
		
		VLoopLength = new JMenuItem(new JosmAction(tr("Loop length"), null, tr("Set the length around a looppoint"),null, false) {
			
			public void actionPerformed(ActionEvent e) {
				Object[] possibilities = {"500", "1000", "3000", "5000", "10000"};
				String s = (String)JOptionPane.showInputDialog(Main.parent,tr("Jump in video for x ms"),tr("Loop length"),JOptionPane.QUESTION_MESSAGE,null,possibilities,looplength);
				looplength=Integer.getInteger(s);
				applySettings();
				saveSettings();
							
			}
		});
		
		VDeinterlacer= new JMenu("Deinterlacer");
		VIntNone= new JRadioButtonMenuItem(new JosmAction(tr("none"), null, tr("no deinterlacing"),null, false) {
			
			public void actionPerformed(ActionEvent e) {
				deinterlacer=null;
				applySettings();
				saveSettings();
			}
		});
		VIntBob= new JRadioButtonMenuItem(new JosmAction("bob", null, tr("deinterlacing using line doubling"),null, false) {
			
			public void actionPerformed(ActionEvent e) {
				deinterlacer="bob";
				applySettings();
				saveSettings();
			}
		});
		VIntLinear= new JRadioButtonMenuItem(new JosmAction("linear", null, tr("deinterlacing using linear interpolation"),null, false) {
			
			public void actionPerformed(ActionEvent e) {
				deinterlacer="linear";
				applySettings();
				saveSettings();
			}
		});
		VDeinterlacer.add(VIntNone);
		VDeinterlacer.add(VIntBob);
		VDeinterlacer.add(VIntLinear);
		
		VMenu.add(VAdd);		
		VMenu.add(VStart);
		VMenu.add(Vbackward);
		VMenu.add(Vforward);
		VMenu.add(Vfaster);
		VMenu.add(Vslower);
		VMenu.add(Vloop);
		VMenu.addSeparator();
		VMenu.add(VCenterIcon);
		VMenu.add(VJumpLength);
		VMenu.add(VLoopLength);
		VMenu.add(VDeinterlacer);
		VMenu.add(VSubTitles);
		
	}
	
	
	
	//we can only work on our own layer
	private void enableControlMenus(boolean enabled)
	{
		VStart.setEnabled(enabled);
		Vbackward.setEnabled(enabled);
		Vforward.setEnabled(enabled);
		Vloop.setEnabled(enabled);
	}
	
	//load all properties or set defaults
	private void loadSettings() {
		String temp;		
		temp=Main.pref.get(VM_AUTOCENTER);
		if((temp!=null)&&(temp.length()!=0))autocenter=Boolean.getBoolean(temp); else autocenter=false;
		temp=Main.pref.get(VM_DEINTERLACER);
		if((temp!=null)&&(temp.length()!=0)) deinterlacer=Main.pref.get(temp);
		temp=Main.pref.get(VM_JUMPLENGTH);
		if((temp!=null)&&(temp.length()!=0)) jumplength=Integer.valueOf(temp); else jumplength=1000; 
		temp=Main.pref.get(VM_LOOPLENGTH);
		if((temp!=null)&&(temp.length()!=0)) looplength=Integer.valueOf(temp); else looplength=6000;
		temp=Main.pref.get(VM_MRU);
		if((temp!=null)&&(temp.length()!=0)) mru=Main.pref.get(VM_MRU);else mru=System.getProperty("user.home");
	}
	
	private void applySettings(){
		//Internals
		if(player!=null)
		{
			player.setAutoCenter(autocenter);
			player.setDeinterlacer(deinterlacer);
			player.setJumpLength(jumplength);
			player.setLoopLength(looplength);
		}
		//GUI
		VCenterIcon.setSelected(autocenter);
		VIntNone.setSelected(true);
		if(deinterlacer=="bob")VIntBob.setSelected(true);
		if(deinterlacer=="linear")VIntLinear.setSelected(true);
		
	}
	
	private void saveSettings(){
		Main.pref.put(VM_AUTOCENTER, autocenter);
		Main.pref.put(VM_DEINTERLACER, deinterlacer);
		Main.pref.put(VM_JUMPLENGTH, jumplength.toString());
		Main.pref.put(VM_LOOPLENGTH, looplength.toString());
		Main.pref.put(VM_MRU, mru);
	}
	
	//make a flat copy
	private List<WayPoint> copyGPSLayer(GpxData route)
	{ 
		ls = new LinkedList<WayPoint>();
        for (GpxTrack trk : route.tracks) {
            for (GpxTrackSegment segment : trk.getSegments()) {
                ls.addAll(segment.getWayPoints());
            }
        }
        Collections.sort(ls); //sort basing upon time
        return ls;
	}
  }
