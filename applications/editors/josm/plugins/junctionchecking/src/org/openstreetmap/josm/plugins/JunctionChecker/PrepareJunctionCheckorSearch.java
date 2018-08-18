// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.JunctionChecker;

import java.util.HashSet;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;

/**
 * prüft due Vorbedinungen des JunctionCheck oder Suche und übergibt die Parameter den Junction-Check-Klassen
 * @author  joerg
 */
public class PrepareJunctionCheckorSearch {

    private final JunctionCheckerPlugin plugin;
    private final int n;
    private HashSet<Channel> subset;
    private JunctionCheckTask jct;
    private final boolean produceRelation;

    public PrepareJunctionCheckorSearch(JunctionCheckerPlugin plugin, int n, boolean produceRelation) {
        this.plugin = plugin;
        this.n = n;
        this.subset = new HashSet<>();
        this.produceRelation = produceRelation;
    }

    protected void prepareJunctionCheck() {
        if (prepareSubset()) {
            jct = new JunctionCheckTask(plugin, n, subset, produceRelation);
            MainApplication.worker.submit(jct);
        }
    }

    protected void prepareJunctionSearch() {
        if (prepareSubset()) {
            MainApplication.worker.submit(new JunctionSearchTask(plugin, n, subset, produceRelation));
        }
    }

    private boolean prepareSubset() {
        if (plugin.getChannelDigraph().getSelectedChannels().size() < 6) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), "Less then 6 channels are selected");
            return false;
        }
        subset = plugin.getChannelDigraph().getSelectedChannels();
        return true;
    }
}
