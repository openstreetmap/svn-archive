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
import org.openstreetmap.josm.actions.JosmAction;

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
    
	
	public class CaptureAction extends JosmAction {
	public CaptureAction() {
		super(tr("Capture GPS Track"), "capturemenu", tr("Connect to gpsd server and show current position in LiveGPS layer."), KeyEvent.VK_R, KeyEvent.ALT_MASK, true);
	}

	public void actionPerformed(ActionEvent e) {
		enableTracking(lgpscapture.isSelected());
	}
	}

	public class CenterAction extends JosmAction {
	public CenterAction() {
		super(tr("Center Once"), "centermenu", tr("Center the LiveGPS layer to current position."), KeyEvent.VK_C, 0, true);
	}

	public void actionPerformed(ActionEvent e) {
		if(lgpslayer != null) {
			lgpslayer.center();
		}
	}
	}

	public class AutoCenterAction extends JosmAction {
	public AutoCenterAction() {
		super(tr("Auto-Center"), "autocentermenu", tr("Continuously center the LiveGPS layer to current position."), KeyEvent.VK_HOME, 0, true);
	}

	public void actionPerformed(ActionEvent e) {
		if(lgpslayer != null) {
			lgpslayer.center();
		}
	}
	}

    public LiveGpsPlugin() 
    {        
        JMenuBar menu = Main.main.menu;
        lgpsmenu = new JMenu("LiveGPS");
        lgpsmenu.setMnemonic(KeyEvent.VK_G);
        menu.add(lgpsmenu, 5);
        
        JosmAction captureAction = new CaptureAction();
        JCheckBoxMenuItem captureMenu = new JCheckBoxMenuItem(captureAction);
        lgpsmenu.add(captureMenu);  
        captureMenu.setAccelerator(captureAction.shortCut);

        JosmAction centerAction = new CenterAction();
        JMenuItem centerMenu = new JMenuItem(centerAction);
        lgpsmenu.add(centerMenu);  
        centerMenu.setAccelerator(centerAction.shortCut);

        JosmAction autoCenterAction = new AutoCenterAction();
        JCheckBoxMenuItem autoCenterMenu = new JCheckBoxMenuItem(autoCenterAction);
        lgpsmenu.add(autoCenterMenu);  
        autoCenterMenu.setAccelerator(autoCenterAction.shortCut);
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
        if(newFrame != null) {
            // add dialog
            newFrame.addToggleDialog(lgpsdialog = new LiveGpsDialog(newFrame));
            // connect listeners with acquirer:
            addPropertyChangeListener(lgpsdialog);
        }
    }


    /**
     * @return the lgpsmenu
     */
    public JMenu getLgpsMenu() {
        return this.lgpsmenu;
    }

}

