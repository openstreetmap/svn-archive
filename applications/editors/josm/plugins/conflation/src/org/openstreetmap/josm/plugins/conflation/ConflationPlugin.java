// License: GPL. Copyright 2011 by Josh Doe and others
package org.openstreetmap.josm.plugins.conflation;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;


public class ConflationPlugin extends Plugin {

    ConflationAction action = null;
    Logger logger;

    /**
     * constructor
     */
    public ConflationPlugin(PluginInformation info) {
        super(info);

        try {
            logger = Logger.getLogger(ConflationPlugin.class.getName());
            FileHandler fh = new FileHandler("C:/temp/log.txt");
            logger.addHandler(fh);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(Main.parent, "Failed to create log file",
                    "Failed to create logger", JOptionPane.ERROR_MESSAGE);
        }

        try {
            JMenu conflationMenu = Main.main.menu.addMenu(marktr("Conflation"), KeyEvent.VK_R,
                    Main.main.menu.defaultMenuPos, ht("/Plugin/Conflation"));
            MainMenu.add(conflationMenu, new ConflationAction());


        } catch (Exception e) {
            JOptionPane.showMessageDialog(Main.parent, e.toString(),
                    "Error adding conflation menu item", JOptionPane.ERROR_MESSAGE);
        }
    }
}
