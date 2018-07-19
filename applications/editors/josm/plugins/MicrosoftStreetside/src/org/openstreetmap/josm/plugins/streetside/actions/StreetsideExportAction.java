// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.StreetsidePlugin;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideExportDialog;
import org.openstreetmap.josm.plugins.streetside.io.export.StreetsideExportManager;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action that launches a StreetsideExportDialog and lets you export the images.
 *
 * @author nokutu
 *
 */
public class StreetsideExportAction extends JosmAction {

  private static final long serialVersionUID = 6131359489725632369L;

  final static Logger logger = Logger.getLogger(StreetsideExportAction.class);

  private StreetsideExportDialog dialog;

  /**
   * Main constructor.
   */
  public StreetsideExportAction() {
    super(tr("Export Streetside images"), new ImageProvider(StreetsidePlugin.LOGO).setSize(ImageSizes.DEFAULT),
        tr("Export Streetside images"), Shortcut.registerShortcut("Export Streetside",
            tr("Export Streetside images"), KeyEvent.CHAR_UNDEFINED,
            Shortcut.NONE), false, "streetsideExport", true);
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    JOptionPane pane = new JOptionPane();

    JButton ok = new JButton("Ok");
    ok.addActionListener(e -> pane.setValue(JOptionPane.OK_OPTION));
    JButton cancel = new JButton(tr("Cancel"));
    cancel.addActionListener(e -> pane.setValue(JOptionPane.CANCEL_OPTION));

    dialog = new StreetsideExportDialog(ok);
    pane.setMessage(dialog);
    pane.setOptions(new JButton[] {ok, cancel});

    JDialog dlg = pane.createDialog(Main.parent, tr("Export Streetside images"));
    dlg.setMinimumSize(new Dimension(400, 150));
    dlg.setVisible(true);

    // Checks if the inputs are correct and starts the export process.
    if (pane.getValue() != null
        && (int) pane.getValue() == JOptionPane.OK_OPTION
        && dialog.chooser != null) {
      if (dialog.group.isSelected(dialog.all.getModel())) {
        export(StreetsideLayer.getInstance().getData().getImages());
      } else if (dialog.group.isSelected(dialog.sequence.getModel())) {
        Set<StreetsideAbstractImage> images = new ConcurrentSkipListSet<>();
        for (StreetsideAbstractImage image : StreetsideLayer.getInstance().getData().getMultiSelectedImages()) {
          if (image instanceof StreetsideImage) {
            if (!images.contains(image)) {
              images.addAll(image.getSequence().getImages());
            }
          } else {
            images.add(image);
          }
        }
        export(images);
      } else if (dialog.group.isSelected(dialog.selected.getModel())) {
        export(StreetsideLayer.getInstance().getData().getMultiSelectedImages());
      }
    }
    dlg.dispose();
  }

  /**
   * Exports the given images from the database.
   *
   * @param images
   *          The set of images to be exported.
   */
  public void export(Set<StreetsideAbstractImage> images) {
    MainApplication.worker.execute(new StreetsideExportManager(images,
        dialog.chooser.getSelectedFile().toString()));
  }

  @Override
  protected boolean listenToSelectionChange() {
    return false;
  }

  /**
   * Enabled when streetside layer is in layer list
   */
  @Override
  protected void updateEnabledState() {
    super.updateEnabledState();
    setEnabled(StreetsideLayer.hasInstance());
  }
}
