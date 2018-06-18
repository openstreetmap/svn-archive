// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog to set the walk mode options.
 *
 * @author nokutu
 *
 */
public class StreetsideWalkDialog extends JPanel {

  private static final long serialVersionUID = 7974881240732957573L;

  /** Spin containing the interval value. */
  public SpinnerModel spin;
  /** Whether it must wait for the picture to be downloaded */
  public JCheckBox waitForPicture;
  /** Whether the view must follow the selected image. */
  public JCheckBox followSelection;
  /** Go forward or backwards */
  public JCheckBox goForward;

  /**
   * Main constructor
   */
  public StreetsideWalkDialog() {
    JPanel interval = new JPanel();
    spin = new SpinnerNumberModel(2000, 500, 10000, 500);
    interval.add(new JLabel("Interval (miliseconds): "));
    interval.add(new JSpinner(spin));
    add(interval);

    waitForPicture = new JCheckBox(tr("Wait for full quality pictures"));
    waitForPicture.setSelected(true);
    add(waitForPicture);

    followSelection = new JCheckBox(tr("Follow selected image"));
    followSelection.setSelected(true);
    add(followSelection);

    goForward = new JCheckBox(tr("Go forward"));
    goForward.setSelected(true);
    add(goForward);
  }
}
