// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.streetside.actions.StreetsideDownloadAction;
import org.openstreetmap.josm.plugins.streetside.actions.StreetsideDownloadViewAction;
import org.openstreetmap.josm.plugins.streetside.actions.StreetsideExportAction;
import org.openstreetmap.josm.plugins.streetside.actions.StreetsideWalkAction;
import org.openstreetmap.josm.plugins.streetside.actions.StreetsideZoomAction;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapBuilder;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsidePreferenceSetting;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideViewerDialog;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.ImageInfoHelpPopup;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.ImageInfoPanel;
import org.openstreetmap.josm.plugins.streetside.oauth.StreetsideUser;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This is the main class of the Streetside plugin.
 */
public class StreetsidePlugin extends Plugin {

  public static final ImageProvider LOGO = new ImageProvider("streetside-logo");

  /** Zoom action */
  private static final StreetsideZoomAction ZOOM_ACTION = new StreetsideZoomAction();
  /** Walk action */
  private static final StreetsideWalkAction WALK_ACTION = new StreetsideWalkAction();

  static {
      MainMenu.add(MainApplication.getMenu().fileMenu, new StreetsideExportAction(), false, 14);
      MainMenu.add(MainApplication.getMenu().imagerySubMenu, new StreetsideDownloadAction(), false);
      MainMenu.add(MainApplication.getMenu().viewMenu, ZOOM_ACTION, false, 15);
      MainMenu.add(MainApplication.getMenu().fileMenu, new StreetsideDownloadViewAction(), false, 14);
      MainMenu.add(MainApplication.getMenu().moreToolsMenu, WALK_ACTION, false);
  }

  /**
   * Main constructor.
   *
   * @param info
   *          Required information of the plugin. Obtained from the jar file.
   */
  public StreetsidePlugin(PluginInformation info) {
    super(info);

    if (StreetsideProperties.ACCESS_TOKEN.get() == null) {
      StreetsideUser.setTokenValid(false);
    }
  }

  static StreetsideDataListener[] getStreetsideDataListeners() {
	return new StreetsideDataListener[]{WALK_ACTION, ZOOM_ACTION, CubemapBuilder.getInstance()};
  }


  /**
   * @return the {@link StreetsideWalkAction} for the plugin
   */
  public static StreetsideWalkAction getStreetsideWalkAction() {
    return WALK_ACTION;
  }

  /**
   * Called when the JOSM map frame is created or destroyed.
   */
  @Override
  public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
    if (oldFrame == null && newFrame != null) { // map frame added
      MainApplication.getMap().addToggleDialog(StreetsideMainDialog.getInstance(), false);
      StreetsideMainDialog.getInstance().setImageInfoHelp(new ImageInfoHelpPopup(
    	  MainApplication.getMap().addToggleDialog(ImageInfoPanel.getInstance(), false)
      ));
      MainApplication.getMap().addToggleDialog(StreetsideViewerDialog.getInstance(), false);
    }
    if (oldFrame != null && newFrame == null) { // map frame destroyed
      StreetsideMainDialog.destroyInstance();
      ImageInfoPanel.destroyInstance();
      CubemapBuilder.destroyInstance();
      StreetsideViewerDialog.destroyInstance();
    }
  }

  @Override
  public PreferenceSetting getPreferenceSetting() {
    return new StreetsidePreferenceSetting();
  }

  /**
   * @return the current {@link MapView} without throwing a {@link NullPointerException}
   */
  public static MapView getMapView() {
    final MapFrame mf = MainApplication.getMap();
    if (mf != null) {
      return mf.mapView;
    }
    return null;
  }
}