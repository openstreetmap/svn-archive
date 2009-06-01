package org.openstreetmap.josm.plugins.czechaddress;

import java.awt.event.KeyEvent;
import org.openstreetmap.josm.plugins.czechaddress.actions.PointManipulatorAction;
import org.openstreetmap.josm.plugins.czechaddress.gui.LocationSelector;
import org.openstreetmap.josm.plugins.czechaddress.actions.GroupManipulatorAction;
import org.openstreetmap.josm.plugins.czechaddress.gui.ConflictResolver;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Database;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.ElementWithStreets;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.czechaddress.actions.ConflictResolveAction;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.parser.MvcrParser;
import org.openstreetmap.josm.plugins.czechaddress.actions.FactoryAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.HelpAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.ModifierAction;
import org.openstreetmap.josm.plugins.czechaddress.actions.SplitAreaByEmptyWayAction;
import org.openstreetmap.josm.plugins.czechaddress.gui.Inspector;
import org.openstreetmap.josm.plugins.czechaddress.gui.Renamer;
import org.openstreetmap.josm.plugins.czechaddress.gui.FactoryDialog;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.SelectionMonitor;

/**
 * Plugin for handling address nodes within the Czech Republic.
 * 
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class CzechAddressPlugin extends Plugin implements StatusListener {


    JMenu czechMenu;
    List<JMenuItem> menuItems = new ArrayList<JMenuItem>();

    List<AbstractButton> pluginButtons =
            new ArrayList<AbstractButton>();

    static public Reasoner reasoner = null;
    static public Database database = null;

    static public FactoryDialog     factoryDialog    = null;
    static public ConflictResolver  conflictResolver = null;

    static public Renamer   renamer  = null;
    static public Inspector inspector = null;


    static private String pluginDir = null;

    public CzechAddressPlugin() {

        pluginDir = getPluginDir();
        addStatusListener(this);

        factoryDialog    = new FactoryDialog();
        conflictResolver = new ConflictResolver();

        
        MainMenu.add(Main.main.menu.toolsMenu, new SplitAreaByEmptyWayAction());


        // Prepare for filling the database.
        database = new Database();
        final MvcrParser parser = new MvcrParser();
        //parser.setFilter(null, null, null, "");
        //parser.setFilter("HUSTOPEČE", "HUSTOPEČE", null, null);
        parser.setTargetDatabase(database);
        parser.setStorageDir(pluginDir);

        // Fill the database in separate thread.
        Thread t = new Thread() { @Override public void run() {
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
        
        newFrame.addToggleDialog(factoryDialog);
        newFrame.addMapMode(new IconToggleButton(new FactoryAction(newFrame)));
    }

    static public void initReasoner() {

        // Move houses from list of Houses to list of AddressElements.
        List<House> houses   = location.getAllHouses();
        ArrayList<AddressElement> pool
                = new ArrayList<AddressElement>(houses.size());
        for (House house : houses) pool.add(house);

        // Update database according to the map
        (new Renamer()).setVisible(true);
        (new Inspector()).setVisible(true);

        // And add them to the reasoner.
        reasoner = new Reasoner(pool);
        reasoner.addPrimitives(Main.ds.allPrimitives());
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


    
    static private List<StatusListener> listeners =
                    new ArrayList<StatusListener>();

    static public void addStatusListener(StatusListener l) {
        if (!listeners.contains(l))
            listeners.add(l);
    }

    static public void removeStatusListener(StatusListener l) {
        listeners.remove(l);
    }

    static public void broadcastStatusChange(int statusMessage) {
        for (StatusListener l : listeners)
            l.pluginStatusChanged(statusMessage);
    }

    public void pluginStatusChanged(int message) {
        if (message == MESSAGE_DATABASE_LOADED) {
            czechMenu = Main.main.menu.addMenu("Adresy", KeyEvent.VK_A, 4);
            menuItems.add(MainMenu.add(czechMenu, new PointManipulatorAction()));
            menuItems.add(MainMenu.add(czechMenu, new GroupManipulatorAction()));
            menuItems.add(MainMenu.add(czechMenu, new ConflictResolveAction()));
            menuItems.add(MainMenu.add(czechMenu, new ModifierAction()));
            menuItems.add(MainMenu.add(czechMenu, new HelpAction()));
            return;
        }

        if (message == MESSAGE_LOCATION_CHANGED) {
            initReasoner();
            return;
        }


        // SelectionMonitor cannot be used because of synchronization problems.
        /*if (message == MESSAGE_REASONER_REASONED) {
            System.out.println("ReasonerReasoned");
            if (selectionMonitor == null)
                selectionMonitor = new SelectionMonitor();
        }*/
    }
}
