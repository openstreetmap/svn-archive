// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.streetside.utils.TestUtil.StreetsideTestRules;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class StreetsideAbstractImageTest {

  @Rule
  public JOSMTestRules rules = new StreetsideTestRules().platform();

  @Ignore
  @Test
  public void testIsModified() {
    StreetsideImage img = new StreetsideImage("key___________________", new LatLon(0, 0), 0);
    assertFalse(img.isModified());
    img.turn(1e-4);
    img.stopMoving();
    assertTrue(img.isModified());
    img.turn(-1e-4);
    img.stopMoving();
    assertFalse(img.isModified());
    img.move(1e-4, 1e-4);
    img.stopMoving();
    assertTrue(img.isModified());
    img.move(-1e-4, -1e-4);
    img.stopMoving();
    assertFalse(img.isModified());
  }
}
