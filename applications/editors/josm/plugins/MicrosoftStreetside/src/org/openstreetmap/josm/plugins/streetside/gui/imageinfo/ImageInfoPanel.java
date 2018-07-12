// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.imageinfo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.StringSelection;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.data.preferences.AbstractProperty.ValueChangeListener;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.gui.boilerplate.SelectableLabel;
import org.openstreetmap.josm.plugins.streetside.gui.boilerplate.StreetsideButton;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideURL;
import org.openstreetmap.josm.tools.I18n;

public final class ImageInfoPanel extends ToggleDialog implements StreetsideDataListener, DataSelectionListener {
  private static final long serialVersionUID = 4141847503072417190L;

  final static Logger logger = Logger.getLogger(ImageInfoPanel.class);

  private static ImageInfoPanel instance;

  private final JTextPane imgKeyValue;
  private final WebLinkAction imgLinkAction;
  private final ClipboardAction copyImgKeyAction;
  private final AddTagToPrimitiveAction addStreetsideTagAction;
  private final JTextPane seqKeyValue;

  private ValueChangeListener<Boolean> imageLinkChangeListener;

  private ImageInfoPanel() {
    super(
      I18n.tr("Streetside 360° image info"),
      "streetside-info",
      I18n.tr("Displays detail information on the currently selected Streetside image"),
      null,
      150
    );
    SelectionEventManager.getInstance().addSelectionListener(this);

    imgKeyValue = new SelectableLabel();

    imgLinkAction = new WebLinkAction(I18n.tr("View in browser"), null);

    copyImgKeyAction = new ClipboardAction(I18n.tr("Copy key"), null);
    StreetsideButton copyButton = new StreetsideButton(copyImgKeyAction, true);
    copyImgKeyAction.setPopupParent(copyButton);

    addStreetsideTagAction = new AddTagToPrimitiveAction(I18n.tr("Add Streetside tag"));

    seqKeyValue = new SelectableLabel();

    JPanel root = new JPanel(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 5, 0, 5);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    root.add(new JLabel(I18n.tr("Image actions")), gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.gridx = 0;
    gbc.gridy = 1;
    root.add(new JLabel(I18n.tr("Image key")), gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.gridx = 0;
    gbc.gridy = 2;
    root.add(new JLabel(I18n.tr("Sequence key")), gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 1;
    gbc.gridy = 0;
    root.add(new StreetsideButton(imgLinkAction, true), gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.gridx = 1;
    gbc.gridy = 1;
    root.add(imgKeyValue, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.gridx = 1;
    gbc.gridy = 2;
    root.add(seqKeyValue, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.gridx = 2;
    gbc.gridy = 1;
    root.add(copyButton, gbc);

    createLayout(root, true, null);
    selectedImageChanged(null, null);
  }

  public static ImageInfoPanel getInstance() {
    synchronized (ImageInfoPanel.class) {
      if (instance == null) {
        instance = new ImageInfoPanel();
      }
      return instance;
    }
  }

  /**
   * Destroys the unique instance of the class.
   */
  public static synchronized void destroyInstance() {
    instance = null;
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.josm.gui.dialogs.ToggleDialog#stateChanged()
   */
  @Override
  protected void stateChanged() {
    super.stateChanged();
    if (isDialogShowing()) { // If the user opens the dialog once, no longer show the help message
      StreetsideProperties.IMAGEINFO_HELP_COUNTDOWN.put(0);
    }
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.josm.plugins.streetside.StreetsideDataListener#imagesAdded()
   */
  @Override
  public void imagesAdded() {
    // Method is not needed, but enforcesd by the interface StreetsideDataListener
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.josm.plugins.streetside.StreetsideDataListener#selectedImageChanged(org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage, org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage)
   */
  @Override
  public synchronized void selectedImageChanged(final StreetsideAbstractImage oldImage, final StreetsideAbstractImage newImage) {
    logger.info(String.format(
      "Selected Streetside image changed from %s to %s.",
      oldImage instanceof StreetsideImage ? ((StreetsideImage) oldImage).getId() : "‹none›",
      newImage instanceof StreetsideImage ? ((StreetsideImage) newImage).getId() : "‹none›"
    ));

    imgKeyValue.setEnabled(newImage instanceof StreetsideImage);
    final String newImageKey = newImage instanceof StreetsideImage ? ((StreetsideImage) newImage).getId(): null;
    if (newImageKey != null) {
      imageLinkChangeListener = b -> imgLinkAction.setURL(
        StreetsideURL.MainWebsite.browseImage(newImageKey)
      );
      imageLinkChangeListener.valueChanged(null);
      StreetsideProperties.IMAGE_LINK_TO_BLUR_EDITOR.addListener(imageLinkChangeListener);

      imgKeyValue.setText(newImageKey);
      copyImgKeyAction.setContents(new StringSelection(newImageKey));
      addStreetsideTagAction.setTag(new Tag("streetside", newImageKey));
    } else {
      if (imageLinkChangeListener != null) {
        StreetsideProperties.IMAGE_LINK_TO_BLUR_EDITOR.removeListener(imageLinkChangeListener);
        imageLinkChangeListener = null;
      }
      imgLinkAction.setURL(null);

      imgKeyValue.setText('‹' + I18n.tr("image has no key") + '›');
      copyImgKeyAction.setContents(null);
      addStreetsideTagAction.setTag(null);
    }

    final boolean partOfSequence = newImage != null && newImage.getSequence() != null && newImage.getSequence().getId() != null;
    seqKeyValue.setEnabled(partOfSequence);
    if (partOfSequence) {
      seqKeyValue.setText(newImage.getSequence().getId());
    } else {
      seqKeyValue.setText('‹' + I18n.tr("sequence has no id") + '›');
    }
  }

  /* (non-Javadoc)
   * @see org.openstreetmap.josm.data.SelectionChangedListener#selectionChanged(java.util.Collection)
   */
  @Override
  public synchronized void selectionChanged(final SelectionChangeEvent event) {
    final Collection<? extends OsmPrimitive> sel = event.getSelection();
    if (StreetsideProperties.DEBUGING_ENABLED.get()) {
      logger.debug(String.format("Selection changed. %d primitives are selected.", sel == null ? 0 : sel.size()));
    }
    addStreetsideTagAction.setTarget(sel != null && sel.size() == 1 ? sel.iterator().next() : null);
  }
}