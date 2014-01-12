package org.openstreetmap.josm.plugins.elevation.tests;



import junit.framework.TestCase;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.elevation.HgtReader;

public class HgtReaderTest extends TestCase {

    /**
     * Setup test.
     */
    public void setUp() {
        Main.pref = new Preferences();
    }

    public void testGetElevationFromHgt() {
	// Staufenberg, Hessen
	testHgtData(50.6607106, 8.7337029, "N50E008.hgt", 199);
	// Ulrichstein, Hessen
	testHgtData(50.5767627, 9.1938483, "N50E009.hgt", 560);
	// Fujijama
	//testHgtData(35.360555, 138.727777, "N35E138.hgt", 3741);
    }

    private void testHgtData(final double lat, final double lon,
	    final String expTag, final int expHeight) {
	LatLon l = new LatLon(lat, lon);
	HgtReader hr = new HgtReader();
	String text = hr.getHgtFileName(l);
	
	assertEquals(expTag, text);
	
	double d = hr.getElevationFromHgt(l);
	System.out.println(d);
	assertFalse("Data missing or void for coor " + l, Double.isNaN(d));
	
	assertEquals((int)d, expHeight);
    }
}
