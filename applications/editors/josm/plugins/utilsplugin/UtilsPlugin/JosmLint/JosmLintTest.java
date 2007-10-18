package UtilsPlugin.JosmLint;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

public abstract interface JosmLintTest {
        public JosmLintTestResult runTest( OsmPrimitive o );
}
