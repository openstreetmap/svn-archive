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

import org.openstreetmap.josm.Main;
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
import org.openstreetmap.josm.gui.layer.LayerManager;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.plugins.streetside.cache.CacheUtils;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.plugins.streetside.history.StreetsideRecord;
import org.openstreetmap.josm.plugins.streetside.io.download.StreetsideDownloader;
import org.openstreetmap.josm.plugins.streetside.io.download.StreetsideDownloader.DOWNLOAD_MODE;
import org.openstreetmap.josm.plugins.streetside.mode.AbstractMode;
import org.openstreetmap.josm.plugins.streetside.mode.JoinMode;
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

	/** The radius of the image marker */
	private static final int IMG_MARKER_RADIUS = 7;
	/** The radius of the circular sector that indicates the camera angle */
	private static final int CA_INDICATOR_RADIUS = 15;
	/** The angle of the circular sector that indicates the camera angle */
	private static final int CA_INDICATOR_ANGLE = 40;
	/** Length of the edge of the small sign, which indicates that traffic signs have been found in an image. */
	private static final int TRAFFIC_SIGN_SIZE = 6;
	/** A third of the height of the sign, for easier calculations */
	private static final double TRAFFIC_SIGN_HEIGHT_3RD = Math.sqrt(
			Math.pow(StreetsideLayer.TRAFFIC_SIGN_SIZE, 2) - Math.pow(StreetsideLayer.TRAFFIC_SIGN_SIZE / 2d, 2)
			) / 3;

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
	private final StreetsideLocationChangeset locationChangeset = new StreetsideLocationChangeset();

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
			ds.addDataSetListener(StreetsideLayer.DATASET_LISTENER);
		}
		MainApplication.getLayerManager().addLayer(this);
		MainApplication.getLayerManager().addActiveLayerChangeListener(this);
		if (!GraphicsEnvironment.isHeadless()) {
			setMode(new SelectMode());
			if (StreetsideDownloader.getMode() == DOWNLOAD_MODE.OSM_AREA) {
				StreetsideDownloader.downloadOSMArea();
			}
			if (StreetsideDownloader.getMode() == DOWNLOAD_MODE.VISIBLE_AREA) {
				mode.zoomChanged();
			}
		}
		// Does not execute when in headless mode
		if (Main.main != null && !StreetsideMainDialog.getInstance().isShowing()) {
			StreetsideMainDialog.getInstance().showDialog();
		}
		if (StreetsidePlugin.getMapView() != null) {
			StreetsideMainDialog.getInstance().getStreetsideImageDisplay().repaint();

			// There is no delete image action for Streetside (Mapillary functionality here removed).

			//getLocationChangeset().addChangesetListener(StreetsideChangesetDialog.getInstance());
		}
		createHatchTexture();
		invalidate();
	}

	public static void invalidateInstance() {
		if (StreetsideLayer.hasInstance()) {
			StreetsideLayer.getInstance().invalidate();
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
		StreetsideLayer.instance = null;
	}

	/**
	 * Returns the unique instance of this class.
	 *
	 * @return The unique instance of this class.
	 */
	public static synchronized StreetsideLayer getInstance() {
		if (StreetsideLayer.instance != null) {
			if (!MainApplication.getLayerManager().containsLayer(StreetsideLayer.instance)) {
				MainApplication.getLayerManager().addLayer(StreetsideLayer.instance);
			}
			return StreetsideLayer.instance;
		}
		final StreetsideLayer layer = new StreetsideLayer();
		StreetsideLayer.instance = layer;
		layer.init();
		return layer;
	}

	/**
	 * @return if the unique instance of this layer is currently instantiated and added to the {@link LayerManager}
	 */
	public static boolean hasInstance() {
		return StreetsideLayer.instance != null && MainApplication.getLayerManager().containsLayer(StreetsideLayer.instance);
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

	/**
	   * Returns the {@link StreetsideLocationChangeset} object, which acts as the database of the
	   * Layer.
	   *
	   * @return The {@link MapillaryData} object that stores the database.
	   */
	  public StreetsideLocationChangeset getLocationChangeset() {
	    return locationChangeset;
	  }


	@Override
	public synchronized void destroy() {
		// TODO: Add destroy code for CubemapBuilder, et al.? @rrh
		StreetsideLayer.clearInstance();
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
				MainApplication.getLayerManager().getEditDataSet().removeDataSetListener(StreetsideLayer.DATASET_LISTENER);
			}
		} catch (final IllegalArgumentException e) {
			// TODO: It would be ideal, to fix this properly. But for the moment let's catch this, for when a listener has already been removed.
		}
		super.destroy();
	}

	@Override
	public boolean isModified() {
		// TODO: Add cubemap modification here? @rrh
		return data.getImages().parallelStream().anyMatch(StreetsideAbstractImage::isModified);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		getData().getImages().parallelStream().forEach(img -> img.setVisible(visible));
		if (MainApplication.getMap() != null) {
			//StreetsideFilterDialog.getInstance().refresh();
		}
	}

	/**
	 * Initialize the hatch pattern used to paint the non-downloaded area.
	 */
	private void createHatchTexture() {
		final BufferedImage bi = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D big = bi.createGraphics();
		big.setColor(StreetsideProperties.BACKGROUND.get());
		final Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
		big.setComposite(comp);
		big.fillRect(0, 0, 15, 15);
		big.setColor(StreetsideProperties.OUTSIDE_DOWNLOADED_AREA.get());
		big.drawLine(0, 15, 15, 0);
		final Rectangle r = new Rectangle(0, 0, 15, 15);
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

		// Draw sequence line
		g.setStroke(new BasicStroke(2));
		final StreetsideAbstractImage selectedImage = getData().getSelectedImage();
		for (final StreetsideSequence seq : getData().getSequences()) {
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
		}
		/*for (final StreetsideAbstractImage imageAbs : data.getImages()) {
			if (imageAbs.isVisible() && mv != null && mv.contains(mv.getPoint(imageAbs.getMovingLatLon()))) {
				drawImageMarker(g, imageAbs);
			}
		}*/
		if (mode instanceof JoinMode) {
			mode.paint(g, mv, box);
		}
	}

	/**
	 * Draws an image marker onto the given Graphics context.
	 * @param g the Graphics context
	 * @param img the image to be drawn onto the Graphics context
	 */
	/*private void drawImageMarker(final Graphics2D g, final StreetsideAbstractImage img) {
		if (img == null || img.getLatLon() == null) {
			Logging.warn("An image is not painted, because it is null or has no LatLon!");
			return;
		}
		final StreetsideAbstractImage selectedImg = getData().getSelectedImage();
		final Point p = MainApplication.getMap().mapView.getPoint(img.getMovingLatLon());

		// Determine colors
		final Color markerC;
		final Color directionC;
		if (selectedImg != null && getData().getMultiSelectedImages().contains(img)) {
			markerC = img instanceof StreetsideImportedImage
					? StreetsideColorScheme.SEQ_IMPORTED_HIGHLIGHTED
							: StreetsideColorScheme.SEQ_HIGHLIGHTED;
			directionC = img instanceof StreetsideImportedImage
					? StreetsideColorScheme.SEQ_IMPORTED_HIGHLIGHTED_CA
							: StreetsideColorScheme.SEQ_HIGHLIGHTED_CA;
		} else if (selectedImg != null && selectedImg.getSequence() != null && selectedImg.getSequence().equals(img.getSequence())) {
			markerC = img instanceof StreetsideImportedImage
					? StreetsideColorScheme.SEQ_IMPORTED_SELECTED
							: StreetsideColorScheme.SEQ_SELECTED;
			directionC = img instanceof StreetsideImportedImage
					? StreetsideColorScheme.SEQ_IMPORTED_SELECTED_CA
							: StreetsideColorScheme.SEQ_SELECTED_CA;
		} else {
			markerC = img instanceof StreetsideImportedImage
					? StreetsideColorScheme.SEQ_IMPORTED_UNSELECTED
							: StreetsideColorScheme.SEQ_UNSELECTED;
			directionC = img instanceof StreetsideImportedImage
					? StreetsideColorScheme.SEQ_IMPORTED_UNSELECTED_CA
							: StreetsideColorScheme.SEQ_UNSELECTED_CA;
		}

		// Paint direction indicator
		g.setColor(directionC);
		g.fillArc(p.x - StreetsideLayer.CA_INDICATOR_RADIUS, p.y - StreetsideLayer.CA_INDICATOR_RADIUS, 2 * StreetsideLayer.CA_INDICATOR_RADIUS, 2 * StreetsideLayer.CA_INDICATOR_RADIUS, (int) (90 - img.getMovingHe() - StreetsideLayer.CA_INDICATOR_ANGLE / 2d), StreetsideLayer.CA_INDICATOR_ANGLE);
		// Paint image marker
		g.setColor(markerC);
		g.fillOval(p.x - StreetsideLayer.IMG_MARKER_RADIUS, p.y - StreetsideLayer.IMG_MARKER_RADIUS, 2 * StreetsideLayer.IMG_MARKER_RADIUS, 2 * StreetsideLayer.IMG_MARKER_RADIUS);

		// Paint highlight for selected or highlighted images
		if (img.equals(getData().getHighlightedImage()) || getData().getMultiSelectedImages().contains(img)) {
			g.setColor(Color.WHITE);
			g.setStroke(new BasicStroke(2));
			g.drawOval(p.x - StreetsideLayer.IMG_MARKER_RADIUS, p.y - StreetsideLayer.IMG_MARKER_RADIUS, 2 * StreetsideLayer.IMG_MARKER_RADIUS, 2 * StreetsideLayer.IMG_MARKER_RADIUS);
		}

		// TODO: reimplement detections for Bing Metadata? RRH
		if (img instanceof StreetsideImage && !((StreetsideImage) img).getDetections().isEmpty()) {
			final Path2D trafficSign = new Path2D.Double();
			trafficSign.moveTo(p.getX() - StreetsideLayer.TRAFFIC_SIGN_SIZE / 2d, p.getY() - StreetsideLayer.TRAFFIC_SIGN_HEIGHT_3RD);
			trafficSign.lineTo(p.getX() + StreetsideLayer.TRAFFIC_SIGN_SIZE / 2d, p.getY() - StreetsideLayer.TRAFFIC_SIGN_HEIGHT_3RD);
			trafficSign.lineTo(p.getX(), p.getY() + 2 * StreetsideLayer.TRAFFIC_SIGN_HEIGHT_3RD);
			trafficSign.closePath();
			g.setColor(Color.WHITE);
			g.fill(trafficSign);
			g.setStroke(new BasicStroke(1));
			g.setColor(Color.RED);
			g.draw(trafficSign);
		}
	}*/

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
		final IntSummaryStatistics seqSizeStats = getData().getSequences().stream().mapToInt(seq -> seq.getImages().size()).summaryStatistics();
		return new StringBuilder(I18n.tr("Streetside layer"))
				.append("\n")
				.append(I18n.tr(
						"{0} sequences, each containing between {1} and {2} images (ø {3})",
						getData().getSequences().size(),
						seqSizeStats.getCount() <= 0 ? 0 : seqSizeStats.getMin(),
								seqSizeStats.getCount() <= 0 ? 0 : seqSizeStats.getMax(),
										seqSizeStats.getAverage()
						))
				.append("\n\n")
				.append(I18n.tr(
						"{0} imported images",
						getData().getImages().stream().filter(i -> i instanceof StreetsideImportedImage).count()
						))
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
				MainApplication.getLayerManager().getEditLayer().getDataSet().addDataSetListener(StreetsideLayer.DATASET_LISTENER);
			}
			if (e.getPreviousDataLayer() != null) {
				e.getPreviousDataLayer().getDataSet().removeDataSetListener(StreetsideLayer.DATASET_LISTENER);
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
		// TODO: Never used - could this be of use? @rrh
		updateNearestImages();
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.mapillary.StreetsideDataListener#selectedImageChanged(org.openstreetmap.josm.plugins.mapillary.MapillaryAbstractImage, org.openstreetmap.josm.plugins.mapillary.MapillaryAbstractImage)
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
					final Optional<StreetsideAbstractImage> resImg = seq.getImages().parallelStream()
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

	/**
	 * Returns the closest images belonging to a different sequence and
	 * different from the specified target image.
	 *
	 * @param target the image for which you want to find the nearest other images
	 * @param limit the maximum length of the returned array
	 * @return An array containing the closest images belonging to different sequences sorted by distance from target.
	 */
	/*private StreetsideCubemap[] getNearestCubemapsFromDifferentSequences(StreetsideAbstractImage target, int limit) {
		return data.getSequences().parallelStream()
				.filter(seq -> seq.getId() != null && !seq.getId().equals(target.getSequence().getId()))
				.map(seq -> { // Maps sequence to image from sequence that is nearest to target
					final Optional<StreetsideAbstractImage> resCb = seq.getImages().parallelStream()
							.filter(cb -> cb instanceof StreetsideCubemap && cb.isVisible())
							.min(new NearestCbToTargetComparator(target));
					return resCb.orElse(null);
				})
				.filter(cb -> // Filters out images too far away from target
				cb != null &&
				cb.getMovingLatLon().greatCircleDistance(target.getMovingLatLon())
				< StreetsideProperties.SEQUENCE_MAX_JUMP_DISTANCE.get()
						)
				.sorted(new NearestCbToTargetComparator(target))
				.limit(limit)
				.toArray(StreetsideCubemap[]::new);
	}*/

	private synchronized void updateNearestImages() {
		final StreetsideAbstractImage selected = data.getSelectedImage();
		if (selected != null) {
			// TODO: could this be used to pre-cache cubemaps? @rrh
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
			// TODO: download/pre-caches cubemaps here?
			//CacheUtils.downloadCubemap(nearestImages[0]);
			if (nearestImages.length >= 2) {
				CacheUtils.downloadPicture(nearestImages[1]);
				// TODO: download/pre-caches cubemaps here?
				//CacheUtils.downloadCubemap(nearestImages[1]);
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

	private static class NearestCbToTargetComparator implements Comparator<StreetsideAbstractImage> {
		private final StreetsideAbstractImage target;

		public NearestCbToTargetComparator(StreetsideAbstractImage target) {
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