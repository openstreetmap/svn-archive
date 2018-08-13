// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.io.session.SessionReader.registerSessionLayerImporter;
import static org.openstreetmap.josm.io.session.SessionWriter.registerSessionLayerExporter;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.data.projection.AbstractProjection;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.map.MapPreference;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionPreference;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionGrab;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionGrabPlanImage;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionLoadFromCache;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionNewLocation;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.MenuActionOpenPreferences;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.mapmode.Address;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.mapmode.WMSAdjustAction;
import org.openstreetmap.josm.plugins.fr.cadastre.actions.upload.CheckSourceUploadHook;
import org.openstreetmap.josm.plugins.fr.cadastre.download.CadastreDownloadSource;
import org.openstreetmap.josm.plugins.fr.cadastre.download.CadastreDownloadTask;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.pci.EdigeoPciImporter;
import org.openstreetmap.josm.plugins.fr.cadastre.preferences.CadastrePreferenceSetting;
import org.openstreetmap.josm.plugins.fr.cadastre.session.CadastreSessionExporter;
import org.openstreetmap.josm.plugins.fr.cadastre.session.CadastreSessionImporter;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

/**
 * Plugin to access the French Cadastre WMS server at <a href="https://www.cadastre.gouv.fr">
 * www.cadastre.gouv.fr</a>.<br>
 * This WMS server requires some specific handling like retrieving a cookie for a
 * limitation in case of no activity, or the request to the server shall provide
 * a city/town/village code.
 *
 * @author Pieren &lt;pieren3@gmail.com&gt;,
 *         &lt;matthieu.lochegnies@gmail.com&gt; for the extension to codeCommune;
 *         Don-vip&lt;vincent.privat@gmail.com&gt; for the maintenance and Edigeo support
 *
 * @version 3.0
 * <br>History:
 * <br>0.1 17-Jun-2008 first prototype using a first Lambert projection impl. in core
 * <br>0.2 22-Jun-2008 first stable version
 * <br>0.3 24-Jun-2008 add code departement
 * <br>0.4 06-Jul-2008 - add images scales, icons, menu items disabling
 * <br>                - remove dependencies of wmsplugin
 * <br>                - add option to force a Lambert zone (for median locations)
 * <br>                - add auto-sourcing
 * <br>0.5 16-Aug-2008 - add transparency in layer (allowing multiple wms layers displayed together)
 * <br>                - no overlapping of grabbed images if transparency is enabled
 * <br>                - set one layer per location
 * <br>                - use utf-8 charset in POST request to server
 * <br>                - improve the preferences setting dialog
 * <br>                - cancel the current download is now possible
 * <br>                - add automatic images caching and load on request (+ manage cache directory size)
 * <br>                - enable auto-sourcing only if a WMS layer is used
 * <br>0.6 18-Aug-2008 - suppress the null-exception message after the dialog 'open a layer first'
 * <br>                - process the overlapping images when cache is loaded from disk
 * <br>                - save the last 'new location request' text again in preferences
 * <br>                - avoid duplicate layers with same name
 * <br>                - set text input for new locations in upper case
 * <br>                - the cache directory is configurable in "cadastrewms.cacheDir"
 * <br>                - improve configuration change updates
 * <br>0.7 24-Aug-2008 - mask images only if transparency enabled
 * <br>                - validate projection name by Lambert.toString() method
 * <br>0.8 25-Jan-2009 - display returned list of communes if direct name is not recognized by server
 * <br>                - new possible grab factor of 100 square meters fixed size
 * <br>                - minor fixes due to changes in JOSM core classes
 * <br>                - first draft of raster image support
 * <br>0.9 05-Feb-2009 - grab vectorized full commune bbox, save in file, convert to OSM way
 *                       and simplify
 * <br>1.0 18-Feb-2009 - fix various bugs in color management and preference dialog
 * <br>                - increase PNG picture size requested to WMS (800x1000)
 * <br>                - set 4th grab scale fixed size configurable (from 25 to 1000 meters)
 * <br>1.1 11-Jun-2009 - fixed a null exception error when trying to displace a vectorized layer
 * <br>                - propose to use shortcut F11 for grabbing
 * <br>1.2 16-Aug-2009 - implementation of raster image grabbing, cropping and georeferencing (not the
 * <br>                  overview rasters (Tableau d'assemblage) but directly small units (Feuille)
 * <br>1.3 23-Aug-2009 - improve georeferencing action cancellation
 * <br>                - fixed bug of raster image loaded from cache not working on Java1.6
 * <br>                - improve mouse click bounce detection during georeferencing process
 * <br>1.4 25-Oct-2009 - add support for new Lambert CC 9 Zones projection
 * <br>                - add optional crosspieces display on raster image layers
 * <br>                - add automatic raster images georeferencing when WMS provides data
 * <br>                - re-implement manual adjustment mode in raster image layer
 * <br>1.5 21-Nov-2009 - major changes in projection in core : no magical zone prediction anymore for
 *                       Lambert 4 and 9 zones; grid translation implemented for Lambert 4 zones;
 *                       support of subprojections in preferences for zones setting and UTM20N
 * <br>                - removed autosourcing of empty new nodes
 * <br>1.6 28-Nov-2009 - Fix minor issues if Grab is called without layer (possible since projection rework)
 * <br>1.7 12-Dec-2009 - Change URL's changes for cookie and downgrade imgs resolution due to WMS changes
 * <br>1.8 11-Mar-2010 - filter the mouse button 1 during georeferencing
 * <br>                - retry if getting a new cookie failed (10 times during 30 seconds)
 * <br>                - cookie expiration automatically detected and renewed (after 30 minutes)
 * <br>                - proper WMS layer cleanup at destruction (workaround for memory leak)
 * <br>                - new cache format (v3) storing original image and cropped image bbox + angle
 * <br>                - new cache format (v4) storing original image size for later rotation
 * <br>                - cache files read compatible with previous formats
 * <br>                - raster image rotation issues fixed, now using shift+ctrl key instead of ctrl
 * <br>                - raster image adjustment using default system menu modifier (ctrl for windows) for Mac support
 * <br>                - image resolution configurable (high, medium, low) like the online interface
 * <br>                - layer selection configurable for vectorized images
 * <br>                - improved download cancellation
 * <br>                - from Erik Amzallag:
 * <br>                -     possibility to modify the auto-sourcing text just before upload
 * <br>                - from Clément Ménier:
 * <br>                -     new option allowing an auto-selection of the first cadastre layer for grab
 * <br>                -     non-modal JDialog in MenuActionGrabPlanImage
 * <br>                -     new options in the image filter (bilinear, bicubic)
 * <br>1.9 05-Apr-2010 - added a scroll bar in preferences
 * <br>                - download cancellation improved
 * <br>                - last deployment for Java1.5 compatibility
 * <br>2.0 07-Jul-2010 - update projection for "La Reunion" departement to RGR92, UTM40S.
 * <br>                - add 'departement' as option in the municipality selection
 * <br>                - fixed bug in cache directory size control (and disabled by default)
 * <br>                - add map mode for addressing
 * <br>                - from Nicolas Dumoulin:
 * <br>                -     add "tableau d'assemblage" in raster images for georeferencing (as option)
 * <br>2.1 14-Jan-2011 - add GrabThread moving the grab to a separate thread
 * <br>                - the divided BBox mode starts from the central square and loads the next in a spiral
 * <br>                - move the grabber from CadastrPlugin singleton to each wmsLayer instance to allow grabbing
 *                       of multiple municipalities in parallel.
 * <br>2.2 01-Jul-2011 - replace deprecated Main.proj by newest ProjectionRegistry.getProjection()
 * <br>                - fix list of raster images (Feuilles) parsing failing after a Cadastre server change/maintenance
 * <br>2.3 11-Jan-2013 - add various improvements from Don-Vip (Vincent Privat) trac #8175, #8229 and #5626.
 * <br>2.4 27-Jun-2013 - fix raster image georeferencing issues. Add new MenuActionRefineGeoRef for a new georeferencing
 *                       of already referenced plan image.
 * <br>2.5 06-Aug-2013 - fix transparency issue on new raster images. Temporary disable georeferences parsing not
 *                       working on new cadastre WMS.
 * <br>                - workaround on address help tool when switching to full screen
 * <br>                - improvement when clicking on existing node address street in mode relation
 * <br>                - option to simplify raster images in 2 bits colors (like images served in the past).
 * <br>2.6 10-Sep-2013 - add JOSM "sessions" feature support (list of layers stored in a file)
 * <br>2.7 26-Apr-2014 - switch to Java 7 + bugfixes
 * <br>2.8 21-Jul-2016 - switch to Java 8 + bugfixes
 * <br>2.9 23-Aug-2017 - use new HTTPS links from French cadastre - requires JOSM 12623+ to load Certigna certificate
 * <br>3.0 30-Sep-2017 - add support for direct access to Cadastre vectorial data (Edigeo files)
 */
public class CadastrePlugin extends Plugin {
    static String VERSION = "3.0";

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

    private static boolean menuEnabled = false;

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
     * Creates the plugin and setup the default settings if necessary.
     * @param info plugin information
     */
    public CadastrePlugin(PluginInformation info) {
        super(info);
        Logging.info("Pluging cadastre-fr v"+VERSION+" started...");
        initCacheDir();

        refreshConfiguration();

        UploadAction.registerUploadHook(new CheckSourceUploadHook());
        ExtensionFileFilter.addImporter(new EdigeoPciImporter());

        registerSessionLayerExporter(WMSLayer.class, CadastreSessionExporter.class);
        registerSessionLayerImporter("cadastre-fr", CadastreSessionImporter.class);

        MainApplication.getMenu().openLocation.addDownloadTaskClass(CadastreDownloadTask.class);
        DownloadDialog.addDownloadSource(new CadastreDownloadSource());
    }

    private static void initCacheDir() {
        if (Config.getPref().get("cadastrewms.cacheDir").isEmpty()) {
            cacheDir = new File(Config.getDirs().getCacheDirectory(true), "cadastrewms").getAbsolutePath();
        } else {
            cacheDir = Config.getPref().get("cadastrewms.cacheDir");
        }
        if (cacheDir.charAt(cacheDir.length()-1) != File.separatorChar)
            cacheDir += File.separatorChar;
        Logging.info("current cache directory: "+cacheDir);
    }

    public static void refreshMenu() {
        MainMenu menu = MainApplication.getMenu();

        if (cadastreJMenu == null) {
            cadastreJMenu = menu.addMenu("Cadastre", tr("Cadastre"), KeyEvent.VK_C, menu.getDefaultMenuPos(), ht("/Plugin/CadastreFr"));
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
                @Override
                public void actionPerformed(ActionEvent ev) {
                    Config.getPref().putBoolean("cadastrewms.autosourcing", menuSource.isSelected());
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
            //if (Config.getPref().getBoolean("cadastrewms.buildingsMenu", false))
            //    cadastreJMenu.add(menuActionBuildings);
            cadastreJMenu.add(menuLoadFromCache);
            // all SVG features disabled until official WMS is released
            //cadastreJMenu.add(menuActionBoundaries);
            cadastreJMenu.add(new JMenuItem(new MenuActionOpenPreferences()));
        }
        setEnabledAll(menuEnabled);
    }

    public static void refreshConfiguration() {
        source = checkSourceMillesime();
        autoSourcing = Config.getPref().getBoolean("cadastrewms.autosourcing", true);
        alterColors = Config.getPref().getBoolean("cadastrewms.alterColors");
        drawBoundaries = Config.getPref().getBoolean("cadastrewms.drawBoundaries", false);
        if (alterColors) {
            backgroundTransparent = Config.getPref().getBoolean("cadastrewms.backgroundTransparent");
            transparency = Float.parseFloat(Config.getPref().get("cadastrewms.brightness", "1.0f"));
        } else {
            backgroundTransparent = false;
            transparency = 1.0f;
        }
        String currentResolution = Config.getPref().get("cadastrewms.resolution", "high");
        if (currentResolution.equals("high")) {
            imageWidth = 1000; imageHeight = 800;
        } else if (currentResolution.equals("medium")) {
            imageWidth = 800; imageHeight = 600;
        } else {
            imageWidth = 600; imageHeight = 400;
        }
        refreshLayersURL();
        refreshMenu();
    }

    private static void refreshLayersURL() {
        grabLayers = "";
        grabStyles = "";
        int countLayers = 0;
        if (Config.getPref().getBoolean("cadastrewms.layerWater", true)) {
            grabLayers += LAYER_WATER + ",";
            grabStyles += STYLE_WATER + ",";
            countLayers++;
        }
        if (Config.getPref().getBoolean("cadastrewms.layerBuilding", true)) {
            grabLayers += LAYER_BULDINGS + ",";
            grabStyles += STYLE_BUILDING + ",";
            countLayers++;
        }
        if (Config.getPref().getBoolean("cadastrewms.layerSymbol", true)) {
            grabLayers += LAYER_SYMBOL + ",";
            grabStyles += STYLE_SYMBOL + ",";
            countLayers++;
        }
        if (Config.getPref().getBoolean("cadastrewms.layerParcel", true)) {
            grabLayers += LAYER_PARCELS + ",";
            grabStyles += STYLE_PARCELS + ",";
            countLayers++;
        }
        if (Config.getPref().getBoolean("cadastrewms.layerNumero", true)) {
            grabLayers += LAYER_NUMERO + ",";
            grabStyles += STYLE_NUMERO + ",";
            countLayers++;
        }
        if (Config.getPref().getBoolean("cadastrewms.layerLabel", true)) {
            grabLayers += LAYER_LABEL + ",";
            grabStyles += STYLE_LABEL + ",";
            countLayers++;
        }
        if (Config.getPref().getBoolean("cadastrewms.layerLieudit", true)) {
            grabLayers += LAYER_LIEUDIT + ",";
            grabStyles += STYLE_LIEUDIT + ",";
            countLayers++;
        }
        if (Config.getPref().getBoolean("cadastrewms.layerSection", true)) {
            grabLayers += LAYER_SECTION + ",";
            grabStyles += STYLE_SECTION + ",";
            countLayers++;
        }
        if (Config.getPref().getBoolean("cadastrewms.layerCommune", true)) {
            grabLayers += LAYER_COMMUNE + ",";
            grabStyles += STYLE_COMMUNE + ",";
            countLayers++;
        }
        if (countLayers > 2) { // remove the last ','
            grabLayers = grabLayers.substring(0, grabLayers.length()-1);
            grabStyles = grabStyles.substring(0, grabStyles.length()-1);
        } else {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Please enable at least two WMS layers in the cadastre-fr "
                    + "plugin configuration.\nLayers ''Building'' and ''Parcel'' added by default."));
            Config.getPref().putBoolean("cadastrewms.layerBuilding", true);
            Config.getPref().putBoolean("cadastrewms.layerParcel", true);
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
                if (item.getText().equals(MenuActionGrabPlanImage.NAME) /*||
                    item.getText().equals(MenuActionGrab.name) ||
                    item.getText().equals(MenuActionBoundaries.name) ||
                    item.getText().equals(MenuActionBuildings.name)*/) {
                    item.setEnabled(isEnabled);
                }
        }
        menuEnabled = isEnabled;
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (cadastreJMenu != null) {
            if (oldFrame == null && newFrame != null) {
                setEnabledAll(true);
                MainApplication.getMap().addMapMode(new IconToggleButton(new WMSAdjustAction()));
                MainApplication.getMap().addMapMode(new IconToggleButton(new Address()));
            } else if (oldFrame != null && newFrame == null) {
                setEnabledAll(false);
                //Lambert.layoutZone = -1;
                //LambertCC9Zones.layoutZone = -1;
            }
        }
    }

    public static boolean isLambert() {
        String code = ProjectionRegistry.getProjection().toCode();
        return Arrays.asList(ProjectionPreference.lambert.allCodes()).contains(code);
    }

    public static boolean isUtm_france_dom() {
        String code = ProjectionRegistry.getProjection().toCode();
        return Arrays.asList(ProjectionPreference.utm_france_dom.allCodes()).contains(code);
    }

    public static boolean isLambert_cc9() {
        String code = ProjectionRegistry.getProjection().toCode();
        return Arrays.asList(ProjectionPreference.lambert_cc9.allCodes()).contains(code);
    }

    public static boolean isCadastreProjection() {
        return isLambert() || isUtm_france_dom() || isLambert_cc9();
    }

    public static int getCadastreProjectionLayoutZone() {
        int zone = -1;
        Projection proj = ProjectionRegistry.getProjection();
        if (proj instanceof AbstractProjection) {
            Integer code = ((AbstractProjection) proj).getEpsgCode();
            if (code != null) {
                if (code >= 3942 && code <= 3950) {                 // LambertCC9Zones
                    zone = code - 3942;
                } else if (code >= 27561 && 27564 <= code) {        // Lambert
                    zone = code - 27561;
                } else {                                            // UTM_France_DOM
                    Map<Integer, Integer> utmfr = new HashMap<>();
                    utmfr.put(2969, 0);
                    utmfr.put(2970, 1);
                    utmfr.put(2973, 2);
                    utmfr.put(2975, 3);
                    utmfr.put(2972, 4);
                    if (utmfr.containsKey(code)) {
                        zone = utmfr.get(code);
                    }
                }
            }
        }
        return zone;
    }

    public static void safeSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Logging.debug(e);
        }
    }

    // See OptionPaneUtil
    // FIXME: this is a temporary solution.
    public static void prepareDialog(JDialog dialog) {
        if (Config.getPref().getBoolean("window-handling.option-pane-always-on-top", true)) {
            try {
                dialog.setAlwaysOnTop(true);
            } catch (SecurityException e) {
                Logging.warn(tr("Warning: failed to put option pane dialog always on top. Exception was: {0}", e.toString()));
            }
        }
        dialog.setModal(true);
        dialog.toFront();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Adds the WMSLayer following this rule:<ul>
     * <li>if a WMSLayer exists place this new layer just before this layer</li>
     * <li>Otherwise place it at the bottom</li>
     * </ul>
     * @param wmsLayer the wmsLayer to add
     */
    public static void addWMSLayer(WMSLayer wmsLayer) {
        if (MainApplication.isDisplayingMapView()) {
            int wmsNewLayerPos = MainApplication.getLayerManager().getLayers().size();
            for (Layer l : MainApplication.getLayerManager().getLayersOfType(WMSLayer.class)) {
                int wmsPos = MainApplication.getLayerManager().getLayers().indexOf(l);
                if (wmsPos < wmsNewLayerPos) wmsNewLayerPos = wmsPos;
            }
            MainApplication.getLayerManager().addLayer(wmsLayer);
            // Move the layer to its new position
            MainApplication.getMap().mapView.moveLayer(wmsLayer, wmsNewLayerPos);
        } else
            MainApplication.getLayerManager().addLayer(wmsLayer);
    }

    private static String checkSourceMillesime() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String src = Config.getPref().get("cadastrewms.source",
            "cadastre-dgi-fr source : Direction G\u00e9n\u00e9rale des Imp\u00f4ts - Cadastre. Mise \u00e0 jour : AAAA");
        String srcYear = src.substring(src.lastIndexOf(" ")+1);
        Integer year = null;
        try {
            year = Integer.decode(srcYear);
        } catch (NumberFormatException e) {
            Logging.debug(e);
        }
        if (srcYear.equals("AAAA") || (year != null && year < currentYear)) {
            Logging.info("Replace source year "+srcYear+" by current year "+currentYear);
            src = src.substring(0, src.lastIndexOf(" ")+1)+currentYear;
            Config.getPref().put("cadastrewms.source", src);
        }
        return src;
    }

    /**
     * Ask to change projection if current one is not suitable for French cadastre.
     */
    public static void askToChangeProjection() {
        GuiHelper.runInEDTAndWait(new Runnable() {
            @Override
            public void run() {
                if (JOptionPane.showConfirmDialog(MainApplication.getMainFrame(),
                        tr("To enable the cadastre WMS plugin, change\n"
                                + "the current projection to one of the cadastre\n"
                                + "projections and retry"),
                                tr("Change the current projection"), JOptionPane.OK_CANCEL_OPTION)
                    == JOptionPane.OK_OPTION) {
                    PreferenceDialog p = new PreferenceDialog(MainApplication.getMainFrame());
                    p.selectPreferencesTabByClass(MapPreference.class);
                    p.getTabbedPane().getSetting(ProjectionPreference.class).selectProjection(ProjectionPreference.lambert_cc9);
                    p.setVisible(true);
                }
            }
        });
    }
}
