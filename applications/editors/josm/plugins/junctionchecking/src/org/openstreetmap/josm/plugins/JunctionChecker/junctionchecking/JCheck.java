package org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking;

import java.util.ArrayList;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;

/**
 * @author  joerg
 */
public class JCheck {

	private int exnr;
	private String result = "";

	public boolean jCheck(ArrayList<Channel> entries, ArrayList<Channel> exits,
			int n) {
		for (int i = 0; i < exits.size(); i++) {
			exits.get(i).setEnnrZero();
		}
		if (!(entries.size() == exits.size() && exits.size() == n)) {
			result="Rule 1 broken: " + entries.size() + " entries but "
					+ exits.size() + " exits and n=" + n;
			return false;
		}
		for (int i = 0; i < entries.size(); i++) {
			if (!(entries.get(i).getIndegree() + entries.get(i).getOutdegree() >= 2)) {
				result="rule 4 broken: indegree from entrynode with ID: "
						+ entries.get(i).getNewid() + ": "
						+ entries.get(i).getIndegree() + " OutDegree: "
						+ entries.get(i).getOutdegree();
				return false;
			}
			exnr = 0;
			for (int j = 0; j < exits.size(); j++) {
				if (!(exits.get(j).getIndegree() + exits.get(j).getOutdegree() >= 2)) {
					result="Rule 4 broken, indegree from exitnode with ID: "
							+ exits.get(j).getNewid() + ": "
							+ exits.get(j).getIndegree() + " and outdegree: "
							+ exits.get(j).getOutdegree();
					//log.debug(exits.get(j).toString());
					return false;
				}
				if (entries.get(i).getReachableNodes().contains(exits.get(j))) {
					exnr++;
					exits.get(j).increaseEnnr();
				}
				if (exits.get(j).equals(entries.get(i))) {
					result="Rule 2 broken: node with ID: "
							+ "entries.get(i).getNode().getId()"
							+ "is both entry and exit node";
					return false;
				}
			}

		}
		if (!(exnr >= n - 1)) {
			result="Rule 1 broken";
			return false;
		}
		for (int i = 0; i < exits.size(); i++) {
			if (!(exits.get(i).getEnnr() >= (n - 1))) {
				result="Rule 1 broken, exit node with ID "
						+ exits.get(i).getNewid() + "can only reached from "
						+ exits.get(i).getEnnr() + " entries.";
				return false;
			}
		}
		result = "Jcheck erfolgreich bestanden";
		return true;
	}
	
	/**
	 * gibt das Ergebnis des JChecks in Form als Satz mit Informationen zurück
	 * @return
	 */
	public String getResult() {
		return result;
	}
}
