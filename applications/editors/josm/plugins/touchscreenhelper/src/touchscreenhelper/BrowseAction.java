package touchscreenhelper;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.data.coor.EastNorth;

public class BrowseAction extends MapMode implements MouseListener,
    MouseMotionListener {

    public BrowseAction(MapFrame mapFrame) {
        super(tr("Browse"), "browse", tr("Browse map with left button"),
            mapFrame, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    @Override public void enterMode() {
        super.enterMode();

        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();

        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
    }

    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) !=
            MouseEvent.BUTTON1_DOWN_MASK) {
            endMovement();
            return;
        }

        if (mousePosMove == null)
            startMovement(e);
        EastNorth center = Main.map.mapView.getCenter();
        EastNorth mouseCenter = Main.map.mapView.getEastNorth(e.getX(), e.getY());
        Main.map.mapView.zoomTo(new EastNorth(
            mousePosMove.east() + center.east() - mouseCenter.east(),
            mousePosMove.north() + center.north() - mouseCenter.north()));
    }

    @Override public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            startMovement(e);
    }

    @Override public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            endMovement();
    }

    private EastNorth mousePosMove;
    private boolean movementInPlace = false;

    private void startMovement(MouseEvent e) {
        if (movementInPlace)
            return;
        movementInPlace = true;
        mousePosMove = Main.map.mapView.getEastNorth(e.getX(), e.getY());
    }

    private void endMovement() {
        if (!movementInPlace)
            return;
        movementInPlace = false;
        mousePosMove = null;
    }
}
