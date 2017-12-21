// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geotools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;

import org.geotools.image.ImageWorker;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Logging;

import com.sun.media.jai.imageioimpl.ImageReadWriteSpi;

/**
 * GeoTools plugin, bringing this library to other JOSM plugins.
 */
public class GeoToolsPlugin extends Plugin {

    /**
     * Constructs a new {@code GeoToolsPlugin}.
     * @param info plugin information
     */
    public GeoToolsPlugin(PluginInformation info) {
        super(info);
        initJAI();
        initGeoTools();
        checkEPSG();
    }

    private void initJAI() {
        // Disable mediaLib searching that produces unwanted errors
        // See https://www.java.net/node/666373
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");

        // As JAI-Ext will replace the operation registry, it needs to be loaded before we modify it later on
        if (ImageWorker.isJaiExtEnabled()) {
            Logging.debug("geotools: load JAI-Ext operations");
        }

        // As the JAI jars are bundled in the geotools plugin, JAI initialization does not work,
        // so we need to perform the tasks described here ("Initialization and automatic loading of registry objects"):
        // http://docs.oracle.com/cd/E17802_01/products/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/OperationRegistry.html
        OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        if (registry == null) {
            Logging.error("geotools: error in JAI initialization. Cannot access default operation registry");
        } else {
            // Update registry with com.sun.media.jai.imageioimpl.ImageReadWriteSpi (only class listed javax.media.jai.OperationRegistrySpi)
            // it would be safer to parse this file instead, but a JAI update is very unlikely as it has not been modified since 2005
            try {
                new ImageReadWriteSpi().updateRegistry(registry);
            } catch (IllegalArgumentException e) {
                // See #10652: IAE: A descriptor is already registered against the name "ImageRead" under registry mode "rendered"
                Logging.warn("geotools: error in JAI/ImageReadWriteSpi initialization: "+e.getMessage());
            }

            // Update registry with GeoTools registry file
            try (InputStream in = GeoToolsPlugin.class.getResourceAsStream("/META-INF/registryFile.jai")) {
                if (in == null) {
                    Logging.error("geotools: error in JAI initialization. Cannot access META-INF/registryFile.jai");
                } else {
                    registry.updateFromStream(in);
                }
            } catch (IOException | IllegalArgumentException e) {
                Logging.error("geotools: error in JAI/GeoTools initialization: "+e.getMessage());
            }
        }

        // Manual registering because plugin jar is not on application classpath
        IIORegistry ioRegistry = IIORegistry.getDefaultInstance();
        ClassLoader loader = GeoToolsPlugin.class.getClassLoader();

        Iterator<Class<?>> categories = ioRegistry.getCategories();
        while (categories.hasNext()) {
            @SuppressWarnings("unchecked")
            Iterator<IIOServiceProvider> riter = ServiceLoader.load((Class<IIOServiceProvider>) categories.next(), loader).iterator();
            while (riter.hasNext()) {
                IIOServiceProvider provider = riter.next();
                Logging.debug("Registering " + provider.getClass());
                ioRegistry.registerServiceProvider(provider);
            }
        }
    }

    private void initGeoTools() {
        // Force Axis order. Fix #8248
        // See http://docs.geotools.org/stable/userguide/library/referencing/order.html
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    private void checkEPSG() {
        try {
            CRS.decode("EPSG:4326");
        } catch (NoSuchAuthorityCodeException e) {
            Logging.error("geotools: error in EPSG database initialization. NoSuchAuthorityCodeException: "+e.getMessage());
        } catch (FactoryException e) {
            Logging.error("geotools: error in EPSG database initialization. FactoryException: "+e.getMessage());
        }
    }
}
