package UtilsPlugin.JosmLint;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

public class ConsistancyTest implements JosmLintTest {
        private class NullSegmentResult extends JosmLintTestResult
        {
                public NullSegmentResult( Segment o, ConsistancyTest c )
                {
                        super( o, c );
                }
                public String toString()
                {
                        return "Null segment: "+ObjectDescr(obj);
                }
        }
        private class DuplicateSegmentResult extends JosmLintTestResult
        {
                private Segment seg;
                public DuplicateSegmentResult( Way o, ConsistancyTest c, Segment bad )
                {
                        super( o, c );
                        seg = bad;
                }
                public String toString()
                {
                        return "Duplicate segment: "+ObjectDescr(obj);
                }
                public void cloneFrom(DuplicateSegmentResult res)
                {
                        this.obj = res.obj;
                        this.test = res.test;
                        this.seg = res.seg;
                }
        }
        public ConsistancyTest() {}
        
        public JosmLintTestResult runTest( OsmPrimitive o )
        {
                if( o instanceof Node )
                        return null;
                if( o instanceof Segment )
                {
                        Segment s = (Segment)o;
                        if( !s.incomplete && s.from == s.to )
                                return new NullSegmentResult( s, this );
                }
                if( o instanceof Way )
                {
                        Way w = (Way)o;
                        for ( Segment s : w.segments )
                        {
                                if( w.segments.indexOf(s) != w.segments.lastIndexOf(s) )
                                        return new DuplicateSegmentResult( w, this, s );
                        }
                }
                return null;
        }
}
