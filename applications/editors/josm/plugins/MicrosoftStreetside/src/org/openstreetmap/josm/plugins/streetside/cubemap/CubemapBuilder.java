// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cubemap;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.gui.MainApplication;
import java.util.concurrent.ExecutionException;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideCubemap;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideViewerDialog;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.StreetsideViewerPanel;
import org.openstreetmap.josm.plugins.streetside.utils.GraphicsUtils;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// JavaFX access in Java 8
public class CubemapBuilder implements ITileDownloadingTaskListener, StreetsideDataListener {

  final static Logger logger = Logger.getLogger(CubemapBuilder.class);

	private static CubemapBuilder instance;
	private StreetsideCubemap cubemap;
	protected boolean isBuilding;

  private long startTime;

	private Map<String, BufferedImage> tileImages = new ConcurrentHashMap<>();
  private ExecutorService pool;

	private int currentTileCount = 0;

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

	/**
   * Fired when any image is added to the database.
   */
  @Override
	public void imagesAdded() {
		// Not implemented by the CubemapBuilder
	}

	/**
   * Fired when the selected image is changed by something different from
   * manually clicking on the icon.
   *
   * @param oldImage
   *          Old selected {@link StreetsideAbstractImage}
   * @param newImage
   *          New selected {@link StreetsideAbstractImage}
   *
   * @see StreetsideDataListener
   */
  @Override
  public void selectedImageChanged(StreetsideAbstractImage oldImage, StreetsideAbstractImage newImage) {


    startTime = System.currentTimeMillis();

		if (newImage != null) {

			cubemap = null;
			cubemap = new StreetsideCubemap(newImage.getId(), newImage.getLatLon(), newImage.getHe());
			currentTileCount = 0;
			resetTileImages();

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
    if(StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().getScene() != StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().getLoadingScene()) {
      StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().setScene(
  	      StreetsideViewerPanel.getThreeSixtyDegreeViewerPanel().getLoadingScene()
  	  );
	  }

	  final int maxThreadCount = StreetsideProperties.DOWNLOAD_CUBEFACE_TILES_TOGETHER.get()?6:6 * CubemapUtils.getMaxCols() * CubemapUtils.getMaxRows();

		int fails = 0;

    // TODO: message for progress bar
    String[] message = new String[2];
    message[0] = MessageFormat.format("Downloading Streetside imagery for {0}", imageId);
    message[1] = "Wait for completion…….";

    long startTime = System.currentTimeMillis();

    if(CubemapBuilder.getInstance().getTileImages().keySet().size() > 0) {
      pool.shutdownNow();
      CubemapBuilder.getInstance().resetTileImages();
    }

    try {

      pool = Executors.newFixedThreadPool(maxThreadCount);
      List<Callable<List<String>>> tasks = new ArrayList<>(maxThreadCount);

      if (StreetsideProperties.DOWNLOAD_CUBEFACE_TILES_TOGETHER.get()) {
        EnumSet.allOf(CubemapUtils.CubemapFaces.class).forEach(face -> {
          String tileId = String.valueOf(imageId + face.getValue());
          tasks.add(new TileDownloadingTask(tileId));
        });
      } else {

        // launch 4-tiled (low-res) downloading tasks . . .
        if (!StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
          // download all imagery for each cubeface at once

          for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {
            int tileNr = 0;
            for (int j = 0; j < CubemapUtils.getMaxCols(); j++) {
              for (int k = 0; k < CubemapUtils.getMaxRows(); k++) {

                String tileId = String
                  .valueOf(imageId + CubemapUtils.getFaceNumberForCount(i) + Integer.valueOf(tileNr++).toString());
                tasks.add(new TileDownloadingTask(tileId));
              }
            }
          }
        // launch 16-tiled (high-res) downloading tasks
        } else if (StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {

          for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {
            for (int j = 0; j < CubemapUtils.getMaxCols(); j++) {
              for (int k = 0; k < CubemapUtils.getMaxRows(); k++) {

                String tileId = String
                  .valueOf(imageId + CubemapUtils.getFaceNumberForCount(i) + String.valueOf(Integer.valueOf(j).toString() + Integer.valueOf(k).toString()));
                tasks.add(new TileDownloadingTask(tileId));
              }
            }
          }
        }
      } // finish preparing tasks for invocation

      // execute tasks
			MainApplication.worker.submit(() -> {
			  try {
          List<Future<List<String>>> results = pool.invokeAll(tasks);
          
          if(StreetsideProperties.DEBUGING_ENABLED.get() && results != null) {
            for (Future<List<String>> ff : results) {
              try {
                logger.debug(MessageFormat.format("Completed tile downloading task {0} in {1} seconds.",ff.get().toString(),
                  (System.currentTimeMillis() - startTime)/1000));
              } catch (ExecutionException e) {
                logger.error(e);
              }
            }
          }
        } catch (InterruptedException e) {
         logger.error(e);
        }
			});
    } catch (Exception ee) {
      fails++;
      logger.error("Error loading tile for image " + imageId);
      ee.printStackTrace();
    }

    long stopTime = System.currentTimeMillis();
    long runTime = stopTime - startTime;

    if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug(MessageFormat.format("Tile imagery downloading tasks completed in {0} seconds.", runTime / 1000));
    }

    if (fails > 0) {
      logger.error(Integer.valueOf(fails) + " downloading tasks failed!");
    }
  }

  /**
   * Fired when a TileDownloadingTask has completed downloading an image tile. When all of the tiles for the Cubemap
   * have been downloaded, the CubemapBuilder assembles the cubemap.
   *
   * @param tileId
   *          the complete quadKey of the imagery tile, including cubeface and row/column in quaternary.
   * @see TileDownloadingTask
   */
  @Override
  public void tileAdded(String tileId) {
    // determine whether four tiles have been set for each of the
    // six cubemap faces. If so, build the images for the faces
    // and set the views in the cubemap box.

    if(!tileId.startsWith(cubemap.getId())) {
      return;
    }

    currentTileCount++;

    if (currentTileCount == (CubemapUtils.NUM_SIDES * CubemapUtils.getMaxCols() * CubemapUtils.getMaxRows())) {
      if (StreetsideProperties.DEBUGING_ENABLED.get()) {
        long endTime = System.currentTimeMillis();
        long runTime = (endTime - startTime) / 1000;
        logger.debug(
          MessageFormat.format(
            "{0} tile images ready for building cumbemap faces for cubemap {1} in {2} seconds.", currentTileCount,
            CubemapBuilder.getInstance().getCubemap().getId(), Long.toString(runTime))
          );
      }

      buildCubemapFaces();
    }
  }

  /**
   * Assembles the cubemap once all of the tiles have been downloaded.
   * <p>
   * The tiles for each cubemap face are cropped and stitched together
   * then the ImageViews of the cubemap are set with the new imagery.
   *
   * @see         StreetsideCubemap
   */
   private void buildCubemapFaces() {
		CubemapBox cmb = StreetsideViewerDialog.getInstance().getStreetsideViewerPanel().getCubemapBox();
		ImageView[] views = cmb.getViews();

		Image finalImages[] = new Image[CubemapUtils.NUM_SIDES];

		// build 4-tiled cubemap faces and crop buffers
		if (!StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get()) {
			for (int i = 0; i < CubemapUtils.NUM_SIDES; i++) {

				BufferedImage[] faceTileImages = new BufferedImage[CubemapUtils.getMaxCols() * CubemapUtils.getMaxRows()];

				for (int j = 0; j < (CubemapUtils.getMaxCols() * CubemapUtils.getMaxRows()); j++) {
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

				for (int j = 0; j < CubemapUtils.getMaxCols(); j++) {
					for (int k = 0; k < CubemapUtils.getMaxRows(); k++) {
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

    // reset count and image map after assembly
    resetTileImages();
    currentTileCount = 0;
    isBuilding = false;
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
   * @return the isBuilding
   */
  public boolean isBuilding() {
    return isBuilding;
  }


	/**
	 * Destroys the unique instance of the class.
	 */
	public static synchronized void destroyInstance() {
		CubemapBuilder.instance = null;
	}
}