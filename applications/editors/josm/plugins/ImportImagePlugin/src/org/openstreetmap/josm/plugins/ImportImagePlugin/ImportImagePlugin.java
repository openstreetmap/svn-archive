// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.ImportImagePlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JMenu;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Utils;

/**
 * Plugin class.
 * Provides basic routines for plugin installation and provides the plugin properties.
 *
 *
 * @author Christoph Beekmans, Fabian Kowitz, Anna Robaszkiewicz, Oliver Kuhn, Martin Ulitzny
 *
 */
public class ImportImagePlugin extends Plugin {

    private static Logger logger;

    JMenu mainmenu = null;
    JosmAction loadFileAction = null;

    // plugin properties
    static Properties pluginProps;

    // path constants
    static final String PLUGIN_DIR = Main.pref.getPluginsDirectory().getAbsolutePath() + "/ImportImagePlugin/";
    static final String PLUGINPROPERTIES_FILENAME = "pluginProperties.properties";
    static final String PLUGINPROPERTIES_PATH = PLUGIN_DIR + PLUGINPROPERTIES_FILENAME;
    static final String PLUGINLIBRARIES_DIR = PLUGIN_DIR + "lib/";
    static final String LOGGING_PROPERTIES_FILEPATH = PLUGIN_DIR + "log4j.properties/";

    public Properties getPluginProps() {
        return pluginProps;
    }

    /**
     * constructor
     *
     * @throws IOException if any I/O error occurs
     */
    public ImportImagePlugin(PluginInformation info) throws IOException {
        super(info);

        try {
            // Initialize logger
            initializeLogger();

            // Check whether plugin has already been installed. Otherwise install
            checkInstallation();

            // If resources are available load properties from plugin directory
            if (pluginProps == null || pluginProps.isEmpty()) {
                pluginProps = new Properties();
                pluginProps.load(new File(PLUGINPROPERTIES_PATH).toURI().toURL().openStream());
                logger.debug("Plugin properties loaded");
            }

            // load information about supported reference systems
            PluginOperations.loadCRSData(pluginProps);

            // create new Action for menu entry
            loadFileAction = new LoadImageAction();
            loadFileAction.setEnabled(true);
            if (Main.main != null) {
                MainMenu.add(MainApplication.getMenu().imagerySubMenu, loadFileAction);
            }

            ExtensionFileFilter.addImporter(new ImportImageFileImporter());

        } catch (IOException e) {
            logger.fatal("Error while loading plugin", e);
            throw e;
        }
    }

    /**
     * Checks whether plugin resources are available.
     * If not, start install procedure.
     */
    private void checkInstallation() throws IOException {
        // check plugin resource state
        boolean isInstalled = true;
        if (!new File(PLUGINPROPERTIES_PATH).exists()
                || !new File(PLUGIN_DIR).exists()
                || !new File(PLUGINLIBRARIES_DIR).exists())
            isInstalled = false;


        // if properties file doesn't exist, install plugin
        if (!isInstalled) {

            /*----------- Begin installation ---------------*/

            // check if plugin directory exist
            File pluginDir = new File(PLUGIN_DIR);
            if (!pluginDir.exists()) {
                Utils.mkDirs(pluginDir);
            }

            // check if "lib" directory exist
            File libDir = new File(PLUGINLIBRARIES_DIR);
            if (!libDir.exists()) {
                Utils.mkDirs(libDir);
            }

            // create local properties file
            if (pluginProps == null || pluginProps.isEmpty()) {
                try (FileWriter fw = new FileWriter(new File(PLUGINPROPERTIES_PATH))) {
                    URL propertiesURL = getClass().getResource("resources/" + PLUGINPROPERTIES_FILENAME);
                    if (propertiesURL != null) {
                        pluginProps = new Properties();
                        pluginProps.load(propertiesURL.openStream());
                        pluginProps.store(fw, null);
                    }
                }
                logger.debug("Plugin properties loaded");
            }

            if (!new File(LOGGING_PROPERTIES_FILEPATH).exists()) {
                try (FileWriter fw = new FileWriter(new File(LOGGING_PROPERTIES_FILEPATH))) {
                    URL propertiesURL = getClass().getResource("resources/log4j.properties");
                    if (propertiesURL != null) {
                        Properties loggingProps = new Properties();
                        loggingProps.load(propertiesURL.openStream());
                        loggingProps.store(fw, null);
                    }
                }
                logger.debug("Logging properties created");
            }

            logger.debug("Plugin successfully installed");
        }
    }

    /**
     * Initialize logger.
     */
    private void initializeLogger() {

        Properties props = new Properties();
        try {
            props.load(new File(LOGGING_PROPERTIES_FILEPATH).toURI().toURL().openStream());

            // Set file for logging here:
            props.setProperty("log4j.appender.MyRoFiAppender.file",
                    (Main.pref.getPluginsDirectory().getAbsolutePath() + "/ImportImagePlugin/" + "log.log"));

            PropertyConfigurator.configure(props);

            logger = Logger.getLogger(ImportImagePlugin.class);

            logger.info("Logger successfully initialized.");

            return;

        } catch (IOException e) {
            System.out.println("Logging properties file not found. Using standard settings.");
        }

        // if no log4j.properties file can be found, initialize manually:

        props.setProperty("log4j.rootLogger", "INFO, A");
        props.setProperty("log4j.appender.A", "org.apache.log4j.FileAppender");

        props.setProperty("log4j.appender.A.layout",
                "org.apache.log4j.PatternLayout ");
        props.setProperty("log4j.appender.A.layout.ConversionPattern",
                "%d{ISO8601} %-5p [%t] %c: %m%n");

        // Set file for logging here:
        props.setProperty("log4j.appender.A.file",
                (Main.pref.getPluginsDirectory().getAbsolutePath() + "/ImportImagePlugin/" + "log.log"));

        PropertyConfigurator.configure(props);
        logger = Logger.getLogger(ImportImagePlugin.class);
    }
}
