package org.openstreetmap.josm.plugins.videomapping;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
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

import uk.co.caprica.vlcj.runtime.windows.WindowsRuntimeUtil;

import com.sun.jna.LastErrorException;
import com.sun.jna.NativeLibrary;

import static org.openstreetmap.josm.tools.I18n.*;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;

  /**
 * @author Matthias Meiﬂer (digi_c at arcor dot de)
 * @ released under GPL
 * This Plugin allows you to link a video against a GPS track and playback both synchronously 
 */

//Here we manage properties and start the other classes
public class VideoPlugin extends Plugin implements LayerChangeListener{
	private JMenu VMenu,VDeinterlacer;
	private JosmAction VAdd,VRemove,VStart,Vbackward,Vforward,VJump,Vfaster,Vslower,Vloop;
	private JRadioButtonMenuItem VIntBob,VIntNone,VIntLinear;
    private JCheckBoxMenuItem VCenterIcon,VSubTitles;
    private JMenuItem VJumpLength,VLoopLength;
    private final String PROP_MRU="videomapping.mru";
    private final String PROP_AUTOCENTER="videomapping.autocenter";
    private final String PROP_JUMPLENGTH="videomapping.jumplength";
    private final String PROP_LOOPLENGTH="videomapping.looplength"; 
    private String deinterlacer;
    private boolean autoCenter;
    private Integer jumpLength,loopLength;
    private String mostRecentFolder;
	private GpxLayer gpsLayer;

	public VideoPlugin(PluginInformation info) {
		super(info);
		MapView.addLayerChangeListener(this);				
		createMenusAndShortCuts();
		enableVideoControlMenus(false);
		setDefaults();
		loadProperties();
	}

	private void createMenusAndShortCuts() {
		VMenu = Main.main.menu.addMenu(" Video", KeyEvent.VK_V, Main.main.menu.defaultMenuPos,ht("/Plugin/Videomapping"));
		VMenu.setEnabled(false);
		VAdd= new JosmAction(tr("Import Video"),"videomapping",tr("Sync a video against this GPS track"),null,false) {
            public void actionPerformed(ActionEvent arg0) {                 
                    importVideoFile();
                }
        
        };
        VRemove= new JosmAction(tr("Remove Video"),"videomapping",tr("removes current video from layer"),null,false) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent arg0) {
            }
        };       
        VStart = new JosmAction(tr("Play/Pause"), "audio-playpause", tr("starts/pauses video playback"),
                Shortcut.registerShortcut("videomapping:startstop","Video: "+tr("Play/Pause"),KeyEvent.VK_NUMPAD5, Shortcut.GROUP_DIRECT), false) {            
            public void actionPerformed(ActionEvent e) {                                
                
            }
        };
        Vbackward = new JosmAction(tr("Backward"), "audio-prev", tr("jumps n sec back"),
                Shortcut.registerShortcut("videomapping:backward","Video: "+tr("Backward"),KeyEvent.VK_NUMPAD4, Shortcut.GROUP_DIRECT), false) {
            public void actionPerformed(ActionEvent e) {
               
            }
        };
        Vforward= new JosmAction(tr("Forward"), "audio-next", tr("jumps n sec forward"),
                Shortcut.registerShortcut("videomapping:forward","Video: "+tr("Forward"),KeyEvent.VK_NUMPAD6, Shortcut.GROUP_DIRECT), false) {            
            public void actionPerformed(ActionEvent e) {
                
                            
            }
        };
        Vfaster= new JosmAction(tr("Faster"), "audio-faster", tr("faster playback"),
                Shortcut.registerShortcut("videomapping:faster","Video: "+tr("Faster"),KeyEvent.VK_NUMPAD8, Shortcut.GROUP_DIRECT), false) {
            
            public void actionPerformed(ActionEvent e) {
                
                            
            }
        };
        Vslower= new JosmAction(tr("Slower"), "audio-slower", tr("slower playback"),
                Shortcut.registerShortcut("videomapping:slower","Video: "+tr("Slower"),KeyEvent.VK_NUMPAD2, Shortcut.GROUP_DIRECT), false) {
            
            public void actionPerformed(ActionEvent e) {
                
                            
            }
        };
        VJump= new JosmAction(tr("Jump To"), "jumpto", tr("jumps to the entered gps time"),null, false) {          
            public void actionPerformed(ActionEvent e) {
            	

            }
                            
            
        };
        Vloop= new JosmAction(tr("Loop"), "loop", tr("loops n sec around current position"),
                Shortcut.registerShortcut("videomapping:loop","Video: "+tr("loop"),KeyEvent.VK_NUMPAD7, Shortcut.GROUP_DIRECT), false) {            
            public void actionPerformed(ActionEvent e) {

                            
            }
        };
        
        //now the options menu
        VCenterIcon = new JCheckBoxMenuItem(new JosmAction(tr("Keep centered"), null, tr("follows the video icon automaticly"),null, false) {            
            public void actionPerformed(ActionEvent e) {

                            
            }
        });
        //now the options menu
        VSubTitles = new JCheckBoxMenuItem(new JosmAction(tr("Subtitles"), null, tr("Show subtitles in video"),null, false) {           
            public void actionPerformed(ActionEvent e) {
  
                            
            }
        });
        
        VJumpLength = new JMenuItem(new JosmAction(tr("Jump length"), null, tr("Set the length of a jump"),null, false) {            
            public void actionPerformed(ActionEvent e) {                        
            }
        });
        
        VLoopLength = new JMenuItem(new JosmAction(tr("Loop length"), null, tr("Set the length around a looppoint"),null, false) {            
            public void actionPerformed(ActionEvent e) {
               
                            
            }
        });        
        VDeinterlacer= new JMenu("Deinterlacer");
        VIntNone= new JRadioButtonMenuItem(new JosmAction(tr("none"), null, tr("no deinterlacing"),null, false) {            
            public void actionPerformed(ActionEvent e) {                
            }
        });
        VIntBob= new JRadioButtonMenuItem(new JosmAction("bob", null, tr("deinterlacing using line doubling"),null, false) {            
            public void actionPerformed(ActionEvent e) {

            }
        });
        VIntLinear= new JRadioButtonMenuItem(new JosmAction("linear", null, tr("deinterlacing using linear interpolation"),null, false) {            
            public void actionPerformed(ActionEvent e) {

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

	protected void importVideoFile() {
		JFileChooser fc = new JFileChooser(mostRecentFolder);
        fc.setSelectedFile(new File(mostRecentFolder));
        if(fc.showOpenDialog(Main.main.parent)!=JFileChooser.CANCEL_OPTION)
        {
//        	mostRecentFolder=fc.getSelectedFile().getAbsolutePath();
//        	saveProperties();
        	VideoPositionLayer videoPositionLayer= new VideoPositionLayer(gpsLayer);
        }
		
	}

	private void enableVideoControlMenus(boolean b) {
		VStart.setEnabled(b);
        Vbackward.setEnabled(b);
        Vforward.setEnabled(b);
        Vloop.setEnabled(b);
        Vfaster.setEnabled(b);
        Vslower.setEnabled(b);
        VJump.setEnabled(b);		
	}
	
	private void setDefaults()
	{
		autoCenter=false;
		deinterlacer="";
		jumpLength=1000;
		loopLength=6000;
		mostRecentFolder=System.getProperty("user.home");		
	}
	
	private void loadProperties() {
        String temp;        
        temp=Main.pref.get(PROP_AUTOCENTER);
        if((temp!=null)&&(temp.length()!=0))
        	autoCenter=Boolean.getBoolean(temp);        
        temp=Main.pref.get(PROP_JUMPLENGTH);
        if((temp!=null)&&(temp.length()!=0))
        	jumpLength=Integer.valueOf(temp);
        temp=Main.pref.get(PROP_LOOPLENGTH);
        if((temp!=null)&&(temp.length()!=0))
        	loopLength=Integer.valueOf(temp);
        temp=Main.pref.get(PROP_MRU);
        if((temp!=null)&&(temp.length()!=0))
        	mostRecentFolder=Main.pref.get(PROP_MRU);        
    }
	
	private void saveProperties(){
        Main.pref.put(PROP_AUTOCENTER, autoCenter);
        Main.pref.put(PROP_JUMPLENGTH, jumpLength.toString());
        Main.pref.put(PROP_LOOPLENGTH, loopLength.toString());  
    	Main.pref.put(PROP_MRU, mostRecentFolder);
    }
	
	private void applySettings()
	{
		//GUI
        VCenterIcon.setSelected(autoCenter);
        VIntNone.setSelected(true);
        if(deinterlacer=="")
        	VIntNone.setSelected(true);
        if(deinterlacer=="bob")
        	VIntBob.setSelected(true);
        if(deinterlacer=="linear")
        	VIntLinear.setSelected(true);
	}
	

	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        VMenu.setEnabled(true);
        if (newLayer instanceof GpxLayer)
        {
            VAdd.setEnabled(true);
            gpsLayer=((GpxLayer) newLayer);            
            //TODO append to GPS Layer menu
        }        
    }

	public void layerAdded(Layer arg0) {
		activeLayerChange(null,arg0);
		
	}

	public void layerRemoved(Layer arg0) {
		activeLayerChange(null,arg0);
		
	}
      
    
    
  }
