// Wrapper class for sanselan library
package org.openstreetmap.josm.plugins.photo_geotagging;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.GPSTagConstants;
import org.apache.sanselan.formats.tiff.fieldtypes.FieldType;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

public class ExifGPSTagger {
    /**
     * Set the GPS values in JPEG EXIF metadata.
     * This is taken from one of the examples of the sanselan project.
     *
     * @param jpegImageFile A source image file.
     * @param dst The output file.
     * @param lat latitude
     * @param lon longitude
     * @param gpsTime time in milliseconds
     * @param ele elevation - can be null if not available
     */
    public static void setExifGPSTag(File jpegImageFile, File dst, double lat, double lon, long gpsTime, Double ele) throws IOException {
        try {
            setExifGPSTagWorker(jpegImageFile, dst, lat, lon, gpsTime, ele);
        } catch (ImageReadException ire) {
            throw new IOException(tr("Read error: "+ire), ire);
        } catch (ImageWriteException ire2) {
            throw new IOException(tr("Write error: "+ire2), ire2);
        }
    }

    public static void setExifGPSTagWorker(File jpegImageFile, File dst, double lat, double lon, long gpsTime, Double ele) throws IOException,
            ImageReadException, ImageWriteException
    {
        OutputStream os = null;
        try {
            TiffOutputSet outputSet = null;

            IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
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

            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

            calendar.setTimeInMillis(gpsTime);

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

            Double[] timeStamp = {new Double(hour), new Double(minute), new Double(second)};
            TiffOutputField gpsTimeStamp = TiffOutputField.create(
                    GPSTagConstants.GPS_TAG_GPS_TIME_STAMP,
                    outputSet.byteOrder, timeStamp);
            TiffOutputDirectory exifDirectory = outputSet.getOrCreateGPSDirectory();
            // make sure to remove old value if present (this method will
            // not fail if the tag does not exist).
            exifDirectory.removeField(GPSTagConstants.GPS_TAG_GPS_TIME_STAMP);
            exifDirectory.add(gpsTimeStamp);

            TiffOutputField gpsDateStamp = SanselanFixes.create(
                    GPSTagConstants.GPS_TAG_GPS_DATE_STAMP,
                    outputSet.byteOrder, dateStamp);
            // make sure to remove old value if present (this method will
            // not fail if the tag does not exist).
            exifDirectory.removeField(GPSTagConstants.GPS_TAG_GPS_DATE_STAMP);
            exifDirectory.add(gpsDateStamp);

            SanselanFixes.setGPSInDegrees(outputSet, lon, lat);

            if (ele != null) {
                byte eleRef =  ele >= 0 ? (byte) 0 : (byte) 1;
                TiffOutputField gpsAltitudeRef = new TiffOutputField(
                        GPSTagConstants.GPS_TAG_GPS_ALTITUDE_REF,
                        FieldType.FIELD_TYPE_BYTE, 1, new byte[] { eleRef });
                exifDirectory.removeField(GPSTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
                exifDirectory.add(gpsAltitudeRef);

                Number[] val = new Number[] { Math.abs(ele) };
                byte[] bytes = FieldType.FIELD_TYPE_RATIONAL.writeData(val, outputSet.byteOrder);
                TiffOutputField gpsAltitude = new TiffOutputField(
                        GPSTagConstants.GPS_TAG_GPS_ALTITUDE,
                        FieldType.FIELD_TYPE_RATIONAL, 1, bytes);
                exifDirectory.removeField(GPSTagConstants.GPS_TAG_GPS_ALTITUDE);
                exifDirectory.add(gpsAltitude);
            }

            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);

            os.close();
            os = null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {}
            }
        }
    }
}
