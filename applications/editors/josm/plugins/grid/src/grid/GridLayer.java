package grid;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.GBC;

/**
 * This is a layer that draws a grid
 */
public class GridLayer extends Layer {

    private static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(GridPlugin.class.getResource("/images/grid.png")));
    private LatLon origin, pole;
    private float gridunits;
    private boolean drawLabels;
    private Helmert gridtoworld; //worldtogrid;
    private Color majcol = Color.RED;
    public double a, b, c;
        public GridLayer(String url) {
        super(url.indexOf('/') != -1 ? url.substring(url.indexOf('/')+1) : url);
        origin = new LatLon(0.0,0.0);
        pole = new LatLon(0.0,90.0);
        drawLabels = true;
        gridtoworld = new Helmert(0.0, 0.0, 0);
        //worldtogrid = new Helmert(0.0, 0.0, 0);
    }

    //  private void setGrid(LatLon origin, LatLon pole){
    //  this.origin = origin;
    //  this.pole = pole;
        //need to check pole is perpendicular from origin;
    //  }
    private void setGrid(double a, double b, double c){
        System.out.println("Setting grid to :" + a + ", " + b + ", " + c);
        this.origin = origin;
        this.pole = pole;
        //need to chech pole is perpendicular from origin;
        this.a=a;
        this.b=b;
        this.c=c;
        gridtoworld = new Helmert(a, b, c);
        System.out.println(new LatLon(10,10) + "->" +
                   gridtoworld.transform(new LatLon(10,10)) + "->" +
                   gridtoworld.inverseTransform(gridtoworld.transform(new LatLon(10,10))));
        //worldtogrid = new Helmert(-a, -b, -c);
    }

    private void setUnits(float units){
        gridunits = units;
    }

    private void setUnitsToLatLon(){
        gridunits = 0;
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
            Main.map.repaint();
        }
    }

    private class setWorldAction extends AbstractAction {
        GridLayer layer;
        public setWorldAction(GridLayer layer) {
            super("set to world");
            this.layer = layer;
        }
        public void actionPerformed(ActionEvent e) {
            layer.setGrid(0,0,0);
            Main.map.repaint();
        }
    }
    private class incAAction extends AbstractAction {
        GridLayer layer;
        public incAAction(GridLayer layer) {
            super("increase a");
            this.layer = layer;
        }
        public void actionPerformed(ActionEvent e) {
            layer.setGrid(layer.a+10,layer.b,layer.c);
            Main.map.repaint();
        }
    }
    private class incBAction extends AbstractAction {
        GridLayer layer;
        public incBAction(GridLayer layer) {
            super("increase b");
            this.layer = layer;
        }
        public void actionPerformed(ActionEvent e) {
            layer.setGrid(layer.a,layer.b+10,layer.c);
            Main.map.repaint();
        }
    }
    private class incCAction extends AbstractAction {
        GridLayer layer;
        public incCAction(GridLayer layer) {
            super("increase c");
            this.layer = layer;
        }
        public void actionPerformed(ActionEvent e) {
            layer.setGrid(layer.a,layer.b,layer.c+10);
            Main.map.repaint();
        }
    }
    private class setColorAction extends AbstractAction {
        GridLayer layer;
        public setColorAction(GridLayer layer) {
            super("Customize Color", ImageProvider.get("colorchooser"));
            this.layer = layer;
        }
        public void actionPerformed(ActionEvent e) {
            String col=ColorHelper.color2html(layer.majcol);
            JColorChooser c = new JColorChooser(ColorHelper.html2color(col));
            Object[] options = new Object[]{tr("OK"), tr("Cancel"), tr("Default")};
            int answer = JOptionPane.showOptionDialog(Main.parent, c, tr("Choose a color"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            switch (answer) {
            case 0:
//              Main.pref.put("color.layer "+name, ColorHelper.color2html(c.getColor()));
                majcol = c.getColor();
                break;
            case 1:
                return;
            case 2:
//              Main.pref.put("color.layer "+name, null);
                majcol = Color.RED;
                break;
            }
            Main.map.repaint();
        }
    }

    private class setGridLayoutAction extends AbstractAction {
        GridLayer layer;
        public setGridLayoutAction(GridLayer layer) {
            super("Set grid origin");
            this.layer = layer;
        }
        public void actionPerformed(ActionEvent e) {
            NumberFormat nf = NumberFormat.getInstance();
            JPanel p = new JPanel(new GridBagLayout());
            JTextField latText = new JTextField(nf.format(layer.a));
            JTextField lonText = new JTextField(nf.format(layer.b));
            JTextField devText = new JTextField(nf.format(layer.c));
            p.add(new JLabel(tr("Grid origin location")), GBC.eol());
            p.add(new JLabel(tr("Latitude")));
            p.add(latText, GBC.eol());
            p.add(new JLabel(tr("Longitude")));
            p.add(lonText, GBC.eol());
            p.add(new JLabel(tr("Grid rotation")));
            p.add(devText, GBC.eol());
            Object[] options = new Object[]{tr("OK"), tr("Cancel"), tr("World")};
            int answer = JOptionPane.showOptionDialog(Main.parent, p,tr("Grid layout"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            switch (answer) {
            case 0:
                layer.setGrid(
                    -Double.parseDouble(latText.getText()),
                    -Double.parseDouble(lonText.getText()),
                    Double.parseDouble(devText.getText()));

            case 1:
                return;
            case 2:
                layer.setGrid(0,0,0);
            }
            Main.map.repaint();
        }
    }

    @Override public Icon getIcon() {
        return icon;
    }

    @Override public String getToolTipText() {
        return tr("Grid layer:" + a + "," + b + "," + c);
    }

    @Override public boolean isMergable(Layer other) {
        return false;
    }

    @Override public void mergeFrom(Layer from) {
    }

    @Override public void paint(Graphics g, final MapView mv) {
        //establish viewport size
        int w = mv.getWidth();
        int h = mv.getHeight();

        //establish viewport world coordinates
            LatLon tl = mv.getLatLon(0,0);
            LatLon br = mv.getLatLon(w,h);

            //establish max visible world coordinates
            double wminlat = Math.max(Math.min(tl.lat(),br.lat()),-Main.proj.MAX_LAT);
            double wmaxlat = Math.min(Math.max(tl.lat(),br.lat()), Main.proj.MAX_LAT);
            double wminlon = Math.max(Math.min(tl.lon(),br.lon()),-Main.proj.MAX_LON);
            double wmaxlon = Math.min(Math.max(tl.lon(),br.lon()), Main.proj.MAX_LON);

        //establish viewport grid coordinates
        //because grid is arbitrarily orientated and may be curved check several points on border
        double minlat = 180, maxlat=-180, minlon=90, maxlon=-90;

        for(double x=0;x<=1;x+=0.2){
            LatLon[] p = new LatLon[] {
                gridtoworld.inverseTransform(new LatLon(wminlat, x*wminlon+(1-x)*wmaxlon)),
                gridtoworld.inverseTransform(new LatLon(wmaxlat, x*wminlon+(1-x)*wmaxlon)),
                gridtoworld.inverseTransform(new LatLon(x*wminlat+(1-x)*wmaxlat, wminlon)),
                gridtoworld.inverseTransform(new LatLon(x*wminlat+(1-x)*wmaxlat, wmaxlon))};
            for(int i=0;i<4;i++){
                maxlat=Math.max(p[i].lat(),maxlat);
                minlat=Math.min(p[i].lat(),minlat);
                maxlon=Math.max(p[i].lon(),maxlon);
                minlon=Math.min(p[i].lon(),minlon);
            }
        }

        //also check if the singularities are visible
        LatLon northpole = gridtoworld.transform(new LatLon(90,0));
        LatLon southpole = gridtoworld.transform(new LatLon(-90,0));
        if((northpole.lat()>=wminlat) && (northpole.lat()<=wmaxlat) && (northpole.lon()>=wminlon) && (northpole.lon()<=wmaxlon)){
            maxlat=90;
            minlon=-180;
            maxlon=180;
        }
        if((southpole.lat()>=wminlat) && (southpole.lat()<=wmaxlat) && (southpole.lon()>=wminlon) && (southpole.lon()<=wmaxlon)){
            minlat=-90;
            minlon=-180;
            maxlon=180;
        }

        //span is maximum lat/lon span across visible grid normalised to 1600pixels
        double latspan = (maxlat-minlat) * 1600.0/Math.max(h,w);
        double lonspan = (maxlon-minlon) * 1600.0/Math.max(h,w);

        //grid spacing is power of ten to use for grid interval.
        double latspacing = Math.pow(10,Math.floor(Math.log(latspan)/Math.log(10.0))-1.0);
        double lonspacing = Math.pow(10,Math.floor(Math.log(lonspan)/Math.log(10.0))-1.0);
        if (Math.max(latspan,lonspan)/Math.min(latspan,lonspan)<4){
            lonspacing = latspacing = Math.max(latspacing,lonspacing);
        }
        double latmaj = (latspacing>=10)?3:10;
        double lonmaj = (lonspacing>=10)?3:10;

        //set up stuff need to draw grid
        NumberFormat nf = NumberFormat.getInstance();
        Color mincol = (majcol.darker()).darker();

        g.setFont (new Font("Helvetica", Font.PLAIN, 8));
        FontMetrics fm = g.getFontMetrics();
//      g.setWidth(0);
        for(double lat=latspacing*Math.floor(minlat/latspacing);lat<maxlat;lat+=latspacing){
        for(double lon=lonspacing*Math.floor(minlon/lonspacing);lon<maxlon;lon+=lonspacing){
            LatLon ll0, lli, llj;
            ll0 = gridtoworld.transform(new LatLon(lat,lon));
            lli = gridtoworld.transform(new LatLon(lat+latspacing,lon));
            llj = gridtoworld.transform(new LatLon(lat,lon+lonspacing));
            Point p0=mv.getPoint(ll0);
            Point pi=mv.getPoint(lli);
            Point pj=mv.getPoint(llj);

            if(Math.round(lon/lonspacing)%lonmaj==0)
            g.setColor(majcol);
            else
            g.setColor(mincol);

            drawGridLine(g, mv, ll0, lli);


            if(Math.round(lat/latspacing)%latmaj==0)
            g.setColor(majcol);
            else
            g.setColor(mincol);

            drawGridLine(g, mv, ll0, llj);

            if((Math.round(lon/lonspacing))%lonmaj==0 && (Math.round(lat/latspacing))%latmaj==0 && drawLabels){
            String label = nf.format(lat);
            int tw = fm.stringWidth(label);
            g.drawString(label,p0.x-tw,p0.y-8);
            label = nf.format(lon);
            g.drawString(label,p0.x+2,p0.y+8);
            }
        }
        }

    }

    private void drawGridLine(Graphics g, final MapView mv, LatLon ll0, LatLon ll1){
        Point p0=mv.getPoint(ll0);
        Point p1=mv.getPoint(ll1);

        if(Math.abs(ll0.lon()-ll1.lon())<180){
            g.drawLine(p0.x,p0.y,p1.x,p1.y);
        } else {
            double lat0, lat1, lon0, lon1, latm;
            lon0 = ll0.lon();
            lon1 = ll1.lon();
            if(lon0<0) lon0+=360;
            if(lon1<0) lon1+=360;
            latm = ll0.lat() + (180-lon0)*(ll1.lat()-ll0.lat())/(lon1-lon0);
            Point pm1 = mv.getPoint(new LatLon(latm,180));
            Point pm2 = mv.getPoint(new LatLon(latm,-180));
            if(lon0<=180){
                g.drawLine(p0.x,p0.y,pm1.x,pm1.y);
                g.drawLine(p1.x,p1.y,pm2.x,pm2.y);
            } else {
                g.drawLine(p0.x,p0.y,pm2.x,pm2.y);
                g.drawLine(p1.x,p1.y,pm1.x,pm1.y);
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
                new JMenuItem(new setGridLayoutAction(this)),
                new JMenuItem(new setWorldAction(this)),
                new JMenuItem(new incAAction(this)),
                new JMenuItem(new incBAction(this)),
                new JMenuItem(new incCAction(this)),
                new JMenuItem(new setColorAction(this)),
                new JSeparator(),
                new JMenuItem(new LayerListPopup.InfoAction(this))};
    }
}
