package org.openstreetmap.josm.plugins.piclayer.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.plugins.piclayer.transform.PictureTransform;

@SuppressWarnings("serial")
public class ResetCalibrationAction extends JosmAction {

	private PicLayerAbstract layer;
	public ResetCalibrationAction(PicLayerAbstract layer, PictureTransform transformer) {
		super(tr("Reset Calibration"), null, tr("Reset calibration"), null, false);
		this.layer = layer;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		layer.resetCalibration();
		Main.map.mapView.repaint();
	}
}
