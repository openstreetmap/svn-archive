package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import groovy.util.GroovyTestCase;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import javax.swing.DefaultListSelectionModel;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.coor.*;

import org.openstreetmap.josm.plugins.turnrestrictions.fixtures.JOSMFixture;

import static org.junit.Assert.*;
import org.junit.*;
import javax.swing.JFrame;

import junit.framework.TestCase;
import junit.framework.TestResult;

/**
 * Unit test for {@see JosmSelctionListModel}
 */
class JosmSelectionListModelTest extends GroovyTestCase {
	final shouldFail = new GroovyTestCase().&shouldFail
	
	@Before
	public void setUp() {
		JOSMFixture.createUnitTestFixture().init()
	}
	
	@Test
	public void test_Constructor(){
		DataSet ds = new DataSet()
		OsmDataLayer layer = new OsmDataLayer(ds, "test", null)
		JosmSelectionListModel model = new JosmSelectionListModel(layer, new DefaultListSelectionModel());		
		
		shouldFail(IllegalArgumentException){
			model = new JosmSelectionListModel(layer, null)
		}
		
		shouldFail(IllegalArgumentException){
			model = new JosmSelectionListModel(null, new DefaultListSelectionModel())
		}
	}
	
	@Test
	public void test_setJOSMSelection() {
		DataSet ds = new DataSet()
		OsmDataLayer layer = new OsmDataLayer(ds, "test", null)
		JosmSelectionListModel model = new JosmSelectionListModel(layer, new DefaultListSelectionModel());
		
		// set a selection with three objects 
		def objects = [new Node(new LatLon(1,1)), new Way(), new Relation()]
		model.setJOSMSelection objects
		assert model.getSize() == 3
		
		// null is allowed 
		model.setJOSMSelection(null)
		assert model.getSize() == 0
		assert model.getSelected().isEmpty()
		
		// empty has the same effect
		model.setJOSMSelection([])
		assert model.getSize() == 0
		assert model.getSelected().isEmpty()
	}
	
	@Test
	public void test_setJOSMSelection_withSelected() {
		DataSet ds = new DataSet()
		OsmDataLayer layer = new OsmDataLayer(ds, "test", null)
		JosmSelectionListModel model = new JosmSelectionListModel(layer, new DefaultListSelectionModel());
		def objects = [new Node(new LatLon(1,1)), new Way(), new Relation()]	
		model.setJOSMSelection(objects)
		model.setSelected(objects[0..1])
		assert model.getSelected().asList() as Set == objects[0..1] as Set
		
		// set new selection which includes one object which is currently
        // selected in the model. Should still be selected after setting
		// the new JOSM selection
		objects = objects[1..2] 
		model.setJOSMSelection(objects)
		assert model.getSelected().asList() == [objects[0]]
	}	
	
	@Test
	public void test_getSelected() {
		DataSet ds = new DataSet()
		OsmDataLayer layer = new OsmDataLayer(ds, "test", null)
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		
		JosmSelectionListModel model = new JosmSelectionListModel(layer, selectionModel);
		assert model.getSelected() != null
		assert model.getSelected().isEmpty()
	
		// select one element 
		def objects = [new Node(new LatLon(1,1)), new Way(), new Relation()]	
		model.setJOSMSelection(objects)
		selectionModel.setSelectionInterval(0, 0)
		assert model.getSelected().asList() == [model.getElementAt(0)];
		
		// select two elements
		selectionModel.setSelectionInterval(1,2)
		assert model.getSelected().asList() as Set == [model.getElementAt(1),model.getElementAt(2)] as Set;
	}
	
	@Test
	public void test_setSelected() {
		DataSet ds = new DataSet()
		OsmDataLayer layer = new OsmDataLayer(ds, "test", null)
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		
		// set selected with null is OK - nothing selected thereafter
		JosmSelectionListModel model = new JosmSelectionListModel(layer, selectionModel);
		model.setSelected(null)
		assert model.getSelected().isEmpty()
		
		// set selected with empty list is OK - nothing selected thereafter
		model.setSelected([])
		assert model.getSelected().isEmpty()
		
		// select an object existing in the list of displayed objects 
		def objects = [new Node(new LatLon(1,1)), new Way(), new Relation()]	
		model.setJOSMSelection(objects)
		model.setSelected([objects[0]])
		assert model.getSelected().asList() == [objects[0]];
		
		// select an object not-existing in the list of displayed objects 	
		model.setJOSMSelection(objects)
		model.setSelected([new Way()])
		assert model.getSelected().isEmpty()
	}
	
	@Test 
	public void test_editLayerChanged() {
		DataSet ds = new DataSet()
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();		
		def objects = [new Node(new LatLon(1,1)), new Way(), new Relation()]	
		objects.each {ds.addPrimitive(it)}
		
		OsmDataLayer layer1 = new OsmDataLayer(ds,"layer1", null)
		OsmDataLayer layer2 = new OsmDataLayer(new DataSet(),"layer2", null)
		
		JosmSelectionListModel model = new JosmSelectionListModel(layer1, selectionModel);
		
		// switch from edit layer1 to edit layer2. content of the JOSM selection 
		// should be empty thereafter 
		model.editLayerChanged(layer1, layer2)
		assert model.getSize() == 0
		
		// switch from layer2 to layer1 which has one object selected. Object should
		// be displayed in the JOSM selection list 
		ds.setSelected([objects[0]])
		model.editLayerChanged(layer2, layer1)
		assert model.getSize() == 1
		assert model.getElementAt(0) == objects[0];
		
		// switch to a "null" edit layer (i.e. no edit layer)- nothing should
		// be displayed in the selection list 
		model.editLayerChanged(layer1, null)
		assert model.getSize() == 0
	}
}
