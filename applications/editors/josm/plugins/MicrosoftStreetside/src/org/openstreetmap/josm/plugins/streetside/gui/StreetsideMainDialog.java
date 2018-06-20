// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.cache.CacheEntry;
import org.openstreetmap.josm.data.cache.CacheEntryAttributes;
import org.openstreetmap.josm.data.cache.ICachedLoaderListener;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideImportedImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.StreetsidePlugin;
import org.openstreetmap.josm.plugins.streetside.actions.WalkListener;
import org.openstreetmap.josm.plugins.streetside.actions.WalkThread;
import org.openstreetmap.josm.plugins.streetside.cache.StreetsideCache;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.ImageInfoHelpPopup;
import org.openstreetmap.josm.plugins.streetside.model.UserProfile;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

/**
 * Toggle dialog that shows an image and some buttons.
 *
 * @author nokutu
 */
public final class StreetsideMainDialog extends ToggleDialog implements
        ICachedLoaderListener, StreetsideDataListener {

  private static final long serialVersionUID = 6856496736429480600L;

  private static final String BASE_TITLE = marktr("Streetside image");
  private static final String MESSAGE_SEPARATOR = " — ";

  private static StreetsideMainDialog instance;

  private volatile StreetsideAbstractImage image;

  private final SideButton nextButton = new SideButton(new NextPictureAction());
  private final SideButton previousButton = new SideButton(new PreviousPictureAction());
  /**
   * Button used to jump to the image following the red line
   */
  public final SideButton redButton = new SideButton(new RedAction());
  /**
   * Button used to jump to the image following the blue line
   */
  public final SideButton blueButton = new SideButton(new BlueAction());

  private final SideButton playButton = new SideButton(new PlayAction());
  private final SideButton pauseButton = new SideButton(new PauseAction());
  private final SideButton stopButton = new SideButton(new StopAction());

  private ImageInfoHelpPopup imageInfoHelp;

  /**
   * Buttons mode.
   *
   * @author nokutu
   */
  public enum MODE {
    /**
     * Standard mode to view pictures.
     */
    NORMAL,
    /**
     * Mode when in walk.
     */
    WALK
  }

  /**
   * Object containing the shown image and that handles zoom and drag
   */
  public final StreetsideImageDisplay streetsideImageDisplay;

  private StreetsideCache imageCache;
  private StreetsideCache thumbnailCache;

  private StreetsideMainDialog() {
    super(tr(BASE_TITLE), "streetside-main", tr("Open Streetside window"), null, 200,
        true, StreetsidePreferenceSetting.class);
    addShortcuts();
    streetsideImageDisplay = new StreetsideImageDisplay();

    blueButton.setForeground(Color.BLUE);
    redButton.setForeground(Color.RED);

    setMode(MODE.NORMAL);
  }

  /**
   * Adds the shortcuts to the buttons.
   */
  private void addShortcuts() {
    nextButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke("PAGE_DOWN"), "next");
    nextButton.getActionMap().put("next", new NextPictureAction());
    previousButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke("PAGE_UP"), "previous");
    previousButton.getActionMap().put("previous",
            new PreviousPictureAction());
    blueButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke("control PAGE_UP"), "blue");
    blueButton.getActionMap().put("blue", new BlueAction());
    redButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke("control PAGE_DOWN"), "red");
    redButton.getActionMap().put("red", new RedAction());
  }

  /**
   * Returns the unique instance of the class.
   *
   * @return The unique instance of the class.
   */
  public static synchronized StreetsideMainDialog getInstance() {
    if (instance == null)
      instance = new StreetsideMainDialog();
    return instance;
  }

  /**
   * @return true, iff the singleton instance is present
   */
  public static boolean hasInstance() {
    return instance != null;
  }

  public synchronized void setImageInfoHelp(ImageInfoHelpPopup popup) {
    imageInfoHelp = popup;
  }

  /**
   * Sets a new mode for the dialog.
   *
   * @param mode The mode to be set. Must not be {@code null}.
   */
  public void setMode(MODE mode) {
    switch (mode) {
      case WALK:
        createLayout(
          streetsideImageDisplay,
          Arrays.asList(playButton, pauseButton, stopButton)
        );
        break;
      case NORMAL:
      default:
        createLayout(
          streetsideImageDisplay,
          Arrays.asList(blueButton, previousButton, nextButton, redButton)
        );
        break;
    }
    disableAllButtons();
    if (MODE.NORMAL.equals(mode)) {
      updateImage();
    }
    revalidate();
    repaint();
  }

  /**
   * Destroys the unique instance of the class.
   */
  public static synchronized void destroyInstance() {
    instance = null;
  }

  /**
   * Downloads the full quality picture of the selected StreetsideImage and sets
   * in the StreetsideImageDisplay object.
   */
  public synchronized void updateImage() {
    updateImage(true);
  }

  /**
   * Downloads the picture of the selected StreetsideImage and sets in the
   * StreetsideImageDisplay object.
   *
   * @param fullQuality If the full quality picture must be downloaded or just the
   *                    thumbnail.
   */
  public synchronized void updateImage(boolean fullQuality) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::updateImage);
    } else {
      if (!StreetsideLayer.hasInstance()) {
        return;
      }
      if (image == null) {
        streetsideImageDisplay.setImage(null, null);
        setTitle(tr(BASE_TITLE));
        disableAllButtons();
        return;
      }

      if (imageInfoHelp != null && StreetsideProperties.IMAGEINFO_HELP_COUNTDOWN.get() > 0 && imageInfoHelp.showPopup()) {
        // Count down the number of times the popup will be displayed
        StreetsideProperties.IMAGEINFO_HELP_COUNTDOWN.put(StreetsideProperties.IMAGEINFO_HELP_COUNTDOWN.get() - 1);
      }

      // Enables/disables next/previous buttons
      nextButton.setEnabled(false);
      previousButton.setEnabled(false);
      if (image.getSequence() != null) {
        StreetsideAbstractImage tempImage = image;
        while (tempImage.next() != null) {
          tempImage = tempImage.next();
          if (tempImage.isVisible()) {
            nextButton.setEnabled(true);
            break;
          }
        }
      }
      if (image.getSequence() != null) {
        StreetsideAbstractImage tempImage = image;
        while (tempImage.previous() != null) {
          tempImage = tempImage.previous();
          if (tempImage.isVisible()) {
            previousButton.setEnabled(true);
            break;
          }
        }
      }
      if (image instanceof StreetsideImage) {
        StreetsideImage streetsideImage = (StreetsideImage) image;
        // Downloads the thumbnail.
        streetsideImageDisplay.setImage(null, null);
        if (thumbnailCache != null)
          thumbnailCache.cancelOutstandingTasks();
        thumbnailCache = new StreetsideCache(streetsideImage.getId(),
                StreetsideCache.Type.THUMBNAIL);
        try {
          thumbnailCache.submit(this, false);
        } catch (IOException e) {
          Logging.error(e);
        }

        // Downloads the full resolution image.
        if (fullQuality || new StreetsideCache(streetsideImage.getId(),
                StreetsideCache.Type.FULL_IMAGE).get() != null) {
          if (imageCache != null)
            imageCache.cancelOutstandingTasks();
          imageCache = new StreetsideCache(streetsideImage.getId(),
                  StreetsideCache.Type.FULL_IMAGE);
          try {
            imageCache.submit(this, false);
          } catch (IOException e) {
            Logging.error(e);
          }
        }
      } else if (image instanceof StreetsideImportedImage) {
        StreetsideImportedImage streetsideImage = (StreetsideImportedImage) image;
        try {
          streetsideImageDisplay.setImage(streetsideImage.getImage(), null);
        } catch (IOException e) {
          Logging.error(e);
        }
      }
      updateTitle();
    }

  }

  /**
   * Disables all the buttons in the dialog
   */
  private void disableAllButtons() {
    nextButton.setEnabled(false);
    previousButton.setEnabled(false);
    blueButton.setEnabled(false);
    redButton.setEnabled(false);
  }

  /**
   * Sets a new StreetsideImage to be shown.
   *
   * @param image The image to be shown.
   */
  public synchronized void setImage(StreetsideAbstractImage image) {
    this.image = image;
  }

  /**
   * Updates the title of the dialog.
   */
  public synchronized void updateTitle() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::updateTitle);
    } else if (image != null) {
      StringBuilder title = new StringBuilder(tr(BASE_TITLE));
      if (image instanceof StreetsideImage) {
        StreetsideImage streetsideImage = (StreetsideImage) image;
        UserProfile user = streetsideImage.getUser();
        if (user != null) {
          title.append(MESSAGE_SEPARATOR).append(user.getUsername());
        }
        if (streetsideImage.getCd() != 0) {
          title.append(MESSAGE_SEPARATOR).append(streetsideImage.getDate());
        }
        setTitle(title.toString());
      } else if (image instanceof StreetsideImportedImage) {
        StreetsideImportedImage streetsideImportedImage = (StreetsideImportedImage) image;
        title.append(MESSAGE_SEPARATOR).append(streetsideImportedImage.getFile().getName());
        title.append(MESSAGE_SEPARATOR).append(streetsideImportedImage.getDate());
        setTitle(title.toString());
      }
    }
  }

  /**
   * Returns the {@link StreetsideAbstractImage} object which is being shown.
   *
   * @return The {@link StreetsideAbstractImage} object which is being shown.
   */
  public synchronized StreetsideAbstractImage getImage() {
    return image;
  }

  /**
   * Action class form the next image button.
   *
   * @author nokutu
   */
  private static class NextPictureAction extends AbstractAction {

    private static final long serialVersionUID = 3023827221453154340L;

    /**
     * Constructs a normal NextPictureAction
     */
    NextPictureAction() {
      super(tr("Next picture"));
      putValue(SHORT_DESCRIPTION, tr("Shows the next picture in the sequence"));
      new ImageProvider("dialogs", "next").getResource().attachImageIcon(this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      StreetsideLayer.getInstance().getData().selectNext();
    }
  }

  /**
   * Action class for the previous image button.
   *
   * @author nokutu
   */
  private static class PreviousPictureAction extends AbstractAction {

    private static final long serialVersionUID = -6420511632957956012L;

    /**
     * Constructs a normal PreviousPictureAction
     */
    PreviousPictureAction() {
      super(tr("Previous picture"));
      putValue(SHORT_DESCRIPTION, tr("Shows the previous picture in the sequence"));
      new ImageProvider("dialogs", "previous").getResource().attachImageIcon(this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      StreetsideLayer.getInstance().getData().selectPrevious();
    }
  }

  /**
   * Action class to jump to the image following the red line.
   *
   * @author nokutu
   */
  private static class RedAction extends AbstractAction {

    private static final long serialVersionUID = -6480229431481386376L;

    /**
     * Constructs a normal RedAction
     */
    RedAction() {
      putValue(NAME, tr("Jump to red"));
      putValue(SHORT_DESCRIPTION,
              tr("Jumps to the picture at the other side of the red line"));
      new ImageProvider("dialogs", "red").getResource().attachImageIcon(this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (StreetsideMainDialog.getInstance().getImage() != null) {
        StreetsideLayer.getInstance().getData()
                .setSelectedImage(StreetsideLayer.getInstance().getNNearestImage(1), true);
      }
    }
  }

  /**
   * Action class to jump to the image following the blue line.
   *
   * @author nokutu
   */
  private static class BlueAction extends AbstractAction {

    private static final long serialVersionUID = 6250690644594703314L;

    /**
     * Constructs a normal BlueAction
     */
    BlueAction() {
      putValue(NAME, tr("Jump to blue"));
      putValue(SHORT_DESCRIPTION,
              tr("Jumps to the picture at the other side of the blue line"));
      new ImageProvider("dialogs", "blue").getResource().attachImageIcon(this, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (StreetsideMainDialog.getInstance().getImage() != null) {
        StreetsideLayer.getInstance().getData()
                .setSelectedImage(StreetsideLayer.getInstance().getNNearestImage(2), true);
      }
    }
  }

  private static class StopAction extends AbstractAction implements WalkListener {

    private static final long serialVersionUID = -6561451575815789198L;

    private WalkThread thread;

    /**
     * Constructs a normal StopAction
     */
    StopAction() {
      putValue(NAME, trc("as synonym to halt or stand still", "Stop"));
      putValue(SHORT_DESCRIPTION, tr("Stops the walk."));
      new ImageProvider("dialogs/streetsideStop.png").getResource().attachImageIcon(this, true);
      StreetsidePlugin.getWalkAction().addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (thread != null)
        thread.stopWalk();
    }

    @Override
    public void walkStarted(WalkThread thread) {
      this.thread = thread;
    }
  }

  private static class PlayAction extends AbstractAction implements WalkListener {

    private static final long serialVersionUID = -17943404752082788L;
    private transient WalkThread thread;

    /**
     * Constructs a normal PlayAction
     */
    PlayAction() {
      putValue(NAME, tr("Play"));
      putValue(SHORT_DESCRIPTION, tr("Continues with the paused walk."));
      new ImageProvider("dialogs/streetsidePlay.png").getResource().attachImageIcon(this, true);
      StreetsidePlugin.getWalkAction().addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (thread != null)
        thread.play();
    }

    @Override
    public void walkStarted(WalkThread thread) {
      if (thread != null)
        this.thread = thread;
    }
  }

  private static class PauseAction extends AbstractAction implements WalkListener {

    private static final long serialVersionUID = 4400240686337741192L;

    private WalkThread thread;

    /**
     * Constructs a normal PauseAction
     */
    PauseAction() {
      putValue(NAME, tr("Pause"));
      putValue(SHORT_DESCRIPTION, tr("Pauses the walk."));
      new ImageProvider("dialogs/streetsidePause.png").getResource().attachImageIcon(this, true);
      StreetsidePlugin.getWalkAction().addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      thread.pause();
    }

    @Override
    public void walkStarted(WalkThread thread) {
      this.thread = thread;
    }
  }

  /**
   * When the pictures are returned from the cache, they are set in the
   * {@link StreetsideImageDisplay} object.
   */
  /*@Override
  public void loadingFinished(final CacheEntry data, final CacheEntryAttributes attributes, final LoadResult result) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> loadingFinished(data, attributes, result));
    } else if (data != null && result == LoadResult.SUCCESS) {
      try {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data.getContent()));
        if (img == null) {
          return;
        }
        if (
          streetsideImageDisplay.getImage() == null
          || img.getHeight() > this.streetsideImageDisplay.getImage().getHeight()
        ) {
          final StreetsideAbstractImage mai = getImage();
          this.streetsideImageDisplay.setImage(
            img,
            mai instanceof StreetsideImage ? ((StreetsideImage) getImage()).getDetections() : null
          );
        }
      } catch (IOException e) {
        Logging.error(e);
      }
    }
  }*/

  /**
   * When the pictures are returned from the cache, they are set in the
   * {@link StreetsideImageDisplay} object.
   */
  @Override
  public void loadingFinished(final CacheEntry data, final CacheEntryAttributes attributes, final LoadResult result) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> loadingFinished(data, attributes, result));

    } else if (data != null && result == LoadResult.SUCCESS) {
      try {
        final BufferedImage img = ImageIO.read(new ByteArrayInputStream(data.getContent()));
        if (img == null) {
          return;
        }
        if (
            streetsideImageDisplay.getImage() == null
            || img.getHeight() > streetsideImageDisplay.getImage().getHeight()
            ) {
          //final StreetsideAbstractImage mai = getImage();
          streetsideImageDisplay.setImage(
              img,
              //mai instanceof StreetsideImage ? ((StreetsideImage) getImage()).getDetections() : null
              null);
        }
      } catch (final IOException e) {
        Logging.error(e);
      }
    }
  }

  /**
   * Creates the layout of the dialog.
   *
   * @param data    The content of the dialog
   * @param buttons The buttons where you can click
   */
  public void createLayout(Component data, List<SideButton> buttons) {
    removeAll();
    createLayout(data, true, buttons);
    add(titleBar, BorderLayout.NORTH);
  }

  @Override
  public void selectedImageChanged(StreetsideAbstractImage oldImage, StreetsideAbstractImage newImage) {
    setImage(newImage);
    updateImage();
  }

  @Override
  public void imagesAdded() {
    // This method is enforced by StreetsideDataListener, but only selectedImageChanged() is needed
  }

}
