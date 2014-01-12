package org.openstreetmap.josm.plugins.JunctionChecker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking.JunctionChecker;
import org.xml.sax.SAXException;

/**
 * Diese Klasse wird aufgerufen, wenn in einer Teilmenge von Channels nach Kreuzungen gesucht wird.
 * @author  joerg
 */
public class JunctionSearchTask extends PleaseWaitRunnable{

	private final JunctionChecker jc;
	private final JunctionCheckerPlugin plugin;
	private final int n;
	private final HashSet<Channel> subset;
	private final boolean produceRelation;
	private boolean canceled;

	public JunctionSearchTask(JunctionCheckerPlugin plugin, int n,
			HashSet<Channel> subset,
			boolean produceRelation) {
		super("JunctionSearch",false);
		this.plugin = plugin;
		this.n = n;
		this.subset = subset;
		this.produceRelation = produceRelation;
		jc = new JunctionChecker(plugin.getChannelDigraph(), n);
	}

	@Override
	protected void cancel() {
		this.canceled = true;
		progressMonitor.cancel();
	}

	@Override
	protected void finish() {
		progressMonitor.finishTask();
		if (canceled) {
			return;
		}
		ArrayList<HashSet<Channel>> junctions = jc.getJunctions();
		JOptionPane.showMessageDialog(Main.parent, tr("Number of {0}-ways junctions found: {1}", n, junctions.size()));
		if (produceRelation) {
			for (int i = 0; i < junctions.size(); i++) {
				plugin.getRelationProducer().produceRelation(junctions.get(i) , n);
			}
		}
	}

	@Override
	protected void realRun() throws SAXException, IOException,
	OsmTransferException {
		jc.junctionSearch(new ArrayList<Channel>(subset), getProgressMonitor());
	}

}
