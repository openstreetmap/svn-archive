// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cubemap;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideCubemap;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideViewerDialog;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.StreetsideViewerHelpPopup;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.StreetsideViewerPanel;
import org.openstreetmap.josm.plugins.streetside.utils.CubemapBox;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

@SuppressWarnings("restriction")
public class CubemapBuilder implements ITileDownloadingTaskListener, StreetsideDataListener {

	private static CubemapBuilder instance;
	// TODO: Help Pop-up
	private StreetsideViewerHelpPopup streetsideViewerHelp;
	private StreetsideCubemap cubemap;
	protected boolean cancelled;
	private long startTime;
  private Map<String, BufferedImage> tileImages = new HashMap<String,BufferedImage>();

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

  /**
   * @return the tileImages
   */
  /*public Map<String, BufferedImage> getTileImages() {
    return tileImages;
  }

  *//**
   * @param tileImages the tileImages to set
   *//*
  public void setTileImages(Map<String, BufferedImage> tileImages) {
    this.tileImages = tileImages;
  }*/

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
			Logging.debug("Completed downloading tiles for {0} in {1} seconds.",newImage.getId(),runTime);
		}
	}

	public void reload(String imageId) {
		if (cubemap != null && imageId.equals(cubemap.getId())) {
			tileImages = new HashMap<String,BufferedImage>();
		  //CubemapBuilder.getInstance().getCubemap().resetFaces2TileMap();
			downloadCubemapImages(imageId);
		}
	}

	public void downloadCubemapImages(String imageId) {

		final int maxCols = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;
		final int maxRows = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;
		final int maxThreadCount = 6 * maxCols * maxRows;

		int fails = 0;

		long startTime = System.currentTimeMillis();

		try {

			ExecutorService pool = Executors.newFixedThreadPool(maxThreadCount);
			List<Callable<String>> tasks = new ArrayList<Callable<String>>(maxThreadCount);

			// launch 4-tiled (low-res) downloading tasks . . .
			if (!StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
				for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {
					int tileNr = 0;
					for (int j = 0; j < maxCols; j++) {
						for (int k = 0; k < maxRows; k++) {

							String tileId = String.valueOf(imageId + CubemapUtils.getFaceNumberForCount(i)
									+ Integer.valueOf(tileNr++).toString());// + Integer.valueOf(k).toString()));
							tasks.add(new TileDownloadingTask(tileId));
							Logging.debug(
									I18n.tr("Starting tile downloading task for imageId {0}, cubeface {1}, tileNr {2}",
											tileId, CubemapUtils.getFaceNumberForCount(i), String.valueOf(tileNr)));
						}
					}
				}

				List<Future<String>> results = pool.invokeAll(tasks);
				for (Future<String> ff : results) {

					Logging.debug(I18n.tr("Completed tile downloading task {0} in {1}", ff.get(),
							(startTime - System.currentTimeMillis())/ 1000));
				}

				// launch 16-tiled (high-res) downloading tasks
			} else if (StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
				for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {
					for (int j = 0; j < maxCols; j++) {
						for (int k = 0; k < maxRows; k++) {

							String tileId = String.valueOf(imageId + CubemapUtils.getFaceNumberForCount(i)
									+ String.valueOf(Integer.valueOf(j).toString() + Integer.valueOf(k).toString()));
							tasks.add(new TileDownloadingTask(tileId));
							Logging.debug(
									I18n.tr("Starting tile downloading task for imageId {0}, cubeface {1}, tileID {2}",
											imageId, CubemapUtils.getFaceNumberForCount(i), tileId));
						}
					}
				}

				List<Future<String>> results = pool.invokeAll(tasks);
				for (Future<String> ff : results) {
					Logging.debug(I18n.tr("Completed tile downloading task {0} in {1}", ff.get(),
							(startTime - System.currentTimeMillis())/ 1000));
				}
			}
		} catch (Exception ee) {
			fails++;
			Logging.error("Error loading tile for image {0}", imageId);
			ee.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		long runTime = stopTime - startTime;

		Logging.debug(I18n.tr("Tile imagery downloading tasks completed in {0}", runTime/1000000));

		if (fails > 0) {
			Logging.error(I18n.tr("{0} downloading tasks failed.", Integer.valueOf(fails)));
		}

	}

	@Override
	public void tileAdded(String tileId) {
		// determine whether four tiles have been set for each of the
		// six cubemap faces. If so, build the images for the faces
		// and set the views in the cubemap box.

		int tileCount = 0;

		/*for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {
			String faceNumber = CubemapUtils.getFaceNumberForCount(i);
			Map<String, BufferedImage> faceTileImages = CubemapBuilder.getInstance().getCubemap().getFace2TilesMap()
					.get(faceNumber);
			tileCount += faceTileImages.values().size();
		}*/

		tileCount = CubemapBuilder.getInstance().getTileImages().keySet().size();

		int maxCols = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;
		int maxRows = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;

		if (tileCount == (CubemapUtils.NUM_SIDES * maxCols * maxRows)) {
			Logging.debug(I18n.tr("{0} tile images ready for building cumbemap faces for cubemap {0}", tileCount,
					CubemapBuilder.getInstance().getCubemap().getId()));

			buildCubemapFaces();
		}
	}

	private void buildCubemapFaces() {

	  Logging.debug("Assembling cubemap tile images");
	  CubemapBox cmb = StreetsideViewerDialog.getInstance().getStreetsideViewerPanel().getCubemapBox();
		ImageView[] views = cmb.getViews();

		final int maxCols = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;
		final int maxRows = StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get() ? 4 : 2;

		Image finalImages[] = new Image[CubemapUtils.NUM_SIDES];

		// build 4-tiled cubemap faces and crop buffers
		if (!StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
			for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {

				/*Map<String, BufferedImage> tileImages = CubemapBuilder.getInstance().getCubemap().getFace2TilesMap()
						.get(CubemapUtils.getFaceNumberForCount(i));*/

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
				  final long start = System.nanoTime();
					finalImg = GraphicsUtils.rotateImage(finalImg);
					Logging.debug(I18n.tr("Rotation took {0}", System.nanoTime() - start));
				}
				finalImages[i] = GraphicsUtils.convertBufferedImage2JavaFXImage(finalImg);
			}
			// build 16-tiled cubemap faces and crop buffers
		} else if (StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
			for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {

				int tileCount = 0;

				/*Map<String, Map<String, BufferedImage>> face2TilesMap = CubemapBuilder.getInstance().getCubemap()
						.getFace2TilesMap();*/
				//Map<String, BufferedImage> tileImages = face2TilesMap.get(CubemapUtils.getFaceNumberForCount(i));
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

    /*if (!Platform.isFxApplicationThread()) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {*/

           //try {
             /* GraphicsUtils.PlatformHelper.run(() -> {
                StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().initialize();
            });*/
             //StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().initialize();
             StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel()
                .setScene(StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().getCubemapScene());
            /*} catch (NonInvertibleTransformException nite) {
              // TODO Auto-generated catch block
              Logging.error(I18n.tr("Error setting scene in 360 viewer panel {0}", nite.getMessage()));
            }*/
        /*}
      });
    }*/

    StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().revalidate();
    StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().repaint();

    long endTime = System.currentTimeMillis();
    long runTime = (endTime - startTime) / 1000;
    Logging.debug(
      I18n.tr(
        "Completed downloading, assembling and setting cubemap imagery for cubemap {0} in {1}", cubemap.getId(), runTime
        )
      );
      CubemapBuilder.getInstance().setTileImages(new HashMap<String, BufferedImage>());
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