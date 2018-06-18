// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.preferences.AbstractProperty.ValueChangeEvent;
import org.openstreetmap.josm.data.preferences.AbstractProperty.ValueChangeListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.StreetsidePlugin;
import org.openstreetmap.josm.plugins.streetside.io.download.StreetsideDownloader;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * If in "download images in visible area" mode, downloads all the images in the current view.
 *
 * @author nokutu
 *
 */
public class StreetsideDownloadViewAction extends JosmAction implements ValueChangeListener<String> {

  private static final long serialVersionUID = 6738276777802831669L;

  private static final String DESCRIPTION = I18n.marktr("Download Streetside images in current view");

  /**
   * Main constructor.
   */
  public StreetsideDownloadViewAction() {
    super(
      I18n.tr(DESCRIPTION),
      new ImageProvider(StreetsidePlugin.LOGO).setSize(ImageSizes.DEFAULT),
      I18n.tr(DESCRIPTION),
      Shortcut.registerShortcut("Streetside area", I18n.tr(DESCRIPTION), KeyEvent.VK_PERIOD, Shortcut.SHIFT),
      false,
      "streetsideArea",
      true
    );
    StreetsideProperties.DOWNLOAD_MODE.addListener(this);
    initEnabledState();
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    StreetsideDownloader.downloadVisibleArea();
  }

  @Override
  protected boolean listenToSelectionChange() {
    return false;
  }

  /**
   * Enabled when the Streetside layer is instantiated and download mode is either "osm area" or "manual".
   */
  @Override
  protected void updateEnabledState() {
    super.updateEnabledState();
    setEnabled(
      StreetsideLayer.hasInstance() && (
        StreetsideDownloader.getMode() == StreetsideDownloader.DOWNLOAD_MODE.OSM_AREA
        || StreetsideDownloader.getMode() == StreetsideDownloader.DOWNLOAD_MODE.MANUAL_ONLY
      )
    );
  }

  @Override
  public void valueChanged(ValueChangeEvent<? extends String> e) {
    updateEnabledState();
  }
}
