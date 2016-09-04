// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole.FROM;
import static org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole.TO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * This is a unit test for {@link TurnRestrictionEditorModel}
 */
public class TurnRestrictionEditorModelUnitTest {

    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    private final NavigationControler navigationControlerMock = new NavigationControler() {
        @Override
        public void gotoBasicEditor(BasicEditorFokusTargets focusTarget) {
        }

        @Override
        public void gotoBasicEditor() {
        }

        @Override
        public void gotoAdvancedEditor() {
        }
    };

    private DataSet ds;
    private OsmDataLayer layer;
    private TurnRestrictionEditorModel model;

    Node createNode(Long id, LatLon coor) {
        Node n;
        if (id == null) {
            n = new Node();
        } else {
            n = new Node(id);
        }
        if (coor != null)
            n.setCoor(coor);
        ds.addPrimitive(n);
        return n;
    }

    Way createWay(Long id) {
        Way w;
        if (id == null) {
            w = new Way();
        } else {
            w = new Way(id);
        }
        ds.addPrimitive(w);
        return w;
    }

    Node node(long id) {
        return (Node) ds.getPrimitiveById(new SimplePrimitiveId(id, OsmPrimitiveType.NODE));
    }

    Way way(long id) {
        return (Way) ds.getPrimitiveById(new SimplePrimitiveId(id, OsmPrimitiveType.WAY));
    }

    Relation rel(long id) {
        return (Relation) ds.getPrimitiveById(new SimplePrimitiveId(id, OsmPrimitiveType.RELATION));
    }

    RelationMember rm(String role, OsmPrimitive object) {
        return new RelationMember(role, object);
    }

    void buildDataSet1() {
        // prepare some nodes and ways
        createNode(21L, null);
        createNode(22L, null);
        createNode(31L, null);
        createNode(32L, null);
        createWay(2L);
        createWay(3L);

        way(2).setNodes(Arrays.asList(node(21), node(22)));
        way(3).setNodes(Arrays.asList(node(22), node(31)));

        // a standard turn restriction with a from, a to and a via
        Relation r = new Relation(1);
        r.setMembers(Arrays.asList(rm("from", way(2)), rm("to", way(3)), rm("via", node(22))));
        r.put("type", "restriction");
        r.put("restriction", "no_left_turn");
        ds.addPrimitive(r);
    }

    @Before
    public void setUp() {
        ds = new DataSet();
        layer = new OsmDataLayer(ds, "test", null);
        model = new TurnRestrictionEditorModel(layer, navigationControlerMock);
    }

    /**
     * Test the constructor
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor1() {
        new TurnRestrictionEditorModel(null, navigationControlerMock);
    }

    /**
     * Test the constructor
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2() {
        new TurnRestrictionEditorModel(layer, null);
    }

    @Test
    public void testPopulateEmptyTurnRestriction() {
        // an "empty" turn restriction with a public id
        Relation r = new Relation(1);
        ds.addPrimitive(r);
        assertTrue(model.getTurnRestrictionLeg(FROM).isEmpty());
        assertTrue(model.getTurnRestrictionLeg(TO).isEmpty());
        assertTrue(model.getVias().isEmpty());
        assertEquals("", model.getRestrictionTagValue());
        assertEquals("", model.getExcept().getValue());
    }

    /**
     * Populating the model with a simple default turn restriction: one from member (a way),
     * one to member (a way), one via (the common node of these ways), minimal tag set with
     * type=restriction and restriction=no_left_turn
     *
     */
    @Test
    public void test_populate_SimpleStandardTurnRestriction() {
        buildDataSet1();
        model.populate(rel(1));

        assertEquals(Collections.singleton(way(2)), model.getTurnRestrictionLeg(FROM));
        assertEquals(Collections.singleton(way(3)), model.getTurnRestrictionLeg(TO));
        assertEquals(Arrays.asList(node(22)), model.getVias());
        assertEquals("no_left_turn", model.getRestrictionTagValue());
        assertEquals("", model.getExcept().getValue());
    }

    @Test
    public void setFrom() {
        buildDataSet1();
        model.populate(rel(1));

        createNode(41L, null);
        createNode(42L, null);
        createWay(4L).setNodes(Arrays.asList(node(41), node(42)));

        // set another way as from
        model.setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, way(4).getPrimitiveId());
        assertEquals(Collections.singleton(way(4)), model.getTurnRestrictionLeg(TurnRestrictionLegRole.FROM));

        // delete the/all members with role 'from'
        model.setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, null);
        assertTrue(model.getTurnRestrictionLeg(TurnRestrictionLegRole.FROM).isEmpty());

        try {
            // can't add a node as 'from'
            model.setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, node(21).getPrimitiveId());
            fail();
        } catch (IllegalArgumentException e) {
            // OK
            System.out.println(e.getMessage());
        }

        try {
            // can't set a way as 'from' if it isn't part of the dataset
            model.setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, new Way().getPrimitiveId());
            fail();
        } catch (IllegalStateException e) {
            // OK
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void setTo() {
        buildDataSet1();
        model.populate(rel(1));

        createNode(41L, null);
        createNode(42L, null);
        createWay(4L).setNodes(Arrays.asList(node(41), node(42)));

        // set another way as from
        model.setTurnRestrictionLeg(TurnRestrictionLegRole.TO, way(4).getPrimitiveId());
        assertEquals(Collections.singleton(way(4)), model.getTurnRestrictionLeg(TurnRestrictionLegRole.TO));

        // delete the/all members with role 'from'
        model.setTurnRestrictionLeg(TurnRestrictionLegRole.TO, null);
        assertTrue(model.getTurnRestrictionLeg(TurnRestrictionLegRole.TO).isEmpty());

        try {
            // can't add a node as 'from'
            model.setTurnRestrictionLeg(TurnRestrictionLegRole.TO, node(21).getPrimitiveId());
            fail();
        } catch (IllegalArgumentException e) {
            // OK
            System.out.println(e.getMessage());
        }

        try {
            // can't set a way as 'from' if it isn't part of the dataset
            model.setTurnRestrictionLeg(TurnRestrictionLegRole.TO, new Way().getPrimitiveId());
            fail();
        } catch (IllegalStateException e) {
            // OK
            System.out.println(e.getMessage());
        }
    }

    /**
     * Test setting or deleting the tag 'restriction'
     */
    @Test
    public void setRestrictionTagValue() {
        buildDataSet1();
        model.populate(rel(1));

        model.setRestrictionTagValue("no_left_turn");
        assertEquals("no_left_turn", model.getRestrictionTagValue());

        model.setRestrictionTagValue(null);
        assertEquals("", model.getRestrictionTagValue());

        model.setRestrictionTagValue("  ");
        assertEquals("", model.getRestrictionTagValue());

        model.setRestrictionTagValue(" no_right_Turn ");
        assertEquals("no_right_turn", model.getRestrictionTagValue());
    }

    /**
     * Test setting vias
     */
    @Test
    public void setVias() {
        buildDataSet1();
        model.populate(rel(1));

        // one node as via - OK
        model.setVias(Arrays.asList(node(22)));
        assertEquals(Arrays.asList(node(22)), model.getVias());

        // pass in null as vias -> remove all vias
        model.setVias(null);
        assertTrue(model.getVias().isEmpty());

        // pass in empty list -> remove all vias
        model.setVias(new ArrayList<>());
        assertTrue(model.getVias().isEmpty());

        // create a list of vias with a way and twice a node (which doesn't
        // make sense but is technically allowed)
        //
        createNode(41L, null);
        createNode(42L, null);
        createWay(4L).setNodes(Arrays.asList(node(41), node(42)));
        model.setVias(Arrays.asList(way(4), node(22), node(22)));
        assertEquals(Arrays.asList(way(4), node(22), node(22)), model.getVias());

        // null values in the list of vias are skipped
        model.setVias(Arrays.asList(null, node(22)));
        assertEquals(Arrays.asList(node(22)), model.getVias());

        try {
            // an object which doesn't belong to the same dataset can't be a via
            model.setVias(Arrays.asList(new Node(LatLon.ZERO)));
            fail();
        } catch (IllegalArgumentException e) {
            // OK
            System.out.println(e.getMessage());
        }
    }

    /**
     * Tests whether the three sub models exist
     */
    @Test
    public void submodelsExist() {
        assertNotNull(model.getIssuesModel());
        assertNotNull(model.getRelationMemberEditorModel());
        assertNotNull(model.getTagEditorModel());

        assertEquals(layer, model.getLayer());
    }
}
