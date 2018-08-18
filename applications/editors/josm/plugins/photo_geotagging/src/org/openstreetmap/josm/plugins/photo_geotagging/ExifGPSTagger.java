// Wrapper class for sanselan library
package org.openstreetmap.josm.plugins.photo_geotagging;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

public class ExifGPSTagger {
    /**
     * Set the GPS values in JPEG EXIF metadata.
     * This is taken from one of the examples of the sanselan project.
     *
     * @param jpegImageFile A source image file.
     * @param dst The output file.
     * @param lat latitude
     * @param lon longitude
     * @param gpsTime time - can be null if not available
     * @param speed speed in km/h - can be null if not available
     * @param ele elevation - can be null if not available
     * @param imgDir image direction in degrees (0..360) - can be null if not available
     * @throws IOException in case of I/O error
     */
    public static void setExifGPSTag(File jpegImageFile, File dst, double lat, double lon, Date gpsTime, Double speed, Double ele, Double imgDir) throws IOException {
        try {
            setExifGPSTagWorker(jpegImageFile, dst, lat, lon, gpsTime, speed, ele, imgDir);
        } catch (ImageReadException ire) {
            throw new IOException(tr("Read error: "+ire), ire);
        } catch (ImageWriteException ire2) {
            throw new IOException(tr("Write error: "+ire2), ire2);
        }
    }

    public static void setExifGPSTagWorker(File jpegImageFile, File dst, double lat, double lon, Date gpsTime, Double speed, Double ele, Double imgDir)
            throws IOException, ImageReadException, ImageWriteException {
        TiffOutputSet outputSet = null;

        ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (null != jpegMetadata) {
            TiffImageMetadata exif = jpegMetadata.getExif();

            if (null != exif) {
                outputSet = exif.getOutputSet();
            }
        }

        if (null == outputSet) {
            outputSet = new TiffOutputSet();
        }

        TiffOutputDirectory gpsDirectory = outputSet.getOrCreateGPSDirectory();
        gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_VERSION_ID);
        gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_VERSION_ID, (byte)2, (byte)3, (byte)0, (byte)0);

        if (gpsTime != null) {
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            calendar.setTime(gpsTime);

            final int year =   calendar.get(Calendar.YEAR);
            final int month =  calendar.get(Calendar.MONTH) + 1;
            final int day =    calendar.get(Calendar.DAY_OF_MONTH);
            final int hour =   calendar.get(Calendar.HOUR_OF_DAY);
            final int minute = calendar.get(Calendar.MINUTE);
            final int second = calendar.get(Calendar.SECOND);

            DecimalFormat yearFormatter = new DecimalFormat("0000");
            DecimalFormat monthFormatter = new DecimalFormat("00");
            DecimalFormat dayFormatter = new DecimalFormat("00");

            final String yearStr = yearFormatter.format(year);
            final String monthStr = monthFormatter.format(month);
            final String dayStr = dayFormatter.format(day);
            final String dateStamp = yearStr+":"+monthStr+":"+dayStr;
            //System.err.println("date: "+dateStamp+"  h/m/s: "+hour+"/"+minute+"/"+second);

            // make sure to remove old value if present (this method will
            // not fail if the tag does not exist).
            gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_TIME_STAMP);
            gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_TIME_STAMP, 
                    RationalNumber.valueOf(hour),
                    RationalNumber.valueOf(minute),
                    RationalNumber.valueOf(second));

            // make sure to remove old value if present (this method will
            // not fail if the tag does not exist).
            gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_DATE_STAMP);
            gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_DATE_STAMP, dateStamp);
        }

        outputSet.setGPSInDegrees(lon, lat);

        if (speed != null) {
            gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_SPEED_REF);
            gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_SPEED_REF,
                             GpsTagConstants.GPS_TAG_GPS_SPEED_REF_VALUE_KMPH);

            gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_SPEED);
            gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_SPEED, RationalNumber.valueOf(speed));
        }

        if (ele != null) {
            byte eleRef =  ele >= 0 ? (byte) 0 : (byte) 1;
            gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
            gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF, eleRef);

            gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
            gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE, RationalNumber.valueOf(Math.abs(ele)));
        }

        if (imgDir != null) {
            gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF);
            gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF,
                             GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF_VALUE_TRUE_NORTH);
            // make sure the value is in the range 0.0...<360.0
            if (imgDir < 0.0) {
                imgDir %= 360.0; // >-360.0...-0.0
                imgDir += 360.0; // >0.0...360.0
            }
            if (imgDir >= 360.0) {
                imgDir %= 360.0;
            }
            gpsDirectory.removeField(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION);
            gpsDirectory.add(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION, RationalNumber.valueOf(imgDir));
        }

        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(dst))) {
            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
        }
    }
}
