//License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.imageinfo;

import java.awt.BorderLayout;
import java.text.MessageFormat;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.preferences.AbstractProperty.ValueChangeListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.actions.ImageReloadAction;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapBox;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapBuilder;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapUtils;
import org.openstreetmap.josm.plugins.streetside.gui.boilerplate.StreetsideButton;
import org.openstreetmap.josm.plugins.streetside.utils.GraphicsUtils;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideURL;
import org.openstreetmap.josm.tools.I18n;

public final class StreetsideViewerPanel extends JPanel
		implements StreetsideDataListener {

	private static final long serialVersionUID = 4141847503072417190L;

	final static Logger logger = Logger.getLogger(StreetsideViewerPanel.class);

	private JCheckBox highResImageryCheck;
	private WebLinkAction imgLinkAction;
	private ImageReloadAction imgReloadAction;
	private ValueChangeListener<Boolean> imageLinkChangeListener;

	private static ThreeSixtyDegreeViewerPanel threeSixtyDegreeViewerPanel;

  private StreetsideViewerHelpPopup streetsideViewerHelp;

	public StreetsideViewerPanel() {

		super(new BorderLayout());

		SwingUtilities.invokeLater(() -> initializeAndStartGUI());

		selectedImageChanged(null, null);

		setToolTipText(I18n.tr("Select Microsoft Streetside from the Imagery menu, then click on a blue vector bubble.."));
	}

	private void initializeAndStartGUI() {

		threeSixtyDegreeViewerPanel = new ThreeSixtyDegreeViewerPanel();

		GraphicsUtils.PlatformHelper.run(() -> {
	    	threeSixtyDegreeViewerPanel.initialize();
		});

		add(threeSixtyDegreeViewerPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	    JPanel checkPanel = new JPanel();

	    imgReloadAction = new ImageReloadAction("Reload");

	    StreetsideButton imgReloadButton = new StreetsideButton(imgReloadAction);

	    highResImageryCheck = new JCheckBox("High resolution");
	    highResImageryCheck.setSelected(StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get());
	    highResImageryCheck.addActionListener(
	      action -> StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.put(highResImageryCheck.isSelected())
	    );
	    StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.addListener(
	      valueChange -> highResImageryCheck.setSelected(StreetsideProperties.SHOW_HIGH_RES_STREETSIDE_IMAGERY.get())
	    );
	    checkPanel.add(highResImageryCheck, BorderLayout.WEST);
	    checkPanel.add(imgReloadButton, BorderLayout.EAST);

	    JPanel privacyLink = new JPanel();

	    imgLinkAction = new WebLinkAction("Report a privacy concern with this image", null);
	    privacyLink.add(new StreetsideButton(imgLinkAction, true));
	    checkPanel.add(privacyLink, BorderLayout.PAGE_END);

	    add(threeSixtyDegreeViewerPanel, BorderLayout.CENTER);

	    JPanel bottomPanel = new JPanel();
	    bottomPanel.add(checkPanel, BorderLayout.NORTH);
	    bottomPanel.add(privacyLink, BorderLayout.SOUTH);

	    add(bottomPanel, BorderLayout.PAGE_END);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openstreetmap.josm.plugins.streetside.StreetsideDataListener#imagesAdded(
	 * )
	 */
	@Override
	public void imagesAdded() {
		// Method is not needed, but enforcesd by the interface StreetsideDataListener
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openstreetmap.josm.plugins.streetside.StreetsideDataListener#
	 * selectedImageChanged(org.openstreetmap.josm.plugins.streetside.
	 * StreetsideAbstractImage,
	 * org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage)
	 */
	@Override
	public synchronized void selectedImageChanged(final StreetsideAbstractImage oldImage,
			final StreetsideAbstractImage newImage) {

		// method is invoked with null initially by framework
		if(newImage!=null) {

		    logger.info(String.format(
		      "Selected Streetside image changed from %s to %s.",
		      oldImage instanceof StreetsideImage ? ((StreetsideImage) oldImage).getId() : "‹none›",
		      newImage instanceof StreetsideImage ? ((StreetsideImage) newImage).getId() : "‹none›"
		    ));

		    final String newImageId = CubemapBuilder.getInstance().getCubemap() !=null ? CubemapBuilder.getInstance().getCubemap().getId() : newImage instanceof StreetsideImage ? ((StreetsideImage) newImage).getId(): null;
		    if (newImageId != null) {
		      final String bubbleId = CubemapUtils.convertQuaternary2Decimal(newImageId);
		      imageLinkChangeListener = b -> imgLinkAction.setURL(
		        StreetsideURL.MainWebsite.streetsidePrivacyLink(bubbleId)
		      );

		      if(StreetsideProperties.DEBUGING_ENABLED.get()) {
		        logger.debug(MessageFormat.format("Privacy link set for Streetside image {0} quadKey {1}", bubbleId, newImageId));
		      }

		      imageLinkChangeListener.valueChanged(null);
		      StreetsideProperties.CUBEMAP_LINK_TO_BLUR_EDITOR.addListener(imageLinkChangeListener);
		    } else {
		      if (imageLinkChangeListener != null) {
		        StreetsideProperties.CUBEMAP_LINK_TO_BLUR_EDITOR.removeListener(imageLinkChangeListener);
		        imageLinkChangeListener = null;
		      }
		      imgLinkAction.setURL(null);
		    }
		  }
	}

	public static CubemapBox getCubemapBox() {
		return threeSixtyDegreeViewerPanel.getCubemapBox();
	}

	/**
	 * @return the threeSixtyDegreeViewerPanel
	 */
	public static ThreeSixtyDegreeViewerPanel getThreeSixtyDegreeViewerPanel() {
		return threeSixtyDegreeViewerPanel;
	}
}