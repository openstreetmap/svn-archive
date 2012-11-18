// License: GPL. Copyright 2012 Don-vip
package org.openstreetmap.josm.plugins.geotools;

import java.io.IOException;
import java.io.InputStream;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import com.sun.media.jai.imageioimpl.ImageReadWriteSpi;

public class GeoToolsPlugin extends Plugin {
    public GeoToolsPlugin(PluginInformation info) {
        super(info);
        initJAI();
        checkEPSG();
    }

    private void initJAI() {
        // Disable mediaLib searching that produces unwanted errors
        // See http://www.java.net/node/666373
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
                
        // As the JAI jars are bundled in the geotools plugin, JAI initialization does not work,
        // so we need to perform the tasks described here ("Initialization and automatic loading of registry objects"):
        // http://docs.oracle.com/cd/E17802_01/products/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/OperationRegistry.html
        OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        if (registry == null) {
            System.err.println("geotools: error in JAI initialization. Cannot access default operation registry");
        } else {
            // Update registry with com.sun.media.jai.imageioimpl.ImageReadWriteSpi (only class listed javax.media.jai.OperationRegistrySpi)
            // it would be safer to parse this file instead, but a JAI update is very unlikely as it has not been modified since 2005 
            new ImageReadWriteSpi().updateRegistry(registry);

            // Update registry with GeoTools registry file 
            InputStream in = GeoToolsPlugin.class.getResourceAsStream("/META-INF/registryFile.jai");
            if (in == null) {
                System.err.println("geotools: error in JAI initialization. Cannot access META-INF/registryFile.jai");
            } else {
                try {
                    registry.updateFromStream(in);
                } catch (IOException e) {
                    System.err.println("geotools: error in JAI initialization. Cannot update default operation registry");
                }
                try {
                    in.close();
                } catch (IOException e) {
                    System.err.println("geotools: error in JAI initialization. Cannot close input stream");
                }
            }
            
            // Print JAI registry contents
            //for (String mode : RegistryMode.getModeNames()) {
            //    System.out.println("geotools: JAI mode "+mode+": "+Arrays.toString(registry.getDescriptorNames(mode)));
            //}
        }
    }

    private void checkEPSG() {
        try {
            CRS.decode("EPSG:4326");
        } catch (NoSuchAuthorityCodeException e) {
            System.err.println("geotools: error in EPSG database initialization. NoSuchAuthorityCodeException: "+e.getMessage());
        } catch (FactoryException e) {
            System.err.println("geotools: error in EPSG database initialization. FactoryException: "+e.getMessage());
        }
    }
}