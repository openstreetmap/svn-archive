// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;

import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.actions.StreetsideDownloadAction;
import org.openstreetmap.josm.plugins.streetside.model.ImageDetection;
import org.openstreetmap.josm.plugins.streetside.model.MapObject;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideColorScheme;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;


/**
 * This object is a responsible JComponent which lets you zoom and drag. It is
 * included in a {@link StreetsideMainDialog} object.
 *
 * @author nokutu
 * @see StreetsideImageDisplay
 * @see StreetsideMainDialog
 */
public class StreetsideImageDisplay extends JComponent {

  private static final long serialVersionUID = -3188274185432686201L;

  private final Collection<ImageDetection> detections = new ArrayList<>();

  /** The image currently displayed */
  private volatile BufferedImage image;

  /**
   * The rectangle (in image coordinates) of the image that is visible. This
   * rectangle is calculated each time the zoom is modified
   */
  private volatile Rectangle visibleRect;

  /**
   * When a selection is done, the rectangle of the selection (in image
   * coordinates)
   */
  private Rectangle selectedRect;

  private class ImgDisplayMouseListener implements MouseListener, MouseWheelListener, MouseMotionListener {
    private boolean mouseIsDragging;
    private long lastTimeForMousePoint;
    private Point mousePointInImg;

    /**
     * Zoom in and out, trying to preserve the point of the image that was under
     * the mouse cursor at the same place
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      Image image;
      Rectangle visibleRect;
      synchronized (StreetsideImageDisplay.this) {
        image = getImage();
        visibleRect = StreetsideImageDisplay.this.visibleRect;
      }
      mouseIsDragging = false;
      selectedRect = null;
      if (image != null && Math.min(getSize().getWidth(), getSize().getHeight()) > 0) {
        // Calculate the mouse cursor position in image coordinates, so that
        // we can center the zoom
        // on that mouse position.
        // To avoid issues when the user tries to zoom in on the image
        // borders, this point is not calculated
        // again if there was less than 1.5seconds since the last event.
        if (e.getWhen() - lastTimeForMousePoint > 1500 || mousePointInImg == null) {
          lastTimeForMousePoint = e.getWhen();
          mousePointInImg = comp2imgCoord(visibleRect, e.getX(), e.getY());
        }
        // Set the zoom to the visible rectangle in image coordinates
        if (e.getWheelRotation() > 0) {
          visibleRect.width = visibleRect.width * 3 / 2;
          visibleRect.height = visibleRect.height * 3 / 2;
        } else {
          visibleRect.width = visibleRect.width * 2 / 3;
          visibleRect.height = visibleRect.height * 2 / 3;
        }
        // Check that the zoom doesn't exceed 2:1
        if (visibleRect.width < getSize().width / 2) {
          visibleRect.width = getSize().width / 2;
        }
        if (visibleRect.height < getSize().height / 2) {
          visibleRect.height = getSize().height / 2;
        }
        // Set the same ratio for the visible rectangle and the display area
        int hFact = visibleRect.height * getSize().width;
        int wFact = visibleRect.width * getSize().height;
        if (hFact > wFact) {
          visibleRect.width = hFact / getSize().height;
        } else {
          visibleRect.height = wFact / getSize().width;
        }
        // The size of the visible rectangle is limited by the image size.
        checkVisibleRectSize(image, visibleRect);
        // Set the position of the visible rectangle, so that the mouse
        // cursor doesn't move on the image.
        Rectangle drawRect = calculateDrawImageRectangle(visibleRect);
        visibleRect.x = mousePointInImg.x
            + ((drawRect.x - e.getX()) * visibleRect.width) / drawRect.width;
        visibleRect.y = mousePointInImg.y
            + ((drawRect.y - e.getY()) * visibleRect.height) / drawRect.height;
        // The position is also limited by the image size
        checkVisibleRectPos(image, visibleRect);
        synchronized (StreetsideImageDisplay.this) {
          StreetsideImageDisplay.this.visibleRect = visibleRect;
        }
        StreetsideImageDisplay.this.repaint();
      }
    }

    /** Center the display on the point that has been clicked */
    @Override
    public void mouseClicked(MouseEvent e) {
      // Move the center to the clicked point.
      Image image;
      Rectangle visibleRect;
      synchronized (StreetsideImageDisplay.this) {
        image = getImage();
        visibleRect = StreetsideImageDisplay.this.visibleRect;
      }
      if (image != null && Math.min(getSize().getWidth(), getSize().getHeight()) > 0) {
        if (e.getButton() == StreetsideProperties.PICTURE_OPTION_BUTTON.get()) {
          if (!StreetsideImageDisplay.this.visibleRect.equals(new Rectangle(0, 0, image.getWidth(null), image.getHeight(null)))) {
            // Zooms to 1:1
            StreetsideImageDisplay.this.visibleRect = new Rectangle(0, 0,
                image.getWidth(null), image.getHeight(null));
          } else {
            // Zooms to best fit.
            StreetsideImageDisplay.this.visibleRect = new Rectangle(
                0,
                (image.getHeight(null) - (image.getWidth(null) * getHeight()) / getWidth()) / 2,
                image.getWidth(null),
                (image.getWidth(null) * getHeight()) / getWidth()
            );
          }
          StreetsideImageDisplay.this.repaint();
          return;
        } else if (e.getButton() != StreetsideProperties.PICTURE_DRAG_BUTTON.get()) {
          return;
        }
        // Calculate the translation to set the clicked point the center of
        // the view.
        Point click = comp2imgCoord(visibleRect, e.getX(), e.getY());
        Point center = getCenterImgCoord(visibleRect);
        visibleRect.x += click.x - center.x;
        visibleRect.y += click.y - center.y;
        checkVisibleRectPos(image, visibleRect);
        synchronized (StreetsideImageDisplay.this) {
          StreetsideImageDisplay.this.visibleRect = visibleRect;
        }
        StreetsideImageDisplay.this.repaint();
      }
    }

    /**
     * Initialize the dragging, either with button 1 (simple dragging) or button
     * 3 (selection of a picture part)
     */
    @Override
    public void mousePressed(MouseEvent e) {
      if (getImage() == null) {
        mouseIsDragging = false;
        selectedRect = null;
        return;
      }
      Image image;
      Rectangle visibleRect;
      synchronized (StreetsideImageDisplay.this) {
        image = StreetsideImageDisplay.this.image;
        visibleRect = StreetsideImageDisplay.this.visibleRect;
      }
      if (image == null)
        return;
      if (e.getButton() == StreetsideProperties.PICTURE_DRAG_BUTTON.get()) {
        mousePointInImg = comp2imgCoord(visibleRect, e.getX(), e.getY());
        mouseIsDragging = true;
        selectedRect = null;
      } else if (e.getButton() == StreetsideProperties.PICTURE_ZOOM_BUTTON.get()) {
        mousePointInImg = comp2imgCoord(visibleRect, e.getX(), e.getY());
        checkPointInVisibleRect(mousePointInImg, visibleRect);
        mouseIsDragging = false;
        selectedRect = new Rectangle(mousePointInImg.x, mousePointInImg.y, 0, 0);
        StreetsideImageDisplay.this.repaint();
      } else {
        mouseIsDragging = false;
        selectedRect = null;
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      if (!mouseIsDragging && selectedRect == null)
        return;
      Image image;
      Rectangle visibleRect;
      synchronized (StreetsideImageDisplay.this) {
        image = getImage();
        visibleRect = StreetsideImageDisplay.this.visibleRect;
      }
      if (image == null) {
        mouseIsDragging = false;
        selectedRect = null;
        return;
      }
      if (mouseIsDragging) {
        Point p = comp2imgCoord(visibleRect, e.getX(), e.getY());
        visibleRect.x += mousePointInImg.x - p.x;
        visibleRect.y += mousePointInImg.y - p.y;
        checkVisibleRectPos(image, visibleRect);
        synchronized (StreetsideImageDisplay.this) {
          StreetsideImageDisplay.this.visibleRect = visibleRect;
        }
        StreetsideImageDisplay.this.repaint();
      } else if (selectedRect != null) {
        Point p = comp2imgCoord(visibleRect, e.getX(), e.getY());
        checkPointInVisibleRect(p, visibleRect);
        Rectangle rect = new Rectangle(p.x < mousePointInImg.x ? p.x
            : mousePointInImg.x, p.y < mousePointInImg.y ? p.y
            : mousePointInImg.y, p.x < mousePointInImg.x ? mousePointInImg.x
            - p.x : p.x - mousePointInImg.x,
            p.y < mousePointInImg.y ? mousePointInImg.y - p.y : p.y
                - mousePointInImg.y);
        checkVisibleRectSize(image, rect);
        checkVisibleRectPos(image, rect);
        selectedRect = rect;
        StreetsideImageDisplay.this.repaint();
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (!mouseIsDragging && selectedRect == null)
        return;
      Image image;
      synchronized (StreetsideImageDisplay.this) {
        image = getImage();
      }
      if (image == null) {
        mouseIsDragging = false;
        selectedRect = null;
        return;
      }
      if (mouseIsDragging) {
        mouseIsDragging = false;
      } else if (selectedRect != null) {
        int oldWidth = selectedRect.width;
        int oldHeight = selectedRect.height;
        // Check that the zoom doesn't exceed 2:1
        if (selectedRect.width < getSize().width / 2) {
        	selectedRect.width = getSize().width / 2;
        }
        if (selectedRect.height < getSize().height / 2) {
        	selectedRect.height = getSize().height / 2;
        }
        // Set the same ratio for the visible rectangle and the display
        // area
        int hFact = selectedRect.height * getSize().width;
        int wFact = selectedRect.width * getSize().height;
        if (hFact > wFact) {
          selectedRect.width = hFact / getSize().height;
        } else {
          selectedRect.height = wFact / getSize().width;
        }
        // Keep the center of the selection
        if (selectedRect.width != oldWidth) {
        	selectedRect.x -= (selectedRect.width - oldWidth) / 2;
        }
        if (selectedRect.height != oldHeight) {
        	selectedRect.y -= (selectedRect.height - oldHeight) / 2;
        }
        checkVisibleRectSize(image, selectedRect);
        checkVisibleRectPos(image, selectedRect);
        synchronized (StreetsideImageDisplay.this) {
          visibleRect = selectedRect;
        }
        selectedRect = null;
        StreetsideImageDisplay.this.repaint();
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      // Do nothing, method is enforced by MouseListener
    }

    @Override
    public void mouseExited(MouseEvent e) {
      // Do nothing, method is enforced by MouseListener
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      // Do nothing, method is enforced by MouseListener
    }

    private void checkPointInVisibleRect(Point p, Rectangle visibleRect) {
      if (p.x < visibleRect.x) {
        p.x = visibleRect.x;
      }
      if (p.x > visibleRect.x + visibleRect.width) {
        p.x = visibleRect.x + visibleRect.width;
      }
      if (p.y < visibleRect.y) {
        p.y = visibleRect.y;
      }
      if (p.y > visibleRect.y + visibleRect.height) {
        p.y = visibleRect.y + visibleRect.height;
      }
    }
  }

  /**
   * Main constructor.
   */
  public StreetsideImageDisplay() {
    ImgDisplayMouseListener mouseListener = new ImgDisplayMouseListener();
    addMouseListener(mouseListener);
    addMouseWheelListener(mouseListener);
    addMouseMotionListener(mouseListener);

    StreetsideProperties.SHOW_DETECTED_SIGNS.addListener(valChanged -> repaint());
  }

  /**
   * Sets a new picture to be displayed.
   *
   * @param image The picture to be displayed.
   * @param detections image detections
   */
  public void setImage(BufferedImage image, Collection<ImageDetection> detections) {
    synchronized (this) {
      this.image = image;
      this.detections.clear();
      if (detections != null) {
        this.detections.addAll(detections);
      }
      selectedRect = null;
      if (image != null)
        visibleRect = new Rectangle(0, 0, image.getWidth(null),
            image.getHeight(null));
    }
    repaint();
  }

  /**
   * Returns the picture that is being displayed
   *
   * @return The picture that is being displayed.
   */
  public BufferedImage getImage() {
    return image;
  }

  /**
   * Paints the visible part of the picture.
   */
  @Override
  public void paintComponent(Graphics g) {
    Image image;
    Rectangle visibleRect;
    synchronized (this) {
      image = this.image;
      visibleRect = this.visibleRect;
    }
    if (image == null) {
      g.setColor(Color.black);
      String noImageStr = StreetsideLayer.hasInstance() ? tr("No image selected") : tr("Press \"{0}\" to download images", StreetsideDownloadAction.SHORTCUT.getKeyText());
      Rectangle2D noImageSize = g.getFontMetrics(g.getFont()).getStringBounds(
          noImageStr, g);
      Dimension size = getSize();
      g.drawString(noImageStr,
          (int) ((size.width - noImageSize.getWidth()) / 2),
          (int) ((size.height - noImageSize.getHeight()) / 2));
    } else {
      Rectangle target = calculateDrawImageRectangle(visibleRect);
      g.drawImage(image, target.x, target.y, target.x + target.width, target.y
          + target.height, visibleRect.x, visibleRect.y, visibleRect.x
          + visibleRect.width, visibleRect.y + visibleRect.height, null);
      if (selectedRect != null) {
        Point topLeft = img2compCoord(visibleRect, selectedRect.x,
            selectedRect.y);
        Point bottomRight = img2compCoord(visibleRect, selectedRect.x
            + selectedRect.width, selectedRect.y + selectedRect.height);
        g.setColor(new Color(128, 128, 128, 180));
        g.fillRect(target.x, target.y, target.width, topLeft.y - target.y);
        g.fillRect(target.x, target.y, topLeft.x - target.x, target.height);
        g.fillRect(bottomRight.x, target.y, target.x + target.width
            - bottomRight.x, target.height);
        g.fillRect(target.x, bottomRight.y, target.width, target.y
            + target.height - bottomRight.y);
        g.setColor(Color.black);
        g.drawRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x,
            bottomRight.y - topLeft.y);
      }

      if (StreetsideProperties.SHOW_DETECTED_SIGNS.get()) {
        Point upperLeft = img2compCoord(visibleRect, 0, 0);
        Point lowerRight = img2compCoord(visibleRect, getImage().getWidth(), getImage().getHeight());

        // Transformation, which can convert you a Shape relative to the unit square to a Shape relative to the Component
        AffineTransform unit2compTransform = AffineTransform.getTranslateInstance(upperLeft.getX(), upperLeft.getY());
        unit2compTransform.concatenate(AffineTransform.getScaleInstance(lowerRight.getX() - upperLeft.getX(), lowerRight.getY() - upperLeft.getY()));

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        for (ImageDetection d : detections) {
          final Shape shape = d.getShape().createTransformedShape(unit2compTransform);
          g2d.setColor(d.isTrafficSign() ? StreetsideColorScheme.IMAGEDETECTION_TRAFFICSIGN : StreetsideColorScheme.IMAGEDETECTION_UNKNOWN);
          g2d.draw(shape);
          if (d.isTrafficSign()) {
            g2d.drawImage(
              MapObject.getIcon(d.getValue()).getImage(),
              shape.getBounds().x, shape.getBounds().y,
              shape.getBounds().width, shape.getBounds().height,
              null
            );
          }
        }
      }
    }
  }

  private Point img2compCoord(Rectangle visibleRect, int xImg, int yImg) {
    Rectangle drawRect = calculateDrawImageRectangle(visibleRect);
    return new Point(drawRect.x + ((xImg - visibleRect.x) * drawRect.width)
        / visibleRect.width, drawRect.y
        + ((yImg - visibleRect.y) * drawRect.height) / visibleRect.height);
  }

  private Point comp2imgCoord(Rectangle visibleRect, int xComp, int yComp) {
    Rectangle drawRect = calculateDrawImageRectangle(visibleRect);
    return new Point(
        visibleRect.x + ((xComp - drawRect.x) * visibleRect.width) / drawRect.width,
        visibleRect.y + ((yComp - drawRect.y) * visibleRect.height) / drawRect.height
    );
  }

  private static Point getCenterImgCoord(Rectangle visibleRect) {
    return new Point(visibleRect.x + visibleRect.width / 2, visibleRect.y + visibleRect.height / 2);
  }

  private Rectangle calculateDrawImageRectangle(Rectangle visibleRect) {
    return calculateDrawImageRectangle(visibleRect, new Rectangle(0, 0, getSize().width, getSize().height));
  }

  /**
   * calculateDrawImageRectangle
   *
   * @param imgRect
   *          the part of the image that should be drawn (in image coordinates)
   * @param compRect
   *          the part of the component where the image should be drawn (in
   *          component coordinates)
   * @return the part of compRect with the same width/height ratio as the image
   */
  private static Rectangle calculateDrawImageRectangle(Rectangle imgRect, Rectangle compRect) {
    int x = 0;
    int y = 0;
    int w = compRect.width;
    int h = compRect.height;
    int wFact = w * imgRect.height;
    int hFact = h * imgRect.width;
    if (wFact != hFact) {
      if (wFact > hFact) {
        w = hFact / imgRect.height;
        x = (compRect.width - w) / 2;
      } else {
        h = wFact / imgRect.width;
        y = (compRect.height - h) / 2;
      }
    }
    return new Rectangle(x + compRect.x, y + compRect.y, w, h);
  }

  /**
   * Zooms to 1:1 and, if it is already in 1:1, to best fit.
   */
  public void zoomBestFitOrOne() {
    Image image;
    Rectangle visibleRect;
    synchronized (this) {
      image = this.image;
      visibleRect = this.visibleRect;
    }
    if (image == null)
      return;
    if (visibleRect.width != image.getWidth(null)
        || visibleRect.height != image.getHeight(null)) {
      // The display is not at best fit. => Zoom to best fit
      visibleRect = new Rectangle(0, 0, image.getWidth(null),
          image.getHeight(null));
    } else {
      // The display is at best fit => zoom to 1:1
      Point center = getCenterImgCoord(visibleRect);
      visibleRect = new Rectangle(center.x - getWidth() / 2, center.y
          - getHeight() / 2, getWidth(), getHeight());
      checkVisibleRectPos(image, visibleRect);
    }
    synchronized (this) {
      this.visibleRect = visibleRect;
    }
    repaint();
  }

  private static void checkVisibleRectPos(Image image, Rectangle visibleRect) {
    if (visibleRect.x < 0) {
      visibleRect.x = 0;
    }
    if (visibleRect.y < 0) {
      visibleRect.y = 0;
    }
    if (visibleRect.x + visibleRect.width > image.getWidth(null)) {
      visibleRect.x = image.getWidth(null) - visibleRect.width;
    }
    if (visibleRect.y + visibleRect.height > image.getHeight(null)) {
      visibleRect.y = image.getHeight(null) - visibleRect.height;
    }
  }

  private static void checkVisibleRectSize(Image image, Rectangle visibleRect) {
    if (visibleRect.width > image.getWidth(null)) {
      visibleRect.width = image.getWidth(null);
    }
    if (visibleRect.height > image.getHeight(null)) {
      visibleRect.height = image.getHeight(null);
    }
  }
}
