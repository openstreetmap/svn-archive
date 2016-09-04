// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openstreetmap.josm.plugins.turnrestrictions.TurnRestrictionBuilder.intersectionAngle;
import static org.openstreetmap.josm.plugins.turnrestrictions.TurnRestrictionBuilder.selectToWayAfterSplit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnrestrictions.TurnRestrictionBuilder.RelativeWayJoinOrientation;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionType;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class TurnRestrictionBuilderTest {

    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    TurnRestrictionBuilder builder = new TurnRestrictionBuilder();

    boolean hasExactlyOneMemberWithRole(Relation r, final String role) {
        return r.getMembers().stream().filter(rm -> role.equals(rm.getRole())).findFirst().isPresent();
    }

    OsmPrimitive memberWithRole(Relation r, final String role) {
        Optional<RelationMember> opt = r.getMembers().stream().filter(rm -> role.equals(rm.getRole())).findFirst();
        if (!opt.isPresent())
            return null;
        return opt.get().getMember();
    }

    static void assertEmptyTurnRestriction(Relation r) {
        assertNotNull(r);
        assertEquals("restriction", r.get("type"));
        assertEquals(0, r.getMembersCount());
    }

    /**
     * Selection consist of one way and the start node of the way ->
     * propose a No-U-Turn restriction
     *
     */
    @Test
    public void noUTurn_1() {
        Way w = new Way(1);
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        w.setNodes(Arrays.asList(n1, n2));

        List<OsmPrimitive> sel = Arrays.asList(w, n1);
        TurnRestrictionBuilder builder = new TurnRestrictionBuilder();
        Relation r = builder.build(sel);

        assertNotNull(r);
        assertEquals(3, r.getMembersCount());
        assertTrue(hasExactlyOneMemberWithRole(r, "from"));
        assertTrue(hasExactlyOneMemberWithRole(r, "to"));
        assertTrue(hasExactlyOneMemberWithRole(r, "via"));
        assertEquals(w, memberWithRole(r, "from"));
        assertEquals(w, memberWithRole(r, "to"));
        assertEquals(n1, memberWithRole(r, "via"));
        assertEquals("no_u_turn", r.get("restriction"));
    }

    /**
    * Selection consist of one way and the end node of the way ->
    * propose a No-U-Turn restriction
    *
    */
   @Test
   public void noUTurn_2() {
       Way w = new Way(1);
       Node n1 = new Node(1);
       Node n2 = new Node(2);
       w.setNodes(Arrays.asList(n1, n2));

       List<OsmPrimitive> sel = Arrays.asList(w, n2);
       TurnRestrictionBuilder builder = new TurnRestrictionBuilder();
       Relation r = builder.build(sel);

       assertNotNull(r);
       assertEquals(3, r.getMembersCount());
       assertTrue(hasExactlyOneMemberWithRole(r, "from"));
       assertTrue(hasExactlyOneMemberWithRole(r, "to"));
       assertTrue(hasExactlyOneMemberWithRole(r, "via"));
       assertEquals(w, memberWithRole(r, "from"));
       assertEquals(w, memberWithRole(r, "to"));
       assertEquals(n2, memberWithRole(r, "via"));
       assertEquals("no_u_turn", r.get("restriction"));
   }

   @Test
   public void nullSelection() {
       assertEmptyTurnRestriction(builder.build(null));
   }

   @Test
   public void emptySelection() {
       assertEmptyTurnRestriction(builder.build(new ArrayList<>()));
   }

   /**
    * One selected way -> build a turn restriction with a "from" leg
    * only
    */
   @Test
   public void oneSelectedWay() {
       Way w = new Way(1);
       Relation tr = builder.build(Arrays.asList(w));
       assertNotNull(tr);
       assertEquals("restriction", tr.get("type"));
       assertEquals(1, tr.getMembersCount());
       assertEquals(w, memberWithRole(tr, "from"));
   }

   /**
    * Two unconnected ways in the selection. The first one becomes the from leg,
    * the second one the two leg.
    */
   @Test
   public void twoUnconnectedWays() {
       Way w1 = new Way(1);
       w1.setNodes(Arrays.asList(new Node(11), new Node(12)));
       Way w2 = new Way(2);
       w2.setNodes(Arrays.asList(new Node(21), new Node(22)));

       Relation tr = builder.build(Arrays.asList(w1, w2));
       assertNotNull(tr);
       assertEquals("restriction", tr.get("type"));
       assertFalse(tr.hasKey("restriction"));
       assertEquals(2, tr.getMembersCount());
       assertEquals(w1, memberWithRole(tr, "from"));
       assertEquals(w2, memberWithRole(tr, "to"));
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
       Node n1 = new Node(1);
       n1.setCoor(new LatLon(1, 1));
       Node n2 = new Node(2);
       n2.setCoor(new LatLon(2, 1));
       Node n3 = new Node(3);
       n3.setCoor(new LatLon(2, 2));

       Way w1 = new Way(1);
       w1.setNodes(Arrays.asList(n1, n2));
       Way w2 = new Way(2);
       w2.setNodes(Arrays.asList(n2, n3));

       assertEquals(Math.toRadians(90), TurnRestrictionBuilder.phi(w1), 1e-7);
       assertEquals(Math.toRadians(0), TurnRestrictionBuilder.phi(w2), 1e-7);

       Relation tr = builder.build(Arrays.asList(w1, w2, n2));

       assertNotNull(tr);
       assertEquals("restriction", tr.get("type"));
       assertEquals(3, tr.getMembersCount());
       assertEquals(w1, memberWithRole(tr, "from"));
       assertEquals(w2, memberWithRole(tr, "to"));
       assertEquals(n2, memberWithRole(tr, "via"));

       assertEquals("no_right_turn", tr.get("restriction"));

       /*
        * opposite order, from w2 to w1. In this case we have left turn.
        */

       tr = builder.build(Arrays.asList(w2, w1, n2));

       double a = intersectionAngle(w2, w1);
       System.out.println("a=" + Math.toDegrees(a));

       assertNotNull(tr);
       assertEquals("restriction", tr.get("type"));
       assertEquals(3, tr.getMembersCount());
       assertEquals(w2, memberWithRole(tr, "from"));
       assertEquals(w1, memberWithRole(tr, "to"));
       assertEquals(n2, memberWithRole(tr, "via"));

       assertEquals("no_left_turn", tr.get("restriction"));
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
       Node n1 = new Node(1);
       n1.setCoor(new LatLon(5, 5));
       Node n2 = new Node(2);
       n2.setCoor(new LatLon(7, 5));
       Node n3 = new Node(3);
       n3.setCoor(new LatLon(7, 2));

       Way w1 = new Way(1);
       w1.setNodes(Arrays.asList(n1, n2));
       Way w2 = new Way(2);
       w2.setNodes(Arrays.asList(n3, n2));

       assertEquals(Math.toRadians(90), TurnRestrictionBuilder.phi(w1), 1e-7);
       assertEquals(Math.toRadians(0), TurnRestrictionBuilder.phi(w2), 1e-7);
       assertEquals(Math.toRadians(180), TurnRestrictionBuilder.phi(w2, true), 1e-7);

       Relation tr = builder.build(Arrays.asList(w1, w2, n2));

       assertNotNull(tr);
       assertEquals("restriction", tr.get("type"));
       assertEquals(3, tr.getMembersCount());
       assertEquals(w1, memberWithRole(tr, "from"));
       assertEquals(w2, memberWithRole(tr, "to"));
       assertEquals(n2, memberWithRole(tr, "via"));

       assertEquals("no_left_turn", tr.get("restriction"));

       /*
        * opposite order, from w2 to w1. In this case we have right turn.
        */
       tr = builder.build(Arrays.asList(w2, w1, n2));

       assertNotNull(tr);
       assertEquals("restriction", tr.get("type"));
       assertEquals(3, tr.getMembersCount());
       assertEquals(w2, memberWithRole(tr, "from"));
       assertEquals(w1, memberWithRole(tr, "to"));
       assertEquals(n2, memberWithRole(tr, "via"));
       assertEquals("no_right_turn", tr.get("restriction"));
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
      Node n1 = new Node(1);
      n1.setCoor(new LatLon(5, 5));
      Node n2 = new Node(2);
      n2.setCoor(new LatLon(7, 5));
      Node n3 = new Node(3);
      n3.setCoor(new LatLon(6, 7));

      Way w1 = new Way(1);
      w1.setNodes(Arrays.asList(n1, n2));
      Way w2 = new Way(2);
      w2.setNodes(Arrays.asList(n2, n3));

      Relation tr = builder.build(Arrays.asList(w1, w2, n2));

      assertNotNull(tr);
      assertEquals("restriction", tr.get("type"));
      assertEquals(3, tr.getMembersCount());
      assertEquals(w1, memberWithRole(tr, "from"));
      assertEquals(w2, memberWithRole(tr, "to"));
      assertEquals(n2, memberWithRole(tr, "via"));

      assertEquals("no_right_turn", tr.get("restriction"));
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
     Node n1 = new Node(1);
     n1.setCoor(new LatLon(10, 10));
     Node n2 = new Node(2);
     n2.setCoor(new LatLon(8, 15));
     Node n3 = new Node(3);
     n3.setCoor(new LatLon(5, 11));

     Way w1 = new Way(1);
     w1.setNodes(Arrays.asList(n1, n2));
     Way w2 = new Way(2);
     w2.setNodes(Arrays.asList(n2, n3));

     Relation tr = builder.build(Arrays.asList(w1, w2, n2));

     assertNotNull(tr);
     assertEquals("restriction", tr.get("type"));
     assertEquals(3, tr.getMembersCount());
     assertEquals(w1, memberWithRole(tr, "from"));
     assertEquals(w2, memberWithRole(tr, "to"));
     assertEquals(n2, memberWithRole(tr, "via"));

     assertEquals("no_right_turn", tr.get("restriction"));

     /*
      * opposite order, from w2 to w1. In  this case we have left turn.
      */
     tr = builder.build(Arrays.asList(w2, w1, n2));

     assertNotNull(tr);
     assertEquals("restriction", tr.get("type"));
     assertEquals(3, tr.getMembersCount());
     assertEquals(w2, memberWithRole(tr, "from"));
     assertEquals(w1, memberWithRole(tr, "to"));
     assertEquals(n2, memberWithRole(tr, "via"));

     assertEquals("no_left_turn", tr.get("restriction"));
    }

    static Node nn(long id, double lat, double lon) {
        Node n = new Node(id);
        n.setCoor(new LatLon(lat, lon));
        return n;
    }

    static Way nw(long id, Node... nodes) {
        Way w = new Way(id);
        w.setNodes(Arrays.asList(nodes));
        return w;
    }

     /**
      *                              n3
      *                           (10,10)
      *                             ^
      *                             | to
      *      n1      from           |
      *    (5,5) -------------->  (5,10) n2
      */
     @Test
     public void intersectionAngle_1() {
         Node n1 = nn(1, 5, 5);
         Node n2 = nn(2, 5, 10);
         Node n3 = nn(3, 10, 10);
         Way from = nw(1, n1, n2);
         Way to = nw(2, n2, n3);

         double a = TurnRestrictionBuilder.intersectionAngle(from, to);
         RelativeWayJoinOrientation o = TurnRestrictionBuilder.determineWayJoinOrientation(from, to);
         assertEquals(-90, Math.toDegrees(a), 1e-7);
         assertEquals(RelativeWayJoinOrientation.LEFT, o);

         /*
          * if reversed from, the intersection angle is still -90
          */
         from = nw(1, n2, n1);
         to = nw(2, n2, n3);
         a = TurnRestrictionBuilder.intersectionAngle(from, to);
         o = TurnRestrictionBuilder.determineWayJoinOrientation(from, to);
         assertEquals(-90, Math.toDegrees(a), 1e-7);
         assertEquals(RelativeWayJoinOrientation.LEFT, o);

         /*
         * if reversed to, the intersection angle is still -90
         */
         from = nw(1, n1, n2);
         to = nw(2, n3, n2);
         a = TurnRestrictionBuilder.intersectionAngle(from, to);
         o = TurnRestrictionBuilder.determineWayJoinOrientation(from, to);
         assertEquals(-90, Math.toDegrees(a), 1e-7);
         assertEquals(RelativeWayJoinOrientation.LEFT, o);

         /*
         * if reversed both, the intersection angle is still -90
         */
         from = nw(1, n2, n1);
         to = nw(2, n3, n2);
         a = TurnRestrictionBuilder.intersectionAngle(from, to);
         o = TurnRestrictionBuilder.determineWayJoinOrientation(from, to);
         assertEquals(-90, Math.toDegrees(a), 1e-7);
         assertEquals(RelativeWayJoinOrientation.LEFT, o);
     }

     /**
     *      n1      from
     *    (5,5) -------------->  (5,10) n2
     *                              |
     *                              | to
     *                              |
     *                              v
     *                            (2,10)
     *                              n3
     *
     */
    @Test
    public void intersectionAngle_2() {
        Node n1 = nn(1, 5, 5);
        Node n2 = nn(2, 5, 10);
        Node n3 = nn(3, 2, 10);
        Way from = nw(1, n1, n2);
        Way to = nw(2, n2, n3);

        double a = TurnRestrictionBuilder.intersectionAngle(from, to);
        assertEquals(90, Math.toDegrees(a), 1e-7);

        /*
         * if reversed from, the intersection angle is still 90
         */
        from = nw(1, n2, n1);
        to = nw(2, n2, n3);
        a = TurnRestrictionBuilder.intersectionAngle(from, to);
        assertEquals(90, Math.toDegrees(a), 1e-7);

        /*
        * if reversed to, the intersection angle is still 90
        */
        from = nw(1, n1, n2);
        to = nw(2, n3, n2);
        a = TurnRestrictionBuilder.intersectionAngle(from, to);
        assertEquals(90, Math.toDegrees(a), 1e-7);

        /*
        * if reversed both, the intersection angle is still 90
        */
        from = nw(1, n2, n1);
        to = nw(2, n3, n2);
        a = TurnRestrictionBuilder.intersectionAngle(from, to);
        assertEquals(90, Math.toDegrees(a), 1e-7);
    }

    /**
     *
     *
     *             (-1,-6) (n3)
     *             ^
     *            /
     *           /  to
     *          /
     *      (-5, -10) n2
    *           ^
    *           |
    *           | from
    *           |
    *       (-10,-10) n1
    */
   @Test
   public void intersectionAngle_3() {
       Node n1 = nn(1, -10, -10);
       Node n2 = nn(2, -5, -10);
       Node n3 = nn(3, -1, -6);
       Way from = nw(1, n1, n2);
       Way to = nw(2, n2, n3);

       double a = TurnRestrictionBuilder.intersectionAngle(from, to);
       assertEquals(45, Math.toDegrees(a), 1e-7);

       /*
        * if reversed from, the intersection angle is still 45
        */
       from = nw(1, n2, n1);
       to = nw(2, n2, n3);
       a = TurnRestrictionBuilder.intersectionAngle(from, to);
       assertEquals(45, Math.toDegrees(a), 1e-7);

       /*
       * if reversed to, the intersection angle is still 45
       */
       from = nw(1, n1, n2);
       to = nw(2, n3, n2);
       a = TurnRestrictionBuilder.intersectionAngle(from, to);
       assertEquals(45, Math.toDegrees(a), 1e-7);

       /*
       * if reversed both, the intersection angle is still 45
       */
       from = nw(1, n2, n1);
       to = nw(2, n3, n2);
       a = TurnRestrictionBuilder.intersectionAngle(from, to);
       assertEquals(45, Math.toDegrees(a), 1e-7);
   }

   /**
   *
   *
   *         (-1,-14) (n3)
   *            ^
   *            \
   *             \ to
   *              \
   *          (-5, -10) n2
  *               ^
  *               |
  *               | from
  *               |
  *           (-10,-10) n1
  */
 @Test
 public void intersectionAngle_4() {
     Node n1 = nn(1, -10, -10);
     Node n2 = nn(2, -5, -10);
     Node n3 = nn(3, -1, -14);
     Way from = nw(1, n1, n2);
     Way to = nw(2, n2, n3);

     double a = TurnRestrictionBuilder.intersectionAngle(from, to);
     assertEquals(-45, Math.toDegrees(a), 1e-7);

     /*
      * if reversed from, the intersection angle is still -45
      */
     from = nw(1, n2, n1);
     to = nw(2, n2, n3);
     a = TurnRestrictionBuilder.intersectionAngle(from, to);
     assertEquals(-45, Math.toDegrees(a), 1e-7);

     /*
     * if reversed to, the intersection angle is still -45
     */
     from = nw(1, n1, n2);
     to = nw(2, n3, n2);
     a = TurnRestrictionBuilder.intersectionAngle(from, to);
     assertEquals(-45, Math.toDegrees(a), 1e-7);

     /*
     * if reversed both, the intersection angle is still 45
     */
     from = nw(1, n2, n1);
     to = nw(2, n3, n2);
     a = TurnRestrictionBuilder.intersectionAngle(from, to);
     assertEquals(-45, Math.toDegrees(a), 1e-7);
 }


     /*
     *
     *      n21        w21        n22       w22            n23
     *    (10,10)-------------> (10,15) -------------- > (10,20)
     *                            ^
     *                            |
     *                            | w1
     *                            |
     *                          (5,15)
     *                            n11
     */
    @Test
    public void splitToWay() {
        Node n11 = new Node(11);
        n11.setCoor(new LatLon(5, 15));

        Node n21 = new Node(21);
        n21.setCoor(new LatLon(10, 10));
        Node n22 = new Node(22);
        n22.setCoor(new LatLon(10, 15));
        Node n23 = new Node(23);
        n23.setCoor(new LatLon(10, 20));

        Way w1 = new Way(1);
        w1.setNodes(Arrays.asList(n11, n22));
        Way w21 = new Way(21);
        w21.setNodes(Arrays.asList(n21, n22));
        Way w22 = new Way(22);
        w22.setNodes(Arrays.asList(n22, n23));

        Way adjustedTo = selectToWayAfterSplit(
            w1,
            w21,
            w22,
            TurnRestrictionType.NO_LEFT_TURN
        );

        assertNotNull(adjustedTo);
        assertEquals(w21, adjustedTo);

        adjustedTo = selectToWayAfterSplit(
            w1,
            w21,
            w22,
            TurnRestrictionType.NO_RIGHT_TURN
        );

        assertNotNull(adjustedTo);
        assertEquals(w22, adjustedTo);

        adjustedTo = selectToWayAfterSplit(
            w1,
            w21,
            w22,
            TurnRestrictionType.ONLY_LEFT_TURN
        );

        assertNotNull(adjustedTo);
        assertEquals(w21, adjustedTo);

        adjustedTo = selectToWayAfterSplit(
            w1,
            w21,
            w22,
            TurnRestrictionType.ONLY_RIGHT_TURN
        );

        assertNotNull(adjustedTo);
        assertEquals(w22, adjustedTo);

        adjustedTo = selectToWayAfterSplit(
            w1,
            w21,
            w22,
            TurnRestrictionType.NO_STRAIGHT_ON
        );

        assertNull(adjustedTo);
    }
}
