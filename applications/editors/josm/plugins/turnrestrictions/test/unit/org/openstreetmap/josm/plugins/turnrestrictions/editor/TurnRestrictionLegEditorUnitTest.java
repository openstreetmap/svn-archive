// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit test for the {@link TurnRestrictionLegEditor}
 */
public class TurnRestrictionLegEditorUnitTest {

    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    private DataSet ds;
    private OsmDataLayer layer;
    private TurnRestrictionEditorModel model;

    @Before
    public void setUp() {
        ds = new DataSet();
        layer = new OsmDataLayer(ds, "test", null);
        model = new TurnRestrictionEditorModel(layer, new NavigationControler() {
            @Override
            public void gotoBasicEditor(BasicEditorFokusTargets focusTarget) {
            }

            @Override
            public void gotoBasicEditor() {
            }

            @Override
            public void gotoAdvancedEditor() {
            }
        });
    }

    @Test
    public void testConstructor1() {
        TurnRestrictionLegEditor editor = new TurnRestrictionLegEditor(model, TurnRestrictionLegRole.FROM);
        assertEquals(model, editor.getModel());
        assertEquals(TurnRestrictionLegRole.FROM, editor.getRole());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2() {
        new TurnRestrictionLegEditor(null, TurnRestrictionLegRole.FROM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor3() {
        new TurnRestrictionLegEditor(model, null);
    }
}
