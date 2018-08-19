// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import static org.junit.Assert.assertEquals;

import javax.swing.JOptionPane;

import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.testutils.mockers.JOptionPaneSimpleMocker;

/**
 * Tests {@link PluginState} class.
 *
 * @author nokutu
 * @see PluginState
 */
public class PluginStateTest {

  /**
   * Test the methods related to the download.
   */
  @Test
  public void downloadTest() {
    assertEquals(false, PluginState.isDownloading());
    PluginState.startDownload();
    assertEquals(true, PluginState.isDownloading());
    PluginState.startDownload();
    assertEquals(true, PluginState.isDownloading());
    PluginState.finishDownload();
    assertEquals(true, PluginState.isDownloading());
    PluginState.finishDownload();
    assertEquals(false, PluginState.isDownloading());
  }

  /**
   * Tests the methods related to the upload.
   */
  @Test
  public void uploadTest() {
    TestUtils.assumeWorkingJMockit();
    JOptionPaneSimpleMocker jopsMocker = new JOptionPaneSimpleMocker();
    jopsMocker.getMockResultMap().put(
            "You have successfully uploaded 2 images to Bing.com",
            JOptionPane.OK_OPTION
        );
    assertEquals(false, PluginState.isUploading());
    PluginState.addImagesToUpload(2);
    assertEquals(2, PluginState.getImagesToUpload());
    assertEquals(0, PluginState.getImagesUploaded());
    assertEquals(true, PluginState.isUploading());
    PluginState.imageUploaded();
    assertEquals(1, PluginState.getImagesUploaded());
    assertEquals(true, PluginState.isUploading());
    PluginState.imageUploaded();
    assertEquals(false, PluginState.isUploading());
    assertEquals(2, PluginState.getImagesToUpload());
    assertEquals(2, PluginState.getImagesUploaded());
  }
}
