package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 * Check coastlines for errors
 *
 * @author frsantos
 */
public class Coastlines extends Test
{
    protected static int UNORDERED_COASTLINES = 901;

    /** All ways, grouped by cells */
    Map<Point2D,List<Way>> _cellWays;
    /** The already detected errors */
    Bag<Way, Way> _errorWays;

    /**
     * Constructor
     */
    public Coastlines()
    {
        super(tr("Coastlines."),
              tr("This test checks that coastlines are correct."));
    }

    @Override
    public void startTest()
    {
        _cellWays = new HashMap<Point2D,List<Way>>(1000);
        _errorWays = new Bag<Way, Way>();
    }

    @Override
    public void endTest()
    {
        _cellWays = null;
        _errorWays = null;
    }

    @Override
    public void visit(Way w)
    {
        if( !w.isUsable() )
            return;

        String natural = w.get("natural");
        if( natural == null || !natural.equals("coastline") )
            return;

        List<List<Way>> cellWays = Util.getWaysInCell(w, _cellWays);
        for( List<Way> ways : cellWays)
        {
            for( Way w2 : ways)
            {
                if( _errorWays.contains(w, w2) || _errorWays.contains(w2, w) )
                    continue;

                String natural2 = w.get("natural");
                if( natural2 == null || !natural2.equals("coastline") )
                    continue;

                if( w.getNodes().get(0).equals(w2.getNodes().get(0)) || w.getNodes().get(w.getNodesCount() - 1).equals(w2.getNodes().get(w2.getNodesCount() - 1)))
                {
                    List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
                    primitives.add(w);
                    primitives.add(w2);
                    errors.add( new TestError(this, Severity.ERROR, tr("Unordered coastline"), UNORDERED_COASTLINES, primitives) );
                    _errorWays.add(w, w2);
                }
            }
            ways.add(w);
        }
    }
}
