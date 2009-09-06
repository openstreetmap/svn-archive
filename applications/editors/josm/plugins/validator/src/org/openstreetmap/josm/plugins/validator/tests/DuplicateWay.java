package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
/**
 * Tests if there are duplicate ways
 */
public class DuplicateWay extends Test
{

    private class WayPair {
        public List<LatLon> coor;
        public Map<String, String> keys;
        public WayPair(List<LatLon> _coor,Map<String, String> _keys) {
            coor=_coor;
            keys=_keys;
        }
        @Override
        public int hashCode() {
            return coor.hashCode()+keys.hashCode();
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof WayPair)) return false;
            WayPair wp = (WayPair) obj;
            return wp.coor.equals(coor) && wp.keys.equals(keys);
        }
    }

    protected static int DUPLICATE_WAY = 1401;

    /** Bag of all ways */
    Bag<WayPair, OsmPrimitive> ways;

    /**
     * Constructor
     */
    public DuplicateWay()
    {
        super(tr("Duplicated ways")+".",
              tr("This test checks that there are no ways with same tags and same node coordinates."));
    }


    @Override
    public void startTest()
    {
        ways = new Bag<WayPair, OsmPrimitive>(1000);
    }

    @Override
    public void endTest()
    {
        for(List<OsmPrimitive> duplicated : ways.values() )
        {
            if( duplicated.size() > 1)
            {
                TestError testError = new TestError(this, Severity.ERROR, tr("Duplicated ways"), DUPLICATE_WAY, duplicated);
                errors.add( testError );
            }
        }
        ways = null;
    }

    @Override
    public void visit(Way w)
    {
        if( !w.isUsable() )
            return;
        List<Node> wNodes=w.getNodes();
        Vector<LatLon> wLat=new Vector<LatLon>(wNodes.size());
        for(int i=0;i<wNodes.size();i++) {
                 wLat.add(wNodes.get(i).getCoor());
        }
        Map<String, String> wkeys=w.getKeys();
        wkeys.remove("created_by");
        WayPair wKey=new WayPair(wLat,wkeys);
        ways.add(wKey, w);
    }

    /**
     * Fix the error by removing all but one instance of duplicate ways
     */
    @Override
    public Command fixError(TestError testError)
    {
        Collection<? extends OsmPrimitive> sel = testError.getPrimitives();
        LinkedList<Way> ways = new LinkedList<Way>();

        for (OsmPrimitive osm : sel)
            if (osm instanceof Way)
                ways.add((Way)osm);

        if( ways.size() < 2 )
            return null;

        long idToKeep = 0;
        // Only one way will be kept - the one with lowest positive ID, if such exist
        // or one "at random" if no such exists. Rest of the ways will be deleted
        for (Way w: ways) {
            if (w.getId() > 0) {
                if (idToKeep == 0 || w.getId() < idToKeep) idToKeep = w.getId();
            }
        }

        if (idToKeep > 0) {
            //Remove chosen way from the list, rest of ways in the list will be deleted
            for (Way w: ways) {
                if (w.getId() == idToKeep) {
                        ways.remove(w);
                    break;
                }
            }
        } else {
            //Remove first way from the list, delete the rest
            ways.remove(0);
        }

        //Delete all ways in the list
        //Note: nodes are not deleted, these can be detected and deleted at next pass
        Collection<Command> commands = new LinkedList<Command>();
        commands.add(new DeleteCommand(ways));
        Main.main.undoRedo.add(new SequenceCommand(tr("Delete duplicate ways"), commands));
        return null;
    }

    @Override
    public boolean isFixable(TestError testError)
    {
        return (testError.getTester() instanceof DuplicateWay);
    }
}
