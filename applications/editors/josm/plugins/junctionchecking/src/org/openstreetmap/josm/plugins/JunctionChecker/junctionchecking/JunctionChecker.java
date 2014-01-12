package org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking;

import java.util.ArrayList;
import java.util.HashSet;

import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;

/**
 * JunctionChecker startet das Überprüfen eines Subgraphen auf eine existierende
 * Kreuzung
 *
 * @author joerg
 *
 */
public class JunctionChecker {

	private ArrayList<Channel> subgraph;
	private ArrayList<Channel> entries;
	private ArrayList<Channel> exits;
	private ArrayList<Channel> cycleedges;
	private ArrayList<Channel> subjunction;
	private int n;
	private final JPrepare jPrepare;
	private JProcess jProcess;
	private final ChannelDiGraph channeldigraph;
	private final JCheck jCheck;
	private ArrayList<Channel> E;
	private int Grid[][];
	private boolean Check;
	private boolean smallerJunction;
	private JMinimality m;
	// Variable wird beim KreuzungsSuchen benutzt, sonst ist sie leer!
	private ArrayList<HashSet<Channel>> junctions = new ArrayList<HashSet<Channel>>();
	//dient zur Zeitmessung
	private long startIterate = 0;
	private long stopIterate = 0;
	private long startGenerate = 0;

	public JunctionChecker(ChannelDiGraph channeldigraph, int n) {
		this.jPrepare = new JPrepare(channeldigraph);
		this.channeldigraph = channeldigraph;
		this.n = n;
		this.jCheck = new JCheck();
		this.subjunction = new ArrayList<Channel>();
		smallerJunction = false;
	}

	/**
	 * startet das Überprüfen einer Teilmenge auf die Kreuzungskriterien
	 * @param subgraph
	 * @param pm
	 */
	public void checkjunctions(ArrayList<Channel> subgraph, ProgressMonitor pm) {
		jPrepare.jPrepare(subgraph);
		entries = jPrepare.getEntries();
		exits = jPrepare.getExits();
		jProcess = new JProcess(subgraph, channeldigraph);
		jProcess.jProcess(jPrepare.getEntries());
		boolean result = jCheck.jCheck(entries, exits, n);
		//jPrepare.resetSubgraph();
		if (result == true) {
			this.collectECandidates(subgraph);
			this.ConstructGrid();
			m = new JMinimality(Grid, n, E, entries, exits, channeldigraph, pm,
					true);
			m.GenerateSubcolumns();
			Check = m.IterateThroughKn();
			if (!Check) {
				smallerJunction = true;
			}
			subjunction = m.getSubJunctionCandidate();
		} else {
			Check = false;
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn nach Kreuzungen in einer Teilmenge
	 * gesucht werden soll
	 *
	 * @param subgraph
	 * @param firstjunction soll nur die erste mögliche Kreuzung ausgegeben werden oder alle
	 */
	public void junctionSearch(ArrayList<Channel> subgraph, ProgressMonitor pm) {
		jPrepare.jPrepare(subgraph);
		entries = jPrepare.getEntries();
		exits = jPrepare.getExits();
		jProcess = new JProcess(subgraph, channeldigraph);
		jProcess.jProcess(jPrepare.getEntries());
		this.collectECandidates(subgraph);
		this.ConstructGrid();
		jPrepare.resetSubgraph();
		m = new JMinimality(Grid, n, E, new ArrayList<Channel>(), new ArrayList<Channel>(), channeldigraph, pm, false);
		m.GenerateSubcolumns();
		Check = m.IterateThroughKn();
		junctions = checkJunctionCandidates(m.getJunctionCandidates());
	}

	public void junctionSearch(ArrayList<Channel> subgraph) {
		jPrepare.jPrepare(subgraph);
		entries = jPrepare.getEntries();
		exits = jPrepare.getExits();
		jProcess = new JProcess(subgraph, channeldigraph);
		jProcess.jProcess(jPrepare.getEntries());
		this.collectECandidates(subgraph);
		this.ConstructGrid();
		jPrepare.resetSubgraph();
		m = new JMinimality(Grid, n, E, new ArrayList<Channel>(), new ArrayList<Channel>(), channeldigraph, false);
		startGenerate = System.currentTimeMillis();
		m.GenerateSubcolumns();
		startIterate = System.currentTimeMillis();
		Check = m.IterateThroughKn();
		stopIterate = System.currentTimeMillis();
		junctions = checkJunctionCandidates(m.getJunctionCandidates());
	}

	/**
	 * Überprüft die Kreuzunskandidaten, die JMinimality gefunden hat, welche davon eine Kreuzung darstellen (eine Kreuzung
	 * darf keine weiteren Kreuzungen enthalten)
	 */
	private ArrayList<HashSet<Channel>> checkJunctionCandidates(ArrayList<HashSet<Channel>> junctioncandidates){
		ArrayList<HashSet<Channel>> junctions = (ArrayList<HashSet<Channel>>) junctioncandidates.clone();
		for (int i = 0; i < junctioncandidates.size(); i++) {
			for (int j = 0; j < junctioncandidates.size(); j++) {
				if (junctioncandidates.get(i).containsAll(junctioncandidates.get(j))) {
					junctions.removeAll(junctioncandidates.get(i));
				}
				else {
				}
			}
		}
		return junctions;
	}


	private void collectECandidates(ArrayList<Channel> subgraph) {
		E = new ArrayList<Channel>();
		for (int i = 0; i < subgraph.size(); i++) {
			if ((subgraph.get(i).getIndegree() + subgraph.get(i).getOutdegree() >= 3)
					|| entries.contains(subgraph.get(i))
					|| exits.contains(subgraph.get(i))) {
				E.add(subgraph.get(i));
			}
		}
	}

	private void ConstructGrid() {
		Grid = new int[E.size()][E.size()];
		for (int y = 0; y < E.size(); y++) {
			for (int x = 0; x < E.size(); x++) {
				if (x != y && !(entries.contains(E.get(x)))
						&& !(exits.contains(E.get(y)))
						&& E.get(y).getReachableNodes().contains(E.get(x))) {
					Grid[y][x] = 1;
					//log.trace("Grid-Position auf 1 gesetzT (y/x): " + y + ":"
					//		+ x + "(Entry/exit):" + E.get(y).getNewid() + ":" +
					//		E.get(x).getNewid());
				} else {
					Grid[y][x] = 0;
				}
			}
		}
	}

	public long getMeasuredIterateTime() {
		return (stopIterate - startIterate);
	}

	public long getMeasuredGenerateTime() {
		return (startIterate - startGenerate);
	}

	public ArrayList<Channel> getSubgraph() {
		return subgraph;
	}

	public void setSubgraph(ArrayList<Channel> subgraph) {
		this.subgraph = subgraph;
	}

	public ArrayList<Channel> getEntries() {
		return entries;
	}

	public void setEntries(ArrayList<Channel> entries) {
		this.entries = entries;
	}

	public ArrayList<Channel> getExits() {
		return exits;
	}

	public void setExits(ArrayList<Channel> exits) {
		this.exits = exits;
	}

	public ArrayList<Channel> getCycleedges() {
		return cycleedges;
	}

	public void setCycleedges(ArrayList<Channel> cycleedges) {
		this.cycleedges = cycleedges;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	/**
	 * gibt die kleinere, gefundene Kreuzung zurück (wenn es sie gibt)
	 *
	 * @return
	 */
	public ArrayList<Channel> getSubJunction() {
		return subjunction;
	}

	/**
	 * gibt das Ergebnis des JCheck zurück (bei false keine generelle Aussage
	 * möglich)
	 *
	 * @return false = keine Kreuzung ODER enthält kleinere Kreuzung true =
	 *         Kreuzung
	 *
	 */
	public boolean getCheck() {
		return Check;
	}

	/**
	 * gibt den Wert des JMinimality zurück (wenn er ausgeführt wurde)
	 *
	 * @return true = keine kleinere kreuzung, false = kleinere kreuzung
	 *         enthalten
	 */
	public boolean isSmallerJunction() {
		return smallerJunction;
	}

	/**
	 * das Ergebnis des JCheck als String
	 *
	 * @return
	 */
	public String getJCheckResult() {
		return jCheck.getResult();
	}

	/**
	 * gitb die bei der Kruezungssuche gefundenen Kreuzungen zurück, sonst
	 * nichts
	 *
	 * @return
	 */
	public ArrayList<HashSet<Channel>> getJunctions() {
		return junctions;
	}
}
