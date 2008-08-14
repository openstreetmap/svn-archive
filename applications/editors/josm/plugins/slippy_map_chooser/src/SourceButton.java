import java.awt.Graphics;
import java.awt.Point;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.tools.ImageProvider;

public class SourceButton {

	private int x = 0;
	private int y = 30;

	private ImageIcon enlargeImage;
	private ImageIcon shrinkImage;
	private ImageIcon imageMapnik;
	private ImageIcon imageOsmarender;

	private boolean isEnlarged = false;

	private boolean isMapnik = true;

	public static final int HIDE_OR_SHOW = 1;
	public static final int MAPNIK = 2;
	public static final int OSMARENDER = 3;

	public SourceButton() {
		enlargeImage = ImageProvider.get("", "layer-switcher-maximize.png");
		shrinkImage = ImageProvider.get("", "layer-switcher-minimize.png");
		imageMapnik = ImageProvider.get("", "blue_Mapnik.png");
		imageOsmarender = ImageProvider.get("", "blue_Osmarender.png");
	}

	public void paint(Graphics g) {

		if (isEnlarged) {
			if (isMapnik) {
				g.drawImage(imageMapnik.getImage(), g.getClipBounds().width
						- imageMapnik.getIconWidth(), y, null);
			} else {
				g.drawImage(imageOsmarender.getImage(), g.getClipBounds().width
						- imageMapnik.getIconWidth(), y, null);
			}

			if (shrinkImage != null) {
				this.x = g.getClipBounds().width - shrinkImage.getIconWidth();
				g.drawImage(shrinkImage.getImage(), x, y, null);
			}

		} else {
			if (enlargeImage != null) {
				this.x = g.getClipBounds().width - enlargeImage.getIconWidth();
				g.drawImage(enlargeImage.getImage(), x, y, null);
			}
		}
	}

	public void toggle() {
		this.isEnlarged = !this.isEnlarged;

	}

	public int hit(Point point) {
		if (isEnlarged) {
			if (x < point.x && point.x < x + shrinkImage.getIconWidth()) {
				if (y < point.y && point.y < y + shrinkImage.getIconHeight()) {
					return HIDE_OR_SHOW;
				}
			} else if (x - imageMapnik.getIconWidth() < point.x && point.x < x) {
				if (y < point.y && point.y < y + imageMapnik.getIconHeight() / 2) {
					isMapnik = false;
					return OSMARENDER;
				} else if (y + imageMapnik.getIconHeight() / 2 < point.y
						&& point.y < y + imageMapnik.getIconHeight()) {
					isMapnik = true;
					return MAPNIK;
				}
			}
		} else {
			if (x < point.x && point.x < x + enlargeImage.getIconWidth()) {
				if (y < point.y && point.y < y + enlargeImage.getIconHeight()) {
					return HIDE_OR_SHOW;
				}
			}
		}

		return 0;
	}
}
