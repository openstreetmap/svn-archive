// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.StreetsidePlugin;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideWalkDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;

/**
 * Walks forward at a given interval.
 *
 * @author nokutu
 *
 */
public class StreetsideWalkAction extends JosmAction implements StreetsideDataListener {

  private static final long serialVersionUID = 3454223919402245818L;

  private WalkThread thread;
  private final List<WalkListener> listeners = new ArrayList<>();

  /**
   *
   */
  public StreetsideWalkAction() {
    super(tr("Walk mode"), new ImageProvider(StreetsidePlugin.LOGO).setSize(ImageSizes.DEFAULT),
        tr("Walk mode"), null,
        false, "streetsideWalk", true);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    StreetsideWalkDialog dialog = new StreetsideWalkDialog();
    JOptionPane pane = new JOptionPane(dialog, JOptionPane.PLAIN_MESSAGE,
        JOptionPane.OK_CANCEL_OPTION);
    JDialog dlg = pane.createDialog(Main.parent, tr("Walk mode"));
    dlg.setMinimumSize(new Dimension(400, 150));
    dlg.setVisible(true);
    if (pane.getValue() != null
        && (int) pane.getValue() == JOptionPane.OK_OPTION) {
      thread = new WalkThread((int) dialog.spin.getValue(),
          dialog.waitForPicture.isSelected(),
          dialog.followSelection.isSelected(), dialog.goForward.isSelected());
      fireWalkStarted();
      thread.start();
      StreetsideMainDialog.getInstance().setMode(StreetsideMainDialog.MODE.WALK);
    }
  }

  @Override
  public void imagesAdded() {
    // Nothing
  }

  /**
   * Adds a listener.
   *
   * @param lis
   *          The listener to be added.
   */
  public void addListener(WalkListener lis) {
    listeners.add(lis);
  }

  /**
   * Removes a listener.
   *
   * @param lis
   *          The listener to be added.
   */
  public void removeListener(WalkListener lis) {
    listeners.remove(lis);
  }

  private void fireWalkStarted() {
    if (listeners.isEmpty()) {
      return;
    }
    for (WalkListener lis : listeners) {
      lis.walkStarted(thread);
    }
  }

  @Override
  protected boolean listenToSelectionChange() {
    return false;
  }

  @Override
  public void selectedImageChanged(StreetsideAbstractImage oldImage, StreetsideAbstractImage newImage) {
    if (oldImage == null && newImage != null) {
      setEnabled(true);
    } else if (oldImage != null && newImage == null) {
      setEnabled(false);
    }
  }

  /**
   * Enabled when a mapillary image is selected.
   */
  @Override
  protected void updateEnabledState() {
    super.updateEnabledState();
    setEnabled(StreetsideLayer.hasInstance() && StreetsideLayer.getInstance().getData().getSelectedImage() != null);
  }

}
