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

	public VideoPlugin(PluginInformation info) {
		super(info);
		// TODO Auto-generated constructor stub
	}

	public void activeLayerChange(Layer arg0, Layer arg1) {
		// TODO Auto-generated method stub
		
	}

	public void layerAdded(Layer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void layerRemoved(Layer arg0) {
		// TODO Auto-generated method stub
		
	}
      
    
    
  }
