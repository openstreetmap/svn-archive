// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;


/**
 * GUI for exporting images.
 *
 * @author nokutu
 *
 */
public class StreetsideExportDialog extends JPanel implements ActionListener {

  private static final long serialVersionUID = -2746815082016025516L;
  /** Button to export all downloaded images. */
  public final JRadioButton all;
  /**
   * Button to export all images in the sequence of the selected StreetsideImage.
   */
  public final JRadioButton sequence;
  /**
   * Button to export all images belonging to the selected
   * {@link StreetsideImage} objects.
   */
  public final JRadioButton selected;
  /** Group of button containing all the options. */
  public final ButtonGroup group;
  private final JButton choose;
  private final JLabel path;
  /** File chooser. */
  public JFileChooser chooser;
  private final JButton ok;

  /**
   * Main constructor.
   *
   * @param ok
   *          The button for to OK option.
   */
  public StreetsideExportDialog(JButton ok) {
    this.ok = ok;
    ok.setEnabled(false);

    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    RewriteButtonAction action = new RewriteButtonAction(this);
    group = new ButtonGroup();
    all = new JRadioButton(action);
    all.setText(tr("Export all images"));
    sequence = new JRadioButton(action);
    sequence.setText(tr("Export selected sequence"));
    selected = new JRadioButton(action);
    selected.setText(tr("Export selected images"));
    group.add(all);
    group.add(sequence);
    group.add(selected);
    // Some options are disabled depending on the circumstances
    sequence.setEnabled(StreetsideLayer.getInstance().getData().getSelectedImage() instanceof StreetsideImage);
    if (StreetsideLayer.getInstance().getData().getMultiSelectedImages().isEmpty()) {
     selected.setEnabled(false);
    }

    path = new JLabel(tr("Select a directory"));
    choose = new JButton(tr("Explore"));
    choose.addActionListener(this);

    // All options belong to the same JPanel so the are in line.
    JPanel jpanel = new JPanel();
    jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.PAGE_AXIS));
    jpanel.add(all);
    jpanel.add(sequence);
    jpanel.add(selected);
    jpanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    path.setAlignmentX(Component.CENTER_ALIGNMENT);
    choose.setAlignmentX(Component.CENTER_ALIGNMENT);

    add(jpanel);
    add(path);
    add(choose);
  }

  /**
   * Creates the folder chooser GUI.
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    chooser = new JFileChooser();
    chooser.setCurrentDirectory(new java.io.File(System
        .getProperty("user.home")));
    chooser.setDialogTitle(tr("Select a directory"));
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      path.setText(chooser.getSelectedFile().toString());
      updateUI();
      ok.setEnabled(true);
    }
  }

  /**
   * Enables/disables some parts of the panel depending if the rewrite button is
   * active.
   *
   * @author nokutu
   *
   */
  public class RewriteButtonAction extends AbstractAction {

    private static final long serialVersionUID = 1035332841101190301L;

	private String lastPath;
    private final StreetsideExportDialog dlg;

    /**
     * Main constructor.
     *
     * @param dlg
     *          Parent dialog.
     */
    public RewriteButtonAction(StreetsideExportDialog dlg) {
      this.dlg = dlg;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public void actionPerformed(ActionEvent arg0) {
      choose
          .setEnabled(true);
      if (lastPath != null) {
        dlg.path.setText(lastPath);
      }
    }
  }
}
