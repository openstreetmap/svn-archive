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
 * Diese Klasse wird aufgerufen, wenn eine Teilmenge von Channels auf die Kreuzungskriterien geprüft
 * wird.
 * @author  joerg
 */
public class JunctionCheckTask extends PleaseWaitRunnable{

	private final JunctionChecker jc;
	private final JunctionCheckerPlugin plugin;
	private final int n; //Grad der Kreuzung
	private final HashSet<Channel> subset; //Teilmge der zu prüfenden Channel
	private final boolean producerelation;
	private boolean canceled;

	public JunctionCheckTask(JunctionCheckerPlugin plugin, int n, HashSet<Channel> subset, boolean produceRelation) {
		super("JunctionCheck", false);
		this.plugin = plugin;
		this.n = n;
		this.subset = subset;
		this.producerelation = produceRelation;
		jc = new JunctionChecker(plugin.getChannelDigraph() , n);
	}

	@Override
	protected void cancel() {
		canceled = true;
		progressMonitor.cancel();
	}

	@Override
	protected void finish() {
		if (canceled) {
			return;
		}
		progressMonitor.finishTask();
		if (jc.isSmallerJunction() ) {
			showjunction();
			JOptionPane.showMessageDialog(Main.parent, tr ("The marked channels contains a junctioncandidate (white). To test this candidat mark these channel and press the \"Check\" button again."));
		}
		else if (jc.getCheck()) {
			showjunction();
			JOptionPane.showMessageDialog(Main.parent, tr ("The marked channels are a {0}-ways junction", n));
			plugin.getChannelDigraph().ereaseJunctioncandidate();
			for (int i = 0; i < jc.getSubJunction().size(); i++) {
				plugin.getChannelDigraph().addJunctioncandidateChannel(jc.getSubJunction().get(i));
			}
			if (producerelation) {
				this.plugin.getRelationProducer().produceRelation(subset, n);
			}
		}
		else if (!jc.getCheck()) {
			JOptionPane.showMessageDialog(Main.parent, tr ("The marked channels are not a junction:") + jc.getJCheckResult());

		}

	}

	@Override
	protected void realRun() throws SAXException, IOException,
	OsmTransferException {
		jc.checkjunctions(new ArrayList<Channel>(subset), getProgressMonitor());
	}

	public JunctionChecker getJunctionChecker() {
		return jc;
	}

	/**
	 * zeigt den gefundenen Kreuzungskandidatena an
	 */
	private void showjunction() {
		plugin.getChannelDigraph().ereaseJunctioncandidate();
		for (int i = 0; i < jc.getSubJunction().size(); i++) {
			plugin.getChannelDigraph().addJunctioncandidateChannel(jc.getSubJunction().get(i));
		}
		Main.map.mapView.setActiveLayer(plugin.getChannelDigraphLayer());
	}
}
