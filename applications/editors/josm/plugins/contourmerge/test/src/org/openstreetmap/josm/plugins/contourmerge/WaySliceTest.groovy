package org.openstreetmap.josm.plugins.contourmerge;

import java.util.List;

import static org.junit.Assert.*;
import org.junit.*;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.contourmerge.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.contourmerge.WaySlice;

class WaySliceTest {
	def shouldFail = new GroovyTestCase().&shouldFail
	
	def newNode(id){
		return new Node(id)
	}
	
	def newWay(id, Node... nodes){
		Way w = new Way(id,1)
		w.setNodes(Arrays.asList(nodes))
		return w
	}
	
	def newWay(id, List<Node> nodes){
		Way w = new Way(id,1)
		w.setNodes(nodes)
		return w
	}
	
	@BeforeClass
	static public void startJOSMFixtures() {
		JOSMFixture.createUnitTestFixture().init()
	}
	
	@Test
	public void constructor_inDirection(){
		def w = newWay(1, newNode(1), newNode(2), newNode(4), newNode(5))
		
		WaySlice ws 
		ws = new WaySlice(w, 0, 1)		
		assert ws.getWay() == w
		assert ws.getStart() == 0
		assert ws.getEnd() == 1
		
		ws = new WaySlice(w, 1, 3)
		assert ws.getWay() == w
		assert ws.getStart() == 1
		assert ws.getEnd() == 3
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(null, 1, 3) // way must not be null
		}
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, -1, 3)  // start index >= 0 required 
		}
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 4, 3)   // start index < num nodes required 
		}
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 0, -1)  // end index >= 0 required
		}

		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 0, 4)   // end index < num nodes required 
		}

		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 3, 2)  // start < end required 
		}
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 3, 3)  // start < end required
		}
	}
	
	@Test
	public void constructor_inOppositeDirection(){
		def n = newNode(1)
		// this is a closed way
		def w = newWay(1, n, newNode(2), newNode(4), newNode(5), n)
		
		WaySlice ws
		ws = new WaySlice(w, 0, 1, true)
		assert ws.getWay() == w
		assert ws.getStart() == 0
		assert ws.getEnd() == 1
		assert ws.isInDirection()
		
		ws = new WaySlice(w, 0, 1, false)
		assert ws.getWay() == w
		assert ws.getStart() == 0
		assert ws.getEnd() == 1
		assert ! ws.isInDirection()
		
		ws = new WaySlice(w, 1, 3, false)
		assert ws.getWay() == w
		assert ws.getStart() == 1
		assert ws.getEnd() == 3
		assert ! ws.isInDirection()
		
	
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(null, 1, 3,false) // way must not be null
		}
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, -1, 3,false)  // start index >= 0 required
		}
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 4, 3, false)   // start index < num nodes required
		}
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 0, -1, false)  // end index >= 0 required
		}

		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 0, 4, false)   // end index < num nodes required
		}

		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 3, 2, false)  // start < end required
		}
		
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 3, 3, false)  // start < end required
		}
		
		w = newWay(2, n, newNode(2), newNode(3), newNode(4))
		shouldFail(IllegalArgumentException){
			ws = new WaySlice(w, 0,1, false)  // way slice in opposite direction not allowed
			                                  // for an open way
		}
	}
	
	@Test
	public void getStartTearOffIndex() {
		def w = newWay(1, newNode(1), newNode(2), newNode(3), newNode(4), newNode(5)) // an open way
		
		WaySlice ws
		ws = new WaySlice(w, 2,3)
		assert ws.getStartTearOffIdx() == 1
		
		ws = new WaySlice(w, 0, 3)
		assert ws.getStartTearOffIdx() == -1 // no tear off node available
		
		def n = newNode(1)
		w = newWay(1, n, newNode(2), newNode(3), newNode(4), newNode(5),n) // a closed way
		
		/*
		*+------------------------------------------------+
	   * |                                                |
	   * n1----------n2---------n3-----------n4-----------n5
	   *                        x *********** x              ~ slice
	   *             ^                                       ~ expected lower tear off index                        
	   */
		ws = new WaySlice(w, 2, 3)
		assert ws.getStartTearOffIdx() == 1

		/*
		*+------------------------------------------------+
	   * |                                                |
	   * n1----------n2---------n3-----------n4-----------n5
	   * x *********** x                                       ~ slice
	   *                                                  ^    ~ expected lower tear off index
	   */
		ws = new WaySlice(w, 0, 1)
		assert ws.getStartTearOffIdx() == 4

		/*
		*+------------------------------------------------+
	   * |                                                |
	   * n1----------n2---------n3-----------n4-----------n5
	   * x ********************************************** x    ~ slice
	   *                                                       ~ expected lower tear off index
	   */
		ws = new WaySlice(w, 0, 4)
		assert ws.getStartTearOffIdx() == -1
		
		
		/*
		*    +------------------------------------------------+
	   *     |                                                |
	   *     n1----------n2---------n3-----------n4-----------n5
	   * ****x                                                x***   ~ slice
	   *                                          ^                  ~ expected upper tear off index
	   */
		ws = new WaySlice(w, 0, 4, false)
		assert ws.getStartTearOffIdx() == 3
	}
	
	
	@Test
	public void getEndTearOffIndex() {
		def w = newWay(1, newNode(1), newNode(2), newNode(3),newNode(4), newNode(5)) // an open way
		
		WaySlice ws
		ws = new WaySlice(w, 1,2)
		assert ws.getEndTearOffIdx() == 3
		
		ws = new WaySlice(w, 0, 4)
		assert ws.getEndTearOffIdx() == -1 // no tear off node available
		
		def n = newNode(1)
		w = newWay(1, n, newNode(2), newNode(3), newNode(4), newNode(5),n) // a closed way
		
		/*
		*+------------------------------------------------+
	   * |                                                |
	   * n1----------n2---------n3-----------n4-----------n5
	   *                        x *********** x              ~ slice
	   *             ^                                       ~ expected upper tear off index
	   */
		ws = new WaySlice(w, 2, 3)
		assert ws.getEndTearOffIdx() == 4
		
		
		/*
		*+------------------------------------------------+
	   * |                                                |
	   * n1----------n2---------n3-----------n4-----------n5
	   *                                     x *********** x  ~ slice
	   * ^                                                    ~ expected upper tear off index
	   */
		ws = new WaySlice(w, 3, 4)
		assert ws.getEndTearOffIdx() == 0

		/*
		*+------------------------------------------------+
	   * |                                                |
	   * n1----------n2---------n3-----------n4-----------n5
	   * x *********** x                                       ~ slice
	   *                        ^                              ~ expected upper tear off index
	   */
		ws = new WaySlice(w, 0, 1)
		assert ws.getEndTearOffIdx() == 2

		/*
		*+------------------------------------------------+
	   * |                                                |
	   * n1----------n2---------n3-----------n4-----------n5
	   * x ********************************************** x    ~ slice
	   *                                                       ~ expected upper tear off index
	   */
		ws = new WaySlice(w, 0, 4)
		assert ws.getEndTearOffIdx() == -1
		
		
		/*
		*    +------------------------------------------------+
	   *     |                                                |
	   *     n1----------n2---------n3-----------n4-----------n5
	   * ****x                                                x***   ~ slice
	   *                  ^                                          ~ expected upper tear off index
	   */
		ws = new WaySlice(w, 0, 4, false)
		assert ws.getEndTearOffIdx() == 1
	}
	
	@Test
	public void getNumSegments() {
		def w = newWay(1, newNode(1), newNode(2), newNode(3), newNode(4), newNode(5)) // an open way
		
		def ws = new WaySlice(w, 0, 3)
		assert ws.getNumSegments() == 3
		
		ws = new WaySlice(w, 1, 2)
		assert ws.getNumSegments() == 1
		
		Node n = newNode(1)
		w = newWay(1, n, newNode(2), newNode(3), newNode(4), newNode(5), n) // a closed way

		ws = new WaySlice(w, 0, 3)
		assert ws.getNumSegments() == 3
		ws = ws.getOpositeSlice()
		assert ws.getNumSegments() == 2
		
		ws = new WaySlice(w, 1, 2)
		assert ws.getNumSegments() == 1
		ws = ws.getOpositeSlice()
		assert ws.getNumSegments() == 4
	}
	
	
	def  n(i){
		return new Node(i)
	}
	
	@Test
	public void replaceNodes_OpenWay() {		
		def w = newWay(1, newNode(1), newNode(2), newNode(3), newNode(4), newNode(5)) // an open way
		def newnodes = [newNode(10), newNode(11), newNode(12)]
		
		def ws = new WaySlice(w, 1, 2)
		Way wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(1), n(10), n(11), n(12), n(4), n(5)]
		
		ws = new WaySlice(w, 0, 2)
		wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(10), n(11), n(12), n(4), n(5)]
		
		ws = new WaySlice(w, 3, 4)
		wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(1),n(2), n(3), n(10), n(11), n(12)]

		ws = new WaySlice(w, 0, 4)
		wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(10), n(11), n(12)]
	}
	
	@Test
	public void replaceNodes_ClosedWay_InDirection() {
		Node n1 = new Node(1)
		def w = newWay(1, n1, newNode(2), newNode(3), newNode(4), newNode(5), n1) // a closed way
		def newnodes = [newNode(10), newNode(11), newNode(12)]
		
		def ws = new WaySlice(w, 1, 2)
		Way wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n1, n(10), n(11), n(12), n(4), n(5), n1]
		
		ws = new WaySlice(w, 0, 2)
		wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(10), n(11), n(12), n(4), n(5), n(10)]
		
		ws = new WaySlice(w, 3, 4)
		wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(1),n(2), n(3), n(10), n(11), n(12), n(1)]

		ws = new WaySlice(w, 0, 4)
		wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(10), n(11), n(12), n(10)]
	}
	
	@Test
	public void replaceNodes_ClosedWay_ReverseDirection() {
		Node n1 = new Node(1)
		def w = newWay(1, n1, newNode(2), newNode(3), newNode(4), newNode(5), n1) // a closed way
		def newnodes = [newNode(10), newNode(11), newNode(12)]
		
		def ws = new WaySlice(w, 1, 2, false)
		Way wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(10), n(11), n(12), n(10)]
		
		ws = new WaySlice(w, 0, 2, false)
		wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(10), n(11), n(12), n(2), n(10)]
		
		ws = new WaySlice(w, 0, 4, false)
		wn = ws.replaceNodes(newnodes)
		assert wn.getNodes() == [n(10), n(11), n(12), n(2), n(3), n(4), n(10)]
	}
	
	
}
