package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;


public class WMSAdjustAction extends MapMode implements
    MouseListener, MouseMotionListener{

    GeorefImage selectedImage;
    boolean mouseDown;
    EastNorth prevEastNorth;
    private WMSLayer adjustingLayer;

    public WMSAdjustAction(MapFrame mapFrame) {
        super(tr("Adjust WMS"), "adjustwms",
                        tr("Adjust the position of the selected WMS layer"), mapFrame,
                        ImageProvider.getCursor("normal", "move"));
    }

    
    
    @Override public void enterMode() {
        super.enterMode();       
        if (!hasWMSLayersToAdjust()) {
        	warnNoWMSLayers();
        	return;
        }
        List<WMSLayer> wmsLayers = Main.map.mapView.getLayersOfType(WMSLayer.class);
        if (wmsLayers.size() == 1) {
        	adjustingLayer = wmsLayers.get(0);
        } else {
        	adjustingLayer = (WMSLayer)askAdjustLayer(Main.map.mapView.getLayersOfType(WMSLayer.class));
        }
        if (adjustingLayer == null)
        	return;
        if (!adjustingLayer.isVisible()) {
        	adjustingLayer.setVisible(true);
        }
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
        adjustingLayer = null;
    }

    @Override public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;

        if (adjustingLayer.isVisible()) {
            prevEastNorth=Main.map.mapView.getEastNorth(e.getX(),e.getY());
            selectedImage = adjustingLayer.findImage(prevEastNorth);
            if(selectedImage!=null) {
                Main.map.mapView.setCursor
                    (Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        }
    }

    @Override public void mouseDragged(MouseEvent e) {
        if(selectedImage!=null) {
            EastNorth eastNorth=
                    Main.map.mapView.getEastNorth(e.getX(),e.getY());
            adjustingLayer.displace(
                eastNorth.east()-prevEastNorth.east(),
                eastNorth.north()-prevEastNorth.north()
            );
            prevEastNorth = eastNorth;
            Main.map.mapView.repaint();
        }
    }

    @Override public void mouseReleased(MouseEvent e) {
        Main.map.mapView.repaint();
        Main.map.mapView.setCursor(Cursor.getDefaultCursor());
        selectedImage = null;
        prevEastNorth = null;
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override public void mouseClicked(MouseEvent e) {
    }

    // This only makes the buttons look disabled, but since no keyboard shortcut is
    // provided there aren't any other means to activate this tool
    @Override public boolean layerIsSupported(Layer l) {
        return (l instanceof WMSLayer) && l.isVisible();
    }
    
    /**
    * the list cell renderer used to render layer list entries
    * 
    */
   static public class LayerListCellRenderer extends DefaultListCellRenderer {

       protected boolean isActiveLayer(Layer layer) {
           if (Main.map == null)
               return false;
           if (Main.map.mapView == null)
               return false;
           return Main.map.mapView.getActiveLayer() == layer;
       }

       @Override
       public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
               boolean cellHasFocus) {
           Layer layer = (Layer) value;
           JLabel label = (JLabel) super.getListCellRendererComponent(list, layer.getName(), index, isSelected,
                   cellHasFocus);
           Icon icon = layer.getIcon();
           label.setIcon(icon);
           label.setToolTipText(layer.getToolTipText());
           return label;
       }
   }

   /**
    * Prompts the user with a list of WMS layers which can be adjusted  
    * 
    * @param adjustableLayers the list of adjustable layers 
    * @return  the selected layer; null, if no layer was selected 
    */
   protected Layer askAdjustLayer(List<? extends Layer> adjustableLayers) {
       JComboBox layerList = new JComboBox();
       layerList.setRenderer(new LayerListCellRenderer());
       layerList.setModel(new DefaultComboBoxModel(adjustableLayers.toArray()));
       layerList.setSelectedIndex(0);
   
       JPanel pnl = new JPanel();
       pnl.setLayout(new GridBagLayout());
       pnl.add(new JLabel(tr("Please select the WMS layer to adjust.")), GBC.eol());
       pnl.add(layerList, GBC.eol());
   
       ExtendedDialog diag = new ExtendedDialog(
    		   Main.parent, 
    		   tr("Select WMS layer"), 
    		   new String[] { tr("Start adjusting"),tr("Cancel") }
    		   );
       diag.setContent(pnl);
       diag.setButtonIcons(new String[] { "mapmode/adjustwms", "cancel" });
       diag.showDialog();
       int decision = diag.getValue();
       if (decision != 1)
           return null;
       Layer adjustLayer = (Layer) layerList.getSelectedItem();
       return adjustLayer;
   }

   /**
    * Displays a warning message if there are no WMS layers to adjust 
    * 
    */
   protected void warnNoWMSLayers() {
       JOptionPane.showMessageDialog(
    		   Main.parent,
               tr("There are currently no WMS layer to adjust."),
               tr("No layers to adjust"), 
               JOptionPane.WARNING_MESSAGE
       );
   }
   
   /**
    * Replies true if there is at least one WMS layer 
    * 
    * @return true if there is at least one WMS layer
    */
   protected boolean hasWMSLayersToAdjust() {
	   if (Main.map == null) return false;
	   if (Main.map.mapView == null) return false;
	   return ! Main.map.mapView.getLayersOfType(WMSLayer.class).isEmpty();
   }



	@Override
	protected void updateEnabledState() {
		setEnabled(hasWMSLayersToAdjust());
	}   
}
