// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.gui.layer.markerlayer;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Marker representing an image. Uses a special icon, and when clicked,
 * displays an image view dialog. Re-uses some code from GeoImageLayer.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class ImageMarker extends ButtonMarker {

	public URL imageUrl;

	public static ImageMarker create(LatLon ll, String url) {
		try {
			return new ImageMarker(ll, new URL(url));
		} catch (Exception ex) {
			return null;
		}
	}

	private ImageMarker(LatLon ll, URL imageUrl) {
		super(ll, "photo.png");
		this.imageUrl = imageUrl;
	}

	@Override public void actionPerformed(ActionEvent ev) {
		final JPanel p = new JPanel(new BorderLayout());
		final JScrollPane scroll = new JScrollPane(new JLabel(loadScaledImage(imageUrl, 580)));
		final JViewport vp = scroll.getViewport();
		p.add(scroll, BorderLayout.CENTER);

		final JToggleButton scale = new JToggleButton(ImageProvider.get("misc", "rectangle"));

		JPanel p2 = new JPanel();
		p2.add(scale);
		p.add(p2, BorderLayout.SOUTH);
		scale.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev) {
				p.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (scale.getModel().isSelected())
					((JLabel)vp.getView()).setIcon(loadScaledImage(imageUrl, Math.max(vp.getWidth(), vp.getHeight())));
				else
					((JLabel)vp.getView()).setIcon(new ImageIcon(imageUrl));
				p.setCursor(Cursor.getDefaultCursor());
			}
		});
		scale.setSelected(true);
		JOptionPane pane = new JOptionPane(p, JOptionPane.PLAIN_MESSAGE);
		JDialog dlg = pane.createDialog(Main.parent, imageUrl.toString());
		dlg.setModal(false);
		dlg.setVisible(true);
	}

	private static Icon loadScaledImage(URL u, int maxSize) {
		Image img = new ImageIcon(u).getImage();
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		if (w>h) {
			h = Math.round(maxSize*((float)h/w));
			w = maxSize;
		} else {
			w = Math.round(maxSize*((float)w/h));
			h = maxSize;
		}
		return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
	}

}
