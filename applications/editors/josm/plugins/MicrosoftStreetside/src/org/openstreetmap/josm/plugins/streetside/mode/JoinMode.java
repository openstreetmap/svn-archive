// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.mode;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideImportedImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.history.StreetsideRecord;
import org.openstreetmap.josm.plugins.streetside.history.commands.CommandJoin;
import org.openstreetmap.josm.plugins.streetside.history.commands.CommandUnjoin;

/**
 * In this mode the user can join pictures to make sequences or unjoin them.
 *
 * @author nokutu
 *
 */
public class JoinMode extends AbstractMode {

  private StreetsideImportedImage lastClick;
  private MouseEvent lastPos;

  /**
   * Main constructor.
   */
  public JoinMode() {
    this.cursor = Cursor.CROSSHAIR_CURSOR;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    final StreetsideAbstractImage highlighted = StreetsideLayer.getInstance().getData().getHighlightedImage();
    if (highlighted == null) {
      return;
    }
    if (this.lastClick == null && highlighted instanceof StreetsideImportedImage) {
      this.lastClick = (StreetsideImportedImage) highlighted;
    } else if (this.lastClick != null
        && highlighted instanceof StreetsideImportedImage) {
      if (
        (
          (highlighted.previous() == null && this.lastClick.next() == null) ||
          (highlighted.next() == null && this.lastClick.previous() == null)
        )
        && highlighted.getSequence() != this.lastClick.getSequence()
      ) {
        StreetsideRecord.getInstance().addCommand(new CommandJoin(this.lastClick, highlighted));
      } else if (this.lastClick.next() == highlighted || this.lastClick.previous() == highlighted) {
        StreetsideRecord.getInstance().addCommand(
          new CommandUnjoin(Arrays.asList(this.lastClick, highlighted))
        );
      }
      this.lastClick = null;
    }
    StreetsideLayer.invalidateInstance();
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    this.lastPos = e;
    if (!(MainApplication.getLayerManager().getActiveLayer() instanceof StreetsideLayer))
      return;
    StreetsideAbstractImage closestTemp = getClosest(e.getPoint());
    StreetsideLayer.getInstance().getData().setHighlightedImage(closestTemp);
    StreetsideLayer.invalidateInstance();
  }

  @Override
  public void paint(Graphics2D g, MapView mv, Bounds box) {
    if (this.lastClick != null) {
      g.setColor(Color.WHITE);
      Point p0 = mv.getPoint(this.lastClick.getMovingLatLon());
      Point p1 = this.lastPos.getPoint();
      g.drawLine(p0.x, p0.y, p1.x, p1.y);
    }
  }

  @Override
  public String toString() {
    return tr("Join mode");
  }
}
