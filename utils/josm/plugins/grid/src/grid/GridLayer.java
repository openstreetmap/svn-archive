package grid;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.io.IOException;
import java.text.NumberFormat;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * This is a layer that draws a grid
 */
public class GridLayer extends Layer {

	private static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(GridPlugin.class.getResource("/images/grid.png")));
        private LatLon origin, pole;
    private float gridunits;
    private boolean drawLabels;

    	public GridLayer(String url) {
	    super(url.indexOf('/') != -1 ? url.substring(url.indexOf('/')+1) : url);
	    origin = new LatLon(0.0,0.0);
	    pole = new LatLon(0.0,90.0);
	    drawLabels = true;
	}

    private void setGrid(LatLon origin, LatLon pole){
	this.origin = origin;
	this.pole = pole;
	//need to chech pole is perpendicular from origin;
    }
    private void toggleLabels(){
	drawLabels=!drawLabels;
    }
    private class toggleLabelsAction extends AbstractAction {
	GridLayer layer;
	public toggleLabelsAction(GridLayer layer) {
	    super("show labels");
	    this.layer = layer;
	}
	public void actionPerformed(ActionEvent e) {
	    layer.toggleLabels();
	}
    }

	@Override public Icon getIcon() {
		return icon;
	}

	@Override public String getToolTipText() {
		return tr("Grid layer:" + origin);
	}

	@Override public boolean isMergable(Layer other) {
		return false;
	}

	@Override public void mergeFrom(Layer from) {
	}

	@Override public void paint(Graphics g, final MapView mv) {
	    //draw grid here?

	    //establish viewport size
	    int w = mv.getWidth();
	    int h = mv.getHeight();
	    
	    //establish viewport world coordinates
	    LatLon tl = mv.getLatLon(0,0);
	    LatLon br = mv.getLatLon(w,h);

	    //establish max visible grid coordinates (currently also world coordinates)
	    double minlat = Math.max(Math.min(tl.lat(),br.lat()),-Main.proj.MAX_LAT);
	    double maxlat = Math.min(Math.max(tl.lat(),br.lat()), Main.proj.MAX_LAT);
	    double minlon = Math.max(Math.min(tl.lon(),br.lon()),-Main.proj.MAX_LON);
	    double maxlon = Math.min(Math.max(tl.lon(),br.lon()), Main.proj.MAX_LON);

	    //span is maximum lat/lon span across visible grid normalised to 800pixels
	    double span = Math.max((maxlat-minlat),(maxlon-minlon))
		* 800.0/Math.max(h,w);

	    //grid spacing is power of ten to use for grid interval.
	    double spacing = Math.pow(10,Math.floor(Math.log(span)/Math.log(10.0))-1.0);

	    //set up stuff need to draw grid
	    NumberFormat nf = NumberFormat.getInstance();
	    Color majcol = Color.RED;
	    Color mincol = (majcol.darker()).darker();


	    System.out.println("Span: "+span);
	    System.out.println("Spacing: "+spacing);
	    g.setFont (new Font("Helvetica", Font.PLAIN, 8));
	    FontMetrics fm = g.getFontMetrics();

	    for(double lat=spacing*Math.floor(minlat/spacing);lat<maxlat;lat+=spacing){
		for(double lon=spacing*Math.floor(minlon/spacing);lon<maxlon;lon+=spacing){
		    LatLon ll0, lli, llj; 
		    ll0 = new LatLon(lat,lon);
		    lli = new LatLon(lat+spacing,lon);
		    llj = new LatLon(lat,lon+spacing);
		    Point p0=mv.getPoint(Main.proj.latlon2eastNorth(ll0));
		    Point pi=mv.getPoint(Main.proj.latlon2eastNorth(lli));
		    Point pj=mv.getPoint(Main.proj.latlon2eastNorth(llj));

		    if(Math.round(lon/spacing)%10==0)
			g.setColor(majcol);
		    else
			g.setColor(mincol);
		    g.drawLine(p0.x,p0.y,pi.x,pi.y);

		    if(Math.round(lat/spacing)%10==0)
			g.setColor(majcol);
		    else
			g.setColor(mincol);
		    g.drawLine(p0.x,p0.y,pj.x,pj.y);

		    if((Math.round(lon/spacing))%10==0 && (Math.round(lat/spacing))%10==0 && drawLabels){
			String label = nf.format(lat);
			int tw = fm.stringWidth(label); 
			g.drawString(label,p0.x-tw,p0.y-8);
			label = nf.format(lon);
			g.drawString(label,p0.x+2,p0.y+8);
		    }
		}		
	    }

	}

	@Override public void visitBoundingBox(BoundingXYVisitor v) {
		// doesn't have a bounding box
	}

	@Override public Object getInfoComponent() {
		return getToolTipText();
	}

	@Override public Component[] getMenuEntries() {
		return new Component[]{
				new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)),
				new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
				new JMenuItem(new toggleLabelsAction(this)),
				new JSeparator(),
				new JMenuItem(new LayerListPopup.InfoAction(this))};
	}
}
