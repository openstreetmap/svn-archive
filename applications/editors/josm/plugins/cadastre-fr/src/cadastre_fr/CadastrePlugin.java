package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
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
 */
public class CadastrePlugin extends Plugin {
    static String VERSION = "1.4";

    static JMenu cadastreJMenu;

    public static CadastreGrabber cadastreGrabber = new CadastreGrabber();

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

    static private boolean menuEnabled = false;

    /**
     * Creates the plugin and setup the default settings if necessary
     *
     * @throws Exception
     */
    public CadastrePlugin() throws Exception {
        System.out.println("Pluging \"cadastre-fr\" started...");
        if (Main.pref.get("cadastrewms.cacheDir").equals(""))
            cacheDir = Main.pref.getPreferencesDir()+"plugins/cadastrewms/";
        else {
            cacheDir = Main.pref.get("cadastrewms.cacheDir");
            if (cacheDir.charAt(cacheDir.length()-1) != '\\' )
                cacheDir += '\\';
        }
        System.out.println("current cache directory: "+cacheDir);

        refreshConfiguration();

        UploadAction.registerUploadHook(new CheckSourceUploadHook());
    }

    public static void refreshMenu() {
        MainMenu menu = Main.main.menu;

        if (cadastreJMenu == null) {
            cadastreJMenu = menu.addMenu(marktr("Cadastre"), KeyEvent.VK_C, menu.defaultMenuPos);
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

            JMenuItem menuResetCookie = new JMenuItem(new MenuActionResetCookie());
            JMenuItem menuLambertZone = new JMenuItem(new MenuActionLambertZone());
            JMenuItem menuLoadFromCache = new JMenuItem(new MenuActionLoadFromCache());
            //JMenuItem menuActionBoundaries = new JMenuItem(new MenuActionBoundaries());
            //JMenuItem menuActionBuildings = new JMenuItem(new MenuActionBuildings());

            cadastreJMenu.add(menuGrab);
            cadastreJMenu.add(menuActionGrabPlanImage);
            cadastreJMenu.add(menuSettings);
            cadastreJMenu.add(menuSource);
            cadastreJMenu.add(menuResetCookie);
            cadastreJMenu.add(menuLambertZone);
            cadastreJMenu.add(menuLoadFromCache);
            // all SVG features disabled until official WMS is released
            //cadastreJMenu.add(menuActionBoundaries);
            //cadastreJMenu.add(menuActionBuildings);
        }
        setEnabledAll(menuEnabled);
    }

    public static void refreshConfiguration() {
        source = Main.pref.get("cadastrewms.source",
                "cadastre-dgi-fr source : Direction G\u00e9n\u00e9rale des Imp\u00f4ts - Cadastre ; mise \u00e0 jour : AAAA");
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
                    tr("Plugin cadastre-fr used traditionaly for grabbing the key shortcut F11\n"+
                    "which is currently allocated for full-screen switch by default\n"+
                    "Would you like to restore F11 for grab action ?"),
                    tr("Restore grab shortcut F11"),
                    JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.OK_OPTION) {
                System.out.println("redefine fullscreen shortcut F11 to shift+F11");
                Main.pref.put("shortcut.shortcut."+i, "menu:view:fullscreen;Toggle Full Screen view;122;5;122;64;false;true");
                JOptionPane.showMessageDialog(Main.parent,tr("JOSM is stopped for the change to take effect."));
                System.exit(0);
            }
        } else
            System.out.println("shortcut F11 already redefined; do not change");

        refreshMenu();
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
                } else if (item.getText().equals(MenuActionLambertZone.name)) {
                    item.setEnabled(!isEnabled);
                }
        }
        menuEnabled = isEnabled;
    }

    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (cadastreJMenu != null) {
            if (oldFrame == null && newFrame != null) {
                setEnabledAll(true);
                Main.map.addMapMode(new IconToggleButton
                        (new WMSAdjustAction(Main.map)));
            } else if (oldFrame != null && newFrame == null) {
                setEnabledAll(false);
                Lambert.layoutZone = -1;
                LambertCC9Zones.layoutZone = -1;
            }
        }
    }
    
    public static boolean isCadastreProjection() {
            return Main.proj.toString().equals(new Lambert().toString())
            || Main.proj.toString().equals(new UTM_20N_Guadeloupe_Fort_Marigot().toString())
            || Main.proj.toString().equals(new UTM_20N_Guadeloupe_Ste_Anne().toString())
            || Main.proj.toString().equals(new UTM_20N_Martinique_Fort_Desaix().toString())
            || Main.proj.toString().equals(new GaussLaborde_Reunion().toString())
            || Main.proj.toString().equals(new LambertCC9Zones().toString());
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
}
