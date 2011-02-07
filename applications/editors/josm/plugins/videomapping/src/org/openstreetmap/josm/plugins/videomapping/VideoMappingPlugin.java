package org.openstreetmap.josm.plugins.videomapping;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.text.MaskFormatter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.*;
import org.openstreetmap.josm.plugins.videomapping.video.GPSVideoPlayer;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.*;

import static org.openstreetmap.josm.tools.I18n.*;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;

  /**
 * @author Matthias Meiﬂer (digi_c at arcor dot de)
 * @ released under GPL
 * This Plugin allows you to link a video against a GPS track and playback both synchronously 
 */

//Here we manage properties and start the other classes
public class VideoMappingPlugin extends Plugin implements LayerChangeListener{
      private JMenu VMenu,VDeinterlacer;
      private GpxLayer GpsLayer;
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
      private String deinterlacer;
      private boolean autocenter;
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
            GpsLayer=((GpxLayer) newLayer);            
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
                        enableControlMenus(true);
                        layer = new PositionLayer(fc.getSelectedFile(),GpsLayer);
                        Main.main.addLayer(layer);
                        //TODO Check here if we can sync allready now
                        VAdd.setEnabled(false);
                        VRemove.setEnabled(true);
                        layer.getVideoPlayer().setSubtitleAction(VSubTitles);
                        player=layer.getVideoPlayer();
                    }
                }
        
        };
        VRemove= new JosmAction(tr("Remove Video"),"videomapping",tr("removes current video from layer"),null,false) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent arg0) {
                player.removeVideo();
            }
        };
        
        VStart = new JosmAction(tr("Play/Pause"), "audio-playpause", tr("starts/pauses video playback"),
                Shortcut.registerShortcut("videomapping:startstop","Video: "+tr("Play/Pause"),KeyEvent.VK_NUMPAD5, Shortcut.GROUP_DIRECT), false) {
            
            public void actionPerformed(ActionEvent e) {                                
                if(player.playing()) player.pause(); else player.play();
            }
        };
        Vbackward = new JosmAction(tr("Backward"), "audio-prev", tr("jumps n sec back"),
                Shortcut.registerShortcut("videomapping:backward","Video: "+tr("Backward"),KeyEvent.VK_NUMPAD4, Shortcut.GROUP_DIRECT), false) {
            public void actionPerformed(ActionEvent e) {
                player.backward();
            }
        };
        Vforward= new JosmAction(tr("Forward"), "audio-next", tr("jumps n sec forward"),
                Shortcut.registerShortcut("videomapping:forward","Video: "+tr("Forward"),KeyEvent.VK_NUMPAD6, Shortcut.GROUP_DIRECT), false) {
            
            public void actionPerformed(ActionEvent e) {
                player.forward();
                            
            }
        };
        Vfaster= new JosmAction(tr("Faster"), "audio-faster", tr("faster playback"),
                Shortcut.registerShortcut("videomapping:faster","Video: "+tr("Faster"),KeyEvent.VK_NUMPAD8, Shortcut.GROUP_DIRECT), false) {
            
            public void actionPerformed(ActionEvent e) {
                player.faster();
                            
            }
        };
        Vslower= new JosmAction(tr("Slower"), "audio-slower", tr("slower playback"),
                Shortcut.registerShortcut("videomapping:slower","Video: "+tr("Slower"),KeyEvent.VK_NUMPAD2, Shortcut.GROUP_DIRECT), false) {
            
            public void actionPerformed(ActionEvent e) {
                player.slower();
                            
            }
        };
        VJump= new JosmAction(tr("Jump To"), "jumpto", tr("jumps to the entered gps time"),null, false) {          
            public void actionPerformed(ActionEvent e) {
            	String s;
            	try {
            	JOptionPane d=new JOptionPane(tr("Jump to"), JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            	final JFormattedTextField inp = new JFormattedTextField(new MaskFormatter("##:##:##"));
            	inp.setText("00:00:01");
            	inp.setInputVerifier(new InputVerifier() {					
					@Override
					public boolean verify(JComponent input) {
						// TODO Auto-generated method stub
						return false;
					}
				});
            	SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    	inp.requestFocus();
                    }
                });
            	//TODO here we should show the GPS time range to the user
            	if(d.showConfirmDialog(Main.main.panel,inp, tr("Jump to"),JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)
            	{
	            	Date t;
	                SimpleDateFormat sdf= new SimpleDateFormat("hh:mm:ss");
	                t = sdf.parse(inp.getText());
	                if (t!=null)
	                {
	                    player.jumpToGPSTime(t);
	                }                       
            	}
            	} catch (ParseException e1) {
	                    // TODO Auto-generated catch block
	                    e1.printStackTrace();
	            }

            }
                            
            
        };
        Vloop= new JosmAction(tr("Loop"), "loop", tr("loops n sec around current position"),
                Shortcut.registerShortcut("videomapping:loop","Video: "+tr("loop"),KeyEvent.VK_NUMPAD7, Shortcut.GROUP_DIRECT), false) {
            
            public void actionPerformed(ActionEvent e) {
                player.loop();
                            
            }
        };
        
        //now the options menu
        VCenterIcon = new JCheckBoxMenuItem(new JosmAction(tr("Keep centered"), null, tr("follows the video icon automaticly"),null, false) {
            
            public void actionPerformed(ActionEvent e) {
                autocenter=VCenterIcon.isSelected();
                player.setAutoCenter(autocenter);
                applySettings();
                saveSettings();
                            
            }
        });
        //now the options menu
        VSubTitles = new JCheckBoxMenuItem(new JosmAction(tr("Subtitles"), null, tr("Show subtitles in video"),null, false) {
            
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
        VMenu.add(VJump);
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
    
    
  }
