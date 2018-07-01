// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import org.junit.Rule;
import org.junit.Test;

import org.openstreetmap.josm.plugins.streetside.utils.TestUtil.StreetsideTestRules;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class StreetsidePropertiesTest {

  @Rule
  public JOSMTestRules rules = new StreetsideTestRules();

  @Test
  public void test() {
    TestUtil.testUtilityClass(StreetsideProperties.class);
  }

}
