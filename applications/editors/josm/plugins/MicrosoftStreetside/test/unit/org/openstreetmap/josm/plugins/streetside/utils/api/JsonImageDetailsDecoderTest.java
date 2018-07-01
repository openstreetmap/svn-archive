// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils.api;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideData;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideImportedImage;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapUtils;
import org.openstreetmap.josm.plugins.streetside.utils.JsonUtil;
import org.openstreetmap.josm.plugins.streetside.utils.TestUtil;
import org.openstreetmap.josm.plugins.streetside.utils.TestUtil.StreetsideTestRules;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class JsonImageDetailsDecoderTest {

  @Rule
  public JOSMTestRules rules = new StreetsideTestRules().platform().preferences();

  @Test
  public void testUtilityClass() {
    TestUtil.testUtilityClass(JsonImageDetailsDecoder.class);
  }

  @Ignore
  @Test
  public void testDecodeImageInfos() {
    JsonObject searchImagesResponse = Json.createReader(
      JsonImageDetailsDecoderTest.class.getResourceAsStream("/api/v3/responses/searchImages.json")
    ).readObject();
    StreetsideData data = new StreetsideDataMock();
    StreetsideImage img1 = new StreetsideImage("_yA5uXuSNugmsK5VucU6Bg", new LatLon(0, 0), 0);
    StreetsideImage img2 = new StreetsideImage("nmF-Wq4EvVTgAUmBicSCCg", new LatLon(0, 0), 0);
    StreetsideImage img3 = new StreetsideImage("arbitrary_key", new LatLon(0, 0), 0);
    StreetsideAbstractImage img4 = new StreetsideImportedImage(CubemapUtils.IMPORTED_ID, new LatLon(0, 0), 0, null);
    img4.setHe(0);
    data.add(img1);
    data.add(img2);
    data.add(img3);
    data.add(img4);
    JsonImageDetailsDecoder.decodeImageInfos(searchImagesResponse, data);
    assertEquals(1_491_803_490_334L, img1.getHe()); // 2017-04-10T05:51:30.334Z
    assertEquals(1_491_803_486_853L, img2.getHe()); // 2017-04-10T05:51:26.853Z
    assertEquals(0L, img3.getHe());
    assertEquals(0L, img4.getHe());
  }

  @Test
  public void testInvalidImageInfos() {
    StreetsideDataMock data = new StreetsideDataMock();
    JsonImageDetailsDecoder.decodeImageInfos(null, data);
    JsonImageDetailsDecoder.decodeImageInfos(JsonUtil.string2jsonObject("{}"), null);
    JsonImageDetailsDecoder.decodeImageInfos(JsonUtil.string2jsonObject("{}"), data);
    JsonImageDetailsDecoder.decodeImageInfos(JsonUtil.string2jsonObject("{\"type\":\"FeatureCollection\", \"features\":0}"), data);
    JsonImageDetailsDecoder.decodeImageInfos(JsonUtil.string2jsonObject("{\"type\":\"FeatureCollection\", \"features\":[0, null]}"), data);
    assertEquals(0, data.getNumImageRerievals());
  }

  @Test
  public void testInvalidImageInfo() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method decodeImageInfo = JsonImageDetailsDecoder.class.getDeclaredMethod("decodeImageInfo", JsonObject.class, StreetsideData.class);
    StreetsideDataMock data = new StreetsideDataMock();
    decodeImageInfo.setAccessible(true);
    decodeImageInfo.invoke(null, null, data);
    decodeImageInfo.invoke(null, JsonUtil.string2jsonObject("{}"), null);
    decodeImageInfo.invoke(null, JsonUtil.string2jsonObject("{\"properties\":null}"), data);
    decodeImageInfo.invoke(null, JsonUtil.string2jsonObject("{\"properties\":{}}"), data);
    decodeImageInfo.invoke(null, JsonUtil.string2jsonObject("{\"properties\":{\"key\":\"arbitrary_key\"}}"), data);
    assertEquals(0, data.getNumImageRerievals());
  }

  private static class StreetsideDataMock extends StreetsideData {
    private int imageRetrievals;

    /**
     * Returns how often the method {@link #getImages()} has been accessed for this instance.
     * @return
     */
    public int getNumImageRerievals() {
      return imageRetrievals;
    }

    @Override
    public Set<StreetsideAbstractImage> getImages() {
      imageRetrievals++;
      return super.getImages();
    }
  }
}
