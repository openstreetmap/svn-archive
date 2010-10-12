package org.openstreetmap.josm.plugins.turnrestrictions;
import groovy.util.GroovyTestCase;

import static org.junit.Assert.*;
import org.junit.*;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.plugins.turnrestrictions.fixtures.JOSMFixture;
import org.openstreetmap.josm.data.coor.LatLon;

class TurnRestrictionBuilderTest{
	
	def TurnRestrictionBuilder builder;

	def boolean hasExactlyOneMemberWithRole(Relation r, String role ){
		return r.getMembers().find {RelationMember rm -> rm.getRole() == role} != null
	}
	
	def memberWithRole(Relation r, String role) {
		def RelationMember rm = r.getMembers().find {RelationMember rm -> rm.getRole() == role}
		return rm.getMember()
	}
	
	def void assertEmptyTurnRestriction(Relation r){
		assert r != null
		assert r.get("type") == "restriction"
		assert r.getMembersCount() == 0
	}
	
	@Before
	public void setUp() {
		JOSMFixture.createUnitTestFixture().init()
		builder = new TurnRestrictionBuilder() {
			def double testPhi(Way w){
				return super.phi(w)
			}
			
			def double testPhi(Way w, boolean doRevert){
				return super.phi(w,doRevert)
			}
		}
	}

	/**
	 * Selection consist of one way and the start node of the way ->
	 * propose a No-U-Turn restriction
	 * 		
	 */
	@Test
	public void noUTurn_1() {
		Way w = new Way(1)
		Node n1 = new Node(1)
		Node n2 = new Node(2)
		w.setNodes([n1,n2])
		
		def sel = [w,n1]
		TurnRestrictionBuilder builder = new TurnRestrictionBuilder()
		Relation r = builder.build(sel)  
		
		assert r != null
		assert r.getMembersCount() == 3
		assert hasExactlyOneMemberWithRole(r, "from")
		assert hasExactlyOneMemberWithRole(r, "to")
		assert hasExactlyOneMemberWithRole(r, "via")
		assert memberWithRole(r, "from") == w
		assert memberWithRole(r, "to") == w
		assert memberWithRole(r, "via") == n1
		assert r.get("restriction") == "no_u_turn"			
	}
	
	
	/**
	* Selection consist of one way and the end node of the way ->
	* propose a No-U-Turn restriction
	*
	*/
   @Test
   public void noUTurn_2() {
	   Way w = new Way(1)
	   Node n1 = new Node(1)
	   Node n2 = new Node(2)
	   w.setNodes([n1,n2])
	   
	   def sel = [w,n2]
	   TurnRestrictionBuilder builder = new TurnRestrictionBuilder()
	   Relation r = builder.build(sel)
	   
	   assert r != null
	   assert r.getMembersCount() == 3
	   assert hasExactlyOneMemberWithRole(r, "from")
	   assert hasExactlyOneMemberWithRole(r, "to")
	   assert hasExactlyOneMemberWithRole(r, "via")
	   assert memberWithRole(r, "from") == w
	   assert memberWithRole(r, "to") == w
	   assert memberWithRole(r, "via") == n2
	   assert r.get("restriction") == "no_u_turn"
   }
   
   @Test
   public void nullSelection() {
	   def tr = builder.build(null)
	   assertEmptyTurnRestriction(tr)
   }
   
   @Test
   public void emptySelection() {
	   def tr = builder.build([])
	   assertEmptyTurnRestriction(tr)
   }
   
   /**
    * One selected way -> build a turn restriction with a "from" leg
    * only
    */
   @Test
   public void oneSelectedWay() {
	   Way w = new Way(1)
	   Relation tr = builder.build([w])
	   assert tr != null
	   assert tr.get("type") == "restriction"
	   assert tr.getMembersCount() == 1
	   assert memberWithRole(tr, "from") == w
   }   
   
   /**
    * Two unconnected ways in the selection. The first one becomes the from leg,
    * the second one the two leg.
    */
   @Test
   public void twoUnconnectedWays() {
	   Way w1 = new Way(1)
	   w1.setNodes([new Node(11), new Node(12)])
	   Way w2 = new Way(2)
	   w2.setNodes([new Node(21), new Node(22)])
	   
	   Relation tr = builder.build([w1,w2])
	   assert tr != null
	   assert tr.get("type") == "restriction"
	   assert ! tr.hasKey("restriction")
	   assert tr.getMembersCount() == 2
	   assert memberWithRole(tr, "from") == w1
	   assert memberWithRole(tr, "to") == w2
   }
   
   /**
    * Two connected ways. end node of the first way connects to start node of 
    * the second way. 
    *       w2 
    *    -------->
    *    ^ 
    *    | w1
    *    |
    */
   @Test
   public void twoConnectedWays_1() {
	   Node n1 = new Node(1)
	   n1.setCoor(new LatLon(1,1))
	   Node n2 = new Node(2)
	   n2.setCoor(new LatLon(2,1))
	   Node n3 = new Node(3)
	   n3.setCoor(new LatLon(2,2))
	   
	   Way w1 = new Way(1)
	   w1.setNodes([n1,n2])
	   Way w2 = new Way(2)
	   w2.setNodes([n2,n3])

	   assert builder.testPhi(w1) == Math.toRadians(90)	   
	   assert builder.testPhi(w2) == Math.toRadians(0)
	   	   
	   Relation tr = builder.build([w1,w2,n2])
	   
	   assert tr != null
	   assert tr.get("type") == "restriction"
	   assert tr.getMembersCount() == 3
	   assert memberWithRole(tr, "from") == w1
	   assert memberWithRole(tr, "to") == w2
	   assert memberWithRole(tr, "via") == n2
	   
	   assert tr.get("restriction") == "no_right_turn"
	   	   
	   /*
	    * opposite order, from w2 to w1. In this case we have left turn.
	    */
	   
	   tr = builder.build([w2,w1,n2])
	   
	   assert tr != null
	   assert tr.get("type") == "restriction"
	   assert tr.getMembersCount() == 3
	   assert memberWithRole(tr, "from") == w2
	   assert memberWithRole(tr, "to") == w1
	   assert memberWithRole(tr, "via") == n2
	   
	   assert tr.get("restriction") == "no_left_turn"
   }
   
   /**
	* Two connected ways. end node of the first way connects to end node of
	* the second way. left turn.
	* 
	*                   w2
	*           (7,2) -------> (7,5)
	*                            ^
	*                            | w1
	*                            |  
	*                          (5,5)
	*/
   @Test
   public void twoConnectedWays_2() {
	   Node n1 = new Node(1)
	   n1.setCoor(new LatLon(5,5))
	   Node n2 = new Node(2)
	   n2.setCoor(new LatLon(7,5))
	   Node n3 = new Node(3)
	   n3.setCoor(new LatLon(7,2))
	   
	   Way w1 = new Way(1)
	   w1.setNodes([n1,n2])
	   Way w2 = new Way(2)
	   w2.setNodes([n3,n2])
	   
	   assert builder.testPhi(w1) == Math.toRadians(90)
	   assert builder.testPhi(w2) == Math.toRadians(0)
	   assert builder.testPhi(w2,true) == Math.toRadians(180)
	   
	   Relation tr = builder.build([w1,w2,n2])
	   
	   assert tr != null
	   assert tr.get("type") == "restriction"
	   assert tr.getMembersCount() == 3
	   assert memberWithRole(tr, "from") == w1
	   assert memberWithRole(tr, "to") == w2
	   assert memberWithRole(tr, "via") == n2
	   
	   assert tr.get("restriction") == "no_left_turn"
	   
	   
	   /*
	    * opposite order, from w2 to w1. In this case we have right turn.
	    */
	   tr = builder.build([w2,w1,n2])
	   
	   assert tr != null
	   assert tr.get("type") == "restriction"
	   assert tr.getMembersCount() == 3
	   assert memberWithRole(tr, "from") == w2
	   assert memberWithRole(tr, "to") == w1
	   assert memberWithRole(tr, "via") == n2	   
	   assert tr.get("restriction") == "no_right_turn"
   }
   
   /**
   * Two connected ways. end node of the first way connects to end node of
   * the second way. left turn.
   *
   *                 
   *           (7,5) -
   *             ^     -    w2
   *             | w1     ------> (6,7)
   *             |
   *           (5,5)
   */
  @Test
  public void twoConnectedWays_3() {
	  Node n1 = new Node(1)
	  n1.setCoor(new LatLon(5,5))
	  Node n2 = new Node(2)
	  n2.setCoor(new LatLon(7,5))
	  Node n3 = new Node(3)
	  n3.setCoor(new LatLon(6,7))
	  
	  Way w1 = new Way(1)
	  w1.setNodes([n1,n2])
	  Way w2 = new Way(2)
	  w2.setNodes([n2,n3])
	  	  	  
	  Relation tr = builder.build([w1,w2,n2])
	  
	  assert tr != null
	  assert tr.get("type") == "restriction"
	  assert tr.getMembersCount() == 3
	  assert memberWithRole(tr, "from") == w1
	  assert memberWithRole(tr, "to") == w2
	  assert memberWithRole(tr, "via") == n2
	  
	  assert tr.get("restriction") == "no_right_turn"	 
  }
  
  
  /**
  * Two connected ways. end node of the first way connects to end node of
  * the second way. left turn.
  *
  *           
  *           (10,10)
  *                 \                   
  *                  \            
  *                   \
  *                    v           
  *                     (8,15)
  *                    /                     
  *                   /
  *                  /
  *                 v
  *            (5,11)
  */
 @Test
 public void twoConnectedWays_4() {
	 Node n1 = new Node(1)
	 n1.setCoor(new LatLon(10,10))
	 Node n2 = new Node(2)
	 n2.setCoor(new LatLon(8,15))
	 Node n3 = new Node(3)
	 n3.setCoor(new LatLon(5,11))
	 
	 Way w1 = new Way(1)
	 w1.setNodes([n1,n2])
	 Way w2 = new Way(2)
	 w2.setNodes([n2,n3])

	 Relation tr = builder.build([w1,w2,n2])
	 
	 assert tr != null
	 assert tr.get("type") == "restriction"
	 assert tr.getMembersCount() == 3
	 assert memberWithRole(tr, "from") == w1
	 assert memberWithRole(tr, "to") == w2
	 assert memberWithRole(tr, "via") == n2
	 
	 assert tr.get("restriction") == "no_right_turn"

	 
	 /*
	  * opposite order, from w2 to w1. In  this case we have left turn.
	  */
	 tr = builder.build([w2,w1,n2])
	 
	 assert tr != null
	 assert tr.get("type") == "restriction"
	 assert tr.getMembersCount() == 3
	 assert memberWithRole(tr, "from") == w2
	 assert memberWithRole(tr, "to") == w1
	 assert memberWithRole(tr, "via") == n2
	 
	 assert tr.get("restriction") == "no_left_turn"
	}
}
