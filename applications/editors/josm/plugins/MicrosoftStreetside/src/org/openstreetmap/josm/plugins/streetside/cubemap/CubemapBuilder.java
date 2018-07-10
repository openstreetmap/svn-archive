// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cubemap;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideCubemap;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideViewerDialog;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.StreetsideViewerPanel;
import org.openstreetmap.josm.plugins.streetside.utils.CubemapBox;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// JavaFX access in Java 8
@SuppressWarnings("restriction")
public class CubemapBuilder implements ITileDownloadingTaskListener, StreetsideDataListener {

  final static Logger logger = Logger.getLogger(CubemapBuilder.class);

	private static CubemapBuilder instance;
	private StreetsideCubemap cubemap;
	protected boolean cancelled;
	private long startTime;
  private Map<String, BufferedImage> tileImages = new HashMap<>();

  /**
   * @return the tileImages
   */
  public Map<String, BufferedImage> getTileImages() {
    return tileImages;
  }

  /**
   * @param tileImages the tileImages to set
   */
  public void setTileImages(Map<String, BufferedImage> tileImages) {
    this.tileImages = tileImages;
  }

  private CubemapBuilder() {
		// private constructor to avoid instantiation
	}

	@Override
	public void imagesAdded() {
		// Do nothing
	}

	@Override
	public void selectedImageChanged(StreetsideAbstractImage oldImage, StreetsideAbstractImage newImage) {
		startTime = System.currentTimeMillis();

		if (newImage != null) {

			cubemap = null;
			cubemap = new StreetsideCubemap(newImage.getId(), newImage.getLatLon(), newImage.getHe());
			cubemap.setCd(newImage.getCd());

			// download cubemap images in different threads and then subsequently
			// set the cubeface images in JavaFX
			downloadCubemapImages(cubemap.getId());

			long runTime = (System.currentTimeMillis()-startTime)/1000;
			if(StreetsideProperties.DEBUGING_ENABLED.get()) {
			  logger.debug(MessageFormat.format("Completed downloading tiles for {0} in {1} seconds.", newImage.getId() , runTime));
			}
		}
	}

	public void reload(String imageId) {
		if (cubemap != null && imageId.equals(cubemap.getId())) {
			tileImages = new HashMap<>();
		  downloadCubemapImages(imageId);
		}
	}

	public void downloadCubemapImages(String imageId) {

		final int maxCols = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;
		final int maxRows = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;
		final int maxThreadCount = 6 * maxCols * maxRows;

		int fails = 0;

		int min = 0;   int max = (StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()?96:24)*2;

		String[] message = new String[2];
    message[0] = MessageFormat.format("Downloading Streetside imagery for {0}", imageId);
    message[1] = "Wait for completion…….";

		long startTime = System.currentTimeMillis();

		try {

			ExecutorService pool = Executors.newFixedThreadPool(maxThreadCount);
			List<Callable<String>> tasks = new ArrayList<>(maxThreadCount);

			// launch 4-tiled (low-res) downloading tasks . . .
			if (!StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
				for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {
					int tileNr = 0;
					for (int j = 0; j < maxCols; j++) {
						for (int k = 0; k < maxRows; k++) {

							String tileId = String.valueOf(imageId + CubemapUtils.getFaceNumberForCount(i)
									+ Integer.valueOf(tileNr++).toString());
							tasks.add(new TileDownloadingTask(tileId));
						}
					}
				}

				List<Future<String>> results = pool.invokeAll(tasks);
				for (Future<String> ff : results) {

					if(StreetsideProperties.DEBUGING_ENABLED.get()) {
					  logger.debug(MessageFormat.format("Completed tile downloading task {0} in {1} seconds.", ff.get(), (startTime - System.currentTimeMillis())/ 1000));
					}
				}

				// launch 16-tiled (high-res) downloading tasks
			} else if (StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
				for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {
					for (int j = 0; j < maxCols; j++) {
						for (int k = 0; k < maxRows; k++) {

							String tileId = String.valueOf(imageId + CubemapUtils.getFaceNumberForCount(i)
									+ String.valueOf(Integer.valueOf(j).toString() + Integer.valueOf(k).toString()));
							tasks.add(new TileDownloadingTask(tileId));
						}
					}
				}

				List<Future<String>> results = pool.invokeAll(tasks);
				for (Future<String> ff : results) {
					if(StreetsideProperties.DEBUGING_ENABLED.get()) {
					  logger.debug(MessageFormat.format("Completed tile downloading task {0} in {1} seconds.",ff.get(),
							(startTime - System.currentTimeMillis())/ 1000));
					}
				}
			}
		} catch (Exception ee) {
			fails++;
			logger.error("Error loading tile for image " + imageId);
			ee.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		long runTime = stopTime - startTime;

		if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug(MessageFormat.format("Tile imagery downloading tasks completed in {0} seconds.",  runTime/1000000));
		}

		if (fails > 0) {
			logger.error(Integer.valueOf(fails) + " downloading tasks failed!");
		}

	}

	@Override
	public void tileAdded(String tileId) {
		// determine whether all tiles have been set for each of the
		// six cubemap faces. If so, build the images for the faces
		// and set the views in the cubemap box.

		int tileCount = 0;

		tileCount = CubemapBuilder.getInstance().getTileImages().keySet().size();

		int maxCols = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;
		int maxRows = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;

		if (tileCount == (CubemapUtils.NUM_SIDES * maxCols * maxRows)) {
		  if (StreetsideProperties.DEBUGING_ENABLED.get()) {
        logger.debug(MessageFormat.format("{0} tile images ready for building cumbemap faces for cubemap {1}.", tileCount,
					CubemapBuilder.getInstance().getCubemap().getId()));
		  }

			buildCubemapFaces();
		}
	}

	private void buildCubemapFaces() {

	  if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug("Assembling cubemap tile images");
	  }

	  CubemapBox cmb = StreetsideViewerDialog.getInstance().getStreetsideViewerPanel().getCubemapBox();
		ImageView[] views = cmb.getViews();

		final int maxCols = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;
		final int maxRows = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;

		Image finalImages[] = new Image[CubemapUtils.NUM_SIDES];

		// build 4-tiled cubemap faces and crop buffers
		if (!StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
			for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {

				BufferedImage[] faceTileImages = new BufferedImage[maxCols * maxRows];

				for (int j = 0; j < (maxCols * maxRows); j++) {
					String tileId = String.valueOf(getCubemap().getId() + CubemapUtils.getFaceNumberForCount(i)
							+ Integer.valueOf(j).toString());
					BufferedImage currentTile = tileImages.get(tileId);

					faceTileImages[j] = currentTile;
				}

				BufferedImage finalImg = GraphicsUtils.buildMultiTiledCubemapFaceImage(faceTileImages);

				// rotate top cubeface 180 degrees - misalignment workaround
				if (i == 4) {
				  finalImg = GraphicsUtils.rotateImage(finalImg);
				}
				finalImages[i] = GraphicsUtils.convertBufferedImage2JavaFXImage(finalImg);
			}
			// build 16-tiled cubemap faces and crop buffers
		} else if (StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
			for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {

				int tileCount = 0;

				BufferedImage[] faceTileImages = new BufferedImage[StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY
						.get() ? 16 : 4];

				for (int j = 0; j < maxCols; j++) {
					for (int k = 0; k < maxRows; k++) {
						String tileId = String.valueOf(getCubemap().getId() + CubemapUtils.getFaceNumberForCount(i)
								+ CubemapUtils.convertDoubleCountNrto16TileNr(
										String.valueOf(Integer.valueOf(j).toString() + Integer.valueOf(k).toString())));
						BufferedImage currentTile = tileImages.get(tileId);
						faceTileImages[tileCount++] = currentTile;
					}
				}
				BufferedImage finalImg = GraphicsUtils.buildMultiTiledCubemapFaceImage(faceTileImages);
				// rotate top cubeface 180 degrees - misalignment workaround
				if (i == 4) {
					finalImg = GraphicsUtils.rotateImage(finalImg);
				}
				finalImages[i] = GraphicsUtils.convertBufferedImage2JavaFXImage(finalImg);
			}
		}

		for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {
			views[i].setImage(finalImages[i]);
		}

    StreetsideViewerDialog.getInstance().getStreetsideViewerPanel().revalidate();
    StreetsideViewerDialog.getInstance().getStreetsideViewerPanel().repaint();

    StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel()
                .setScene(StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().getCubemapScene());

    StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().revalidate();
    StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().repaint();

    long endTime = System.currentTimeMillis();
    long runTime = (endTime - startTime) / 1000;

    String message = MessageFormat.format("Completed downloading, assembling and setting cubemap imagery for cubemap {0} in  {1} seconds.", cubemap.getId(),
      runTime);

    if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug(message);
    }

    CubemapBuilder.getInstance().resetTileImages();
	}

	private void resetTileImages() {
    tileImages = new HashMap<>();
  }

  /**
	 * @return the cubemap
	 */
	public synchronized StreetsideCubemap getCubemap() {
		return cubemap;
	}

	/**
	 * @param cubemap
	 *            the cubemap to set
	 */
	public static void setCubemap(StreetsideCubemap cubemap) {
		CubemapBuilder.getInstance().cubemap = cubemap;
	}

	public static CubemapBuilder getInstance() {
		if (instance == null) {
			instance = new CubemapBuilder();
		}
		return instance;
	}

	/**
	 * @return true, iff the singleton instance is present
	 */
	public static boolean hasInstance() {
		return CubemapBuilder.instance != null;
	}

	/**
	 * Destroys the unique instance of the class.
	 */
	public static synchronized void destroyInstance() {
		CubemapBuilder.instance = null;
	}
}