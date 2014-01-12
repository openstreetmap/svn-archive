package org.openstreetmap.josm.plugins.JunctionChecker.filter;

import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMWay;
import org.openstreetmap.josm.plugins.JunctionChecker.reader.XMLFilterReader;

/**
 * @author  joerg
 */
public class ExecuteFilter {
	
	private Filter[] filter;
	private XMLFilterReader xmlfilterreader;
	private OSMGraph incominggraph;
	private OSMGraph outgoinggraph;
	
	public ExecuteFilter(Filter[] filter, OSMGraph incoming){
		this.filter = filter;
		this.incominggraph = incoming;
		outgoinggraph = new OSMGraph();
		outgoinggraph.setBbbottom(incoming.getBbbottom());
		outgoinggraph.setBbleft(incoming.getBbleft());
		outgoinggraph.setBbright(incoming.getBbright());
		outgoinggraph.setBbtop(incoming.getBbtop());
		outgoinggraph.setRelations(incoming.getRelationsAshashmap());
	}
	
	public ExecuteFilter() {
	}
	
	public void filter(){
		OSMWay[] tempWays = incominggraph.getWays();
		String key;
		//alle Einträge im Filter durchgehen
		for (int i = 0; i < filter.length; i++) {
			//alle Ways durchgehen
			for (int j = 0; j < tempWays.length; j++) {
				key = filter[i].getKeyValue();
				//prüfen, ob Way Key-Wert des Filters enthält
				if (tempWays[j].hasKey(key)) {
					//prüfen, ob Way auch einen Value-Wert des Filtereintrags enthält
					if (filter[i].hasTagValue(tempWays[j].getValue(key))) {
						//Way outgoinggraph hinzufügen
						outgoinggraph.addWay(tempWays[j]);
						for (int j2 = 0; j2 < tempWays[j].getNodes().length; j2++) {
							//zum way gehörende Nodes einfügen, aber nur, wenn diese
							//vorher noch nicht im outgoinggraph vorhanden sind
							if (!outgoinggraph.hasNode(tempWays[j].getNodes()[j2].getId())) {
								outgoinggraph.addNode(tempWays[j].getNodes()[j2]);
							}
							
						}
					}
				}
			}
		}
	}
	
	public Filter[] getFilter() {
		return filter;
	}

	public void setFilter(Filter[] filter) {
		this.filter = filter;
	}

	public XMLFilterReader getXmlfilterreader() {
		return xmlfilterreader;
	}

	public void setXmlfilterreader(XMLFilterReader xmlfilterreader) {
		this.xmlfilterreader = xmlfilterreader;
	}

	public OSMGraph getIncominggraph() {
		return incominggraph;
	}

	public void setIncominggraph(OSMGraph incominggraph) {
		this.incominggraph = incominggraph;
	}

	public OSMGraph getOutgoinggraph() {
		return outgoinggraph;
	}

	public void setOutgoinggraph(OSMGraph outgoinggraph) {
		this.outgoinggraph = outgoinggraph;
	}
}