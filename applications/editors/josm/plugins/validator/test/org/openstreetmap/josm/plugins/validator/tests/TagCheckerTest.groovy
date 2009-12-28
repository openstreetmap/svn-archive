package org.openstreetmap.josm.plugins.validator.tests;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import org.openstreetmap.josm.plugins.validator.tests.TagChecker
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;


class TagCheckerTest {
	
	@BeforeClass
	static public void init() {
		TagChecker.initializeData()
	}
	
	@Test
	public void fixme_lowercase() {
		TagChecker t  = new TagChecker()
		
		def ds = new DataSet()
		def n1 = new Node(new LatLon(1.0,1.0))
		n1.put("fixme", "suvey later")
		ds.addPrimitive n1

		def nodes = [n1]

		t.startTest NullProgressMonitor.INSTANCE
		t.visit nodes
		t.endTest()
		
		assert t.@errors.size() == 1
	}
	
	@Test
	public void fixme_upercase() {
		TagChecker t  = new TagChecker()
		
		def ds = new DataSet()
		def n1 = new Node(new LatLon(1.0,1.0))
		n1.put("FIXME", "suvey later")
		ds.addPrimitive n1
		
		def nodes = [n1]
		
		t.startTest NullProgressMonitor.INSTANCE
		t.visit nodes
		t.endTest()
		
		assert t.@errors.size() == 1
	}
	
}
