// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.mode;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideData;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.plugins.streetside.history.StreetsideRecord;
import org.openstreetmap.josm.plugins.streetside.history.commands.CommandMove;
import org.openstreetmap.josm.plugins.streetside.history.commands.CommandTurn;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;

/**
 * Handles the input event related with the layer. Mainly clicks.
 *
 * @author nokutu
 */
public class SelectMode extends AbstractMode {
  private StreetsideAbstractImage closest;
  private StreetsideAbstractImage lastClicked;
  private final StreetsideRecord record;
  private boolean nothingHighlighted;
  private boolean imageHighlighted;

  /**
   * Main constructor.
   */
  public SelectMode() {
    record = StreetsideRecord.getInstance();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.getButton() != MouseEvent.BUTTON1) {
      return;
    }
    StreetsideAbstractImage closest = getClosest(e.getPoint());
    if (!(MainApplication.getLayerManager().getActiveLayer() instanceof StreetsideLayer)
            && closest != null && MainApplication.getMap().mapMode == MainApplication.getMap().mapModeSelect) {
      lastClicked = this.closest;
      StreetsideLayer.getInstance().getData().setSelectedImage(closest);
      return;
    } else if (MainApplication.getLayerManager().getActiveLayer() != StreetsideLayer.getInstance()) {
      return;
    }
    // Double click
    if (e.getClickCount() == 2 && StreetsideLayer.getInstance().getData().getSelectedImage() != null && closest != null) {
      closest.getSequence().getImages().forEach(StreetsideLayer.getInstance().getData()::addMultiSelectedImage);
    }
    lastClicked = this.closest;
    this.closest = closest;
    if (closest != null && StreetsideLayer.getInstance().getData().getMultiSelectedImages().contains(closest)) {
      return;
    }
    // ctrl+click
    if (e.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.CTRL_MASK) && closest != null) {
      StreetsideLayer.getInstance().getData().addMultiSelectedImage(closest);
      // shift + click
    } else if (
            e.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)
                    && lastClicked instanceof StreetsideImage
            ) {
      if (this.closest != null
              && this.closest.getSequence() == lastClicked.getSequence()) {
        int i = this.closest.getSequence().getImages().indexOf(this.closest);
        int j = lastClicked.getSequence().getImages().indexOf(lastClicked);
        StreetsideLayer.getInstance().getData().addMultiSelectedImage(
                i < j
                        ? new ConcurrentSkipListSet<>(this.closest.getSequence().getImages().subList(i, j + 1))
                        : new ConcurrentSkipListSet<>(this.closest.getSequence().getImages().subList(j, i + 1))
        );
      }
      // click
    } else {
      StreetsideLayer.getInstance().getData().setSelectedImage(closest);
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    StreetsideAbstractImage highlightImg = StreetsideLayer.getInstance().getData().getHighlightedImage();
    if (
            MainApplication.getLayerManager().getActiveLayer() == StreetsideLayer.getInstance()
                && SwingUtilities.isLeftMouseButton(e)
                && highlightImg != null && highlightImg.getLatLon() != null
            ) {
      Point highlightImgPoint = MainApplication.getMap().mapView.getPoint(highlightImg.getTempLatLon());
      if (e.isShiftDown()) { // turn
        StreetsideLayer.getInstance().getData().getMultiSelectedImages().parallelStream().filter(img -> !(img instanceof StreetsideImage) || StreetsideProperties.DEVELOPER.get())
                .forEach(img -> img.turn(Math.toDegrees(Math.atan2(e.getX() - highlightImgPoint.getX(), -e.getY() + highlightImgPoint.getY())) - highlightImg.getTempHe()));
      } else { // move
        LatLon eventLatLon = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
        LatLon imgLatLon = MainApplication.getMap().mapView.getLatLon(highlightImgPoint.getX(), highlightImgPoint.getY());
        StreetsideLayer.getInstance().getData().getMultiSelectedImages().parallelStream().filter(img -> !(img instanceof StreetsideImage) || StreetsideProperties.DEVELOPER.get())
                .forEach(img -> img.move(eventLatLon.getX() - imgLatLon.getX(), eventLatLon.getY() - imgLatLon.getY()));
      }
      StreetsideLayer.invalidateInstance();
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    final StreetsideData data = StreetsideLayer.getInstance().getData();
    if (data.getSelectedImage() == null) {
      return;
    }
    if (!Objects.equals(data.getSelectedImage().getTempHe(), data.getSelectedImage().getMovingHe())) {
      double from = data.getSelectedImage().getTempHe();
      double to = data.getSelectedImage().getMovingHe();
      record.addCommand(new CommandTurn(data.getMultiSelectedImages(), to - from));
    } else if (!Objects.equals(data.getSelectedImage().getTempLatLon(), data.getSelectedImage().getMovingLatLon())) {
      LatLon from = data.getSelectedImage().getTempLatLon();
      LatLon to = data.getSelectedImage().getMovingLatLon();
      record.addCommand(new CommandMove(data.getMultiSelectedImages(), to.getX() - from.getX(), to.getY() - from.getY()));
    }
    data.getMultiSelectedImages().parallelStream().filter(Objects::nonNull).forEach(StreetsideAbstractImage::stopMoving);
    StreetsideLayer.invalidateInstance();
  }

  /**
   * Checks if the mouse is over pictures.
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    if (MainApplication.getLayerManager().getActiveLayer() instanceof OsmDataLayer
            && MainApplication.getMap().mapMode != MainApplication.getMap().mapModeSelect) {
      return;
    }
    if (!StreetsideProperties.HOVER_ENABLED.get()) {
      return;
    }

    StreetsideAbstractImage closestTemp = getClosest(e.getPoint());

    final OsmDataLayer editLayer = MainApplication.getLayerManager().getEditLayer();
    if (editLayer != null) {
      if (closestTemp != null && !imageHighlighted) {
        if (MainApplication.getMap().mapMode != null) {
          MainApplication.getMap().mapMode.putValue("active", Boolean.FALSE);
        }
        imageHighlighted = true;
      } else if (closestTemp == null && imageHighlighted && nothingHighlighted) {
        if (MainApplication.getMap().mapMode != null) {
          MainApplication.getMap().mapMode.putValue("active", Boolean.TRUE);
        }
        nothingHighlighted = false;
      } else if (imageHighlighted && !nothingHighlighted && editLayer.data != null) {
        for (OsmPrimitive primivitive : MainApplication.getLayerManager().getEditLayer().data.allPrimitives()) {
          primivitive.setHighlighted(false);
        }
        imageHighlighted = false;
        nothingHighlighted = true;
      }
    }

    if (StreetsideLayer.getInstance().getData().getHighlightedImage() != closestTemp && closestTemp != null) {
      StreetsideLayer.getInstance().getData().setHighlightedImage(closestTemp);
      StreetsideMainDialog.getInstance().setImage(closestTemp);
      StreetsideMainDialog.getInstance().updateImage(false);

    } else if (StreetsideLayer.getInstance().getData().getHighlightedImage() != closestTemp && closestTemp == null) {
      StreetsideLayer.getInstance().getData().setHighlightedImage(null);
      StreetsideMainDialog.getInstance().setImage(StreetsideLayer.getInstance().getData().getSelectedImage());
      StreetsideMainDialog.getInstance().updateImage();
    }

    StreetsideLayer.invalidateInstance();
  }

  @Override
  public void paint(Graphics2D g, MapView mv, Bounds box) {
  }

  @Override
  public String toString() {
    return tr("Select mode");
  }
}
