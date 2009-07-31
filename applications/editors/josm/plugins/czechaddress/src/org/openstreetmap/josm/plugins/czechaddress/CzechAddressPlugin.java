package org.openstreetmap.josm.plugins.czechaddress;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.czechaddress.actions.ConflictResolveAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.FactoryAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.GroupManipulatorAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.HelpAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.ManagerAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.PointManipulatorAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.SplitAreaByEmptyWayAction;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Database;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithStreets;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Street;
import org.openstreetmap.josm.plugins.czechaddress.gui.ConflictResolver;
import org.openstreetmap.josm.plugins.czechaddress.gui.FactoryDialog;
import org.openstreetmap.josm.plugins.czechaddress.gui.LocationSelector;
import org.openstreetmap.josm.plugins.czechaddress.gui.ManagerDialog;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.SelectionMonitor;
import org.openstreetmap.josm.plugins.czechaddress.parser.MvcrParser;

/**
 * Plugin for handling address nodes within the Czech Republic.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class CzechAddressPlugin extends Plugin implements StatusListener {


    private JMenu czechMenu;
    private List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
    private static Logger logger = Logger.getLogger(CzechAddressPlugin.class.getName());

    public void initLoggers() {

        String filename = getPluginDir() + "-log.xml";
        /*final Logger[] loggers = new Logger[]
            {logger, Reasoner.logger, ConflictResolver.logger};*/

        try {
            Handler fileHandler = new FileHandler(filename);
            fileHandler.setLevel(Level.ALL);
            for (Enumeration<String> e = LogManager.getLogManager().getLoggerNames();
                    e.hasMoreElements();) {
                String name = e.nextElement();
                if (!name.startsWith(CzechAddressPlugin.class.getPackage().getName()))
                    continue;

                Logger.getLogger(name).setLevel(Level.FINE);
                Logger.getLogger(name).addHandler(fileHandler);
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "cannot create file", ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, "permission denied", ex);
        }
    }

    public CzechAddressPlugin() {

        /*boolean x;
        x = StringUtils.matchAbbrev("Ahoj lidi", "Ahoj lidi");
        System.out.println(x ? "Match" : "Differ");
        x = StringUtils.matchAbbrev("Bož. Němcové", "Boženy Němca.");
        System.out.println(x ? "Match" : "Differ");*/

        addStatusListener(this);

        ConflictResolver.getInstance();
        SelectionMonitor.getInstance();
        FactoryDialog.getInstance();
        Reasoner.getInstance();

        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (assertionsEnabled) initLoggers();

        MainMenu.add(Main.main.menu.toolsMenu, new SplitAreaByEmptyWayAction());

        // Prepare for filling the database.
        final MvcrParser parser = new MvcrParser();
        parser.setTargetDatabase(Database.getInstance());
        parser.setStorageDir(getPluginDir());

        // Fill the database in separate thread.
        Thread t = new Thread("CzechAddress: DB preload") {
          @Override public void run() {
            super.run();
            try {
               parser.fillDatabase();
               broadcastStatusChange(StatusListener.MESSAGE_DATABASE_LOADED);
            } catch (DatabaseLoadException dle) {
               dle.printStackTrace();
               System.err.println("CzechAddress: " +
                            "Selhalo načtení databáze. Plugin je neaktivní.");
            }
        }};

        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {

        if (newFrame == null)
            return;

        newFrame.addToggleDialog(FactoryDialog.getInstance());
        newFrame.addMapMode(new IconToggleButton(new FactoryAction(newFrame)));
    }

    static public void initReasoner() {
        Reasoner reasoner = Reasoner.getInstance();

        synchronized(reasoner) {
            reasoner.reset();
            reasoner.openTransaction();
            for (House house : location.getAllHouses())
                reasoner.update(house);

            for (Street street : location.getAllStreets())
                reasoner.update(street);

            for (OsmPrimitive prim : Main.ds.allPrimitives()) {
                if (House.isMatchable(prim) || Street.isMatchable(prim))
                    reasoner.update(prim);
            }
            reasoner.closeTransaction();
        }
        ManagerDialog dialog = new ManagerDialog();
        if (dialog.countAutomaticRenameProposals() > 0)
            dialog.setVisible(true);
    }

    static private ElementWithStreets location = null;
    static public  ElementWithStreets getLocation() {
        if (location == null)
            changeLocation();
        return location;
    }

    static public void changeLocation() {
        ElementWithStreets newLocation = LocationSelector.selectLocation();

        if (newLocation != null && newLocation != location) {
            location = newLocation;
            broadcastStatusChange(MESSAGE_LOCATION_CHANGED);
        }
    }

    static private Set<StatusListener> listeners = new HashSet<StatusListener>();
    static public void addStatusListener(StatusListener l)    {listeners.add(l);}
    static public void removeStatusListener(StatusListener l) {listeners.remove(l);}
    static public void broadcastStatusChange(int statusMessage) {
        for (StatusListener listener : listeners)
            listener.pluginStatusChanged(statusMessage);
    }

    public void pluginStatusChanged(int message) {
        if (message == MESSAGE_DATABASE_LOADED) {
            czechMenu = Main.main.menu.addMenu("Adresy", KeyEvent.VK_A, 4);
            menuItems.add(MainMenu.add(czechMenu, new PointManipulatorAction()));
            menuItems.add(MainMenu.add(czechMenu, new GroupManipulatorAction()));
            menuItems.add(MainMenu.add(czechMenu, new ConflictResolveAction()));
            menuItems.add(MainMenu.add(czechMenu, new ManagerAction()));
            menuItems.add(MainMenu.add(czechMenu, new HelpAction()));
            return;
        }

        if (message == MESSAGE_LOCATION_CHANGED) {
            initReasoner();
            return;
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return Preferences.getInstance();
    }



}
