package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

import org.junit.*
import org.openstreetmap.josm.data.osm.DataSet
import org.openstreetmap.josm.gui.layer.OsmDataLayer
import org.openstreetmap.josm.testutils.JOSMTestRules

/**
 * Unit test for the {@link TurnRestrictionLegEditor}
 */
class TurnRestrictionLegEditorUnitTest  {

    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

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
