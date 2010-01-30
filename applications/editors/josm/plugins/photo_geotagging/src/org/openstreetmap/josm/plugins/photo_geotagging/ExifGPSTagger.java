// This is from a file of the sanselan project that is supposed to show, how the library can be used:
// https://svn.apache.org/repos/asf/commons/proper/sanselan/trunk/src/test/java/org/apache/sanselan/sampleUsage/WriteExifMetadataExample.java
package org.openstreetmap.josm.plugins.photo_geotagging;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

public class ExifGPSTagger {
    /**
     * Set the GPS values in JPEG EXIF metadata.
     * This is taken from one of the examples of the sanselan project.
     * 
     * @param jpegImageFile
     *            A source image file.
     * @param dst
     *            The output file.
     * @throws IOException
     * @throws ImageReadException
     * @throws ImageWriteException
     */
    public static void setExifGPSTag(File jpegImageFile, File dst, double lat, double lon) throws IOException {
        try {
            setExifGPSTagWorker(jpegImageFile, dst, lat, lon);
        } catch (ImageReadException ire) {
            throw new IOException(tr("Read error!"));
        } catch (ImageWriteException ire2) {
            throw new IOException(tr("Write error!"));
        }
    }       
   
    public static void setExifGPSTagWorker(File jpegImageFile, File dst, double lat, double lon) throws IOException,
            ImageReadException, ImageWriteException
    {
        OutputStream os = null;
        try
        {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata)
            {
                // note that exif might be null if no Exif metadata is found.
                TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif)
                {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet)
                outputSet = new TiffOutputSet();

            {
                outputSet.setGPSInDegrees(lon, lat);
            }

            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);

            os.close();
            os = null;
        } finally
        {
            if (os != null)
                try
                {
                    os.close();
                } catch (IOException e)
                {

                }
        }
    }
}
