package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.GBC;

public class MenuActionGrabPlanImage extends JosmAction implements Runnable, MouseListener {

    /**
     * Action calling the wms grabber for non georeferenced images called "plan image"
     */
    private static final long serialVersionUID = 1L;

    public static String name = "Georeference an image";
    
    private DownloadWMSPlanImage downloadWMSPlanImage;
    private WMSLayer wmsLayer;
    private int countMouseClicked = 0;
    private int mode = 0;
    private int cGetCorners = 1;
    private int cGetLambertCrosspieces = 2;
    private EastNorth ea1;
    private long mouseClickedTime = 0;
    private EastNorth georefpoint1;
    private EastNorth georefpoint2;
    /**
     * The time which needs to pass between two clicks during georeferencing, in milliseconds
     */
    private int initialClickDelay;

    public MenuActionGrabPlanImage() {
        super(tr(name), "cadastre_small", tr("Grab non-georeferenced image"), null, false);
    }

    public void actionCompleted() {
        countMouseClicked = 0;
        mode = 0;
        mouseClickedTime = System.currentTimeMillis();
    }
    
    public void actionInterrupted() {
        actionCompleted();
        wmsLayer = null;
    }
    
    @Override
    protected void updateEnabledState() {
        if (wmsLayer == null || Main.map == null || Main.map.mapView == null) return;
        if (countMouseClicked == 0 && mode == 0) return;
        for (Layer l : Main.map.mapView.getAllLayersAsList())
            if (l == wmsLayer)
                return;
        JOptionPane.showMessageDialog(Main.parent, tr("Georeferencing interrupted"));
        actionInterrupted();
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (Main.map != null) {
            if (CadastrePlugin.isCadastreProjection()) {
                //wmsLayer = WMSDownloadAction.getLayer();
                wmsLayer = new MenuActionNewLocation().addNewLayer(new ArrayList<WMSLayer>());
                if (wmsLayer == null) return;
                downloadWMSPlanImage = new DownloadWMSPlanImage();
                downloadWMSPlanImage.download(wmsLayer);
                initialClickDelay = Main.pref.getInteger("cadastrewms.georef-click-delay",200);
                // download sub-images of the cadastre scan and join them into one single
                Main.worker.execute(this);
            } else {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("To enable the cadastre WMS plugin, change\n"
                         + "the current projection to one of the cadastre\n"
                         + "projections and retry"));
            }
        }
    }

    public void run() {
        // wait until plan image is fully loaded and joined into one single image
        boolean loadedFromCache = downloadWMSPlanImage.waitFinished();
        if (wmsLayer.images.size() == 1 && !loadedFromCache) {
            mouseClickedTime = System.currentTimeMillis();
            Main.map.mapView.addMouseListener(this);
            if (Main.pref.getBoolean("cadastrewms.noImageCropping", false) == false)
                startCropping();
            else
                startGeoreferencing();
        } else // action cancelled or image loaded from cache (and already georeferenced)
            Main.map.repaint();
    }

    public void mouseClicked(MouseEvent e) {
        if (System.currentTimeMillis() - mouseClickedTime < initialClickDelay) {
            System.out.println("mouse click bounce detected");
            return; // mouse click anti-bounce
        }
        else
            mouseClickedTime = System.currentTimeMillis();
        countMouseClicked++;
        EastNorth ea = Main.proj.latlon2eastNorth(Main.map.mapView.getLatLon(e.getX(), e.getY()));
        System.out.println("clic:"+countMouseClicked+" ,"+ea+", mode:"+mode);
        // ignore clicks outside the image
        if (ea.east() < wmsLayer.images.get(0).min.east() || ea.east() > wmsLayer.images.get(0).max.east()
                || ea.north() < wmsLayer.images.get(0).min.north() || ea.north() > wmsLayer.images.get(0).max.north())
            return;
        if (mode == cGetCorners) {
            if (countMouseClicked == 1) {
                ea1 = ea;
                continueCropping();
            }
            if (countMouseClicked == 2) {
                wmsLayer.cropImage(ea1, ea);
                Main.map.mapView.repaint();
                startGeoreferencing();
            }
        } else if (mode == cGetLambertCrosspieces) {
            if (countMouseClicked == 1) {
                ea1 = ea; 
                if (inputLambertPosition())
                    continueGeoreferencing();
            }
            if (countMouseClicked == 2) {
                if (inputLambertPosition()) {
                    Main.map.mapView.removeMouseListener(this);
                    affineTransform(ea1, ea, georefpoint1, georefpoint2);
                    wmsLayer.saveNewCache();
                    Main.map.mapView.repaint();
                    actionCompleted();
                }
            }
        }
    }
    
    /**
     * 
     * @return false if all operations are canceled
     */
    private boolean startCropping() {
	    mode = cGetCorners;
	    countMouseClicked = 0;
		Object[] options = { "OK", "Cancel" };
		int ret = JOptionPane.showOptionDialog( null, 
				tr("Click first corner for image cropping\n(two points required)"),
				tr("Image cropping"),
	    		JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
	    		null, options, options[0]);
	    if (ret == JOptionPane.OK_OPTION) {
	        mouseClickedTime = System.currentTimeMillis();
	    } else
	    	if (canceledOrRestartCurrAction("image cropping"))
	    		return startCropping();
	    return true;
    }
    
    /**
     * 
     * @return false if all operations are canceled
     */
    private boolean continueCropping() {
		Object[] options = { "OK", "Cancel" };
		int ret = JOptionPane.showOptionDialog( null, 
				tr("Click second corner for image cropping"),
				tr("Image cropping"),
	    		JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
	    		null, options, options[0]);
	    if (ret != JOptionPane.OK_OPTION) {
	    	if (canceledOrRestartCurrAction("image cropping"))
	    		return startCropping();
	    }
	    return true;
    }
    
    /**
     * 
     * @return false if all operations are canceled
     */
    private boolean startGeoreferencing() {
	    countMouseClicked = 0;
	    mode = cGetLambertCrosspieces;
		Object[] options = { "OK", "Cancel" };
		int ret = JOptionPane.showOptionDialog( null, 
				tr("Click first Lambert crosspiece for georeferencing\n(two points required)"),
				tr("Image georeferencing"),
	    		JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
	    		null, options, options[0]);
	    if (ret == JOptionPane.OK_OPTION) {
	        mouseClickedTime = System.currentTimeMillis();
	    } else
	    	if (canceledOrRestartCurrAction("georeferencing"))
	    		return startGeoreferencing();
	    return true;
    }

    /**
     * 
     * @return false if all operations are canceled
     */
    private boolean continueGeoreferencing() {
		Object[] options = { "OK", "Cancel" };
		int ret = JOptionPane.showOptionDialog( null, 
				tr("Click second Lambert crosspiece for georeferencing"),
				tr("Image georeferencing"),
	    		JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
	    		null, options, options[0]);
	    if (ret != JOptionPane.OK_OPTION) {
	    	if (canceledOrRestartCurrAction("georeferencing"))
	    		return startGeoreferencing();
	    }
	    return true;
    }
    
    /**
     * 
     * @return false if all operations are canceled
     */
    private boolean canceledOrRestartCurrAction(String action) {
    	Object[] options = { "Cancel", "Retry" };
    	int selectedValue = JOptionPane.showOptionDialog( null, 
        		tr("Do you want to cancel completely\n"+
        				"or just retry "+action+" ?"), "",
        		JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
        		null, options, options[0]);
        if (selectedValue == 0) { // "Cancel"
        	// remove layer
        	Main.map.mapView.removeLayer(wmsLayer);
            wmsLayer = null;
            Main.map.mapView.removeMouseListener(this);
        	return false;
        } else
            countMouseClicked = 0;
        return true;
    }
    
    private boolean inputLambertPosition() {
        JLabel labelEnterPosition = new JLabel(tr("Enter cadastre east,north position"));
        JLabel labelWarning = new JLabel(tr("(Warning: verify north with arrow !!)"));
        JPanel p = new JPanel(new GridBagLayout());
        JLabel labelEast = new JLabel(tr("East"));
        JLabel labelNorth = new JLabel(tr("North"));
        final JTextField inputEast = new JTextField();
        final JTextField inputNorth = new JTextField();
        p.add(labelEnterPosition, GBC.eol());
        p.add(labelWarning, GBC.eol());
        p.add(labelEast, GBC.std().insets(0, 0, 10, 0));
        p.add(inputEast, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 5, 0, 5));
        p.add(labelNorth, GBC.std().insets(0, 0, 10, 0));
        p.add(inputNorth, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 5, 0, 5));
        JOptionPane pane = new JOptionPane(p, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
        String number;
        if (countMouseClicked == 1) number = "first";
        else number = "second";
        pane.createDialog(Main.parent, tr("Set "+number+" Lambert coordinates")).setVisible(true);
        if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue())) {
            if (canceledOrRestartCurrAction("georeferencing"))
                startGeoreferencing();
            return false;
        }
        if (inputEast.getText().length() != 0 && inputNorth.getText().length() != 0) {
            try {
                double e = Double.parseDouble(inputEast.getText());
                double n = Double.parseDouble(inputNorth.getText());
                if (countMouseClicked == 1)
                    georefpoint1 = new EastNorth(e, n);
                else
                    georefpoint2 = new EastNorth(e, n);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Use point org1 as anchor for scale, then move org1 to dst1, then rotate org2 on dst2 
     * around org1/dst1 anchor 
     * @param org1 first point at original coordinate system (the grabbed image)
     * @param org2 second point "
     * @param dst1 first point at final destination coordinate system (the real east/north coordinate system) 
     * @param dst2 second point "
     */
    private void affineTransform(EastNorth org1, EastNorth org2, EastNorth dst1, EastNorth dst2) {
        double angle = dst1.heading(dst2) - org1.heading(org2);
        double proportion = dst1.distance(dst2)/org1.distance(org2);
        // move
        double dx = dst1.getX() - org1.getX();
        double dy = dst1.getY() - org1.getY();
        wmsLayer.images.get(0).shear(dx, dy);
        org1 = org1.add(dx, dy); // org1=dst1 now
        org2 = org2.add(dx, dy);
        // rotate : org1(=dst1 now) is anchor for rotation and scale
        wmsLayer.images.get(0).rotate(dst1, angle);
        org2 = org2.rotate(dst1, angle);
        // scale image from anchor org1(=dst1 now)
        wmsLayer.images.get(0).scale(dst1, proportion);
    }
    
    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
    }
        
}
