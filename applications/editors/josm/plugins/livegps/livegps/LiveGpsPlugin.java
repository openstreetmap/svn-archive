package livegps;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
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
    private LiveGpsDialog lgpsdialog;
    List<PropertyChangeListener>listenerQueue;
    
	private Collection<Collection<GpsPoint>> data = new ArrayList<Collection<GpsPoint>>();
    private LiveGpsLayer lgpslayer;
    
    public LiveGpsPlugin() 
    {        
        JMenuBar menu = Main.main.menu;
        lgpsmenu = new JMenu("LiveGPS");
        lgpsmenu.setMnemonic(KeyEvent.VK_G);
        menu.add(lgpsmenu, 2);
        lgpscapture = new JCheckBoxMenuItem(tr("Capture GPS Track"));
        lgpscapture.setSelected(false);
        lgpscapture.setAccelerator(KeyStroke.getKeyStroke("alt R"));
        lgpscapture.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ev) {
        	    enableTracking(lgpscapture.isSelected());
        	}
        });
        lgpsmenu.add(lgpscapture);

        lgpscenter = new JMenuItem(tr("Center Once"), KeyEvent.VK_C);
        lgpscenter.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ev) {
        	    if(lgpslayer != null) {
        	        lgpslayer.center();
        	    }
        	}
        });
        lgpsmenu.add(lgpscenter);
        
        
        lgpsautocenter = new JCheckBoxMenuItem(tr("Auto-Center on current position"));
        lgpsautocenter.setSelected(true);
        lgpsautocenter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
        lgpsautocenter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setAutoCenter(lgpsautocenter.isSelected());
            }
        });
        lgpsmenu.add(lgpsautocenter);        
    }
    
    /**
     * Set to <code>true</code> if the current position should always be in the center of the map.
     * @param autoCenter if <code>true</code> the map is always centered.
     */
    public void setAutoCenter(boolean autoCenter) {
        lgpsautocenter.setSelected(autoCenter); // just in case this method was not called from the menu
        if(lgpslayer != null) {
            lgpslayer.setAutoCenter(autoCenter);
            if (autoCenter) lgpslayer.center();
        }
    }
    
    /**
     * Returns <code>true</code> if autocenter is selected.
     * @return <code>true</code> if autocenter is selected.
     */
    public boolean isAutoCenter() {
        return lgpsautocenter.isSelected();
    }
    
    /**
     * Enable or disable gps tracking
     * @param enable if <code>true</code> tracking is started.
     */
    public void enableTracking(boolean enable) {
        if ((acquirer != null) && (!enable))
        {
            acquirer.shutdown();
            acquirerThread = null;
        }
        else if(enable)
        {
            if (acquirer == null) {
                acquirer = new LiveGpsAcquirer();
                if (lgpslayer == null) {
                    lgpslayer = new LiveGpsLayer(data);
                    Main.main.addLayer(lgpslayer);
                    lgpslayer.setAutoCenter(isAutoCenter());
                }
                // connect layer with acquirer:
                addPropertyChangeListener(lgpslayer);
                // add all listeners that were added before the acquirer existed:
                if(listenerQueue != null) {
                    for(PropertyChangeListener listener : listenerQueue) {
                        addPropertyChangeListener(listener);
                    }
                    listenerQueue.clear();
                }
            }
            if(acquirerThread == null) {
                acquirerThread = new Thread(acquirer);
                acquirerThread.start();
            }
        }
    }
    
    
    /**
     * Add a listener for gps events.
     * @param listener the listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(acquirer != null) {
            acquirer.addPropertyChangeListener(listener);
        } else {
            if(listenerQueue == null) {
                listenerQueue = new ArrayList<PropertyChangeListener>();
            }
            listenerQueue.add(listener);
        }
    }


    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.Plugin#mapFrameInitialized(org.openstreetmap.josm.gui.MapFrame, org.openstreetmap.josm.gui.MapFrame)
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        // add dialog
        newFrame.addToggleDialog(lgpsdialog = new LiveGpsDialog(newFrame));
        // connect listeners with acquirer:
        addPropertyChangeListener(lgpsdialog);
    }


    /**
     * @return the lgpsmenu
     */
    public JMenu getLgpsMenu() {
        return this.lgpsmenu;
    }

}
