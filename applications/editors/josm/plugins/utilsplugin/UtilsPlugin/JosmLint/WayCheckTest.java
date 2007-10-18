package UtilsPlugin.JosmLint;

import java.util.Collection;
import java.util.ArrayList;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

public class WayCheckTest implements JosmLintTest {
        private class DiscontinuousWayResult extends JosmLintTestResult
        {
                public DiscontinuousWayResult( Way o, WayCheckTest c )
                {
                        super( o, c );
                }
                public String toString()
                {
                        return "Discontinuous way: "+ObjectDescr(obj);
                }
        }
        private class NullWayResult extends JosmLintTestResult
        {
                public NullWayResult( Way o, WayCheckTest c )
                {
                        super( o, c );
                }
                public String toString()
                {
                        return "Null way: "+ObjectDescr(obj);
                }
        }
        private class UnorderedWayResult extends JosmLintTestResult
        {
                private Segment seg;
                public UnorderedWayResult( Way o, WayCheckTest c )
                {
                        super( o, c );
                }
                public String toString()
                {
                        return "Unordered way: "+ObjectDescr(obj);
                }
        }
        public WayCheckTest() {}
        
        public JosmLintTestResult runTest( OsmPrimitive o )
        {
                if( o instanceof Node )
                        return null;
                if( o instanceof Segment )
                        return null;
                if( o instanceof Way )
                {
                        Way w = (Way)o;
                        if( w.isIncomplete() )
                                return null;
                        if( w.segments.size() == 0 )
                                return new NullWayResult( w, this );
                        boolean unordered = false;
                        ArrayList<Segment> unused = new ArrayList<Segment>(w.segments);
                        Segment s1 = unused.get(0);
                        unused.remove(s1);
                        Node start = s1.from;
                        Node end = s1.to;
                        boolean change = true;
                        while(change)
                        {
                                change = false;
                                ArrayList<Segment> temp = new ArrayList<Segment>(unused);
                                for ( Segment s : temp )
                                {
                                        if( s.from == s.to )
                                        {
                                                unused.remove(s);
                                                change = true;
                                                continue;
                                        }
                                        if( s.from == end )
                                        {
                                                end = s.to;
                                                change = true;
                                                unused.remove(s);
                                        }
                                        else if( s.to == start )
                                        {
                                                start = s.from;
                                                unused.remove(s);
                                                change = true;
                                                unordered = true;
                                        }
                                }
                        }
                        if( unused.size() != 0 )
                        {
                                return new DiscontinuousWayResult( w, this );
                        }
                        if( unordered )
                        {
                                return new UnorderedWayResult( w, this );
                        }
                        return null;
                }
                return null;
        }
}
