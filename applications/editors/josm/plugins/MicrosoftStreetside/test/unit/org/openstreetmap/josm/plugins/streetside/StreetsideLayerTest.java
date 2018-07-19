// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapUtils;
import org.openstreetmap.josm.plugins.streetside.utils.TestUtil.StreetsideTestRules;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class StreetsideLayerTest {

  @Rule
  public JOSMTestRules rules = new StreetsideTestRules().main().preferences().projection();

  private static Layer getDummyLayer() {
    return ImageryLayer.create(new ImageryInfo("dummy", "https://example.org"));
  }

  @Test
  public void testGetIcon() {
    assertNotNull(StreetsideLayer.getInstance().getIcon());
  }

  @Test
  public void testIsMergable() {
    assertFalse(StreetsideLayer.getInstance().isMergable(getDummyLayer()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMergeFrom() {
    StreetsideLayer.getInstance().mergeFrom(getDummyLayer());
  }

  @Test
  public void testSetVisible() {
    StreetsideLayer.getInstance().getData().add(new StreetsideImage(CubemapUtils.TEST_IMAGE_ID, new LatLon(0.0, 0.0), 0.0));
    StreetsideLayer.getInstance().getData().add(new StreetsideImage(CubemapUtils.TEST_IMAGE_ID, new LatLon(0.0, 0.0), 0.0));
    StreetsideImage invisibleImage = new StreetsideImage(CubemapUtils.TEST_IMAGE_ID, new LatLon(0.0, 0.0), 0.0);
    invisibleImage.setVisible(false);
    StreetsideLayer.getInstance().getData().add(invisibleImage);

    StreetsideLayer.getInstance().setVisible(false);
    for (StreetsideAbstractImage img : StreetsideLayer.getInstance().getData().getImages()) {
      assertEquals(false, img.isVisible());
    }


    StreetsideLayer.getInstance().setVisible(true);
    for (StreetsideAbstractImage img : StreetsideLayer.getInstance().getData().getImages()) {
      assertEquals(true, img.isVisible());
    }
  }

  @Test
  public void testGetInfoComponent() {
    Object comp = StreetsideLayer.getInstance().getInfoComponent();
    assertTrue(comp instanceof String);
    assertTrue(((String) comp).length() >= 9);
  }

  @Test
  public void testClearInstance() {
    StreetsideLayer.getInstance();
    assertTrue(StreetsideLayer.hasInstance());
    JOSMTestRules.cleanLayerEnvironment();
    assertFalse(StreetsideLayer.hasInstance());
    StreetsideLayer.getInstance();
    assertTrue(StreetsideLayer.hasInstance());
  }
}
