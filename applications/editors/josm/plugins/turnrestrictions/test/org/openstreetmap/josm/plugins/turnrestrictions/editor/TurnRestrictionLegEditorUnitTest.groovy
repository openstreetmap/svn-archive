package org.openstreetmap.josm.plugins.turnrestrictions.editor;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

import static org.junit.Assert.*;
import org.junit.*;
/**
 * Unit test for the {@see TurnRestrictionLegEditor}
 * 
 */
class TurnRestrictionLegEditorUnitTest {
	final shouldFail = new GroovyTestCase().&shouldFail
	
	def navigationControlerMock = [
       gotoBasicEditor:{}, 
       gotoAdvancedEditor: {}
	] as NavigationControler
	
	private DataSet ds
	private OsmDataLayer layer
	private TurnRestrictionEditorModel model 
	
	@Before
	public void setUp() {
		ds = new DataSet()
		layer = new OsmDataLayer(ds, "test", null)		
		model = new TurnRestrictionEditorModel(layer, navigationControlerMock);
	}
	 
	@Test
	public void test_Constructor() {
		
		TurnRestrictionLegEditor editor = new TurnRestrictionLegEditor(model, TurnRestrictionLegRole.FROM)
		
		assert editor.getModel() == model
		assert editor.getRole() == TurnRestrictionLegRole.FROM
		
		shouldFail(IllegalArgumentException) {
			editor = new TurnRestrictionLegEditor(null, TurnRestrictionLegRole.FROM)
		}

		shouldFail(IllegalArgumentException) {
			editor = new TurnRestrictionLegEditor(model, null)
		}
	}
}
