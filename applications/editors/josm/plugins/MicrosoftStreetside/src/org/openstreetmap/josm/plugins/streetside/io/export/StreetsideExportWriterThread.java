// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.io.export;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.log4j.Logger;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;

/**
 * Writes the images from the queue in the file system.
 *
 * @author nokutu
 * @see StreetsideExportManager
 */
public class StreetsideExportWriterThread extends Thread {

  final static Logger logger = Logger.getLogger(StreetsideExportWriterThread.class);

  private final String path;
  private final ArrayBlockingQueue<BufferedImage> queue;
  private final ArrayBlockingQueue<StreetsideAbstractImage> queueImages;
  private final int amount;
  private final ProgressMonitor monitor;

  /**
   * Main constructor.
   *
   * @param path
   *          Path to write the pictures.
   * @param queue
   *          Queue of {@link StreetsideAbstractImage} objects.
   * @param queueImages
   *          Queue of {@link BufferedImage} objects.
   * @param amount
   *          Amount of images that are going to be exported.
   * @param monitor
   *          Progress monitor.
   */
  public StreetsideExportWriterThread(String path,
      ArrayBlockingQueue<BufferedImage> queue,
      ArrayBlockingQueue<StreetsideAbstractImage> queueImages, int amount,
      ProgressMonitor monitor) {
    this.path = path;
    this.queue = queue;
    this.queueImages = queueImages;
    this.amount = amount;
    this.monitor = monitor;
  }

  @Override
  public void run() {
    monitor.setCustomText("Downloaded 0/" + amount);
    BufferedImage img;
    StreetsideAbstractImage mimg;
    String finalPath = "";
    for (int i = 0; i < amount; i++) {
      try {
        img = queue.take();
        mimg = queueImages.take();


        // Transforms the image into a byte array.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        // Write EXIF tags
        TiffOutputSet outputSet = null;
        TiffOutputDirectory exifDirectory;
        TiffOutputDirectory gpsDirectory;
        // If the image is imported, loads the rest of the EXIF data.

        if (null == outputSet) {
          outputSet = new TiffOutputSet();
        }
        exifDirectory = outputSet.getOrCreateExifDirectory();
        gpsDirectory = outputSet.getOrCreateGPSDirectory();

        gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF);
        gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF,
            GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF_VALUE_TRUE_NORTH);

        gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION);
        gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION,
            RationalNumber.valueOf(mimg.getMovingHe()));

        exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);

        outputSet.setGPSInDegrees(mimg.getMovingLatLon().lon(), mimg.getMovingLatLon().lat());
        OutputStream os = new BufferedOutputStream(new FileOutputStream(finalPath + ".jpg"));
        new ExifRewriter().updateExifMetadataLossless(imageBytes, os, outputSet);

        os.close();
      } catch (InterruptedException e) {
        logger.info("Streetside export cancelled");
        return;
      } catch (IOException | ImageReadException | ImageWriteException e) {
        logger.error(e);
      }

      // Increases the progress bar.
      monitor.worked(PleaseWaitProgressMonitor.PROGRESS_BAR_MAX / amount);
      monitor.setCustomText("Downloaded " + (i + 1) + "/" + amount);
    }
  }
}
