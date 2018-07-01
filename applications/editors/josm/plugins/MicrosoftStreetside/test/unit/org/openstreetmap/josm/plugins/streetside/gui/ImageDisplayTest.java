// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui;

import static org.junit.Assert.assertEquals;

import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.plugins.streetside.utils.TestUtil.StreetsideTestRules;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Tests {@link StreetsideImageDisplay}
 */
public class ImageDisplayTest {

  @Rule
  public JOSMTestRules rules = new StreetsideTestRules().preferences();

  private static final BufferedImage DUMMY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

  @Ignore
  @Test
  public void testImagePersistence() {
    StreetsideImageDisplay display = new StreetsideImageDisplay();
    display.setImage(DUMMY_IMAGE, null);
    assertEquals(DUMMY_IMAGE, display.getImage());
  }

  /**
   * This test does not check if the scroll events result in the correct changes in the {@link StreetsideImageDisplay},
   * it only checks if the tested method runs through.
   */
  @Ignore
  @Test
  public void testMouseWheelMoved() {
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    StreetsideImageDisplay display = new StreetsideImageDisplay();
    final MouseWheelEvent dummyScroll = new MouseWheelEvent(display, 42, System.currentTimeMillis(), 0, 0, 0, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 3);
    display.getMouseWheelListeners()[0].mouseWheelMoved(dummyScroll);

    display.setImage(DUMMY_IMAGE, null);

    display.getMouseWheelListeners()[0].mouseWheelMoved(dummyScroll);

    // This is necessary to make the size of the component > 0. If you know a more elegant solution, feel free to change it.
    JFrame frame = new JFrame();
    frame.setSize(42, 42);
    frame.getContentPane().add(display);
    frame.pack();

    display.getMouseWheelListeners()[0].mouseWheelMoved(dummyScroll);
  }

  /**
   * This test does not check if the scroll events result in the correct changes in the {@link StreetsideImageDisplay},
   * it only checks if the tested method runs through.
   */
  @Ignore
  @Test
  public void testMouseClicked() {
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    for (int button = 1; button <= 3; button++) {
      StreetsideImageDisplay display = new StreetsideImageDisplay();
      final MouseEvent dummyClick = new MouseEvent(display, 42, System.currentTimeMillis(), 0, 0, 0, 1, false, button);
      display.getMouseListeners()[0].mouseClicked(dummyClick);

      display.setImage(DUMMY_IMAGE, null);

      display.getMouseListeners()[0].mouseClicked(dummyClick);

      // This is necessary to make the size of the component > 0. If you know a more elegant solution, feel free to change it.
      JFrame frame = new JFrame();
      frame.setSize(42, 42);
      frame.getContentPane().add(display);
      frame.pack();

      display.getMouseListeners()[0].mouseClicked(dummyClick);
    }
  }
}
