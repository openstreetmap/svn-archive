package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class Address extends MapMode implements MouseListener, MouseMotionListener, ActionListener {
    private static final long serialVersionUID = 1L;
    
    static final String tagHighway = "highway";
    static final String tagHighwayName = "name";
    static final String tagHouseNumber = "addr:housenumber";
    static final String tagHouseStreet = "addr:street";
    static final String relationAddrType = "associatedStreet";
    static final String relationStreetNameAttr = "name";
    static final String relationAddrStreetRole = "street";
    
    private JRadioButton plus_one = new JRadioButton("+1", false);
    private JRadioButton plus_two = new JRadioButton("+2", true);
    private JRadioButton minus_one = new JRadioButton("-1", false);
    private JRadioButton minus_two = new JRadioButton("-2", false);

    JDialog dialog = null;
    JButton clearButton = null;
    final JTextField inputNumber = new JTextField();
    final JTextField inputStreet = new JTextField();

    public Address(MapFrame mapFrame) {
        super(tr("Add address"), "buildings", 
                tr("Create house number and street name relation"),
                Shortcut.registerShortcut("mapmode:buildings", tr("Mode: {0}", tr("Buildings")), KeyEvent.VK_E, Shortcut.GROUP_EDIT),
                mapFrame, getCursor());
    }

    @Override public void enterMode() {
        super.enterMode();
        if (dialog == null) {
            JPanel p = new JPanel(new GridBagLayout());
            JLabel number = new JLabel(tr("Number"));
            JLabel street = new JLabel(tr("Street"));
            p.add(number, GBC.std().insets(0, 0, 5, 0));
            p.add(inputNumber, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 5, 0, 5));
            p.add(street, GBC.std().insets(0, 0, 5, 0));
            inputStreet.setEditable(false);
            p.add(inputStreet, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 5, 0, 5));
            clearButton = new JButton("Clear");
            clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    inputNumber.setText("");
                    inputStreet.setText("");
                }
            });
            ButtonGroup bgIncremental = new ButtonGroup();
            bgIncremental.add(plus_one);
            bgIncremental.add(plus_two);
            bgIncremental.add(minus_one);
            bgIncremental.add(minus_two);
            p.add(minus_one, GBC.std().insets(10, 0, 10, 0));
            p.add(plus_one, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 0, 0, 0));
            p.add(minus_two, GBC.std().insets(10, 0, 10, 0));
            p.add(plus_two, GBC.std().insets(10, 0, 10, 0));
            p.add(clearButton, GBC.eol().fill(GBC.HORIZONTAL).insets(10, 0, 0, 0));

            final Object[] options = {};
            final JOptionPane pane = new JOptionPane(p,
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION,
                    null, options, null);
            dialog = pane.createDialog(Main.parent, tr("Enter addresses"));
            dialog.setModal(false);
            dialog.setAlwaysOnTop(true);
        }
        dialog.setVisible(true);
        Main.map.mapView.addMouseListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        dialog.setVisible(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        MapView mv = Main.map.mapView;
        Point mousePos = e.getPoint();
        List<Way> mouseOnExistingWays = new ArrayList<Way>();
        mouseOnExistingWays = new ArrayList<Way>();
        Node currentMouseNode = mv.getNearestNode(mousePos, OsmPrimitive.isSelectablePredicate);
        if(currentMouseNode != null) {
            String num = currentMouseNode.get(tagHouseNumber);
            if (num != null) {
                try {
                    Integer.parseInt(num); 
                    inputNumber.setText(num);
                } catch (NumberFormatException en) {
                    System.out.println("Unable to parse house number \"" + num + "\"");
                }
            }
            if (currentMouseNode.get(tagHouseStreet) != null)
                inputStreet.setText(currentMouseNode.get(tagHouseNumber));
            else {
                // check if the node belongs to an associatedStreet relation
                List<OsmPrimitive> l = currentMouseNode.getReferrers();
                for (OsmPrimitive osm : l) {
                    if (osm instanceof Relation && osm.hasKey("type") && osm.get("type").equals(relationAddrType)) {
                        if (osm.hasKey(relationStreetNameAttr)) {
                            inputStreet.setText(osm.get(relationStreetNameAttr));
                            break;
                        } else {
                            for (RelationMember rm : ((Relation)osm).getMembers())
                                if (rm.getRole().equals(relationAddrStreetRole)) {
                                    OsmPrimitive osp = rm.getMember();
                                    if (osp.hasKey(tagHighwayName)) {
                                        inputStreet.setText(osp.get(tagHighwayName));
                                        break;
                                    }
                                }
                        }
                    }
                }
            }
        } else {
            List<WaySegment> wss = mv.getNearestWaySegments(mousePos, OsmPrimitive.isSelectablePredicate);
            for(WaySegment ws : wss) {
                if (ws.way.get(tagHighway) != null && ws.way.get(tagHighwayName) != null)
                    mouseOnExistingWays.add(ws.way);
            }
            if (mouseOnExistingWays.size() == 1) {
                // clicked on existing highway => set new street name
                inputStreet.setText(mouseOnExistingWays.get(0).get(tagHighwayName));
            } else if (mouseOnExistingWays.size() == 0) {
                // clicked a non highway and not a node => add the new address 
                Node n = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isSelectablePredicate);
                if (n != null)
                    if (!n.hasKey(tagHouseNumber))
                        addAddrToNode(n);
                else
                    createNewNode(mousePos);
            }
        }

    }
    
    private void addAddrToNode(Node n) {
        // node exists : just add the tag addr:housenumber and member in relation
    }
    
    private void createNewNode(Point mousePos) {
        
    }

    private static Cursor getCursor() {
        try {
            return ImageProvider.getCursor("crosshair", null);
        } catch (Exception e) {
        }
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }
}
