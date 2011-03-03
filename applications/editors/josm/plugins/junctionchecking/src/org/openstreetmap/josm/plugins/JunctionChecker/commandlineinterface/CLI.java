package org.openstreetmap.josm.plugins.JunctionChecker.commandlineinterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import org.openstreetmap.josm.plugins.JunctionChecker.connectedness.StrongConnectednessCalculator;
import org.openstreetmap.josm.plugins.JunctionChecker.converting.ChannelDigraphBuilder;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.filter.ExecuteFilter;
import org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking.JunctionChecker;
import org.openstreetmap.josm.plugins.JunctionChecker.reader.OSMXMLReader;
import org.openstreetmap.josm.plugins.JunctionChecker.reader.XMLFilterReader;
import org.openstreetmap.josm.plugins.JunctionChecker.writing.OSMXMLWriter;

public class CLI {

	/**
	 * Die Klasse ist zum Erstellen statistischer Tests, oder zur Erzeugung einer Channel-Digraph-XML-Datei
	 * @param args
	 */
	public static void main(String[] args) {

		String inputosm = "";
		String outputosm = "";
		int maxchannelsearch = 0;
		int ticks = 0;
		int n = 0;
		int runs = 0;

		final String WAYFILTERFILE = "/resources/xml/waysfilter.xml";

		if (args.length != 6) {
			System.out.println("Parameter:\n inputosm (osmxml) \n outputchannelosm (outputosmxml) \n maxchannelsearch (wieviele channel sollen max. überprüft werdne) \n ticks (schrittweite) \n n (n-wege-kreuzung) \n durchläufe (wieviele durchläufe pro suchdurchgang)");
			return;
		} else {
			inputosm = args[0];
			outputosm = args[1];
			maxchannelsearch = Integer.parseInt(args[2]);
			ticks = Integer.parseInt(args[3]);
			n = Integer.parseInt(args[4]);
			runs = Integer.parseInt(args[5]);
		}

		// XML datei einlesen
		File file = new File(inputosm);
		OSMXMLReader xmlreader = new OSMXMLReader(file);
		xmlreader.parseXML();

		// Filter mit gewünschten Ways laden
		XMLFilterReader reader = new XMLFilterReader(
				WAYFILTERFILE);
		reader.parseXML();

		// gewünschte Ways filtern
		ExecuteFilter ef = new ExecuteFilter(reader.getFilters(), xmlreader
				.getOSMGraph());
		ef.filter();

		// ChannelDiGraphen erzeugen
		ChannelDigraphBuilder cdgb = new ChannelDigraphBuilder(ef.getOutgoinggraph());
		cdgb.buildChannelDigraph();

		// DiGraph "versiegeln"
		//DiGraphSealer sealer = new DiGraphSealer(cdgb.getDigraph(), cdgb
		//		.getNewid());
		//sealer.sealingGraph();

		StrongConnectednessCalculator scc = new StrongConnectednessCalculator(cdgb.getDigraph());
		scc.calculateSCC();
		//System.out.println(scc.showNotstronglyConnectednessParts());

		if (maxchannelsearch == 0) {
			OSMXMLWriter oxw = new OSMXMLWriter(outputosm, cdgb.getDigraph());
			try {
				oxw.writeXML();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			;

			System.out.println("OSMXML erzeugt, breche an dieser Stelle ab");
			return;
		}

		JunctionChecker jc = new JunctionChecker(cdgb.getDigraph(), n);
		ArrayList<Channel> subset = new ArrayList<Channel>();

		Channel seed = new Channel();
		Channel vorChannel;
		Channel tempChannel;
		boolean isIn = false;
		int jcf;
		long measuredIterateThroughTime = 0;
		long measuredGenerateSubColumnTime = 0;
		long measuredTime = 0;
		long start;

		//Unzusammenhängenden Teilgraph erzeugen
		/*
		for (int i = 6; i < maxchannelsearch; i = i + ticks) {
			//diff = 0;
			start = System.currentTimeMillis();
			jcf = 0;
			//System.out.println("maxchannel: " + i);
			for (int j = 0; j < runs; j++) {
				//System.out.println("run: " +j);
				subset.clear();
				for (int j2 = 0; j2 <= i; j2++) {
					subset.add(cdgb.getDigraph()
							.getChannelAtPosition(
									(int) ((cdgb.getDigraph().getChannels()
											.size()) * Math.random())));
				}
				//System.out.println("jc gestartet");
				start = System.currentTimeMillis();
				jc.junctionSearch(subset);
				measuredTime += (System.currentTimeMillis() - start);
				//System.out.println("jc beendet");
				//diff = diff + (System.currentTimeMillis() - start);
				measuredIterateThroughTime += jc.getMeasuredIterateTime();
				measuredGenerateSubColumnTime += jc.getMeasuredGenerateTime();
			}
			System.out.println("Channels: " + (i) + " Time(Iterate): " + (measuredIterateThroughTime/runs) + " Time(Generate): " + (measuredGenerateSubColumnTime/runs) +" Time(overall): "+ (measuredTime/runs) + " junctionsfound: " + jcf);
		}*/

		//Zusammenhängenden Teilgraph erzeugen

		for (int i = 5; i < maxchannelsearch; i = i + ticks) {
			measuredIterateThroughTime = 0;
			measuredGenerateSubColumnTime = 0;
			measuredTime =0;
			jcf = 0;
			//System.out.println("maxchannel: " + i);
			for (int j = 0; j < runs; j++) {
				//System.out.println("run: " +j);
				subset.clear();
				do {
					seed = cdgb.getDigraph()
					.getChannelAtPosition(
							(int) ((cdgb.getDigraph().getChannels()
									.size()) * Math.random()));
				}
				while(!seed.isStrongConnected());
				subset.add(seed);
				//System.out.println("Seed: " + seed.getNewid());
				vorChannel = seed;
				for (int k = 0; k < i - 1; k++) {
					isIn = false;
					do {
						tempChannel = getNeighbourChannel(vorChannel);
						if (!subset.contains(tempChannel)) {
							subset.add(tempChannel);
							//System.out.println("zugefügt: " + tempChannel.getNewid());
							seed = tempChannel;
							isIn = true;
						}
						else {
							vorChannel = tempChannel;
							isIn = false;
						}
					}while (isIn == false);
				}
				start = System.currentTimeMillis();
				jc.junctionSearch(subset);
				measuredTime += (System.currentTimeMillis() - start);
				//System.out.println("jc beendet");
				measuredIterateThroughTime += jc.getMeasuredIterateTime();
				measuredGenerateSubColumnTime += jc.getMeasuredGenerateTime();
				jcf = jcf + jc.getJunctions().size();
			}
			System.out.println("Channels: " + (i) + " Time(Iterate): " + (measuredIterateThroughTime/runs) + " Time(Generate): " + (measuredGenerateSubColumnTime/runs) +" Time(overall): "+ (measuredTime/runs) + " junctionsfound: " + jcf);
		}
	}

	private static Channel getNeighbourChannel(Channel seedChannel) {
		if (Math.random() < 0.5) {
			if (seedChannel.getPredChannels().size() >=1 ) {
				return seedChannel.getPredChannels().get((int) (seedChannel.getPredChannels().size() * Math.random()));
			}
			else return seedChannel;
		}
		else {
			if (seedChannel.getLeadsTo().size() >=1 ) {
				return seedChannel.getLeadsTo().get((int) (seedChannel.getLeadsTo().size() * Math.random())).getToChannel();
			}
			else return seedChannel;
		}
	}
}
