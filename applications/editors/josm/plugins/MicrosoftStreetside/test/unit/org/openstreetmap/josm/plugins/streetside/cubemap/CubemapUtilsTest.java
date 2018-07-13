package org.openstreetmap.josm.plugins.streetside.cubemap;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class CubemapUtilsTest {

  @SuppressWarnings("static-method")
  @Test
  public final void testConvertDecimal2Quaternary() {
   final long decimal0 = 680730040l;
   final long decimal1 = 680931568l;
   String res = CubemapUtils.convertDecimal2Quaternary(decimal0);
   assertEquals("220210301312320", res);
   res = CubemapUtils.convertDecimal2Quaternary(decimal1);
   assertEquals("220211203003300", res);
  }

  @SuppressWarnings("static-method")
  @Test
  public final void testConvertQuaternary2Decimal() {
    final String quadKey0 = "220210301312320";
    final String quadKey1 = "220211203003300";
    String res = CubemapUtils.convertQuaternary2Decimal(quadKey0);
    assertEquals("680730040", res);
    res = CubemapUtils.convertQuaternary2Decimal(quadKey1);
    assertEquals("680931568", res);
  }

  @SuppressWarnings("static-method")
  @Test
  public final void testGetFaceNumberForCount() {
    String faceNrFront = CubemapUtils.getFaceNumberForCount(0);
    String faceNrRight = CubemapUtils.getFaceNumberForCount(1);
    String faceNrBack = CubemapUtils.getFaceNumberForCount(2);
    String faceNrLeft = CubemapUtils.getFaceNumberForCount(3);
    String faceNrUp = CubemapUtils.getFaceNumberForCount(4);
    String faceNrDown = CubemapUtils.getFaceNumberForCount(5);
    assertEquals(faceNrFront, "01");
    assertEquals(faceNrRight, "02");
    assertEquals(faceNrBack, "03");
    assertEquals(faceNrLeft, "10");
    assertEquals(faceNrUp, "11");
    assertEquals(faceNrDown, "12");
  }

  @SuppressWarnings("static-method")
  @Ignore
  @Test
  public final void testGetCount4FaceNumber() {
    int count4Front = CubemapUtils.getCount4FaceNumber("01");
    int count4Right = CubemapUtils.getCount4FaceNumber("02");
    int count4Back = CubemapUtils.getCount4FaceNumber("03");
    int count4Left = CubemapUtils.getCount4FaceNumber("10");
    int count4Up = CubemapUtils.getCount4FaceNumber("11");
    int count4Down = CubemapUtils.getCount4FaceNumber("12");
    assertEquals(count4Front, 0);
    assertEquals(count4Right, 1);
    assertEquals(count4Back, 2);
    assertEquals(count4Left, 3);
    assertEquals(count4Up, 4);
    assertEquals(count4Down, 5);
  }

  @SuppressWarnings("static-method")
  @Test
  public final void testConvertDoubleCountNrto16TileNr() {
    String x0y0 = CubemapUtils.convertDoubleCountNrto16TileNr("00");
    String x0y1 = CubemapUtils.convertDoubleCountNrto16TileNr("01");
    String x0y2 = CubemapUtils.convertDoubleCountNrto16TileNr("02");
    String x0y3 = CubemapUtils.convertDoubleCountNrto16TileNr("03");
    String x1y0 = CubemapUtils.convertDoubleCountNrto16TileNr("10");
    String x1y1 = CubemapUtils.convertDoubleCountNrto16TileNr("11");
    String x1y2 = CubemapUtils.convertDoubleCountNrto16TileNr("12");
    String x1y3 = CubemapUtils.convertDoubleCountNrto16TileNr("13");
    String x2y0 = CubemapUtils.convertDoubleCountNrto16TileNr("20");
    String x2y1 = CubemapUtils.convertDoubleCountNrto16TileNr("21");
    String x2y2 = CubemapUtils.convertDoubleCountNrto16TileNr("22");
    String x2y3 = CubemapUtils.convertDoubleCountNrto16TileNr("23");
    String x3y0 = CubemapUtils.convertDoubleCountNrto16TileNr("30");
    String x3y1 = CubemapUtils.convertDoubleCountNrto16TileNr("31");
    String x3y2 = CubemapUtils.convertDoubleCountNrto16TileNr("32");
    String x3y3 = CubemapUtils.convertDoubleCountNrto16TileNr("33");

    assertEquals(x0y0, "00");
    assertEquals(x0y1, "01");
    assertEquals(x0y2, "10");
    assertEquals(x0y3, "11");
    assertEquals(x1y0, "02");
    assertEquals(x1y1, "03");
    assertEquals(x1y2,"12");
    assertEquals(x1y3, "13");
    assertEquals(x2y0, "20");
    assertEquals(x2y1, "21");
    assertEquals(x2y2, "30");
    assertEquals(x2y3, "31");
    assertEquals(x3y0, "22");
    assertEquals(x3y1,"23");
    assertEquals(x3y2, "32");
    assertEquals(x3y3, "33");
  }

}
