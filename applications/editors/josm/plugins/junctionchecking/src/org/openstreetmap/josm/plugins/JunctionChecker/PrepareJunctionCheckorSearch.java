package org.openstreetmap.josm.plugins.JunctionChecker;

import java.util.HashSet;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
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

	public PrepareJunctionCheckorSearch(JunctionCheckerPlugin plugin, int n, boolean producerelation) {
		this.plugin = plugin;
		this.n = n;
		this.subset = new HashSet<Channel>();
		this.produceRelation = producerelation;
	}


	protected void prepareJunctionCheck() {
		if (prepareSubset()) {
			jct = new JunctionCheckTask(plugin, n, subset, produceRelation);
			Main.worker.submit(jct);
		}
	}

	protected void prepareJunctionSearch() {
		if (prepareSubset()) {
			JunctionSearchTask jst = new JunctionSearchTask(plugin, n, subset, produceRelation);
			Main.worker.submit(jst);
		}
	}

	private boolean prepareSubset(){
		if (plugin.getChannelDigraph().getSelectedChannels().size() < 6) {
			JOptionPane.showMessageDialog(Main.parent, "Less then 6 channels are selected");
			return false;
		}
		subset = plugin.getChannelDigraph().getSelectedChannels();
		return true;
	}
}
