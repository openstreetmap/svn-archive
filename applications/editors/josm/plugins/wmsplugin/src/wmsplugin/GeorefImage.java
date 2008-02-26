package wmsplugin;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class GeorefImage implements Serializable {
	public BufferedImage image;
	public EastNorth min, max;

	public GeorefImage(BufferedImage img, EastNorth min, EastNorth max) {
		image = img;
		this.min = min;
		this.max = max;
	}

	public void displace(double dx, double dy) {
		min = new EastNorth(min.east() + dx, min.north() + dy);
		max = new EastNorth(max.east() + dx, max.north() + dy);
	}

	public boolean contains(EastNorth en) {
		return min.east() <= en.east() && en.east() <= max.east()
			&& min.north() <= en.north() && en.north() <= max.north();
	}

	public void paint(Graphics g, NavigatableComponent nc) {
		if (image == null || min == null || max == null) return;

		Point minPt = nc.getPoint(min), maxPt = nc.getPoint(max);

		if (!g.hitClip(minPt.x, maxPt.y,
				maxPt.x - minPt.x, minPt.y - maxPt.y))
			return;

		g.drawImage(image,
			minPt.x, maxPt.y, maxPt.x, minPt.y, // dest
			0, 0, image.getWidth(), image.getHeight(), // src
			null);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		max = (EastNorth) in.readObject();
		min = (EastNorth) in.readObject();
		image = (BufferedImage) ImageIO.read(ImageIO.createImageInputStream(in));
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(max);
		out.writeObject(min);
		ImageIO.write(image, "png", ImageIO.createImageOutputStream(out));
	}
}
