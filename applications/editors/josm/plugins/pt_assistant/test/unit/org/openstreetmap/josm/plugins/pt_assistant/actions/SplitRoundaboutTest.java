// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;

public class SplitRoundaboutTest extends AbstractTest {

    DataSet ds;
    OsmDataLayer layer;
    SplitRoundaboutAction action;
    SplitWayAction swa;
    Way r1, r2, r3;

    @Before
    public void init() throws FileNotFoundException, IllegalDataException {

        new JOSMFixture("../../JOSM/test/config/unit-josm.home").init(true);

        ds = OsmReader.parseDataSet(new FileInputStream(AbstractTest.PATH_TO_ROUNDABOUT), null);
        layer = new OsmDataLayer(ds, OsmDataLayer.createNewName(), null);
        Main.getLayerManager().addLayer(layer);

        Main.pref.put("pt_assistant.roundabout-splitter.alignalways", true);
        action = new SplitRoundaboutAction();
        swa = Main.main.menu.splitWay;
        swa.setEnabled(true);
        r1 = (Way) ds.getPrimitiveById(new SimplePrimitiveId(293302077L, OsmPrimitiveType.WAY));
        r2 = (Way) ds.getPrimitiveById(new SimplePrimitiveId(205833435L, OsmPrimitiveType.WAY));
        r3 = (Way) ds.getPrimitiveById(new SimplePrimitiveId(25739002L, OsmPrimitiveType.WAY));
    }

    @Test
    public void test1() {

        Map<Relation, Integer> savedPositions = action.getSavedPositions(r1);
        List<Node> splitNodes = action.getSplitNodes(r1);
        assertEquals(4, splitNodes.size());
        ds.setSelected(splitNodes);
        swa.actionPerformed(null);

        Collection<Way> splitWays = ds.getSelectedWays();
        action.updateRelations(savedPositions, splitNodes, splitWays);

        splitWays.forEach(w -> {
                if (w.firstNode().getUniqueId() == 267843779L && w.lastNode().getUniqueId() == 2968718407L)
                assertEquals(w.getReferrers().size(), 5);
            else if (w.firstNode().getUniqueId() == 2968718407L && w.lastNode().getUniqueId() == 2383688231L)
                assertEquals(w.getReferrers().size(), 0);
            else if (w.firstNode().getUniqueId() == 2383688231L && w.lastNode().getUniqueId() == 267843741L)
                assertEquals(w.getReferrers().size(), 5);
            else if (w.firstNode().getUniqueId() == 267843741L && w.lastNode().getUniqueId() == 267843779L)
                assertEquals(w.getReferrers().size(), 0);
            else
                fail();
        });
    }

     @Test
     public void test2() {
        Map<Relation, Integer> savedPositions = action.getSavedPositions(r2);
        List<Node> splitNodes = action.getSplitNodes(r2);
        assertEquals(4, splitNodes.size());
        ds.setSelected(splitNodes);
        swa.actionPerformed(null);

        Collection<Way> splitWays = ds.getSelectedWays();
        action.updateRelations(savedPositions, splitNodes, splitWays);

        splitWays.forEach(w -> {
            if(w.firstNode().getUniqueId() == 2158181809L && w.lastNode().getUniqueId() == 2158181798L)
                assertEquals(w.getReferrers().size(), 8);
            else if (w.firstNode().getUniqueId() == 2158181798L && w.lastNode().getUniqueId() == 2158181789L)
                assertEquals(w.getReferrers().size(), 0);
            else if (w.firstNode().getUniqueId() == 2158181789L && w.lastNode().getUniqueId() == 2158181803L)
                assertEquals(w.getReferrers().size(), 8);
            else if (w.firstNode().getUniqueId() == 2158181803L && w.lastNode().getUniqueId() == 2158181809L)
                assertEquals(w.getReferrers().size(), 0);
            else
                fail();
        });
     }

     @Test
     public void test3() {
         Map<Relation, Integer> savedPositions = action.getSavedPositions(r3);
        List<Node> splitNodes = action.getSplitNodes(r3);
        assertEquals(4, splitNodes.size());
        ds.setSelected(splitNodes);
        swa.actionPerformed(null);

        Collection<Way> splitWays = ds.getSelectedWays();
        action.updateRelations(savedPositions, splitNodes, splitWays);

        splitWays.forEach(w -> {
            if(w.firstNode().getUniqueId() == 280697532L && w.lastNode().getUniqueId() == 280697452L)
                assertEquals(w.getReferrers().size(), 0);
            else if (w.firstNode().getUniqueId() == 280697452L && w.lastNode().getUniqueId() == 280697591L)
                assertEquals(w.getReferrers().size(), 2);
            else if (w.firstNode().getUniqueId() == 280697591L && w.lastNode().getUniqueId() == 280697534L)
                assertEquals(w.getReferrers().size(), 0);
            else if (w.firstNode().getUniqueId() == 280697534L && w.lastNode().getUniqueId() == 280697532L)
                assertEquals(w.getReferrers().size(), 1);
            else
                fail();
        });
     }

    @After
    public void end() {
        Main.getLayerManager().removeLayer(layer);
    }
}
