package livegps;

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
import org.openstreetmap.josm.gui.layer.MarkerLayer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer.GpsPoint;
import org.openstreetmap.josm.plugins.Plugin;

public class LiveGpsPlugin extends Plugin 
{
	private LiveGpsAcquirer acquirer = null;
	private Thread acquirerThread = null;
    private JMenu lgpsmenu;
    private JCheckBoxMenuItem lgpscapture;
    private JMenuItem lgpscenter;
    private JCheckBoxMenuItem lgpsautocenter;
    
	private Collection<Collection<GpsPoint>> data = new ArrayList<Collection<GpsPoint>>();
    private LiveGpsLayer lgpslayer;
    
    public LiveGpsPlugin() 
    {
        JMenuBar menu = Main.main.menu;
        lgpsmenu = new JMenu("LiveGPS");
        menu.add(lgpsmenu, 2);
        lgpscapture = new JCheckBoxMenuItem("Capture GPS Track");
        lgpscapture.setSelected(false);
        lgpscapture.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent ev) {
        		if ((acquirer != null) && (!lgpscapture.isSelected()))
        		{
        			acquirer.shutdown();
        			acquirer = null;
        			acquirerThread = null;
        		}
        		else if ((acquirer == null) && (lgpscapture.isSelected()))
        		{
        			acquirer = new LiveGpsAcquirer();
        			if (lgpslayer == null)
        			{
        		    	lgpslayer = new LiveGpsLayer(data);
        				Main.main.addLayer(lgpslayer);
        			}
        			acquirer.setOutputLayer(lgpslayer);
        			acquirerThread = new Thread(acquirer);
        			acquirerThread.start();
        		}
        	}
        });
        lgpsmenu.add(lgpscapture);

        lgpscenter = new JMenuItem("Center Once", KeyEvent.VK_C);
        lgpscenter.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ev) {
        		lgpslayer.center();
        	}
        });
        lgpsmenu.add(lgpscenter);
        
        
        lgpsautocenter = new JCheckBoxMenuItem("Auto-Center on current position");
        lgpsautocenter.setSelected(false);
        lgpsautocenter.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent ev) {
        		lgpslayer.setAutoCenter(lgpsautocenter.isSelected());
        		if (lgpsautocenter.isSelected()) lgpslayer.center();
        	}
        });
        lgpsmenu.add(lgpsautocenter);
        
    }
}
