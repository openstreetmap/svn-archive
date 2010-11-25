package org.openstreetmap.josm.plugins.trustosm.gui.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

public class KeySignaturesDialog extends JPanel {

	protected boolean isCollapsed;

	protected TitleBar titleBar;

	/** the label in the title bar which shows whether the toggle dialog is expanded or collapsed */
	private JLabel lblMinimized;


	public KeySignaturesDialog(PGPPublicKey key) {
		super(new BorderLayout());

		String userid = "Unknown";
		Iterator iter = key.getUserIDs();
		if (iter.hasNext()) {
			userid = (String)iter.next();
		}

		isCollapsed = false;

		titleBar = new TitleBar(userid);
		add(titleBar, BorderLayout.NORTH);
		add(createKeySigPanel(key));

	}

	public static JPanel createKeySigPanel(PGPPublicKey key) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		Iterator iter = key.getSignatures();
		while (iter.hasNext()) {
			PGPSignature sig = (PGPSignature)iter.next();
			String uid = "0x"+Long.toHexString(sig.getKeyID()).substring(8).toUpperCase();
			p.add(new JLabel(uid));
		}
		return p;
	}

	/**
	 * Collapses the toggle dialog to the title bar only
	 *
	 */
	public void collapse() {
		if (!isCollapsed) {
			//setContentVisible(false);
			isCollapsed = true;
			setPreferredSize(new Dimension(0,20));
			setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
			setMinimumSize(new Dimension(Integer.MAX_VALUE,20));
			lblMinimized.setIcon(ImageProvider.get("misc", "minimized"));
		}
		else throw new IllegalStateException();
	}

	/**
	 * Expands the toggle dialog
	 */
	protected void expand() {
		if (isCollapsed) {
			//		setContentVisible(true);
			isCollapsed = false;
			setPreferredSize(new Dimension(0,200));
			setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
			lblMinimized.setIcon(ImageProvider.get("misc", "normal"));
		}
		else throw new IllegalStateException();
	}

	/**
	 * Sets the visibility of all components in this toggle dialog, except the title bar
	 *
	 * @param visible true, if the components should be visible; false otherwise
	 */
	protected void setContentVisible(boolean visible) {
		Component comps[] = getComponents();
		for(int i=0; i<comps.length; i++) {
			if(comps[i] != titleBar) {
				comps[i].setVisible(visible);
			}
		}
	}

	/**
	 * The title bar displayed in docked mode
	 *
	 */
	protected class TitleBar extends JPanel {
		final private JLabel lblTitle;

		public TitleBar(String toggleDialogName) {
			setLayout(new GridBagLayout());
			lblMinimized = new JLabel(ImageProvider.get("misc", "minimized"));
			add(lblMinimized);

			lblTitle = new JLabel(toggleDialogName);
			add(lblTitle, GBC.std().fill(GBC.HORIZONTAL));

			addMouseListener(
					new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							// toggleExpandedState
							if (isCollapsed) {
								expand();
							} else {
								collapse();
							}
						}
					}
			);

			setToolTipText(tr("Click to minimize/maximize the panel content"));
			setTitle(toggleDialogName);
		}

		public void setTitle(String title) {
			lblTitle.setText(title);
		}

		public String getTitle() {
			return lblTitle.getText();
		}
	}

}
