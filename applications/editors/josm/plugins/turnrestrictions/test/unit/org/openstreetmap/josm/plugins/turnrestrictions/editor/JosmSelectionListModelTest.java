// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.ListSelectionModel;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit test for {@see JosmSelctionListModel}
 */
public class JosmSelectionListModelTest {

    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    @Test
    public void testConstructor() {
        assertNotNull(new JosmSelectionListModel(new OsmDataLayer(new DataSet(), "test", null)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() {
        new JosmSelectionListModel(null);
    }

    @Test
    public void test_setJOSMSelection() {
        DataSet ds = new DataSet();
        OsmDataLayer layer = new OsmDataLayer(ds, "test", null);
        JosmSelectionListModel model = new JosmSelectionListModel(layer);

        // set a selection with three objects
        model.setJOSMSelection(Arrays.asList(new Node(new LatLon(1, 1)), new Way(), new Relation()));
        assertEquals(3, model.getSize());

        // null is allowed
        model.setJOSMSelection(null);
        assertEquals(0, model.getSize());
        assertTrue(model.getSelected().isEmpty());

        // empty has the same effect
        model.setJOSMSelection(new ArrayList<>());
        assertEquals(0, model.getSize());
        assertTrue(model.getSelected().isEmpty());
    }

    @Test
    public void test_setJOSMSelection_withSelected() {
        DataSet ds = new DataSet();
        OsmDataLayer layer = new OsmDataLayer(ds, "test", null);
        JosmSelectionListModel model = new JosmSelectionListModel(layer);
        List<OsmPrimitive> objects = (Arrays.asList(new Node(new LatLon(1, 1)), new Way(), new Relation()));
        model.setJOSMSelection(objects);
        model.setSelected(objects.subList(0, 1));
        assertEquals(new HashSet<>(objects.subList(0, 1)), model.getSelected());

        // set new selection which includes one object which is currently
        // selected in the model. Should still be selected after setting
        // the new JOSM selection
        objects = objects.subList(1, 2);
        model.setJOSMSelection(objects);
        assertEquals(Collections.singleton(objects.get(0)), model.getSelected());
    }

    @Test
    public void test_getSelected() {
        DataSet ds = new DataSet();
        OsmDataLayer layer = new OsmDataLayer(ds, "test", null);

        JosmSelectionListModel model = new JosmSelectionListModel(layer);
        ListSelectionModel selectionModel = model.getListSelectionModel();

        assertNotNull(model.getSelected());
        assertTrue(model.getSelected().isEmpty());

        // select one element
        model.setJOSMSelection(Arrays.asList(new Node(new LatLon(1, 1)), new Way(), new Relation()));
        selectionModel.setSelectionInterval(0, 0);
        assertEquals(Collections.singleton(model.getElementAt(0)), model.getSelected());

        // select two elements
        selectionModel.setSelectionInterval(1, 2);
        assertEquals(new HashSet<>(Arrays.asList(model.getElementAt(1), model.getElementAt(2))), model.getSelected());
    }

    @Test
    public void test_setSelected() {
        // set selected with null is OK - nothing selected thereafter
        JosmSelectionListModel model = new JosmSelectionListModel(new OsmDataLayer(new DataSet(), "test", null));
        model.setSelected(null);
        assertTrue(model.getSelected().isEmpty());

        // set selected with empty list is OK - nothing selected thereafter
        model.setSelected(new ArrayList<>());
        assertTrue(model.getSelected().isEmpty());

        // select an object existing in the list of displayed objects
        List<OsmPrimitive> objects = (Arrays.asList(new Node(new LatLon(1, 1)), new Way(), new Relation()));
        model.setJOSMSelection(objects);
        model.setSelected(Arrays.asList(objects.get(0)));
        assertEquals(Collections.singleton(objects.get(0)), model.getSelected());

        // select an object not-existing in the list of displayed objects
        model.setJOSMSelection(objects);
        model.setSelected(Arrays.asList(new Way()));
        assertTrue(model.getSelected().isEmpty());
    }

    @Test
    public void test_editLayerChanged() {
        DataSet ds = new DataSet();

        List<OsmPrimitive> objects = (Arrays.asList(new Node(new LatLon(1, 1)), new Way(), new Relation()));
        objects.stream().forEach(ds::addPrimitive);

        OsmDataLayer layer1 = new OsmDataLayer(ds, "layer1", null);
        OsmDataLayer layer2 = new OsmDataLayer(new DataSet(), "layer2", null);

        MainApplication.getLayerManager().addLayer(layer1);
        MainApplication.getLayerManager().addLayer(layer2);

        JosmSelectionListModel model = new JosmSelectionListModel(layer1);
        MainApplication.getLayerManager().addActiveLayerChangeListener(model);
        // switch from edit layer1 to edit layer2. content of the JOSM selection
        // should be empty thereafter
        MainApplication.getLayerManager().setActiveLayer(layer1);
        MainApplication.getLayerManager().setActiveLayer(layer2);
        assertEquals(0, model.getSize());

        // switch from layer2 to layer1 which has one object selected. Object should
        // be displayed in the JOSM selection list
        ds.setSelected(Collections.singleton(objects.get(0)));
        MainApplication.getLayerManager().setActiveLayer(layer1);
        assertEquals(1, model.getSize());
        assertEquals(objects.get(0), model.getElementAt(0));

        // switch to a "null" edit layer (i.e. no edit layer)- nothing should
        // be displayed in the selection list
        MainApplication.getLayerManager().removeLayer(layer2);
        MainApplication.getLayerManager().removeLayer(layer1);
        assertEquals(0, model.getSize());

        MainApplication.getLayerManager().removeActiveLayerChangeListener(model);
    }
}
