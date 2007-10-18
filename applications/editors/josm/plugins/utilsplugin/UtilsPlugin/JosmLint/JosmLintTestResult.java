package UtilsPlugin.JosmLint;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.data.osm.Way;

public class JosmLintTestResult
{
        protected OsmPrimitive obj;
        protected JosmLintTest test;
        
        public JosmLintTestResult( OsmPrimitive o, JosmLintTest t )
        {
                this.obj = o;
                this.test = t;
        }
        public boolean recheck()
        {
                JosmLintTestResult newres = test.runTest(obj);
                if( newres == null )
                        return false;
                this.cloneFrom(newres);  /* Pick up changes if any */
                return true;
        }
        public void cloneFrom(JosmLintTestResult res)
        {
                this.obj = res.obj;
                this.test = res.test;
        }
        public static String ObjectDescr(OsmPrimitive o)
        {
                String str;
                if( o instanceof Node )
                {
                        str = "Node "+o.id;
                }
                else if( o instanceof Segment )
                {
                        str = "Segment "+o.id;
                }
                else if( o instanceof Way )
                {
                        str = "Way "+o.id;
                }
                else
                {
                        str = "Unknown object";
                }
                return str;
        }
        // Return object to select when doubleclicked
        public OsmPrimitive getSelection()
        {
                return obj;
        }
}
