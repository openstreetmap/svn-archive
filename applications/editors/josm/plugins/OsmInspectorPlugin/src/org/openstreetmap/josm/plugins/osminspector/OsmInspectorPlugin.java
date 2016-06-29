package org.openstreetmap.josm.plugins.osminspector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.download.DownloadSelection;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

public class OsmInspectorPlugin extends Plugin 
implements ZoomChangeListener, 
MouseListener, PreferenceChangedListener, DownloadSelection{

	/** The JOSM user identity manager, it is used for obtaining the user name */
    private final JosmUserIdentityManager userIdentityManager;
   

    /** The bounding box from where the MapDust bugs are down-loaded */
    //private Bounds bBox;

    private OsmInspectorLayer inspectorLayer;
    
	public OsmInspectorPlugin(PluginInformation info) {
		super(info);
		userIdentityManager = JosmUserIdentityManager.getInstance();
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
        Main.pref.put("osmInspector.nickname", "osmi");
        Main.pref.put("osmInspector.showBugs", true);
        Main.pref.put("osmInspector.version", getPluginInformation().version);
        Main.pref.put("osmInspector.localVersion",getPluginInformation().localversion);
        inspectorLayer = null;
    }
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		Main.toolbar.register( new ImportOsmInspectorBugsAction( this ) );
		if (newFrame == null) {
            /* if new MapFrame is null, remove listener */
            NavigatableComponent.removeZoomChangeListener(this);
        } else {
            /* add MapDust dialog window */
            if (Main.map != null && Main.map.mapView != null) {
                /* add MapdustGUI */
                Main.map.setBounds(newFrame.getBounds());
                
                /* add Listeners */
                NavigatableComponent.addZoomChangeListener(this);
                Main.map.mapView.addMouseListener(this);
                Main.pref.addPreferenceChangeListener(this);
                /* put username to preferences */
                Main.pref.put("osmInspector.josmUserName",
                        userIdentityManager.getUserName());
                Main.toolbar.control.add( new ImportOsmInspectorBugsAction( this ) );
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
	public void mouseClicked(MouseEvent arg0) {
	    if (inspectorLayer != null) {
	        inspectorLayer.selectFeatures(arg0.getX(), arg0.getY());
	    }
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
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
