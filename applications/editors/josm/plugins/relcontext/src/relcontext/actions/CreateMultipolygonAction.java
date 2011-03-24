package relcontext.actions;

import java.awt.Dialog.ModalityType;
import java.awt.GridBagLayout;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.*;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.tools.GBC;
import relcontext.ChosenRelation;

/**
 * Creates new multipolygon from selected ways.
 * Choose relation afterwards.
 *
 * @author Zverik
 */
public class CreateMultipolygonAction extends JosmAction {
    private static final String ACTION_NAME = "Create relation";
    private static final String PREF_MULTIPOLY = "reltoolbox.multipolygon.";
    protected ChosenRelation chRel;

    public CreateMultipolygonAction( ChosenRelation chRel ) {
        super("Multi", "data/multipolygon", tr("Create a multipolygon from selected objects"), null, false);
        this.chRel = chRel;
        updateEnabledState();
    }

    public CreateMultipolygonAction() {
        this(null);
    }
    
    public static boolean getDefaultPropertyValue( String property ) {
        if( property.equals("boundary") ) return false;
        else if( property.equals("boundaryways") ) return true;
        else if( property.equals("tags") ) return false;
        else if( property.equals("single") ) return true;
        throw new IllegalArgumentException(property);
    }

    private boolean getPref( String property ) {
        return Main.pref.getBoolean(PREF_MULTIPOLY + property, getDefaultPropertyValue(property));
    }

    public void actionPerformed( ActionEvent e ) {
        // for now, just copying standard action
        MultipolygonCreate mpc = new MultipolygonCreate();
        String error = mpc.makeFromWays(getCurrentDataSet().getSelectedWays());
        if( error != null ) {
            JOptionPane.showMessageDialog(Main.parent, error);
            return;
        }
        Relation rel = new Relation();
        boolean isBoundary = getPref("boundary");
        if( isBoundary ) {
            rel.put("type", "boundary");
            rel.put("boundary", "administrative");
            askForAdminLevelAndName(rel);
        } else
            rel.put("type", "multipolygon");
        for( MultipolygonCreate.JoinedPolygon poly : mpc.outerWays )
            for( Way w : poly.ways )
                rel.addMember(new RelationMember("outer", w));
        for( MultipolygonCreate.JoinedPolygon poly : mpc.innerWays )
            for( Way w : poly.ways )
                rel.addMember(new RelationMember("inner", w));
        if( isBoundary )
            addBoundaryMembers(rel);
        List<Command> list = removeTagsFromInnerWays(rel);
        if( isBoundary && getPref("boundaryways") )
            list.addAll(fixWayTagsForBoundary(rel));
        list.add(new AddCommand(rel));
        Main.main.undoRedo.add(new SequenceCommand(tr("Create multipolygon"), list));
        
        if( chRel != null )
            chRel.set(rel);
    }

    @Override
    protected void updateEnabledState() {
        if( getCurrentDataSet() == null ) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState( Collection<? extends OsmPrimitive> selection ) {
        boolean enabled = true;
        if( selection == null || selection.isEmpty() )
            enabled = false;
        else {
            if( !getPref("boundary") ) {
                for( OsmPrimitive p : selection ) {
                    if( !(p instanceof Way) ) {
                        enabled = false;
                        break;
                    }
                }
            }
        }
        setEnabled(enabled);
    }

    /**
     * Add selected nodes and relations with corresponding roles.
     */
    private void addBoundaryMembers( Relation rel ) {
        for( OsmPrimitive p : getCurrentDataSet().getSelected() ) {
            String role = null;
            if( p.getType().equals(OsmPrimitiveType.RELATION) ) {
                role = "subarea";
            } else if( p.getType().equals(OsmPrimitiveType.NODE) ) {
                Node n = (Node)p;
                if( !n.isIncomplete() ) {
                    if( n.hasKey("place") )
                        role = "admin_centre";
                    else
                        role = "label";
                }
            }
            if( role != null )
                rel.addMember(new RelationMember(role, p));
        }
    }

    /**
     * For all untagged ways in relation, add tags boundary and admin_level.
     */
    private List<Command> fixWayTagsForBoundary( Relation rel ) {
        List<Command> commands = new ArrayList<Command>();
        if( !rel.hasKey("boundary") || !rel.hasKey("admin_level") )
            return commands;
        String adminLevelStr = rel.get("admin_level");
        int adminLevel = 0;
        try {
            adminLevel = Integer.parseInt(adminLevelStr);
        } catch( NumberFormatException e ) {
            return commands;
        }
        Set<OsmPrimitive> waysBoundary = new HashSet<OsmPrimitive>();
        Set<OsmPrimitive> waysAdminLevel = new HashSet<OsmPrimitive>();
        for( OsmPrimitive p : rel.getMemberPrimitives() ) {
            if( p instanceof Way ) {
                int count = 0;
                if( p.hasKey("boundary") && p.get("boundary").equals("administrative") )
                    count++;
                if( p.hasKey("admin_level") )
                    count++;
                if( p.keySet().size() - count == 0 ) {
                    if( !p.hasKey("boundary") )
                        waysBoundary.add(p);
                    if( !p.hasKey("admin_level") ) {
                        waysAdminLevel.add(p);
                    } else {
                        try {
                            int oldAdminLevel = Integer.parseInt(p.get("admin_level"));
                            if( oldAdminLevel > adminLevel )
                                waysAdminLevel.add(p);
                        } catch( NumberFormatException e ) {
                            waysAdminLevel.add(p); // some garbage, replace it
                        }
                    }
                }
            }
        }
        if( !waysBoundary.isEmpty() )
            commands.add(new ChangePropertyCommand(waysBoundary, "boundary", "administrative"));
        if( !waysAdminLevel.isEmpty() )
            commands.add(new ChangePropertyCommand(waysAdminLevel, "admin_level", adminLevelStr));
        return commands;
    }

    /**
     * This method removes tags/value pairs from inner ways that are present in relation or outer ways.
     * It was copypasted from the standard {@link org.openstreetmap.josm.actions.CreateMultipolygonAction}.
     * Todo: rewrite it.
     */
    private List<Command> removeTagsFromInnerWays(Relation relation) {
        Map<String, String> values = new HashMap<String, String>();

        if (relation.hasKeys()){
            for(String key: relation.keySet()) {
                values.put(key, relation.get(key));
            }
        }

        List<Way> innerWays = new ArrayList<Way>();

        for (RelationMember m: relation.getMembers()) {

            if (m.hasRole() && m.getRole() == "inner" && m.isWay() && m.getWay().hasKeys()) {
                innerWays.add(m.getWay());
            }

            if (m.hasRole() && m.getRole() == "outer" && m.isWay() && m.getWay().hasKeys()) {
                Way way = m.getWay();
                for (String key: way.keySet()) {
                    if (!values.containsKey(key)) { //relation values take precedence
                        values.put(key, way.get(key));
                    }
                }
            }
        }

        List<Command> commands = new ArrayList<Command>();

        for(String key: values.keySet()) {
            List<OsmPrimitive> affectedWays = new ArrayList<OsmPrimitive>();
            String value = values.get(key);

            for (Way way: innerWays) {
                if (value.equals(way.get(key))) {
                    affectedWays.add(way);
                }
            }

            if (affectedWays.size() > 0) {
                commands.add(new ChangePropertyCommand(affectedWays, key, null));
            }
        }

        return commands;
    }

    private void askForAdminLevelAndName( Relation rel ) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Enter admin level and name for the border relation:")), GBC.eol().insets(0, 0, 0, 5));

        final JTextField admin = new JTextField();
        admin.setText(Main.pref.get(PREF_MULTIPOLY + "lastadmin", ""));
        panel.add(new JLabel(tr("Admin level")), GBC.std());
        panel.add(Box.createHorizontalStrut(10), GBC.std());
        panel.add(admin, GBC.eol().fill(GBC.HORIZONTAL));

        final JTextField name = new JTextField();
        panel.add(new JLabel(tr("Name")), GBC.std());
        panel.add(Box.createHorizontalStrut(10), GBC.std());
        panel.add(name, GBC.eol().fill(GBC.HORIZONTAL));

        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            @Override
            public void selectInitialValue() {
                admin.requestFocusInWindow();
                admin.selectAll();
            }
        };
        final JDialog dlg = optionPane.createDialog(Main.parent, tr("Create relation"));
        dlg.setModalityType(ModalityType.DOCUMENT_MODAL);

        name.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dlg.setVisible(false);
                optionPane.setValue(JOptionPane.OK_OPTION);
            }
        });

        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if( answer == null || answer == JOptionPane.UNINITIALIZED_VALUE
                || (answer instanceof Integer && (Integer)answer != JOptionPane.OK_OPTION) ) {
            return;
        }

        String admin_level = admin.getText().trim();
        String new_name = name.getText().trim();
        if( admin_level.equals("10") || (admin_level.length() == 1 && Character.isDigit(admin_level.charAt(0))) ) {
            rel.put("admin_level", admin_level);
            Main.pref.put(PREF_MULTIPOLY + "lastadmin", admin_level);
        }
        if( new_name.length() > 0 )
            rel.put("name", new_name);
    }
}
