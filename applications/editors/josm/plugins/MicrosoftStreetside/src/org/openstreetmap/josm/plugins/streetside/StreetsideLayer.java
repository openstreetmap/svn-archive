// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.Optional;

import javax.swing.Action;
import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.AbstractModifiableLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.plugins.streetside.cache.CacheUtils;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.plugins.streetside.history.StreetsideRecord;
import org.openstreetmap.josm.plugins.streetside.io.download.StreetsideDownloader;
import org.openstreetmap.josm.plugins.streetside.io.download.StreetsideDownloader.DOWNLOAD_MODE;
import org.openstreetmap.josm.plugins.streetside.mode.AbstractMode;
import org.openstreetmap.josm.plugins.streetside.mode.SelectMode;
import org.openstreetmap.josm.plugins.streetside.utils.MapViewGeometryUtil;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideColorScheme;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideUtils;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;

/**
 * This class represents the layer shown in JOSM. There can only exist one
 * instance of this object.
 *
 * @author nokutu
 */
public final class StreetsideLayer extends AbstractModifiableLayer implements
ActiveLayerChangeListener, StreetsideDataListener {

  final static Logger logger = Logger.getLogger(StreetsideLayer.class);

  /** The radius of the image marker */
  private static final int IMG_MARKER_RADIUS = 7;
  /** The radius of the circular sector that indicates the camera angle */
  private static final int CA_INDICATOR_RADIUS = 15;
  /** The angle of the circular sector that indicates the camera angle */
  private static final int CA_INDICATOR_ANGLE = 40;

	private static final DataSetListenerAdapter DATASET_LISTENER =
			new DataSetListenerAdapter(e -> {
				if (e instanceof DataChangedEvent && StreetsideDownloader.getMode() == DOWNLOAD_MODE.OSM_AREA) {
					// When more data is downloaded, a delayed update is thrown, in order to
					// wait for the data bounds to be set.
					MainApplication.worker.execute(StreetsideDownloader::downloadOSMArea);
				}
			});

	/** Unique instance of the class. */
	private static StreetsideLayer instance;
	/** The nearest images to the selected image from different sequences sorted by distance from selection. */
	private StreetsideImage[] nearestImages = {};
	/** {@link StreetsideData} object that stores the database. */
	private final StreetsideData data;

	/** Mode of the layer. */
	public AbstractMode mode;

	private volatile TexturePaint hatched;

	private StreetsideLayer() {
		super(I18n.tr("Microsoft Streetside Images"));
		data = new StreetsideData();
		data.addListener(this);
	}

  /**
   * Initializes the Layer.
   */
  private void init() {
    final DataSet ds = MainApplication.getLayerManager().getEditDataSet();
    if (ds != null) {
      ds.addDataSetListener(DATASET_LISTENER);
    }
    MainApplication.getLayerManager().addActiveLayerChangeListener(this);
    if (!GraphicsEnvironment.isHeadless()) {
      setMode(new SelectMode());
      if (StreetsideDownloader.getMode() == DOWNLOAD_MODE.OSM_AREA) {
        MainApplication.worker.execute(StreetsideDownloader::downloadOSMArea);
      }
      if (StreetsideDownloader.getMode() == DOWNLOAD_MODE.VISIBLE_AREA) {
        mode.zoomChanged();
      }
    }
    // Does not execute when in headless mode
    if (!StreetsideMainDialog.getInstance().isShowing()) {
      StreetsideMainDialog.getInstance().showDialog();
    }
    if (StreetsidePlugin.getMapView() != null) {
      StreetsideMainDialog.getInstance().streetsideImageDisplay.repaint();
		}
		createHatchTexture();
		invalidate();
	}

  public static void invalidateInstance() {
    if (hasInstance()) {
      getInstance().invalidate();
    }
  }

  /**
   * Changes the mode the the given one.
   *
   * @param mode The mode that is going to be activated.
   */
  public void setMode(AbstractMode mode) {
    final MapView mv = StreetsidePlugin.getMapView();
    if (this.mode != null && mv != null) {
      mv.removeMouseListener(this.mode);
      mv.removeMouseMotionListener(this.mode);
      NavigatableComponent.removeZoomChangeListener(this.mode);
    }
    this.mode = mode;
    if (mode != null && mv != null) {
      mv.setNewCursor(mode.cursor, this);
      mv.addMouseListener(mode);
      mv.addMouseMotionListener(mode);
      NavigatableComponent.addZoomChangeListener(mode);
      StreetsideUtils.updateHelpText();
    }
  }

  private static synchronized void clearInstance() {
    instance = null;
  }

  /**
   * Returns the unique instance of this class.
   *
   * @return The unique instance of this class.
   */
  public static synchronized StreetsideLayer getInstance() {
    if (instance != null) {
      return instance;
    }
    final StreetsideLayer layer = new StreetsideLayer();
    layer.init();
    instance = layer; // Only set instance field after initialization is complete
    return instance;
  }

  /**
   * @return if the unique instance of this layer is currently instantiated
   */
  public static boolean hasInstance() {
    return instance != null;
  }

	/**
	 * Returns the {@link StreetsideData} object, which acts as the database of the
	 * Layer.
	 *
	 * @return The {@link StreetsideData} object that stores the database.
	 */
	public StreetsideData getData() {
		return data;
	}

  /**
   * Returns the n-nearest image, for n=1 the nearest one is returned, for n=2 the second nearest one and so on.
   * The "n-nearest image" is picked from the list of one image from every sequence that is nearest to the currently
   * selected image, excluding the sequence to which the selected image belongs.
   * @param n the index for picking from the list of "nearest images", beginning from 1
   * @return the n-nearest image to the currently selected image
   */
  public synchronized StreetsideImage getNNearestImage(final int n) {
    return n >= 1 && n <= nearestImages.length ? nearestImages[n - 1] : null;
  }

  @Override
  public synchronized void destroy() {
    clearInstance();
    setMode(null);
    StreetsideRecord.getInstance().reset();
    AbstractMode.resetThread();
    StreetsideDownloader.stopAll();
    if (StreetsideMainDialog.hasInstance()) {
      StreetsideMainDialog.getInstance().setImage(null);
      StreetsideMainDialog.getInstance().updateImage();
    }
    final MapView mv = StreetsidePlugin.getMapView();
    if (mv != null) {
      mv.removeMouseListener(mode);
      mv.removeMouseMotionListener(mode);
    }
    try {
      MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
      if (MainApplication.getLayerManager().getEditDataSet() != null) {
        MainApplication.getLayerManager().getEditDataSet().removeDataSetListener(DATASET_LISTENER);
      }
    } catch (IllegalArgumentException e) {
      // TODO: It would be ideal, to fix this properly. But for the moment let's catch this, for when a listener has already been removed.
    }
    super.destroy();
  }


	@Override
  public boolean isModified() {
    return data.getImages().parallelStream().anyMatch(StreetsideAbstractImage::isModified);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    getData().getImages().parallelStream().forEach(img -> img.setVisible(visible));
  }

  /**
   * Initialize the hatch pattern used to paint the non-downloaded area.
   */
  private void createHatchTexture() {
    BufferedImage bi = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
    Graphics2D big = bi.createGraphics();
    big.setColor(StreetsideProperties.BACKGROUND.get());
    Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
    big.setComposite(comp);
    big.fillRect(0, 0, 15, 15);
    big.setColor(StreetsideProperties.OUTSIDE_DOWNLOADED_AREA.get());
    big.drawLine(0, 15, 15, 0);
    Rectangle r = new Rectangle(0, 0, 15, 15);
    hatched = new TexturePaint(bi, r);
  }

  @Override
  public synchronized void paint(final Graphics2D g, final MapView mv, final Bounds box) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (MainApplication.getLayerManager().getActiveLayer() == this) {
      // paint remainder
      g.setPaint(hatched);
      g.fill(MapViewGeometryUtil.getNonDownloadedArea(mv, data.getBounds()));
    }

    // Draw the blue and red line
    synchronized (StreetsideLayer.class) {
      final StreetsideAbstractImage selectedImg = data.getSelectedImage();
      for (int i = 0; i < nearestImages.length && selectedImg != null; i++) {
        if (i == 0) {
          g.setColor(Color.RED);
        } else {
          g.setColor(Color.BLUE);
        }
        final Point selected = mv.getPoint(selectedImg.getMovingLatLon());
        final Point p = mv.getPoint(nearestImages[i].getMovingLatLon());
        g.draw(new Line2D.Double(p.getX(), p.getY(), selected.getX(), selected.getY()));
      }
    }

    // TODO: Sequence lines removed because Streetside imagery is organized
    // such that the images are sorted by the distance from the center of
    // the bounding box - Redefine sequences?

    // Draw sequence line
    /*g.setStroke(new BasicStroke(2));
    final StreetsideAbstractImage selectedImage = getData().getSelectedImage();
    for (StreetsideSequence seq : getData().getSequences()) {
      if (seq.getImages().contains(selectedImage)) {
        g.setColor(
          seq.getId() == null ? StreetsideColorScheme.SEQ_IMPORTED_SELECTED : StreetsideColorScheme.SEQ_SELECTED
        );
      } else {
        g.setColor(
          seq.getId() == null ? StreetsideColorScheme.SEQ_IMPORTED_UNSELECTED : StreetsideColorScheme.SEQ_UNSELECTED
        );
      }
      g.draw(MapViewGeometryUtil.getSequencePath(mv, seq));
    }*/
    for (StreetsideAbstractImage imageAbs : data.getImages()) {
      if (imageAbs.isVisible() && mv != null && mv.contains(mv.getPoint(imageAbs.getMovingLatLon()))) {
        drawImageMarker(g, imageAbs);
      }
    }
  }

  /**
   * Draws an image marker onto the given Graphics context.
   * @param g the Graphics context
   * @param img the image to be drawn onto the Graphics context
   */
  private void drawImageMarker(final Graphics2D g, final StreetsideAbstractImage img) {
    if (img == null || img.getLatLon() == null) {
      logger.warn("An image is not painted, because it is null or has no LatLon!");
      return;
    }
    final StreetsideAbstractImage selectedImg = getData().getSelectedImage();
    final Point p = MainApplication.getMap().mapView.getPoint(img.getMovingLatLon());

    // Determine colors
    final Color markerC;
    final Color directionC;
    if (selectedImg != null && getData().getMultiSelectedImages().contains(img)) {
      markerC = StreetsideColorScheme.SEQ_HIGHLIGHTED;
      directionC = StreetsideColorScheme.SEQ_HIGHLIGHTED_CA;
    } else if (selectedImg != null && selectedImg.getSequence() != null && selectedImg.getSequence().equals(img.getSequence())) {
      markerC = StreetsideColorScheme.SEQ_SELECTED;
      directionC = StreetsideColorScheme.SEQ_SELECTED_CA;
    } else {
      markerC = StreetsideColorScheme.SEQ_UNSELECTED;
      directionC = StreetsideColorScheme.SEQ_UNSELECTED_CA;
    }

    // Paint direction indicator
    float alpha = 0.75f;
    int type = AlphaComposite.SRC_OVER;
    AlphaComposite composite =
      AlphaComposite.getInstance(type, alpha);
    g.setComposite(composite);
    g.setColor(directionC);
    g.fillArc(p.x - CA_INDICATOR_RADIUS, p.y - CA_INDICATOR_RADIUS, 2 * CA_INDICATOR_RADIUS, 2 * CA_INDICATOR_RADIUS, (int) (90 - /*img.getMovingHe()*/img.getHe() - CA_INDICATOR_ANGLE / 2d), CA_INDICATOR_ANGLE);
    // Paint image marker
    g.setColor(markerC);
    g.fillOval(p.x - IMG_MARKER_RADIUS, p.y - IMG_MARKER_RADIUS, 2 * IMG_MARKER_RADIUS, 2 * IMG_MARKER_RADIUS);

    // Paint highlight for selected or highlighted images
    if (img.equals(getData().getHighlightedImage()) || getData().getMultiSelectedImages().contains(img)) {
      g.setColor(Color.WHITE);
      g.setStroke(new BasicStroke(2));
      g.drawOval(p.x - IMG_MARKER_RADIUS, p.y - IMG_MARKER_RADIUS, 2 * IMG_MARKER_RADIUS, 2 * IMG_MARKER_RADIUS);
    }
	}

  @Override
  public Icon getIcon() {
    return StreetsidePlugin.LOGO.setSize(ImageSizes.LAYER).get();
  }

  @Override
  public boolean isMergable(Layer other) {
    return false;
  }

  @Override
  public void mergeFrom(Layer from) {
    throw new UnsupportedOperationException(
      "This layer does not support merging yet");
  }

  @Override
  public Action[] getMenuEntries() {
    return new Action[]{
      LayerListDialog.getInstance().createShowHideLayerAction(),
      LayerListDialog.getInstance().createDeleteLayerAction(),
      new LayerListPopup.InfoAction(this)
    };
  }

  @Override
  public Object getInfoComponent() {
    IntSummaryStatistics seqSizeStats = getData().getSequences().stream().mapToInt(seq -> seq.getImages().size()).summaryStatistics();
    return new StringBuilder(I18n.tr("Streetside layer"))
      .append('\n')
      .append(I18n.tr(
        "{0} sequences, each containing between {1} and {2} images (ø {3})",
        getData().getSequences().size(),
        seqSizeStats.getCount() <= 0 ? 0 : seqSizeStats.getMin(),
        seqSizeStats.getCount() <= 0 ? 0 : seqSizeStats.getMax(),
        seqSizeStats.getAverage()
      ))
      .append("\n\n")
      .append("\n+ ")
      .append(I18n.tr(
        "{0} downloaded images",
        getData().getImages().stream().filter(i -> i instanceof StreetsideImage).count()
      ))
      .append("\n= ")
      .append(I18n.tr(
        "{0} images in total",
        getData().getImages().size()
      )).toString();
  }

  @Override
  public String getToolTipText() {
    return I18n.tr("{0} images in {1} sequences", getData().getImages().size(), getData().getSequences().size());
  }

  @Override
  public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
    if (MainApplication.getLayerManager().getActiveLayer() == this) {
      StreetsideUtils.updateHelpText();
    }

    if (MainApplication.getLayerManager().getEditLayer() != e.getPreviousDataLayer()) {
      if (MainApplication.getLayerManager().getEditLayer() != null) {
        MainApplication.getLayerManager().getEditLayer().getDataSet().addDataSetListener(DATASET_LISTENER);
      }
      if (e.getPreviousDataLayer() != null) {
        e.getPreviousDataLayer().getDataSet().removeDataSetListener(DATASET_LISTENER);
      }
    }
  }

  @Override
  public void visitBoundingBox(BoundingXYVisitor v) {
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.josm.plugins.streetside.StreetsideDataListener#imagesAdded()
   */
  @Override
  public void imagesAdded() {
    updateNearestImages();
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.josm.plugins.streetside.StreetsideDataListener#selectedImageChanged(org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage, org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage)
   */
  @Override
  public void selectedImageChanged(StreetsideAbstractImage oldImage, StreetsideAbstractImage newImage) {
    updateNearestImages();
  }

  /**
   * Returns the closest images belonging to a different sequence and
   * different from the specified target image.
   *
   * @param target the image for which you want to find the nearest other images
   * @param limit the maximum length of the returned array
   * @return An array containing the closest images belonging to different sequences sorted by distance from target.
   */
  private StreetsideImage[] getNearestImagesFromDifferentSequences(StreetsideAbstractImage target, int limit) {
    return data.getSequences().parallelStream()
      .filter(seq -> seq.getId() != null && !seq.getId().equals(target.getSequence().getId()))
      .map(seq -> { // Maps sequence to image from sequence that is nearest to target
        Optional<StreetsideAbstractImage> resImg = seq.getImages().parallelStream()
          .filter(img -> img instanceof StreetsideImage && img.isVisible())
          .min(new NearestImgToTargetComparator(target));
        return resImg.orElse(null);
      })
      .filter(img -> // Filters out images too far away from target
        img != null &&
        img.getMovingLatLon().greatCircleDistance(target.getMovingLatLon())
          < StreetsideProperties.SEQUENCE_MAX_JUMP_DISTANCE.get()
       )
      .sorted(new NearestImgToTargetComparator(target))
      .limit(limit)
      .toArray(StreetsideImage[]::new);
  }

  private synchronized void updateNearestImages() {
    final StreetsideAbstractImage selected = data.getSelectedImage();
    if (selected != null) {
      nearestImages = getNearestImagesFromDifferentSequences(selected, 2);
    } else {
      nearestImages = new StreetsideImage[0];
    }
    if (MainApplication.isDisplayingMapView()) {
      StreetsideMainDialog.getInstance().redButton.setEnabled(nearestImages.length >= 1);
      StreetsideMainDialog.getInstance().blueButton.setEnabled(nearestImages.length >= 2);
    }
    if (nearestImages.length >= 1) {
      CacheUtils.downloadPicture(nearestImages[0]);
      if (nearestImages.length >= 2) {
        CacheUtils.downloadPicture(nearestImages[1]);
      }
    }
  }

  private static class NearestImgToTargetComparator implements Comparator<StreetsideAbstractImage> {
    private final StreetsideAbstractImage target;

    public NearestImgToTargetComparator(StreetsideAbstractImage target) {
      this.target = target;
    }
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(StreetsideAbstractImage img1, StreetsideAbstractImage img2) {
      return (int) Math.signum(
        img1.getMovingLatLon().greatCircleDistance(target.getMovingLatLon()) -
        img2.getMovingLatLon().greatCircleDistance(target.getMovingLatLon())
      );
    }
  }
}
