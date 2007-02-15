package slippymap;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.RawGpsLayer.GpsPoint;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * Main class for the slippy map plugin.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class SlippyMapPlugin extends Plugin 
{    
    public SlippyMapPlugin() 
    {
	        
    }
	
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) 
	{
    	SlippyMapLayer smlayer;
    	smlayer = new SlippyMapLayer();
    	Main.main.addLayer(smlayer);
	}
}
