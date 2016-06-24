package org.openstreetmap.josm.plugins.photo_geotagging;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openstreetmap.josm.TestUtils;

public class ExifGPSTaggerTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    @Ignore("To enable after https://issues.apache.org/jira/browse/IMAGING-179 is fixed")
    public void testTicket11757() throws Exception {
        final File in = new File(TestUtils.getTestDataRoot(), "_DSC1234.jpg");
        ExifGPSTagger.setExifGPSTag(in, tempFolder.newFile(), 12, 34, new Date(), 12.34, Math.E, Math.PI);
    }

    @Test
    public void testTicket11757WriteWithoutChange() throws Exception {
        final File in = new File(TestUtils.getTestDataRoot(), "_DSC1234.jpg");
        final TiffImageMetadata exif = ((JpegImageMetadata) Imaging.getMetadata(in)).getExif();
        final TiffOutputSet outputSet = exif.getOutputSet();
        new ExifRewriter().updateExifMetadataLossless(in, new ByteArrayOutputStream(), outputSet);
    }

    @Test
    @Ignore("To enable after https://josm.openstreetmap.de/ticket/11902 is fixed")
    public void testTicket11902() throws Exception {
        final File in = new File(TestUtils.getTestDataRoot(), "IMG_7250_small.JPG");
        final File out = tempFolder.newFile();
        ExifGPSTagger.setExifGPSTag(in, out, 12, 34, new Date(), 12.34, Math.E, Math.PI);
        final Process jhead = Runtime.getRuntime().exec(new String[]{"jhead", out.getAbsolutePath()});
        final String stdout = new Scanner(jhead.getErrorStream()).useDelimiter("\\A").next();
        System.out.println(stdout);
        assertFalse(stdout.contains("Suspicious offset of first Exif IFD value"));
    }
}
