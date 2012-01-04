package org.openstreetmap.josm.plugins.piclayer.actions.transform.affine;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.piclayer.actions.GenericPicTransformAction;
import org.openstreetmap.josm.tools.ImageProvider;

@SuppressWarnings("serial")
public class TransformPointAction extends GenericPicTransformAction {

	public TransformPointAction(MapFrame frame) {
		super(tr("PicLayer Transform point"), tr("Point transformed"), "transformpoint", tr("Transform point on the picture"), frame, ImageProvider.getCursor("crosshair", null));
	}

	@Override
	protected void doAction(MouseEvent e) {
		try {
			Point2D pressed = currentLayer.transformPoint(e.getPoint());
			if (selectedPoint != null) {
			    if (currentLayer.getTransformer().getOriginPoints().size() < 3)
			        JOptionPane.showMessageDialog(null, tr("You should have 3 checkpoints to transform the image!"), tr("PicLayer"), JOptionPane.ERROR_MESSAGE, null);
			    else
			        currentLayer.getTransformer().updatePair(selectedPoint, pressed);
			}

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
