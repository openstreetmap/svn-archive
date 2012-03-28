// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.Visitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReadPostprocessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ReadPostprocessor implements OsmServerReadPostprocessor {
	
	private ArrayList<Long> nodeList;
	private ArrayList<Long> wayList;
	private ArrayList<Long> relationList;
	
	private SeparateDataStorePlugin plugin;

	public ReadPostprocessor(SeparateDataStorePlugin plugin) {
		this.plugin = plugin;
	}
	
    @Override
    public void postprocessDataSet(DataSet ds, ProgressMonitor progress) {
        
		nodeList = new ArrayList<Long>();
		wayList = new ArrayList<Long>();
		relationList = new ArrayList<Long>();

		Visitor adder = new Visitor() {
			@Override
			public void visit(Node n) {
				nodeList.add(n.getId());
				plugin.originalNodes.put(n.getId(), n.save());
			}
			@Override
			public void visit(Way w) {
				wayList.add(w.getId());
				plugin.originalWays.put(w.getId(), w.save());
			}
			@Override
			public void visit(Relation e) {
				relationList.add(e.getId());
				plugin.originalNodes.put(e.getId(), e.save());
			}
			@Override
			public void visit(Changeset cs) {}
		};
		
		for (OsmPrimitive p : ds.allPrimitives()) {
			p.visit(adder);
		}
			
		SdsApi api = SdsApi.getSdsApi();
		String rv = "";
		try {
			rv = api.requestShadowsFromSds(nodeList, wayList, relationList, progress);
		} catch (SdsTransferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// this is slightly inefficient, as we're re-making the string into 
		// an input stream when there was an input stream to be had inside the
		// SdsApi already, but this encapsulates things better.
        InputStream xmlStream;
		try {
			xmlStream = new ByteArrayInputStream(rv.getBytes("UTF-8"));
	        InputSource inputSource = new InputSource(xmlStream);
			SAXParserFactory.newInstance().newSAXParser().parse(inputSource, new SdsParser(ds, plugin));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

}
