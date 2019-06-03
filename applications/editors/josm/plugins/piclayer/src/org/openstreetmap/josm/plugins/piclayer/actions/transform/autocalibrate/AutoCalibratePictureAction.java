package org.openstreetmap.josm.plugins.piclayer.actions.transform.autocalibrate;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.piclayer.actions.GenericPicTransformAction;
import org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate.CalibrationWindow;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Class providing auto calibration action.
 * This class works with {@link AutoCalibrateHandler} class as handler to call calibration action
 * and manage necessary GUIs.
 * Info at https://wiki.openstreetmap.org/wiki/User:Rebsc
 * @author rebsc
 *
 */
public class AutoCalibratePictureAction extends GenericPicTransformAction {

	private static final Logger logger = Logger.getLogger(AutoCalibratePictureAction.class.getName());
	private AutoCalibrateHandler calibrationHandler;
	private CalibrationWindow calibrationWindow;


	public AutoCalibratePictureAction() {
		super(tr("PicLayer auto calibration"), tr("Calibrated"),"autoCalibrate", tr("Calibrate picture with outline"),
				ImageProvider.getCursor("crosshair", null));
		logger.info(this.getClass().getName() + " has been created.");

    	calibrationHandler = new AutoCalibrateHandler();
	}

	@Override
	protected void doAction(MouseEvent e) {
		// do nothing
	}

    @Override
    public void enterMode() {
    	super.enterMode();
    	updateDrawPoints(true);

		currentLayer = (PicLayerAbstract) MainApplication.getLayerManager().getActiveLayer();

		if(currentLayer != null) {
			calibrationHandler.prepare(currentLayer);
			calibrationWindow = calibrationHandler.getMainWindow();
			calibrationWindow.setVisible(true);
		}

    }

    @Override
    public void exitMode() {
        super.exitMode();
        updateDrawPoints(false);
    }

}
