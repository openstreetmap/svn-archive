package imageryadjust;

import java.awt.*;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.*;
import java.util.List;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;


public class ImageryAdjustMapMode extends MapMode implements MouseListener, MouseMotionListener, AWTEventListener, MapFrame.MapModeChangeListener{
    boolean mouseDown;
    EastNorth prevEastNorth;
    private ImageryLayer adjustingLayer;
    private MapMode oldMapMode;

    private final TreeSet<Integer> set = new TreeSet<Integer>();
    private Timer timer;
    private KeyEvent releaseEvent;
    
    public ImageryAdjustMapMode(MapFrame mapFrame) {
        super(tr("Adjust imagery"), "adjustimg",
                tr("Adjust the position of the selected imagery layer"), 
                Shortcut.registerShortcut("imageryadjust:adjustmode", tr("imageryadjust"), KeyEvent.VK_Y, Shortcut.GROUP_EDIT),
                mapFrame,
                ImageProvider.getCursor("normal", "move"));
        MapFrame.addMapModeChangeListener(this);
    }
    
    private List<? extends Layer> getVisibleLayers() {
        List<? extends Layer> all = new ArrayList<Layer>(Main.map.mapView.getLayersOfType(ImageryLayer.class));
        Iterator<? extends Layer> it = all.iterator();
        while (it.hasNext()) {
            if (!it.next().isVisible()) it.remove();
        }
        return all;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
    }
    
    
    
    @Override public void enterMode() {
        super.enterMode();
        if (!hasImageryLayersToAdjust()) {
            warnNoImageryLayers();
            return;
        }
        List<ImageryLayer> layers = Main.map.mapView.getLayersOfType(ImageryLayer.class);
        if (layers.size() == 1) {
            adjustingLayer = layers.get(0);
        } else {
            adjustingLayer = (ImageryLayer)askAdjustLayer(getVisibleLayers());
        }
        if (adjustingLayer == null)
            return;
        if (!adjustingLayer.isVisible()) {
            adjustingLayer.setVisible(true);
        }
       Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        timer = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                 timer.stop();
                 if (set.remove(releaseEvent.getKeyCode())) {
                  doKeyReleaseEvent(releaseEvent);
                 }
            }

        });
        
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
        }
        
        
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
        adjustingLayer = null;
        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        } catch (SecurityException ex) {
        }
    }

    @Override public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;

        if (adjustingLayer.isVisible()) {
            prevEastNorth=Main.map.mapView.getEastNorth(e.getX(),e.getY());
            Main.map.mapView.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
    }

    @Override public void mouseDragged(MouseEvent e) {
        if (adjustingLayer == null || prevEastNorth == null) return;
        EastNorth eastNorth=
            Main.map.mapView.getEastNorth(e.getX(),e.getY());
        adjustingLayer.displace(
                eastNorth.east()-prevEastNorth.east(),
                eastNorth.north()-prevEastNorth.north()
        );
        prevEastNorth = eastNorth;
        Main.map.mapView.repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
        Main.map.mapView.repaint();
        Main.map.mapView.setCursor(Cursor.getDefaultCursor());
        prevEastNorth = null;
    }
    
    private void doKeyEvent(KeyEvent keyEvent) {
    }

    private void doKeyReleaseEvent(KeyEvent releaseEvent) {
        if (releaseEvent.getKeyCode() == getShortcut().getKeyStroke().getKeyCode()) {
            if (oldMapMode!=null && !(oldMapMode instanceof ImageryAdjustMapMode))
            Main.map.selectMapMode(oldMapMode);
        }
    }

    @Override public boolean layerIsSupported(Layer l) {
        //return hasImageryLayersToAdjust();
        return true;        
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event instanceof KeyEvent) {
        KeyEvent e=(KeyEvent) event;
        
        if (event.getID() == KeyEvent.KEY_PRESSED) {
             if (timer.isRunning()) {
                  timer.stop();
                } else {
                  if (set.add((e.getKeyCode()))) doKeyEvent((KeyEvent) event);
                }
        }
        if (event.getID() == KeyEvent.KEY_RELEASED) {
            if (timer.isRunning()) {
              timer.stop();
               if (set.remove(e.getKeyCode())) doKeyReleaseEvent(e);
            } else {
              releaseEvent = e;
              timer.restart();
            }
        }
        }
    }

    @Override
    public void mapModeChange(MapMode oldMapMode, MapMode newMapMode) {
        this.oldMapMode = oldMapMode;
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
        if (adjustableLayers.size()==0) return null;
        if (adjustableLayers.size()==1) return adjustableLayers.get(0);
        JComboBox layerList = new JComboBox();
        layerList.setRenderer(new LayerListCellRenderer());
        layerList.setModel(new DefaultComboBoxModel(adjustableLayers.toArray()));
        layerList.setSelectedIndex(0);

        JPanel pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());
        pnl.add(new JLabel(tr("Please select the imagery layer to adjust.")), GBC.eol());
        pnl.add(layerList, GBC.eol());

        ExtendedDialog diag = new ExtendedDialog(
                Main.parent,
                tr("Select imagery layer"),
                new String[] { tr("Start adjusting"),tr("Cancel") }
        );
        diag.setContent(pnl);
        diag.setButtonIcons(new String[] { "mapmode/adjustimg", "cancel" });
        diag.showDialog();
        int decision = diag.getValue();
        if (decision != 1)
            return null;
        Layer adjustLayer = (Layer) layerList.getSelectedItem();
        return adjustLayer;
    }

    /**
     * Displays a warning message if there are no imagery layers to adjust
     *
     */
    protected void warnNoImageryLayers() {
        JOptionPane.showMessageDialog(
                Main.parent,
                tr("There are currently no imagery layer to adjust."),
                tr("No layers to adjust"),
                JOptionPane.WARNING_MESSAGE
        );
    }

    /**
     * Replies true if there is at least one imagery layer
     *
     * @return true if there is at least one imagery layer
     */
    protected boolean hasImageryLayersToAdjust() {
        if (Main.map == null) return false;
        if (Main.map.mapView == null) return false;
        boolean b = ! Main.map.mapView.getLayersOfType(ImageryLayer.class).isEmpty();
        return b;
    }

    @Override
    protected void updateEnabledState() {
        
        setEnabled(hasImageryLayersToAdjust());
        //setEnabled(hasImageryLayersToAdjust());
    }
}
