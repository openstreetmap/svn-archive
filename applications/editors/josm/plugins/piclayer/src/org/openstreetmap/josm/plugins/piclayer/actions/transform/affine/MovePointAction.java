package org.openstreetmap.josm.plugins.piclayer.actions.transform.affine;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.piclayer.actions.GenericPicTransformAction;
import org.openstreetmap.josm.tools.ImageProvider;

// old version - ctrl move point (not transforming picture)
@SuppressWarnings("serial")
public class MovePointAction extends GenericPicTransformAction {

	public MovePointAction(MapFrame frame) {
        super(tr("PicLayer Move point"), tr("Point added/moved"), "movepoint", tr("Drag or create point on the picture"), frame, ImageProvider.getCursor("crosshair", null));
	}

	@Override
	protected void doAction(MouseEvent e) {
		try {
			Point2D pressed = currentLayer.transformPoint(e.getPoint());
			if (selectedPoint != null) {
				currentLayer.getTransformer().replaceOriginPoint(selectedPoint, pressed);
				selectedPoint = pressed;
			}
		} catch (NoninvertibleTransformException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (currentLayer == null)
			return;

		try {
			Point2D pressed = currentLayer.transformPoint(e.getPoint());
			if (selectedPoint == null)
				currentLayer.getTransformer().addOriginPoint(pressed);

			currentCommand.addIfChanged();
		} catch (NoninvertibleTransformException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void enterMode() {
	    super.enterMode();
	    updateDrawPoints(true);
	}

	@Override
	public void exitMode() {
	    super.exitMode();
	    updateDrawPoints(false);
	}
}
