//License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.imageinfo;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.preferences.AbstractProperty.ValueChangeListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideDataListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.actions.ImageReloadAction;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapUtils;
import org.openstreetmap.josm.plugins.streetside.cubemap.GraphicsUtils;
import org.openstreetmap.josm.plugins.streetside.gui.boilerplate.StreetsideButton;
import org.openstreetmap.josm.plugins.streetside.utils.CubemapBox;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideURL;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;


public final class StreetsideViewerPanel extends JPanel
		implements StreetsideDataListener, SelectionChangedListener {

	private static final long serialVersionUID = 4141847503072417190L;

	private JCheckBox highResImageryCheck;
	private WebLinkAction imgLinkAction;
	private ImageReloadAction imgReloadAction;
	private ValueChangeListener<Boolean> imageLinkChangeListener;

	private static ThreeSixtyDegreeViewerPanel threeSixtyDegreeViewerPanel;

	public StreetsideViewerPanel() {

		super(new BorderLayout());

		SwingUtilities.invokeLater(new Runnable() {
		     @Override
		     public void run() {
		    	 initializeAndStartGUI();
		     }
		 });

		selectedImageChanged(null, null);

		setToolTipText(I18n.tr("Select Microsoft Streetside from the Imagery menu, then click on a blue vector bubble.."));
	}

	private void initializeAndStartGUI() {

		DataSet.addSelectionListener(this);

		threeSixtyDegreeViewerPanel = new ThreeSixtyDegreeViewerPanel();

		GraphicsUtils.PlatformHelper.run(() -> {
	    	threeSixtyDegreeViewerPanel.initialize();
		});

		add(threeSixtyDegreeViewerPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	    JPanel checkPanel = new JPanel();

	    imgReloadAction = new ImageReloadAction(I18n.tr("Reload"));

	    StreetsideButton imgReloadButton = new StreetsideButton(imgReloadAction);
		highResImageryCheck = new JCheckBox(I18n.tr("High resolution"));
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

	    imgLinkAction = new WebLinkAction(I18n.tr("Report a privacy concern with this image"), null);
	    privacyLink.add(new StreetsideButton(imgLinkAction, true));
	    checkPanel.add(privacyLink, BorderLayout.PAGE_END);

	    add(checkPanel, BorderLayout.PAGE_START);
	    add(threeSixtyDegreeViewerPanel, BorderLayout.CENTER);

	    add(privacyLink, BorderLayout.PAGE_END);
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

		    Logging.debug(String.format(
		      "Selected Streetside image changed from %s to %s.",
		      oldImage instanceof StreetsideImage ? ((StreetsideImage) oldImage).getId() : "‹none›",
		      newImage instanceof StreetsideImage ? ((StreetsideImage) newImage).getId() : "‹none›"
		    ));

		    //imgIdValue.setEnabled(newImage instanceof StreetsideImage);
		    final String newImageId = newImage instanceof StreetsideImage ? ((StreetsideImage) newImage).getId(): null;
		    if (newImageId != null) {
		      final String bubbleId = CubemapUtils.convertQuaternary2Decimal(newImageId);
		      imageLinkChangeListener = b -> imgLinkAction.setURL(
		        StreetsideURL.MainWebsite.streetsidePrivacyLink(bubbleId)
		      );
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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openstreetmap.josm.data.SelectionChangedListener#selectionChanged(java.
	 * util.Collection)
	 */
	@Override
	public synchronized void selectionChanged(final Collection<? extends OsmPrimitive> sel) {
		Logging.debug(String.format("Selection changed. %d primitives are selected.", sel == null ? 0 : sel.size()));
	}

	public CubemapBox getCubemapBox() {
		return threeSixtyDegreeViewerPanel.getCubemapBox();
	}

	/**
	 * @return the threeSixtyDegreeViewerPanel
	 */
	public static ThreeSixtyDegreeViewerPanel getThreeSixtyDegreeViewerPanel() {
		return threeSixtyDegreeViewerPanel;
	}
}