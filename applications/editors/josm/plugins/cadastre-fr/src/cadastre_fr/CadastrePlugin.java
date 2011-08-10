// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.data.projection.*;

/**
 *
 * Plugin to access the French Cadastre WMS server at www.cadastre.gouv.fr This
 * WMS server requires some specific handling like retrieving a cookie for a
 * limitation in case of no activity, or the request to the server shall provide
 * a city/town/village code.
 *
 * @author Pieren <pieren3@gmail.com>,
 * @author <matthieu.lochegnies@gmail.com> for the extension to codeCommune
 * @version 0.8
 * History:
 * 0.1 17-Jun-2008 first prototype using a first Lambert projection impl. in core
 * 0.2 22-Jun-2008 first stable version
 * 0.3 24-Jun-2008 add code departement
 * 0.4 06-Jul-2008 - add images scales, icons, menu items disabling
 *                 - remove dependencies of wmsplugin
 *                 - add option to force a Lambert zone (for median locations)
 *                 - add auto-sourcing
 * 0.5 16-Aug-2008 - add transparency in layer (allowing multiple wms layers displayed together)
 *                 - no overlapping of grabbed images if transparency is enabled
 *                 - set one layer per location
 *                 - use utf-8 charset in POST request to server
 *                 - improve the preferences setting dialog
 *                 - cancel the current download is now possible
 *                 - add automatic images caching and load on request (+ manage cache directory size)
 *                 - enable auto-sourcing only if a WMS layer is used
 * 0.6 18-Aug-2008 - suppress the null-exception message after the dialog 'open a layer first'
 *                 - process the overlapping images when cache is loaded from disk
 *                 - save the last 'new location request' text again in preferences
 *                 - avoid duplicate layers with same name
 *                 - set text input for new locations in upper case
 *                 - the cache directory is configurable in "cadastrewms.cacheDir"
 *                 - improve configuration change updates
 * 0.7 24-Aug-2008 - mask images only if transparency enabled
 *                 - validate projection name by Lambert.toString() method
 * 0.8 25-Jan-2009 - display returned list of communes if direct name is not recognized by server
 *                 - new possible grab factor of 100 square meters fixed size
 *                 - minor fixes due to changes in JOSM core classes
 *                 - first draft of raster image support
 * 0.9 05-Feb-2009 - grab vectorized full commune bbox, save in file, convert to OSM way
 *                   and simplify
 * 1.0 18-Feb-2009 - fix various bugs in color management and preference dialog
 *                 - increase PNG picture size requested to WMS (800x1000)
 *                 - set 4th grab scale fixed size configurable (from 25 to 1000 meters)
 * 1.1 11-Jun-2009 - fixed a null exception error when trying to displace a vectorized layer
 *                 - propose to use shortcut F11 for grabbing
 * 1.2 16-Aug-2009 - implementation of raster image grabbing, cropping and georeferencing (not the
 *                   overview rasters (Tableau d'assemblage) but directly small units (Feuille)
 * 1.3 23-Aug-2009 - improve georeferencing action cancellation
 *                 - fixed bug of raster image loaded from cache not working on Java1.6
 *                 - improve mouse click bounce detection during georeferencing process
 * 1.4 25-Oct-2009 - add support for new Lambert CC 9 Zones projection
 *                 - add optional crosspieces display on raster image layers
 *                 - add automatic raster images georeferencing when WMS provides data
 *                 - re-implement manual adjustment mode in raster image layer
 * 1.5 21-Nov-2009 - major changes in projection in core : no magical zone prediction anymore for
 *                   Lambert 4 and 9 zones; grid translation implemented for Lambert 4 zones;
 *                   support of subprojections in preferences for zones setting and UTM20N
 *                 - removed autosourcing of empty new nodes
 * 1.6 28-Nov-2009 - Fix minor issues if Grab is called without layer (possible since projection rework)
 * 1.7 12-Dec-2009 - Change URL's changes for cookie and downgrade imgs resolution due to WMS changes
 * 1.8 11-Mar-2010 - filter the mouse button 1 during georeferencing
 *                 - retry if getting a new cookie failed (10 times during 30 seconds)
 *                 - cookie expiration automatically detected and renewed (after 30 minutes)
 *                 - proper WMS layer cleanup at destruction (workaround for memory leak)
 *                 - new cache format (v3) storing original image and cropped image bbox + angle
 *                 - new cache format (v4) storing original image size for later rotation
 *                 - cache files read compatible with previous formats
 *                 - raster image rotation issues fixed, now using shift+ctrl key instead of ctrl
 *                 - raster image adjustment using default system menu modifier (ctrl for windows) for Mac support
 *                 - image resolution configurable (high, medium, low) like the online interface
 *                 - layer selection configurable for vectorized images
 *                 - improved download cancellation
 *                 - from Erik Amzallag:
 *                 -     possibility to modify the auto-sourcing text just before upload
 *                 - from Clément Ménier:
 *                 -     new option allowing an auto-selection of the first cadastre layer for grab
 *                 -     non-modal JDialog in MenuActionGrabPlanImage
 *                 -     new options in the image filter (bilinear, bicubic)
 * 1.9 05-Apr-2010 - added a scroll bar in preferences
 *                 - download cancellation improved
 *                 - last deployment for Java1.5 compatibility
 * 2.0 07-Jul-2010 - update projection for "La Reunion" departement to RGR92, UTM40S.
 *                 - add 'departement' as option in the municipality selection
 *                 - fixed bug in cache directory size control (and disabled by default)
 *                 - add map mode for addressing
 *                 - from Nicolas Dumoulin:
 *                 -     add "tableau d'assemblage" in raster images for georeferencing (as option)
 * 2.1 14-Jan-2011 - add GrabThread moving the grab to a separate thread
 *                 - the divided BBox mode starts from the central square and loads the next in a spiral
 *                 - move the grabber from CadastrPlugin singleton to each wmsLayer instance to allow grabbing
 *                   of multiple municipalities in parallel.
 * 2.2 01-Jul-2011 - replace deprecated Main.proj by newest Main.getProjection()
 *                 - fix list of raster images (Feuilles) parsing failing after a Cadastre server change/maintenance 
 */
public class CadastrePlugin extends Plugin {
    static String VERSION = "2.1";

    static JMenu cadastreJMenu;

    public static String source = "";

    // true if the checkbox "auto-sourcing" is set in the plugin menu
    public static boolean autoSourcing = false;

    // true when the plugin is first used, e.g. grab from WMS or download cache file
    public static boolean pluginUsed = false;

    public static String cacheDir = null;

    public static boolean alterColors = false;

    public static boolean backgroundTransparent = false;

    public static float transparency = 1.0f;

    public static boolean drawBoundaries = false;

    public static int imageWidth, imageHeight;

    public static String grabLayers, grabStyles = null;

    static private boolean menuEnabled = false;

    private static String LAYER_BULDINGS = "CDIF:LS2";
    private static String STYLE_BUILDING = "LS2_90";
    private static String LAYER_WATER = "CDIF:LS3";
    private static String STYLE_WATER = "LS3_90";
    private static String LAYER_SYMBOL = "CDIF:LS1";
    private static String STYLE_SYMBOL = "LS1_90";
    private static String LAYER_PARCELS = "CDIF:PARCELLE";
    private static String STYLE_PARCELS = "PARCELLE_90";
    private static String LAYER_NUMERO = "CDIF:NUMERO";
    private static String STYLE_NUMERO = "NUMERO_90";
    private static String LAYER_LABEL = "CDIF:PT3,CDIF:PT2,CDIF:PT1";
    private static String STYLE_LABEL = "PT3_90,PT2_90,PT1_90";
    private static String LAYER_LIEUDIT = "CDIF:LIEUDIT";
    private static String STYLE_LIEUDIT = "LIEUDIT_90";
    private static String LAYER_SECTION = "CDIF:SUBSECTION,CDIF:SECTION";
    private static String STYLE_SECTION = "SUBSECTION_90,SECTION_90";
    private static String LAYER_COMMUNE = "CDIF:COMMUNE";
    private static String STYLE_COMMUNE = "COMMUNE_90";
    
    /**
     * Creates the plugin and setup the default settings if necessary
     *
     * @throws Exception
     */
    public CadastrePlugin(PluginInformation info) throws Exception {
        super(info);
        System.out.println("Pluging cadastre-fr v"+VERSION+" started...");
        if (Main.pref.get("cadastrewms.cacheDir").equals(""))
            cacheDir = Main.pref.getPreferencesDir()+"plugins"+File.separatorChar+"cadastrewms"+File.separatorChar;
        else {
            cacheDir = Main.pref.get("cadastrewms.cacheDir");
            if (cacheDir.charAt(cacheDir.length()-1) != File.separatorChar )
                cacheDir += File.separatorChar;
        }
        System.out.println("current cache directory: "+cacheDir);

        refreshConfiguration();

        UploadAction.registerUploadHook(new CheckSourceUploadHook());

    }

    public static void refreshMenu() {
        MainMenu menu = Main.main.menu;

        if (cadastreJMenu == null) {
            cadastreJMenu = menu.addMenu(marktr("Cadastre"), KeyEvent.VK_C, menu.defaultMenuPos, ht("/Plugin/CadastreFr"));
            JosmAction grab = new MenuActionGrab();
            JMenuItem menuGrab = new JMenuItem(grab);
            KeyStroke ks = grab.getShortcut().getKeyStroke();
            if (ks != null) {
                menuGrab.setAccelerator(ks);
            }
            JMenuItem menuActionGrabPlanImage = new JMenuItem(new MenuActionGrabPlanImage());
            JMenuItem menuSettings = new JMenuItem(new MenuActionNewLocation());
            final JCheckBoxMenuItem menuSource = new JCheckBoxMenuItem(tr("Auto sourcing"));
            menuSource.setSelected(autoSourcing);
            menuSource.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    Main.pref.put("cadastrewms.autosourcing", menuSource.isSelected());
                    autoSourcing = menuSource.isSelected();
                }
            });

            //JMenuItem menuResetCookie = new JMenuItem(new MenuActionResetCookie());
            //JMenuItem menuLambertZone = new JMenuItem(new MenuActionLambertZone());
            JMenuItem menuLoadFromCache = new JMenuItem(new MenuActionLoadFromCache());
            // temporary disabled:
            //JMenuItem menuActionBoundaries = new JMenuItem(new MenuActionBoundaries());
            //JMenuItem menuActionBuildings = new JMenuItem(new MenuActionBuildings());

            cadastreJMenu.add(menuGrab);
            cadastreJMenu.add(menuActionGrabPlanImage);
            cadastreJMenu.add(menuSettings);
            cadastreJMenu.add(menuSource);
            //cadastreJMenu.add(menuResetCookie); not required any more
            //cadastreJMenu.add(menuLambertZone);
            //if (Main.pref.getBoolean("cadastrewms.buildingsMenu", false))
            //    cadastreJMenu.add(menuActionBuildings);
            cadastreJMenu.add(menuLoadFromCache);
            // all SVG features disabled until official WMS is released
            //cadastreJMenu.add(menuActionBoundaries);
        }
        setEnabledAll(menuEnabled);
    }

    public static void refreshConfiguration() {
        source = checkSourceMillesime();
        autoSourcing = Main.pref.getBoolean("cadastrewms.autosourcing", true);
        alterColors = Main.pref.getBoolean("cadastrewms.alterColors");
        drawBoundaries = Main.pref.getBoolean("cadastrewms.drawBoundaries", false);
        if (alterColors) {
            backgroundTransparent = Main.pref.getBoolean("cadastrewms.backgroundTransparent");
            transparency = Float.parseFloat(Main.pref.get("cadastrewms.brightness", "1.0f"));
        } else {
            backgroundTransparent = false;
            transparency = 1.0f;
        }
        String currentResolution = Main.pref.get("cadastrewms.resolution", "high");
        if (currentResolution.equals("high")) {
            imageWidth = 1000; imageHeight = 800;
        } else if (currentResolution.equals("medium")){
            imageWidth = 800; imageHeight = 600;
        } else {
            imageWidth = 600; imageHeight = 400;
        }
        refreshLayersURL();

        // overwrite F11 shortcut used from the beginning by this plugin and recently used
        // for full-screen switch in JOSM core
        int i = 0;
        String p = Main.pref.get("shortcut.shortcut."+i, null);
        boolean alreadyRedefined = false;
        while (p != null) {
            String[] s = p.split(";");
            alreadyRedefined = alreadyRedefined || s[0].equals("menu:view:fullscreen");
            i++;
            p = Main.pref.get("shortcut.shortcut."+i, null);
        }
        if (!alreadyRedefined) {
            int reply = JOptionPane.showConfirmDialog(null,
                    tr("Plugin cadastre-fr used traditionally the key shortcut F11 for grabbing,\n"+
                    "which is currently allocated for full-screen switch by default.\n"+
                    "Would you like to restore F11 for grab action?"),
                    tr("Restore grab shortcut F11"),
                    JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.OK_OPTION) {
                System.out.println("redefine fullscreen shortcut F11 to shift+F11");
                Main.pref.put("shortcut.shortcut."+i, "menu:view:fullscreen;Toggle Full Screen view;122;5;122;64;false;true");
                JOptionPane.showMessageDialog(Main.parent,tr("JOSM is stopped for the change to take effect."));
                System.exit(0);
            }
        } else
            System.out.println("Shortcut F11 already redefined; do not change.");

        refreshMenu();
    }

    private static void refreshLayersURL() {
        grabLayers = "";
        grabStyles = "";
        int countLayers = 0;
        if (Main.pref.getBoolean("cadastrewms.layerWater", true)) {
            grabLayers += LAYER_WATER + ",";
            grabStyles += STYLE_WATER + ",";
            countLayers++;
        }
        if (Main.pref.getBoolean("cadastrewms.layerBuilding", true)) {
            grabLayers += LAYER_BULDINGS + ",";
            grabStyles += STYLE_BUILDING + ",";
            countLayers++;
        }
        if (Main.pref.getBoolean("cadastrewms.layerSymbol", true)) {
            grabLayers += LAYER_SYMBOL + ",";
            grabStyles += STYLE_SYMBOL + ",";
            countLayers++;
        }
        if (Main.pref.getBoolean("cadastrewms.layerParcel", true)) {
            grabLayers += LAYER_PARCELS + ",";
            grabStyles += STYLE_PARCELS + ",";
            countLayers++;
        }
        if (Main.pref.getBoolean("cadastrewms.layerNumero", true)) {
            grabLayers += LAYER_NUMERO + ",";
            grabStyles += STYLE_NUMERO + ",";
            countLayers++;
        }
        if (Main.pref.getBoolean("cadastrewms.layerLabel", true)) {
            grabLayers += LAYER_LABEL + ",";
            grabStyles += STYLE_LABEL + ",";
            countLayers++;
        }
        if (Main.pref.getBoolean("cadastrewms.layerLieudit", true)) {
            grabLayers += LAYER_LIEUDIT + ",";
            grabStyles += STYLE_LIEUDIT + ",";
            countLayers++;
        }
        if (Main.pref.getBoolean("cadastrewms.layerSection", true)) {
            grabLayers += LAYER_SECTION + ",";
            grabStyles += STYLE_SECTION + ",";
            countLayers++;
        }
        if (Main.pref.getBoolean("cadastrewms.layerCommune", true)) {
            grabLayers += LAYER_COMMUNE + ",";
            grabStyles += STYLE_COMMUNE + ",";
            countLayers++;
        }
        if (countLayers > 2) { // remove the last ','
            grabLayers = grabLayers.substring(0, grabLayers.length()-1);
            grabStyles = grabStyles.substring(0, grabStyles.length()-1);
        } else {
            JOptionPane.showMessageDialog(Main.parent,tr("Please enable at least two WMS layers in the cadastre-fr " 
                    + "plugin configuration.\nLayers ''Building'' and ''Parcel'' added by default."));
            Main.pref.put("cadastrewms.layerBuilding", true);
            Main.pref.put("cadastrewms.layerParcel", true);
            grabLayers += LAYER_BULDINGS + "," + LAYER_PARCELS;
            grabStyles += STYLE_BUILDING + "," + STYLE_PARCELS;
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new CadastrePreferenceSetting();
    }

    private static void setEnabledAll(boolean isEnabled) {
        for (int i = 0; i < cadastreJMenu.getItemCount(); i++) {
            JMenuItem item = cadastreJMenu.getItem(i);
            if (item != null)
                if (item.getText().equals(MenuActionGrabPlanImage.name) /*||
                    item.getText().equals(MenuActionGrab.name) ||
                    item.getText().equals(MenuActionBoundaries.name) ||
                    item.getText().equals(MenuActionBuildings.name)*/) {
                    item.setEnabled(isEnabled);
                }
        }
        menuEnabled = isEnabled;
    }

    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (cadastreJMenu != null) {
            if (oldFrame == null && newFrame != null) {
                setEnabledAll(true);
                Main.map.addMapMode(new IconToggleButton(new WMSAdjustAction(Main.map)));
                Main.map.addMapMode(new IconToggleButton(new Address(Main.map)));
            } else if (oldFrame != null && newFrame == null) {
                setEnabledAll(false);
                //Lambert.layoutZone = -1;
                //LambertCC9Zones.layoutZone = -1;
            }
        }
    }

    public static boolean isCadastreProjection() {
        return Main.getProjection().toString().equals(new Lambert().toString())
            || Main.getProjection().toString().equals(new UTM_France_DOM().toString())
            || Main.getProjection().toString().equals(new LambertCC9Zones().toString());
    }

    public static void safeSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {}
    }

    // See OptionPaneUtil
    // FIXME: this is a temporary solution.
    public static void prepareDialog(JDialog dialog) {
        if (Main.pref.getBoolean("window-handling.option-pane-always-on-top", true)) {
            try {
                dialog.setAlwaysOnTop(true);
            } catch(SecurityException e) {
                System.out.println(tr("Warning: failed to put option pane dialog always on top. Exception was: {0}", e.toString()));
            }
        }
        dialog.setModal(true);
        dialog.toFront();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Adds the WMSLayer following this rule:<br/>
     * - if a WMSLayer exists place this new layer just before this layer<br/>
     * - Otherwise place it at the bottom
     * @param wmsLayer the wmsLayer to add
     */
    public static void addWMSLayer(WMSLayer wmsLayer) {
        if (Main.map != null && Main.map.mapView != null) {
            int wmsNewLayerPos = Main.map.mapView.getAllLayers().size();
            for(Layer l : Main.map.mapView.getLayersOfType(WMSLayer.class)) {
                int wmsPos = Main.map.mapView.getLayerPos(l);
                if (wmsPos < wmsNewLayerPos) wmsNewLayerPos = wmsPos;
            }
            Main.main.addLayer(wmsLayer);
            // Move the layer to its new position
            Main.map.mapView.moveLayer(wmsLayer, wmsNewLayerPos);
        } else
            Main.main.addLayer(wmsLayer);
    }

    private static String checkSourceMillesime() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentYear = calendar.get(java.util.Calendar.YEAR);
        String src = Main.pref.get("cadastrewms.source",
            "cadastre-dgi-fr source : Direction G\u00e9n\u00e9rale des Imp\u00f4ts - Cadastre. Mise \u00e0 jour : AAAA");
        String srcYear = src.substring(src.lastIndexOf(" ")+1);
        Integer year = null;
        try {
            year = Integer.decode(srcYear);
        } catch (NumberFormatException e) {}
        if (srcYear.equals("AAAA") || (year != null && year < currentYear)) {
            System.out.println("Replace source year "+srcYear+" by current year "+currentYear);
            src = src.substring(0, src.lastIndexOf(" ")+1)+currentYear;
            Main.pref.put("cadastrewms.source", src);
        }
        return src;
    }

}
