package org.openstreetmap.josm.plugins.validator.tests;
import java.util.Collections;

import org.junit.Test;
import org.junit.experimental.theories.PotentialAssignment;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.coor.LatLon

import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.plugins.validator.tests.DuplicateNode;
import org.openstreetmap.josm.plugins.validator.TestError;

class DuplicateNodeTest {
	
	@Test
	public void emptyNodeCollection() {
		DuplicateNode t  = new DuplicateNode()
		
		t.startTest NullProgressMonitor.INSTANCE
		t.visit Collections.emptyList()
		t.endTest()
		
		assert t.@errors.isEmpty()
	}
	
	@Test
	public void twoDuplicateNodes() {
		DuplicateNode t  = new DuplicateNode()
		
		def n1 = new Node(new LatLon(1.0,1.0))
		def n2 = new Node(new LatLon(1.0, 1.0))
		
		def nodes = [n1,n2]
		
		t.startTest NullProgressMonitor.INSTANCE
		t.visit nodes
		assert t.@potentialDuplicates.size() == 1
		t.endTest()
		
		assert t.@errors.size() == 1
		TestError te = t.@errors.get(0)
		assert te.getPrimitives().contains(n1)
		assert te.getPrimitives().contains(n2)		
	}
	
	@Test
	public void twoDuplicateNodes_ReducedPrecision() {
		DuplicateNode t  = new DuplicateNode()
		
		def n1 = new Node(new LatLon(1.0,1.0))
		def n2 = new Node(new LatLon(1.00000001, 1.00000001))
		
		def nodes = [n1,n2]
		
		t.startTest NullProgressMonitor.INSTANCE
		t.visit nodes
		t.endTest()
		
		assert t.@errors.size() == 1
		TestError te = t.@errors.get(0)
		assert te.getPrimitives().contains(n1)
		assert te.getPrimitives().contains(n2)		
	}
	
	@Test
	public void twoDuplicateNodes_DifferentTagSets() {
		DuplicateNode t  = new DuplicateNode()
		
		def n1 = new Node(new LatLon(1.0,1.0))
		n1.put "aaaa", "aaaa"
		
		def n2 = new Node(new LatLon(1.0, 1.0))
		n2.put "bbbb", "bbbb"
		
		def nodes = [n1,n2]
		
		t.startTest NullProgressMonitor.INSTANCE
		t.visit nodes
		t.endTest()
		
		assert t.@errors.size() == 1
		assert t.@errors.get(0).getMessage() == "Nodes at same position"
	}
	
	@Test
	public void fourDuplicateNodes_TwoDifferentTagSets() {
		DuplicateNode t  = new DuplicateNode()
		
		def n1 = new Node(new LatLon(1.0,1.0))
		n1.put "aaaa", "aaaa"
		
		def n2 = new Node(new LatLon(1.0, 1.0))
		n2.put "bbbb", "bbbb"
		
		def n3 = new Node(new LatLon(1.0,1.0))
		n3.put "aaaa", "aaaa"
		
		def n4 = new Node(new LatLon(1.0, 1.0))
		n4.put "bbbb", "bbbb"
		
		def nodes = [n1,n2,n3,n4]
		
		t.startTest NullProgressMonitor.INSTANCE
		t.visit nodes
		t.endTest()
		
		assert t.@errors.size() == 2
		assert t.@errors.get(0).getMessage() == "Duplicated nodes"
		assert t.@errors.get(0).getPrimitives().size() == 2
		assert t.@errors.get(1).getMessage() == "Duplicated nodes"
		assert t.@errors.get(1).getPrimitives().size() == 2		
	}
	
	@Test
	public void SixDuplicateNodes_FourDifferentTagSets() {
		DuplicateNode t  = new DuplicateNode()
		
		def n1 = new Node(new LatLon(1.0,1.0))
		n1.put "aaaa", "aaaa"
		
		def n2 = new Node(new LatLon(1.0, 1.0))
		n2.put "bbbb", "bbbb"
		
		def n3 = new Node(new LatLon(1.0,1.0))
		n3.put "aaaa", "aaaa"
		
		def n4 = new Node(new LatLon(1.0, 1.0))
		n4.put "bbbb", "bbbb"
		
		def n5 = new Node(new LatLon(1.0, 1.0))
		n5.put "55555", "5555"
		
		def n6 = new Node(new LatLon(1.0, 1.0))
		n6.put "6666", "6666"
		
		def nodes = [n1,n2,n3,n4,n5,n6]
		
		t.startTest NullProgressMonitor.INSTANCE
		t.visit nodes
		t.endTest()
		
		assert t.@errors.size() == 3
		assert t.@errors.get(0).getMessage() == "Duplicated nodes"
		assert t.@errors.get(0).getPrimitives().size() == 2
		assert t.@errors.get(1).getMessage() == "Duplicated nodes"
		assert t.@errors.get(1).getPrimitives().size() == 2
		assert t.@errors.get(2).getMessage() == "Nodes at same position"
		assert t.@errors.get(2).getPrimitives().size() == 2		
	}
}
