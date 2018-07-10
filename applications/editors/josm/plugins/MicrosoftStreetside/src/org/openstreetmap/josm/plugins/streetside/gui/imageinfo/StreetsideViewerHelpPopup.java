// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.imageinfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.IllegalComponentStateException;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.plugins.streetside.gui.boilerplate.SelectableLabel;
import org.openstreetmap.josm.plugins.streetside.gui.boilerplate.StreetsideButton;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideColorScheme;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;


public class StreetsideViewerHelpPopup extends JPopupMenu {

	private static final long serialVersionUID = -7840242522398163839L;

	final static Logger logger = Logger.getLogger(StreetsideViewerHelpPopup.class);

	private final Component invokerComp;
	private boolean alreadyDisplayed;

	public StreetsideViewerHelpPopup(Component invoker) {

		invokerComp = invoker;
		removeAll();
		setLayout(new BorderLayout());

		JPanel topBar = new JPanel();
		topBar.add(new JLabel(ImageProvider.get("streetside-logo-white")));
		topBar.setBackground(StreetsideColorScheme.TOOLBAR_DARK_GREY);
		add(topBar, BorderLayout.NORTH);

		JTextPane mainText = new JTextPane();
		mainText.setContentType("text/html");
		mainText.setFont(SelectableLabel.DEFAULT_FONT);
		mainText.setText("<html><div style='width:250px'>" +
				"Welcome to the Microsoft Streetside JOSM Plugin. To view the vector bubbles for the 360 degree imagery, select Imagery->Streetside from the JOSM menu."
				+ "<br><br>"
				+
						"Once the blue bubbles appear on the map, click on a vector bubble and undock/maximize the 360 viewer to view the imagery."
				+ "</div></html>");
		add(mainText, BorderLayout.CENTER);

		JPanel bottomBar = new JPanel();
		bottomBar.setBackground(new Color(0x00FFFFFF, true));
		StreetsideButton infoButton = new StreetsideButton(ImageInfoPanel.getInstance().getToggleAction());
		infoButton.addActionListener(e -> setVisible(false));
		bottomBar.add(infoButton);
		StreetsideButton closeBtn = new StreetsideButton(new AbstractAction() {
			private static final long serialVersionUID = -6193886964751195196L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				StreetsideProperties.IMAGEINFO_HELP_COUNTDOWN.put(0);
			}
		});

		closeBtn.setText(I18n.tr("I got it, close this."));
 bottomBar.add(closeBtn);
 add(bottomBar, BorderLayout.SOUTH);

 setBackground(Color.WHITE);
}

/**
* @return <code>true</code> if the popup is displayed
*/
public boolean showPopup() {
 if (!alreadyDisplayed && invokerComp.isShowing()) {
   try {
     show(invokerComp, invokerComp.getWidth(), 0);
     alreadyDisplayed = true;
     return true;
   } catch (IllegalComponentStateException e) {
     logger.error(MessageFormat.format("Could not show ImageInfoHelpPopup, because probably the invoker component has disappeared from screen. {0}", e.getMessage()));
   }
 }
 return false;
}
}