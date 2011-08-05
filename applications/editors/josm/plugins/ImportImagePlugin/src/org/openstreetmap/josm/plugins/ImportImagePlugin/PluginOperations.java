package org.openstreetmap.josm.plugins.ImportImagePlugin;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.data.DataSourceException;
import org.geotools.data.WorldFileReader;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.Envelope2D;
import org.geotools.image.jai.Registry;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.content.ImageDescription;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.util.InternationalString;



/**
 * Class provides methods for resampling operations, IO and stores important data.
 *
 * @author Christoph Beekmans, Fabian Kowitz, Anna Robaszkiewicz, Oliver Kuhn, Martin Ulitzny
 *
 */
public class PluginOperations {

    private static final Logger logger = Logger.getLogger(PluginOperations.class);

    // contains descriptions of all available CRS
    static Vector<String> crsDescriptions;

    // the standard native CRS of user images
    static CoordinateReferenceSystem defaultSourceCRS;
    // description of 'defaultSourceCRS'
    static String defaultSourceCRSDescription;



    public static enum SUPPORTEDIMAGETYPES {
        tiff, tif, jpg, jpeg, bmp, png
    }

    public static enum POSTFIXES_WORLDFILE {
        wld, jgw, jpgw, pgw, pngw, tfw, tifw, bpw, bmpw,
    };

    /**
     * Reprojects a GridCoverage to a given CRS.
     *
     * @param coverage
     * @param targetCrs
     * @return destination
     * @throws FactoryException
     * @throws NoSuchAuthorityCodeException
     */
    public static GridCoverage2D reprojectCoverage(GridCoverage2D coverage,
            CoordinateReferenceSystem targetCrs) throws NoSuchAuthorityCodeException, FactoryException {

        // TODO: add category for NO_DATA values in coverage (transparency in
        // image)

        GridCoverage2D destination = null;

        DefaultProcessor processor = new DefaultProcessor(null);
        ParameterValueGroup resampleParams = processor.getOperation("Resample")
                .getParameters();

        // set parameters
        resampleParams.parameter("Source").setValue(coverage);
        resampleParams.parameter("CoordinateReferenceSystem").setValue(
                targetCrs);

        // resample coverage with given parameters
        destination = (GridCoverage2D) processor.doOperation(resampleParams);

        return destination;
    }

    /**
     * Creates a org.geotools.coverage.grid.GridCoverage2D from a given file.
     *
     * @param file
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static GridCoverage2D createGridFromFile(File file, CoordinateReferenceSystem refSys, boolean failIfNoPrjFile) throws IOException {

        GridCoverage2D coverage = null;

        if (!file.exists()) throw new FileNotFoundException("File not found.");

        String extension = null;
        String fileNameWithoutExt = null;
        int dotPos = file.getAbsolutePath().lastIndexOf(".");
        extension = file.getAbsolutePath().substring(dotPos);
        fileNameWithoutExt = file.getAbsolutePath().substring(0, dotPos);

        /*------- switch for file type -----------*/
        if (extension.equalsIgnoreCase(".tif")
                || extension.equalsIgnoreCase(".tiff"))
        {

            // try to read GeoTIFF:
            try {
                coverage = readGeoTiff(file, refSys);
                return coverage;
            } catch (DataSourceException dse) {
                if (!dse.getMessage().contains("Coordinate Reference System is not available")){
                    dse.printStackTrace();
                }
            } catch (FactoryException facte) {
                logger.fatal("Error while reading from GeoTIFF:", facte);
                throw new IOException(facte);
            }

            // file is no GeoTiff, searching for Worldfile and projection file:
            String[] postfixes = {"wld", "tfw", "tifw"};
            // try to read Worldfile:
            WorldFileReader tfwReader = null;
            for (int i = 0; i < postfixes.length; i++) {
                File prjFile = new File(fileNameWithoutExt + "." + postfixes[i]);
                if (prjFile.exists()){
                    tfwReader = new WorldFileReader(prjFile);
                }
            }

            if (tfwReader == null) {
                throw new IOException("No Worldfile found.");
            }

            if (refSys == null) {
                // if no crs is delivered try to read projection file:
                refSys = readPrjFile(file);
                if (refSys == null) {
                    if (failIfNoPrjFile) throw new IOException("No projection file found.");
                    logger.debug("no projection given, no projection file found; using unprojected file.");
                }
            }

            BufferedImage img = ImageIO.read(file);

            // create Envelope
            double width = (double) (img.getWidth() * tfwReader.getXPixelSize());
            double height = (double) (img.getHeight() * (-tfwReader.getYPixelSize()));
            double lowerLeft_x = (double) tfwReader.getXULC();
            double lowerLeft_y = (double) tfwReader.getYULC() - height;
            Envelope2D bbox = new Envelope2D(null, new Rectangle2D.Double(lowerLeft_x, lowerLeft_y, width, height));

            coverage = createGridCoverage(img, bbox, refSys);
        }
        //
        else if (extension.equalsIgnoreCase(".jpg")
                || extension.equalsIgnoreCase(".jpeg"))
        {
            String[] postfixes = {"wld", "jgw", "jpgw"};
            // try to read Worldfile:
            WorldFileReader tfwReader = null;
            for (int i = 0; i < postfixes.length; i++) {
                File prjFile = new File(fileNameWithoutExt + "." + postfixes[i]);
                if (prjFile.exists()){
                    tfwReader = new WorldFileReader(prjFile);
                }
            }
            if (tfwReader == null) throw new IOException("No Worldfile found.");

            if (refSys == null) {
                // if no crs is delivered try to read projection file:
                refSys = readPrjFile(file);
                if (refSys == null) {
                    if (failIfNoPrjFile) throw new IOException("No projection file found.");
                    logger.debug("no projection given, no projection file found; using unprojected file.");
                }
            }

            BufferedImage img = ImageIO.read(file);

            // create Envelope
            double width = (double) (img.getWidth() * tfwReader.getXPixelSize());
            double height = (double) (img.getHeight() * (-tfwReader.getYPixelSize()));
            double lowerLeft_x = (double) tfwReader.getXULC();
            double lowerLeft_y = (double) tfwReader.getYULC() - height;
            Envelope2D bbox = new Envelope2D(null, new Rectangle2D.Double(lowerLeft_x, lowerLeft_y, width, height));

            coverage = createGridCoverage(img, bbox, refSys);
        }
        else if(extension.equalsIgnoreCase(".bmp"))
        {
            String[] postfixes = {"wld", "bmpw", "bpw"};
            // try to read Worldfile:
            WorldFileReader tfwReader = null;
            for (int i = 0; i < postfixes.length; i++) {
                File prjFile = new File(fileNameWithoutExt + "." + postfixes[i]);
                if (prjFile.exists()){
                    tfwReader = new WorldFileReader(prjFile);
                }
            }
            if (tfwReader == null) throw new IOException("No Worldfile found.");

            if (refSys == null) {
                // if no crs is delivered try to read projection file:
                refSys = readPrjFile(file);
                if (refSys == null) {
                    if (failIfNoPrjFile) throw new IOException("No projection file found.");
                    logger.debug("no projection given, no projection file found; using unprojected file.");
                }
            }

            BufferedImage img = ImageIO.read(file);

            // create Envelope
            double width = (double) (img.getWidth() * tfwReader.getXPixelSize());
            double height = (double) (img.getHeight() * (-tfwReader.getYPixelSize()));
            double lowerLeft_x = (double) tfwReader.getXULC();
            double lowerLeft_y = (double) tfwReader.getYULC() - height;
            Envelope2D bbox = new Envelope2D(null, new Rectangle2D.Double(lowerLeft_x, lowerLeft_y, width, height));

            coverage = createGridCoverage(img, bbox, refSys);
        }
        else if(extension.equalsIgnoreCase(".png"))
        {

            String[] postfixes = {"wld", "pgw", "pngw"};
            // try to read Worldfile:
            WorldFileReader tfwReader = null;
            for (int i = 0; i < postfixes.length; i++) {
                File prjFile = new File(fileNameWithoutExt + "." + postfixes[i]);
                if (prjFile.exists()){
                    tfwReader = new WorldFileReader(prjFile);
                }
            }
            if(tfwReader == null) throw new IOException("No Worldfile found.");

            if (refSys == null) {
                // if no crs is delivered try to read projection file:
                refSys = readPrjFile(file);
                if (refSys == null) {
                    if (failIfNoPrjFile) throw new IOException("No projection file found.");
                    logger.debug("no projection given, no projection file found; using unprojected file.");
                }
            }

            BufferedImage img = ImageIO.read(file);

            // create Envelope
            double width = (double) (img.getWidth() * tfwReader.getXPixelSize());
            double height = (double) (img.getHeight() * (-tfwReader.getYPixelSize()));
            double lowerLeft_x = (double) tfwReader.getXULC();
            double lowerLeft_y = (double) tfwReader.getYULC() - height;
            Envelope2D bbox = new Envelope2D(null, new Rectangle2D.Double(lowerLeft_x, lowerLeft_y, width, height));

            coverage = createGridCoverage(img, bbox, refSys);
        }
        else{
            throw new IOException("Image type not supported. Supported formats are: \n" +
                    Arrays.toString(SUPPORTEDIMAGETYPES.values()));
        }

        return coverage;
    }

    /**
     * Searches for a projection file (.prj) with the same name of 'file'
     * tries to parse it.
     *
     *
     * @param file image file, not the real world file (will be searched)
     * @return
     * @throws IOException
     */
    public static CoordinateReferenceSystem readPrjFile(File file) throws IOException
    {

        CoordinateReferenceSystem refSys = null;

        String prjFilename = null;
        int dotPos = file.getAbsolutePath().lastIndexOf(".");
        prjFilename = file.getAbsolutePath().substring(0, dotPos) + ".prj";

        File prjFile = new File(prjFilename);
        if (!prjFile.exists()) return null;
        logger.debug("Loading .prj file: " + prjFile.getAbsolutePath());

        StringBuilder sb = new StringBuilder();
        String content = null;
        BufferedReader br = new BufferedReader(new FileReader(prjFile));
        while((content = br.readLine()) != null)
        {
            sb.append(content);
        }

        try {
            refSys = CRS.parseWKT(sb.toString().trim());
        } catch (FactoryException e) {
            throw new IOException("Unable to parse prj-file: '" + prjFile.getName() + "'");
        }

        return refSys;

    }


    /**
     * Method for external use.
     *
     * @param img
     * @param bbox
     * @param crs
     * @return
     */
    public static GridCoverage2D createGridCoverage(BufferedImage img, Envelope2D bbox, CoordinateReferenceSystem crs)
    {
        bbox.setCoordinateReferenceSystem(crs);
        return new GridCoverageFactory().create("", img, bbox);
    }

    /**
     * Method for reading a GeoTIFF file.
     *
     * @param file
     * @param refSys if delivered, the coverage will be forced to use this crs
     * @return
     * @throws IOException
     * @throws FactoryException
     */
    public static GridCoverage2D readGeoTiff(File file, CoordinateReferenceSystem refSys) throws IOException, FactoryException
    {
        GridCoverage2D coverage = null;
        Hints hints = new Hints();
        if(refSys != null)
        {
            hints.put(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, refSys);

        }
        // dont't use the EPSG-Factory because of wrong behaviour
        hints.put(Hints.CRS_AUTHORITY_FACTORY, CRS.getAuthorityFactory(true));

        GeoTiffReader reader = new GeoTiffReader(file, hints);

        coverage = (GridCoverage2D) reader.read(null);

        return coverage;
    }


    /**
     * Loads CRS data from an EPSG database and creates descrptions for each one.
     *
     * @param pluginProps
     * @throws Exception
     */
    public static void loadCRSData(Properties pluginProps)
    {
        String defaultcrsString = pluginProps.getProperty("default_crs_srid");

        crsDescriptions = new Vector<String>();
        Set<String> supportedCodes = CRS.getSupportedCodes("EPSG");
        CRSAuthorityFactory fac = CRS.getAuthorityFactory(false);

        for (Iterator iterator = supportedCodes.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            try {
                if ("WGS84(DD)".equals(string)) {
                    continue;
                }
                InternationalString desc = fac.getDescriptionText("EPSG:" + string);

                String description = desc.toString() + " [-EPSG:" + string + "-]";

                crsDescriptions.add(description);

                if(defaultcrsString != null && defaultcrsString.equalsIgnoreCase("EPSG:" + string)){
                    boolean isEastingFirst = Boolean.valueOf(pluginProps.getProperty("default_crs_eastingfirst"));
                    defaultSourceCRS = CRS.decode("EPSG:" + string, isEastingFirst);
                    defaultSourceCRSDescription = description;
                }
            } catch (NoSuchAuthorityCodeException e) {
                logger.error("Error while loading EPSG data: " + e.getMessage());
            } catch (FactoryException e) {
                logger.error("Error while loading EPSG data: " + e.getMessage());
            }
        }
    }

}
