// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import javax.swing.JComponent;

import org.junit.Test;

public class StreetsideColorSchemeTest {

  @Test
  public void testUtilityClass() {
    TestUtil.testUtilityClass(StreetsideColorScheme.class);
  }

  @Test
  public void testStyleAsDefaultPanel() {
    StreetsideColorScheme.styleAsDefaultPanel();
    StreetsideColorScheme.styleAsDefaultPanel((JComponent[]) null);
  }
}
