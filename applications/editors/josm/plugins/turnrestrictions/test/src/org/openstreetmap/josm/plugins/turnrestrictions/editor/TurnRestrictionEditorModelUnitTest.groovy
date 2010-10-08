package org.openstreetmap.josm.plugins.turnrestrictions.editor;
import groovy.util.GroovyTestCase;

import static org.junit.Assert.*;
import org.junit.*;
import static org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole.*
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation
import org.openstreetmap.josm.data.osm.RelationMember
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.data.osm.DataSet
import org.openstreetmap.josm.data.coor.*
import org.openstreetmap.josm.fixtures.JOSMFixture;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

import org.openstreetmap.josm.plugins.turnrestrictions.fixtures.JOSMFixture;

/**
 * This is a unit test for {@link TurnRestrictionEditorModel}
 *
 */
class TurnRestrictionEditorModelUnitTest extends GroovyTestCase{

	final shouldFail = new GroovyTestCase().&shouldFail
	
	def navigationControlerMock = [
       gotoBasicEditor:{}, 
       gotoAdvancedEditor: {}
	] as NavigationControler
	 
	private DataSet ds
	private OsmDataLayer layer
	private TurnRestrictionEditorModel model 
	
	def createNode(id = null, coor = null) {
	    Node n
	    if (id == null){
	    	n = new Node()
	    } else {
	    	n = new Node(id)
	    }
	    if (coor != null) n.setCoor(coor)
	    ds.addPrimitive(n)
	    return n
	}
	
	def createWay(id=null) {
	    Way w
	    if (id == null){
	    	w = new Way()
	    } else {
	    	w = new Way(id)
	    }
	    ds.addPrimitive(w)
	    return w
	}
	
	def node(id){
		return ds.getPrimitiveById(new SimplePrimitiveId(id, OsmPrimitiveType.NODE))
	}
	
	def way(id) {
		return ds.getPrimitiveById(new SimplePrimitiveId(id, OsmPrimitiveType.WAY))
	}
	
	def rel(id){
		return ds.getPrimitiveById(new SimplePrimitiveId(id, OsmPrimitiveType.RELATION))
	}
	
	def rm(role,object){
		return new RelationMember(role, object);
	}
	
	def buildDataSet1() {		
		// prepare some nodes and ways
		createNode(21)
		createNode(22)
		createNode(31)
		createNode(32)
		createWay(2)
		createWay(3)
			
		way(2).setNodes([node(21), node(22)])
		way(3).setNodes([node(22), node(31)])
		
		// a standard turn restriction with a from, a to and a via
		Relation r = new Relation(1)
		r.setMembers([rm("from", way(2)), rm("to", way(3)), rm("via", node(22))])
		r.put "type", "restriction"
		r.put "restriction", "no_left_turn"		
		ds.addPrimitive r
	}
	
	@Before
	public void setUp() {
		JOSMFixture.createUnitTestFixture().init()
			
		ds = new DataSet()
		layer = new OsmDataLayer(ds, "test", null)		
		model = new TurnRestrictionEditorModel(layer, navigationControlerMock);
	}
	
	
	
	/**
	 * Test the constructor 
	 */
	@Test
	public void test_Constructor() {		
		shouldFail(IllegalArgumentException){
			model = new TurnRestrictionEditorModel(null, navigationControlerMock);			
		}

		shouldFail(IllegalArgumentException){
			model = new TurnRestrictionEditorModel(layer, null);			
		}
	}
	
	@Test
	public void test_populate_EmptyTurnRestriction() {		
		// an "empty" turn restriction with a public id 
		Relation r = new Relation(1)
		ds.addPrimitive r
		assert model.getTurnRestrictionLeg(FROM).isEmpty()
		assert model.getTurnRestrictionLeg(TO).isEmpty()
		assert model.getVias().isEmpty()
		assert model.getRestrictionTagValue() == ""
	    assert model.getExcept().getValue() == ""
	}
	
	/**
	 * Populating the model with a simple default turn restriction: one from member (a way),
	 * one to member (a way), one via (the common node of these ways), minimal tag set with
	 * type=restriction and restriction=no_left_turn
	 * 
	 */
	@Test
	public void test_populate_SimpleStandardTurnRestriction() {		
		buildDataSet1()		
		model.populate(rel(1))
		
		assert model.getTurnRestrictionLeg(FROM).asList() == [way(2)]
		assert model.getTurnRestrictionLeg(TO).asList() == [way(3)]
		assert model.getVias() == [node(22)]
		assert model.getRestrictionTagValue() == "no_left_turn"
	    assert model.getExcept().getValue() == ""
	}
	
	@Test
	public void setFrom() {
		buildDataSet1()		
		model.populate(rel(1))
		
		createNode(41)
		createNode(42)
		createWay(4).setNodes([node(41),node(42)]);
		
		// set another way as from 
		model.setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, way(4).getPrimitiveId())
		assert model.getTurnRestrictionLeg(TurnRestrictionLegRole.FROM).asList() == [way(4)];
		
		// delete the/all members with role 'from'
		model.setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, null)
		assert model.getTurnRestrictionLeg(TurnRestrictionLegRole.FROM).isEmpty()
		
		
		shouldFail(IllegalArgumentException) {
			// can't add a node as 'from'
			model.setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, node(21).getPrimitiveId())
		}
		
		shouldFail(IllegalStateException) {
			// can't set a way as 'from' if it isn't part of the dataset 
			Way way = new Way() 
			model.setTurnRestrictionLeg(TurnRestrictionLegRole.FROM, way.getPrimitiveId())
		}
	}
	
	@Test
	public void setTo() {
		buildDataSet1()		
		model.populate(rel(1))
		
		createNode(41)
		createNode(42)
		createWay(4).setNodes([node(41),node(42)]);
		
		// set another way as from 
		model.setTurnRestrictionLeg(TurnRestrictionLegRole.TO, way(4).getPrimitiveId())
		assert model.getTurnRestrictionLeg(TurnRestrictionLegRole.TO).asList() == [way(4)];
		
		// delete the/all members with role 'from'
		model.setTurnRestrictionLeg(TurnRestrictionLegRole.TO, null)
		assert model.getTurnRestrictionLeg(TurnRestrictionLegRole.TO).isEmpty()
		
		
		shouldFail(IllegalArgumentException) {
			// can't add a node as 'from'
			model.setTurnRestrictionLeg(TurnRestrictionLegRole.TO, node(21).getPrimitiveId())
		}
		
		shouldFail(IllegalStateException) {
			// can't set a way as 'from' if it isn't part of the dataset 
			Way way = new Way() 
			model.setTurnRestrictionLeg(TurnRestrictionLegRole.TO, way.getPrimitiveId())
		}
	}
	
	/**
	 * Test setting or deleting the tag 'restriction'
	 */
	@Test
	public void setRestrictionTagValue() {
		buildDataSet1()		
		model.populate(rel(1))
		
		model.setRestrictionTagValue("no_left_turn")
		assert model.getRestrictionTagValue() == "no_left_turn";
		
		model.setRestrictionTagValue(null)
		assert model.getRestrictionTagValue() == "";
		
		model.setRestrictionTagValue("  ")
		assert model.getRestrictionTagValue() == "";
		
		model.setRestrictionTagValue(" no_right_Turn ")
		assert model.getRestrictionTagValue() == "no_right_turn";		
	}
	
	/**
	 * Test setting vias
	 */
	@Test
	public void setVias() {
		buildDataSet1()		
		model.populate(rel(1))
		
		// one node as via - OK
		model.setVias([node(22)])
		assert model.getVias() == [node(22)];
		
		// pass in null as vias -> remove all vias 
		model.setVias(null)
		assert model.getVias().isEmpty()
		
		// pass in empty list -> remove all vias 
		model.setVias([])
		assert model.getVias().isEmpty()
		
		// create a list of vias with a way and twice a node (which doesn't
		// make sense but is technically allowed)
		//
		createNode(41)
		createNode(42)
		createWay(4).setNodes([node(41), node(42)])
		model.setVias([way(4), node(22), node(22)])
		assert model.getVias() == [way(4), node(22), node(22)];

        // null values in the list of vias are skipped 		                     
        model.setVias([null, node(22)])
        assert model.getVias() == [node(22)]
                                   
        shouldFail(IllegalArgumentException) {
			// an object which doesn't belong to the same dataset can't
			// be a via
			Node n = new Node(new LatLon(0,0))
			model.setVias([n])
		}
	}
	
	/**
	 * Tests whether the three sub models exist
	 */
	@Test
	public void submodelsExist() {
		assert model.getIssuesModel() != null
		assert model.getRelationMemberEditorModel() != null
		assert model.getTagEditorModel() != null
		
		assert model.getLayer() == layer 
	}	
}
