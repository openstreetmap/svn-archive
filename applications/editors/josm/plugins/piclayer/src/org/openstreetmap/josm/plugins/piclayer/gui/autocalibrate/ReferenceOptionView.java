package org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

/**
 *
 * @author rebsc
 *
 */
public class ReferenceOptionView {

	public ReferenceOptionView() {
	}

	public int showAndChoose() {
		Object[] options = {tr("defined"), tr("manual")};
		String title = tr("AutoCalibration - choose selection type");
		String msg = tr("<html>Choose the type of selection you want to use.<br><br>"
				+ "If reference points are defined in shown layer,<br>"
				+ "choose <b>defined</b>-option and select points.<br>"
				+ "Else choose <b>manual</b>-option and set the calibration end points manual.</html>");

		int selected = JOptionPane.showOptionDialog(null,
                                            msg,
                                            title,
                                            JOptionPane.DEFAULT_OPTION,
                                            JOptionPane.INFORMATION_MESSAGE,
                                            null, options, options[0]);

		return selected;
	}

}
