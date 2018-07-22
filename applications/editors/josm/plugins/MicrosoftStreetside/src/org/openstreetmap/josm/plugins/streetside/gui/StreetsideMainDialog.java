// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.cache.CacheEntry;
import org.openstreetmap.josm.data.cache.CacheEntryAttributes;
import org.openstreetmap.josm.data.cache.ICachedLoaderListener;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.StreetsidePlugin;
import org.openstreetmap.josm.plugins.streetside.actions.WalkListener;
import org.openstreetmap.josm.plugins.streetside.actions.WalkThread;
import org.openstreetmap.josm.plugins.streetside.cache.StreetsideCache;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.ImageInfoHelpPopup;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Toggle dialog that shows an image and some buttons.
 *
 * @author nokutu
 * @author renerr18
 */
public final class StreetsideMainDialog extends ToggleDialog implements
		ICachedLoaderListener, StreetsideDataListener {

	private static final long serialVersionUID = 2645654786827812861L;

  final static Logger logger = Logger.getLogger(StreetsideMainDialog.class);

  public static final String BASE_TITLE = I18n.marktr("Microsoft Streetside image");

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
	public StreetsideImageDisplay streetsideImageDisplay;

	private StreetsideCache imageCache;
	public StreetsideCache thumbnailCache;

	private StreetsideMainDialog() {
		super(I18n.tr(StreetsideMainDialog.BASE_TITLE), "streetside-main", I18n.tr("Open Streetside window"), null, 200,
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
    nextButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("PAGE_DOWN"), "next");
    nextButton.getActionMap().put("next", new NextPictureAction());
    previousButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("PAGE_UP"), "previous");
    previousButton.getActionMap().put("previous", new PreviousPictureAction());
    blueButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control PAGE_UP"), "blue");
    blueButton.getActionMap().put("blue", new BlueAction());
    redButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control PAGE_DOWN"), "red");
    redButton.getActionMap().put("red", new RedAction());
  }

	/**
	 * Returns the unique instance of the class.
	 *
	 * @return The unique instance of the class.
	 */
	public static synchronized StreetsideMainDialog getInstance() {
		if (StreetsideMainDialog.instance == null) {
			StreetsideMainDialog.instance = new StreetsideMainDialog();
		}
		return StreetsideMainDialog.instance;
	}

	/**
	 * @return true, iff the singleton instance is present
	 */
	public static boolean hasInstance() {
		return StreetsideMainDialog.instance != null;
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
		StreetsideMainDialog.instance = null;
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
				setTitle(I18n.tr(StreetsideMainDialog.BASE_TITLE));
				return;
			}

			if (imageInfoHelp != null && StreetsideProperties.IMAGEINFO_HELP_COUNTDOWN.get() > 0 && imageInfoHelp.showPopup()) {
				// Count down the number of times the popup will be displayed
				StreetsideProperties.IMAGEINFO_HELP_COUNTDOWN.put(StreetsideProperties.IMAGEINFO_HELP_COUNTDOWN.get() - 1);
			}

			if (image instanceof StreetsideImage) {
				final StreetsideImage streetsideImage = (StreetsideImage) image;
				// Downloads the thumbnail.
				streetsideImageDisplay.setImage(null, null);
				if (thumbnailCache != null) {
					thumbnailCache.cancelOutstandingTasks();
				}
				thumbnailCache = new StreetsideCache(streetsideImage.getId(),
						StreetsideCache.Type.THUMBNAIL);
				try {
					thumbnailCache.submit(this, false);
				} catch (final IOException e) {
					logger.error(e);
				}

				// Downloads the full resolution image.
				if (fullQuality || new StreetsideCache(streetsideImage.getId(),
						StreetsideCache.Type.FULL_IMAGE).get() != null) {
					if (imageCache != null) {
						imageCache.cancelOutstandingTasks();
					}
					imageCache = new StreetsideCache(streetsideImage.getId(),
							StreetsideCache.Type.FULL_IMAGE);
					try {
						imageCache.submit(this, false);
					} catch (final IOException e) {
						logger.error(e);
					}
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
			final StringBuilder title = new StringBuilder(I18n.tr(StreetsideMainDialog.BASE_TITLE));
			if (image instanceof StreetsideImage) {
				title.append(StreetsideMainDialog.MESSAGE_SEPARATOR).append(MessageFormat.format("(heading {0}°)",Double.toString(image.getHe())));
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

		private static final long serialVersionUID = 6333692154558730392L;

		/**
		 * Constructs a normal NextPictureAction
		 */
		NextPictureAction() {
			super(I18n.tr("Next picture"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Shows the next picture in the sequence"));
			new ImageProvider("help", "next").getResource().attachImageIcon(this, true);
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

		private static final long serialVersionUID = 4390593660514657107L;

		/**
		 * Constructs a normal PreviousPictureAction
		 */
		PreviousPictureAction() {
			super(I18n.tr("Previous picture"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Shows the previous picture in the sequence"));
			new ImageProvider("help", "previous").getResource().attachImageIcon(this, true);
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

		private static final long serialVersionUID = -1244456062285831231L;

		/**
		 * Constructs a normal RedAction
		 */
		RedAction() {
			putValue(Action.NAME, I18n.tr("Jump to red"));
			putValue(Action.SHORT_DESCRIPTION,
					I18n.tr("Jumps to the picture at the other side of the red line"));
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

		private static final long serialVersionUID = 5951233534212838780L;

		/**
		 * Constructs a normal BlueAction
		 */
		BlueAction() {
			putValue(Action.NAME, I18n.tr("Jump to blue"));
			putValue(Action.SHORT_DESCRIPTION,
					I18n.tr("Jumps to the picture at the other side of the blue line"));
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

		private static final long serialVersionUID = 8789972456611625341L;

		private WalkThread thread;

		/**
		 * Constructs a normal StopAction
		 */
		StopAction() {
			putValue(Action.NAME, I18n.tr("Stop"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Stops the walk."));
			new ImageProvider("dialogs/streetsideStop.png").getResource().attachImageIcon(this, true);
			StreetsidePlugin.getStreetsideWalkAction().addListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (thread != null) {
				thread.stopWalk();
			}
		}

		@Override
		public void walkStarted(WalkThread thread) {
			this.thread = thread;
		}
	}

	private static class PlayAction extends AbstractAction implements WalkListener {

		private static final long serialVersionUID = -1572747020946842769L;

		private transient WalkThread thread;

		/**
		 * Constructs a normal PlayAction
		 */
		PlayAction() {
			putValue(Action.NAME, I18n.tr("Play"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Continues with the paused walk."));
			new ImageProvider("dialogs/streetsidePlay.png").getResource().attachImageIcon(this, true);
			StreetsidePlugin.getStreetsideWalkAction().addListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (thread != null) {
				thread.play();
			}
		}

		@Override
		public void walkStarted(WalkThread thread) {
			if (thread != null) {
				this.thread = thread;
			}
		}
	}

	private static class PauseAction extends AbstractAction implements WalkListener {

		/**
		 *
		 */
		private static final long serialVersionUID = -8758326399460817222L;
		private WalkThread thread;

		/**
		 * Constructs a normal PauseAction
		 */
		PauseAction() {
			putValue(Action.NAME, I18n.tr("Pause"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Pauses the walk."));
			new ImageProvider("dialogs/streetsidePause.png").getResource().attachImageIcon(this, true);
			StreetsidePlugin.getStreetsideWalkAction().addListener(this);
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
					streetsideImageDisplay.setImage(
							img,
							null);
				}
			} catch (final IOException e) {
				logger.error(e);
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
		if (newImage.getSequence() != null && newImage.next()!=null) {
      nextButton.setEnabled(true);
    }
    if (newImage.getSequence() != null && newImage.previous()!=null) {
      previousButton.setEnabled(true);
    }
		updateImage();
	}

	@Override
	public void imagesAdded() {
		// This method is enforced by StreetsideDataListener, but only selectedImageChanged() is needed
	}

	/**
	 * @return the streetsideImageDisplay
	 */
	public StreetsideImageDisplay getStreetsideImageDisplay() {
		return streetsideImageDisplay;
	}

	/**
	 * @param streetsideImageDisplay the streetsideImageDisplay to set
	 */
	public void setStreetsideImageDisplay(StreetsideImageDisplay streetsideImageDisplay) {
		this.streetsideImageDisplay = streetsideImageDisplay;
	}
}