package org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

/**
 *
 * @author rebsc
 *
 */
public class ResultCheckView {

	public ResultCheckView() {
	}

	public int showAndChoose() {
		Object[] options = {tr("accept"), tr("reset")};
		String title = tr("AutoCalibration - check calibration");
		String msg = tr("<html>Is this image calibrated the right way?</html>");

		int selected = JOptionPane.showOptionDialog(null,
                                            msg,
                                            title,
                                            JOptionPane.DEFAULT_OPTION,
                                            JOptionPane.INFORMATION_MESSAGE,
                                            null, options, options[0]);

		return selected;
	}


}
