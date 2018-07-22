// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Tests for {@link StreetsideData} class.
 *
 * @author nokutu
 * @see StreetsideData
 */
public class StreetsideDataTest {

  /*@Rule
  public JOSMTestRules rules = new StreetsideTestRules().platform();*/

  private StreetsideData data;
  private StreetsideImage img1;
  private StreetsideImage img2;
  private StreetsideImage img3;
  private StreetsideImage img4;

  /**
   * Creates a sample {@link StreetsideData} objects, 4 {@link StreetsideImage}
   * objects and a {@link StreetsideSequence} object.
   */
  @Before
  public void setUp() {
    img1 = new StreetsideImage("id1__________________", new LatLon(0.1, 0.1), 90);
    img2 = new StreetsideImage("id2__________________", new LatLon(0.2, 0.2), 90);
    img3 = new StreetsideImage("id3__________________", new LatLon(0.3, 0.3), 90);
    img4 = new StreetsideImage("id4__________________", new LatLon(0.4, 0.4), 90);
    final StreetsideSequence seq = new StreetsideSequence();

    seq.add(Arrays.asList(img1, img2, img3, img4));

    data = new StreetsideData();
    data.addAll(new ConcurrentSkipListSet<>(seq.getImages()));
  }

  /**
   * Tests the addition of new images. If a second image with the same key as
   * another one in the database, the one that is being added should be ignored.
   */
  @Test
  public void addTest() {
    data = new StreetsideData();
    assertEquals(0, data.getImages().size());
    data.add(img1);
    assertEquals(1, data.getImages().size());
    data.add(img1);
    assertEquals(1, data.getImages().size());
    data.addAll(new ConcurrentSkipListSet<>(Arrays.asList(img2, img3)));
    assertEquals(3, data.getImages().size());
    data.addAll(new ConcurrentSkipListSet<>(Arrays.asList(img3, img4)));
    assertEquals(4, data.getImages().size());
  }

  /**
   * Test that the size is properly calculated.
   */
  @Test
  public void sizeTest() {
    assertEquals(4, data.getImages().size());
    data.add(new StreetsideImage("id5__________________", new LatLon(0.1, 0.1), 90));
    assertEquals(5, data.getImages().size());
  }

  /**
   * Test the {@link StreetsideData#setHighlightedImage(StreetsideAbstractImage)}
   * and {@link StreetsideData#getHighlightedImage()} methods.
   */
  @Test
  public void highlighTest() {
    data.setHighlightedImage(img1);
    assertEquals(img1, data.getHighlightedImage());

    data.setHighlightedImage(null);
    assertEquals(null, data.getHighlightedImage());
  }

  /**
   * Tests the selection of images.
   */
  @Test
  public void selectTest() {
    data.setSelectedImage(img1);
    assertEquals(img1, data.getSelectedImage());

    data.setSelectedImage(img4);
    assertEquals(img4, data.getSelectedImage());

    data.setSelectedImage(null);
    assertEquals(null, data.getSelectedImage());
  }

  /**
   * Tests the {@link StreetsideData#selectNext()} and
   * {@link StreetsideData#selectPrevious()} methods.
   */
  @Ignore
  @Test
  public void nextAndPreviousTest() {
    data.setSelectedImage(img1);

    data.selectNext();
    assertEquals(img2, data.getSelectedImage());
    data.selectNext();
    assertEquals(img3, data.getSelectedImage());
    data.selectPrevious();
    assertEquals(img2, data.getSelectedImage());

    data.setSelectedImage(null);
  }

  @Ignore
  @Test(expected=IllegalStateException.class)
  public void nextOfNullImgTest() {
    data.setSelectedImage(null);
    data.selectNext();
  }

  @Ignore
  @Test(expected=IllegalStateException.class)
  public void previousOfNullImgTest() {
    data.setSelectedImage(null);
    data.selectPrevious();
  }

  /**
   * Test the multiselection of images. When a new image is selected, the
   * multiselected List should reset.
   */
  @Ignore
  @Test
  public void multiSelectTest() {
    assertEquals(0, data.getMultiSelectedImages().size());
    data.setSelectedImage(img1);
    assertEquals(1, data.getMultiSelectedImages().size());
    data.addMultiSelectedImage(img2);
    assertEquals(2, data.getMultiSelectedImages().size());
    data.setSelectedImage(img1);
    assertEquals(1, data.getMultiSelectedImages().size());
  }
}
