package org.openstreetmap.josm.plugins.imagery;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.bbox.SlippyMapBBoxChooser;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginHandler;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.PluginProxy;
import org.openstreetmap.josm.plugins.imagery.tms.TMSTileSourceProvider;
import org.openstreetmap.josm.plugins.imagery.wms.Map_Rectifier_WMSmenuAction;
import org.openstreetmap.josm.plugins.imagery.wms.WMSAdapter;
import org.openstreetmap.josm.plugins.imagery.wms.WMSLayer;
import org.openstreetmap.josm.plugins.imagery.wms.io.WMSLayerExporter;
import org.openstreetmap.josm.plugins.imagery.wms.io.WMSLayerImporter;
import org.openstreetmap.josm.tools.ImageProvider;

public class ImageryPlugin extends Plugin implements LayerChangeListener {

    JMenu imageryJMenu;
    JMenu offsetJMenu = new JMenu(trc("layer","Offset"));

    public static ImageryPlugin instance;
    public static WMSAdapter wmsAdapter = new WMSAdapter();

    public ImageryLayerInfo info = new ImageryLayerInfo();
    // remember state of menu item to restore on changed preferences
    private boolean menuEnabled = false;

    /***************************************************************
     * Remote control initialization:
     * If you need remote control in some other plug-in
     * copy this stuff and the call to initRemoteControl below
     * and replace the RequestHandler subclass in initRemoteControl
     ***************************************************************/

    /** name of remote control plugin */
    private final String REMOTECONTROL_NAME = "remotecontrol";

    /* if necessary change these version numbers to ensure compatibility */

    /** RemoteControlPlugin older than this SVN revision is not compatible */
    final int REMOTECONTROL_MIN_REVISION = 22734;
    /** Imagery Plugin needs this specific API major version of RemoteControlPlugin */
    final int REMOTECONTROL_NEED_API_MAJOR = 1;
    /** All API minor versions starting from this should be compatible */
    final int REMOTECONTROL_MIN_API_MINOR = 0;

    /* these fields will contain state and version of remote control plug-in */
    boolean remoteControlAvailable = false;
    boolean remoteControlCompatible = true;
    boolean remoteControlInitialized = false;
    int remoteControlRevision = 0;
    int remoteControlApiMajor = 0;
    int remoteControlApiMinor = 0;
    int remoteControlProtocolMajor = 0;
    int remoteControlProtocolMinor = 0;

    /**
     * Check if remote control plug-in is available and if its version is
     * high enough and register remote control command for this plug-in.
     */
    private void initRemoteControl() {
        for(PluginProxy pp: PluginHandler.pluginList)
        {
            PluginInformation info = pp.getPluginInformation();
            if(REMOTECONTROL_NAME.equals(info.name))
            {
                remoteControlAvailable = true;
                remoteControlRevision = Integer.parseInt(info.version);
                if(REMOTECONTROL_MIN_REVISION > remoteControlRevision)
                {
                    remoteControlCompatible = false;
                }
            }
        }

        if(remoteControlAvailable && remoteControlCompatible)
        {
            Plugin plugin =
                (Plugin) PluginHandler.getPlugin(REMOTECONTROL_NAME);
            try {
                Method method;
                method = plugin.getClass().getMethod("getVersion");
                Object obj = method.invoke(plugin);
                if((obj != null ) && (obj instanceof int[]))
                {
                    int[] versions = (int[]) obj;
                    if(versions.length >= 4)
                    {
                        remoteControlApiMajor = versions[0];
                        remoteControlApiMinor = versions[1];
                        remoteControlProtocolMajor = versions[2];
                        remoteControlProtocolMinor = versions[3];
                    }
                }

                if((remoteControlApiMajor != REMOTECONTROL_NEED_API_MAJOR) ||
                        (remoteControlApiMinor < REMOTECONTROL_MIN_API_MINOR))
                {
                    remoteControlCompatible = false;
                }
                if(remoteControlCompatible)
                {
                    System.out.println(this.getClass().getSimpleName() + ": initializing remote control");
                    method = plugin.getClass().getMethod("addRequestHandler", String.class, Class.class);
                    // replace command and class when you copy this to some other plug-in
                    // for compatibility with old remotecontrol add leading "/"
                    method.invoke(plugin, "/" + ImageryRemoteHandler.command, ImageryRemoteHandler.class);
                    remoteControlInitialized = true;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if(remoteControlAvailable)
        {
            String msg = null;

            if(remoteControlCompatible)
            {
                if(!remoteControlInitialized)
                {
                    msg  = tr("Could not initialize remote control.");
                }
            }
            else
            {
                msg  = tr("Remote control plugin is not compatible with {0}.",
                        this.getClass().getSimpleName());
            }

            if(msg != null)
            {
                String additionalMessage = tr("{0} will work but remote control for this plugin is disabled.\n"
                        + "You should update the plugins.",
                        this.getClass().getSimpleName());
                String versionMessage = tr("Current version of \"{1}\": {2}, internal version {3}. "
                        + "Need version {4}, internal version {5}.\n"
                        + "If updating the plugins does not help report a bug for \"{0}\".",
                        this.getClass().getSimpleName(),
                        REMOTECONTROL_NAME,
                        ""+remoteControlRevision,
                        (remoteControlApiMajor != 0) ?
                                ""+remoteControlApiMajor+"."+remoteControlApiMinor :
                                    tr("unknown"),
                                    ""+REMOTECONTROL_MIN_REVISION,
                                    ""+REMOTECONTROL_NEED_API_MAJOR+"."+REMOTECONTROL_MIN_API_MINOR );

                String title = tr("{0}: Problem with remote control",
                        this.getClass().getSimpleName());

                System.out.println(this.getClass().getSimpleName() + ": " +
                        msg + "\n" + versionMessage);

                JOptionPane.showMessageDialog(
                        Main.parent,
                        msg + "\n" + additionalMessage,
                        title,
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }

        if(!remoteControlAvailable) {
            System.out.println(this.getClass().getSimpleName() + ": remote control not available");
        }
    }

    /***************************************
     * end of remote control initialization
     ***************************************/

    protected void initExporterAndImporter() {
        ExtensionFileFilter.exporters.add(new WMSLayerExporter());
        ExtensionFileFilter.importers.add(new WMSLayerImporter());
    }

    public ImageryPlugin(PluginInformation info) {
        super(info);
        instance = this;
        this.info.load();
        OffsetBookmark.loadBookmarks();
        refreshMenu();
        initRemoteControl();
        SlippyMapBBoxChooser.addTileSourceProvider(new TMSTileSourceProvider());

        offsetJMenu.setIcon(ImageProvider.get("mapmode/adjustimg"));
        refreshOffsetMenu();
        MapView.addLayerChangeListener(this);
    }

    public void addLayer(ImageryInfo info) {
        this.info.add(info);
        this.info.save();
        refreshMenu();
    }

    public void refreshMenu() {
        MainMenu menu = Main.main.menu;

        if (imageryJMenu == null)
            imageryJMenu = menu.addMenu(marktr("Imagery"), KeyEvent.VK_W, menu.defaultMenuPos, ht("/Plugin/Imagery"));
        else
            imageryJMenu.removeAll();

        // for each configured WMSInfo, add a menu entry.
        for (final ImageryInfo u : info.layers) {
            imageryJMenu.add(new JMenuItem(new AddImageryLayerAction(u)));
        }
        imageryJMenu.addSeparator();
        imageryJMenu.add(new JMenuItem(new Map_Rectifier_WMSmenuAction()));

        imageryJMenu.addSeparator();
        imageryJMenu.add(offsetJMenu);
        imageryJMenu.addSeparator();
        imageryJMenu.add(new JMenuItem(new
                JosmAction(tr("Blank Layer"), "blankmenu", tr("Open a blank WMS layer to load data from a file"), null, false) {
            @Override
            public void actionPerformed(ActionEvent ev) {
                Main.main.addLayer(new WMSLayer());
            }
        }));
        setEnabledAll(menuEnabled);
    }

    public void refreshOffsetMenu() {
        offsetJMenu.removeAll();
        if (Main.map == null || Main.map.mapView == null) {
            offsetJMenu.setEnabled(false);
            return;
        }
        List<ImageryLayer> layers = Main.map.mapView.getLayersOfType(ImageryLayer.class);
        if (layers.isEmpty()) {
            offsetJMenu.setEnabled(false);
            return;
        }
        offsetJMenu.setEnabled(true);
        if (layers.size() == 1) {
            for (Component c : layers.get(0).getOffsetMenu()) {
                offsetJMenu.add(c);
            }
            return;
        }
        for (ImageryLayer layer : layers) {
            JMenu subMenu = new JMenu(layer.getName());
            subMenu.setIcon(layer.getIcon());
            for (Component c : layer.getOffsetMenu()) {
                subMenu.add(c);
            }
            offsetJMenu.add(subMenu);
        }
    }

    private void setEnabledAll(boolean isEnabled) {
        for(int i=0; i < imageryJMenu.getItemCount(); i++) {
            JMenuItem item = imageryJMenu.getItem(i);

            if(item != null) item.setEnabled(isEnabled);
        }
        menuEnabled = isEnabled;
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame==null && newFrame!=null) {
            setEnabledAll(true);
        } else if (oldFrame!=null && newFrame==null ) {
            setEnabledAll(false);
        }
        refreshOffsetMenu();
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new ImageryPreferenceEditor();
    }

    @Override
    public String getPluginDir()
    {
        return new File(Main.pref.getPluginsDirectory(), "imagery").getPath();
    }

    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
    }

    @Override
    public void layerAdded(Layer newLayer) {
        if (newLayer instanceof ImageryLayer) {
            refreshOffsetMenu();
        }
    }

    @Override
    public void layerRemoved(Layer oldLayer) {
        if (oldLayer instanceof ImageryLayer) {
            refreshOffsetMenu();
        }
    }
}
