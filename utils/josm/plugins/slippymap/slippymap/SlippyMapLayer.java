package slippymap;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.Mercator;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer.GpsPoint;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Class that displays a slippy map layer.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class SlippyMapLayer extends Layer implements ImageObserver {

	ArrayList<HashMap<Integer, SlippyMapTile>> tileStorage = new ArrayList<HashMap<Integer, SlippyMapTile>>(20);
	Point[][] pixelpos = new Point[21][21];
	LatLon lastTopLeft;
	LatLon lastBotRight;

	int z12x0, z12x1, z12y0, z12y1;
	
	private Image bufferImage;
	private SlippyMapTile clickedTile;
	private boolean needRedraw;
	
	private JPopupMenu tileOptionMenu;
	
	public SlippyMapLayer()
	{
		super("Slippy Map");
		for (int i=0; i<18; i++)
			tileStorage.add(new HashMap<Integer, SlippyMapTile>());
		
		tileOptionMenu = new JPopupMenu();
		tileOptionMenu.add(new JMenuItem(new AbstractAction("Load Tile") {
			public void actionPerformed(ActionEvent ae) {
				if (clickedTile != null) 
				{
					clickedTile.loadImage();
					needRedraw = true;
				    Main.map.repaint();
				}
			}
		}));

		tileOptionMenu.add(new JMenuItem(new AbstractAction("Request Update") {
			public void actionPerformed(ActionEvent ae) {
			}
			public boolean isEnabled() { return false; }
		}));

		tileOptionMenu.add(new JMenuItem(new AbstractAction("Show Tile Status") {
			public void actionPerformed(ActionEvent ae) {
			}
			public boolean isEnabled() { return false; }
		}));
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				Main.map.mapView.addMouseListener(new MouseAdapter() {
					@Override public void mouseClicked(MouseEvent e) {
						if (e.getButton() != MouseEvent.BUTTON3)
							return;
						clickedTile = getTileForPixelpos(e.getX(), e.getY());
						tileOptionMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				});

				Main.map.mapView.addLayerChangeListener(new LayerChangeListener(){
					public void activeLayerChange(Layer oldLayer, Layer newLayer) {}
					public void layerAdded(Layer newLayer) {}
					public void layerRemoved(Layer oldLayer) {
						Main.pref.listener.remove(SlippyMapLayer.this);
					}
				});
			}
		});
	}
	

	/**
	 */
	@Override public void paint(Graphics g, MapView mv)
	{
		LatLon topLeft = mv.getLatLon(0, 0);
		LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
		Graphics oldg = g;
		
		if (lastTopLeft != null && 
			lastBotRight != null && 
			topLeft.equalsEpsilon(lastTopLeft) && 
			botRight.equalsEpsilon(lastBotRight) && 
			bufferImage != null && 
			mv.getWidth() == bufferImage.getWidth(null) && 
			mv.getHeight() == bufferImage.getHeight(null) &&
			!needRedraw)
		{

			g.drawImage(bufferImage, 0, 0, null);
			return;
		}
		
		needRedraw = false;
		lastTopLeft = topLeft;
		lastBotRight = botRight;
		bufferImage = mv.createImage(mv.getWidth(), mv.getHeight());
		g = bufferImage.getGraphics();
			
		z12x0 = lonToTileX(topLeft.lon());
		z12x1 = lonToTileX(botRight.lon());
		z12y0 = latToTileY(topLeft.lat());
		z12y1 = latToTileY(botRight.lat());
		
		if (z12x0>z12x1) { int tmp = z12x0; z12x0=z12x1; z12x1=tmp; }
		if (z12y0>z12y1) { int tmp = z12y0; z12y0=z12y1; z12y1=tmp; }
		
		if (z12x1-z12x0 > 18) return;
		if (z12y1-z12y0 > 18) return;
		
		for (int x = z12x0 -1; x <= z12x1+1; x++) 
		{
			double lon = tileXToLon(x);
			for (int y = z12y0 -1; y <= z12y1+1; y++) 
			{
				LatLon tmpLL = new LatLon(tileYToLat(y), lon);
				pixelpos[x-z12x0+1][y-z12y0+1] = mv.getPoint(Main.proj.latlon2eastNorth(tmpLL));
			}
		}
		
		int fontHeight = g.getFontMetrics().getHeight();
			
		g.setColor(Color.DARK_GRAY);
		
		for (int x = z12x0 -1; x <= z12x1; x++) 
		{
			for (int y = z12y0 -1; y <= z12y1; y++) 
			{
				int key = y * 2<<18 + x;
				SlippyMapTile tile = tileStorage.get(12).get(key);

				if (tile == null)
				{
					tileStorage.get(12).put(key, tile = new SlippyMapTile(x, y, 12));
				}
				Image img = tile.getImage();
				
				if (img != null)
				{
					Point p = pixelpos[x-z12x0+1][y-z12y0+1];
					Point p2 = pixelpos[x-z12x0+2][y-z12y0+2];
					g.drawImage(img, p.x, p.y, p2.x-p.x, p2.y-p.y, this);
				}
			}
		}
		
		for (int x = z12x0 -1; x <= z12x1; x++) 
		{
			Point p = pixelpos[x-z12x0+1][0];
			
			if (x % 32 == 0) 
			{
				// level 7 tile boundary
				g.fillRect(p.x - 1, 0, 3, mv.getHeight());
			} 
			else 
			{
				g.drawLine(p.x, 0, p.x, mv.getHeight());
			}
		
			for (int y = z12y0 -1; y <= z12y1; y++) 
			{
				int key = y * 2<<18 + x;
				int texty = p.y + 2 + fontHeight;
				SlippyMapTile tile = tileStorage.get(12).get(key);
				p = pixelpos[x-z12x0+1][y-z12y0+2];				
				g.drawString("x=" + x + " y="+ y + " z=12", p.x + 2, texty);
				texty += 1 + fontHeight;
				if ((x % 32 == 0) && (y % 32 == 0))
				{
					g.drawString("x=" + x/32 + " y="+ y/32 + " z=7", p.x+2, texty);
					texty += 1 + fontHeight;
				}
				
				String md = tile.getMetadata();
				if (md != null)
				{
					g.drawString(md, p.x+2, texty);
					texty += 1 + fontHeight;
				}
				
				if (tile.getImage() == null)
				{
					g.drawString("image not loaded", p.x+2, texty);
					texty += 1 + fontHeight;
				}
						
				if (x == z12x0-1) 
				{
					if (y % 32 == 31) 
					{
						g.fillRect(0, p.y - 1, mv.getWidth(), 3);
					} 
					else 
					{
						g.drawLine(0, p.y, mv.getWidth(), p.y);
					}
				}
			}
		}

		oldg.drawImage(bufferImage, 0, 0, null);
	
	}

	SlippyMapTile getTileForPixelpos(int px, int py)
	{
		int tilex=z12x1;
		int tiley=z12y1;
		for (int x = z12x0; x <= z12x1; x++) 
		{

			if (pixelpos[x-z12x0+1][0].x > px) 
			{
				tilex = x-1;
				break;
			}
		}
		if (tilex==-1) return null;
		for (int y = z12y0; y <= z12y1; y++) 
		{

			if (pixelpos[0][y-z12y0+1].y > py) 
			{
				tiley = y-1;
				break;
			}
		}
		if (tiley==-1) return null;
		
		int key = tiley * 2<<18 + tilex;
		SlippyMapTile tile = tileStorage.get(12).get(key);
		if (tile==null) tileStorage.get(12).put(key, tile = new SlippyMapTile(tilex, tiley, 12));
		return tile;
	}
	
	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return ImageProvider.get("slippymap");
	}

	@Override
	public Object getInfoComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Component[] getMenuEntries() {
		/*
		JMenuItem color = new JMenuItem(tr("Customize Color"), ImageProvider.get("colorchooser"));
		color.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String col = Main.pref.get("color.layer "+name, Main.pref.get("color.gps marker", ColorHelper.color2html(Color.gray)));
				JColorChooser c = new JColorChooser(ColorHelper.html2color(col));
				Object[] options = new Object[]{tr("OK"), tr("Cancel"), tr("Default")};
				int answer = JOptionPane.showOptionDialog(Main.parent, c, tr("Choose a color"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				switch (answer) {
				case 0:
					Main.pref.put("color.layer "+name, ColorHelper.color2html(c.getColor()));
					break;
				case 1:
					return;
				case 2:
					Main.pref.put("color.layer "+name, null);
					break;
				}
				Main.map.repaint();
			}
		});
		*/
		
		return new Component[] {
				new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)),
				new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
				new JSeparator(),
				// color,
				new JMenuItem(new RenameLayerAction(associatedFile, this)),
				new JSeparator(),
				new JMenuItem(new LayerListPopup.InfoAction(this))
		};
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void mergeFrom(Layer from) {
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {
		// TODO Auto-generated method stub		
	}

	private int latToTileY(double lat) {
		double l = lat / 180 * Math.PI;
		double pf = Math.log(Math.tan(l) + (1/Math.cos(l)));
		return (int) (2048.0 * (Math.PI - pf) / Math.PI);
	}
	
	private int lonToTileX(double lon) {
		return (int) (512.0 * (lon + 180.0) / 45.0);
	}
	
	private double tileYToLat(int y) {
		return Math.atan(Math.sinh(Math.PI - (Math.PI*y / 2048.0))) * 180 / Math.PI;
	}
	
	private double tileXToLon(int x) {
		return x * 45.0 / 512.0 - 180.0;
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {

	    boolean done = ((infoflags & (ERROR | FRAMEBITS | ALLBITS)) != 0);
	    // Repaint immediately if we are done, otherwise batch up
	    // repaint requests every 100 milliseconds
	    needRedraw = true;
	    Main.map.repaint(done ? 0 : 100);
	    return !done;
	}
}
