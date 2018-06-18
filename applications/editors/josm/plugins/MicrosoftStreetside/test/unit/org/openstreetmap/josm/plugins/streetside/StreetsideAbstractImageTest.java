// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapUtils;
import org.openstreetmap.josm.plugins.streetside.utils.TestUtil.StreetsideTestRules;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class StreetsideAbstractImageTest {

  @Rule
  public JOSMTestRules rules = new StreetsideTestRules().platform();

  /**
   * Test method for {@link org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage#getDate()}.
   */
  @Ignore
  @Test
  public void testGetDate() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+0745"));

    StreetsideAbstractImage mai = new StreetsideImportedImage(CubemapUtils.IMPORTED_ID, new LatLon(0, 0), 0, null);
    //mai.setCapturedAt(1044087606000l); // in timezone GMT+0745 this is Saturday, February 1, 2003 16:05:06
    mai.setHe(1525381954996l); // in timezone PST this is Thu May 03 2018 14:12:34 GMT-0700 (PDT)


    testGetDate("05/03/2018", mai, false, false, false);
    testGetDate("05/03/2018", mai, false, false, true);
    testGetDate("05/03/2018 - 2:12:34 PM (GMT-07:00)", mai, false, true, false);
    testGetDate("05/03/2018 - 2:12:34 (GMT-07:00)", mai, false, true, true);
    testGetDate("2018-05-03", mai, true, false, false);
    testGetDate("2018-05-03", mai, true, false, true);
    testGetDate("2018-05-03 - 2:12:34 PM (GMT-07:00)", mai, true, true, false);
    testGetDate("2018-05-03 - 2:12:34 (GMT-07:00)", mai, true, true, true);

    TimeZone.setDefault(TimeZone.getTimeZone("GMT+0300"));
    //mai.setCapturedAt(1440671802000l); // in Timezone GMT-0123 this is Thursday, August 27, 2015 09:13:42 AM
    mai.setHe(1440671802000l); // in Timezone GMT-0123 this is Thursday, August 27, 2015 09:13:42 AM

    // TODO: fix testGetDate @rrh
    testGetDate("03/05/2018 - 04:57:34 (GMT+07:45)", mai, false, true, true);
  }

  private static void testGetDate(String expected, StreetsideAbstractImage img,
      boolean isoDates, boolean displayHour, boolean format24) {
    Config.getPref().putBoolean("iso.dates", isoDates);
    Config.getPref().putBoolean("streetside.display-hour", displayHour);
    Config.getPref().putBoolean("streetside.format-24", format24);
    assertEquals(expected, img.getDate());
  }

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
