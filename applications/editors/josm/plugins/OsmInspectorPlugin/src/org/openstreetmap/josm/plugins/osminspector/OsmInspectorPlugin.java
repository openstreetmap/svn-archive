package org.openstreetmap.josm.plugins.osminspector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.UserIdentityManager;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.download.DownloadSelection;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.spi.preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.spi.preferences.PreferenceChangedListener;
import org.openstreetmap.josm.tools.Shortcut;

public class OsmInspectorPlugin extends Plugin 
implements ZoomChangeListener, 
MouseListener, PreferenceChangedListener, DownloadSelection{

	/** The JOSM user identity manager, it is used for obtaining the user name */
    private final UserIdentityManager userIdentityManager;
   

    /** The bounding box from where the MapDust bugs are down-loaded */
    //private Bounds bBox;

    private OsmInspectorLayer inspectorLayer;
    
	public OsmInspectorPlugin(PluginInformation info) {
		super(info);
		userIdentityManager = UserIdentityManager.getInstance();
		initializePlugin();
	}
	
	/**
     * Initialize the <code>OsmInspectorPlugin</code> object. Creates the
     * <code>OsmInspectorGUI</code> and initializes the following variables with
     *.
     */
    private void initializePlugin() {
        Shortcut.registerShortcut("OsmInspector", tr("Toggle: {0}", tr("Open OsmInspector")),
                KeyEvent.VK_1, Shortcut.ALT_SHIFT);
        //String name = "Osm Inspector error reports";
        //String tooltip = "Activates the Osm Inspector reporter plugin";
        
        /* add default values for static variables */
        Config.getPref().put("osmInspector.nickname", "osmi");
        Config.getPref().putBoolean("osmInspector.showBugs", true);
        Config.getPref().put("osmInspector.version", getPluginInformation().version);
        Config.getPref().put("osmInspector.localVersion",getPluginInformation().localversion);
        inspectorLayer = null;
    }
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		MainApplication.getToolbar().register( new ImportOsmInspectorBugsAction( this ) );
		if (newFrame == null) {
            /* if new MapFrame is null, remove listener */
            NavigatableComponent.removeZoomChangeListener(this);
        } else {
            /* add MapDust dialog window */
            if (MainApplication.getMap() != null && MainApplication.getMap().mapView != null) {
                /* add MapdustGUI */
                MainApplication.getMap().setBounds(newFrame.getBounds());
                
                /* add Listeners */
                NavigatableComponent.addZoomChangeListener(this);
                MainApplication.getMap().mapView.addMouseListener(this);
                Config.getPref().addPreferenceChangeListener(this);
                /* put username to preferences */
                Config.getPref().put("osmInspector.josmUserName",
                        userIdentityManager.getUserName());
                MainApplication.getToolbar().control.add( new ImportOsmInspectorBugsAction( this ) );
            }
        }
	}

	@Override
	public void zoomChanged() {
		
	}

	@Override
	//
	//  Delegate feature selection to layer
	//
	public void mouseClicked(MouseEvent e) {
	    if (inspectorLayer != null) {
	        inspectorLayer.selectFeatures(e.getX(), e.getY());
	    }
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void preferenceChanged(PreferenceChangeEvent e) {
		
	}

	public OsmInspectorLayer getLayer()
	{
		return inspectorLayer;
	}

	public void setLayer( OsmInspectorLayer theLayer )
	{
		inspectorLayer = theLayer;
	}

	@Override
	public void addGui(DownloadDialog gui) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDownloadArea(Bounds bounds) {
		// TODO Auto-generated method stub
	}
}
