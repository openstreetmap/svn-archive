// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dialog.ModalityType;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.MultipolygonBuilder;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;

import relcontext.ChosenRelation;

/**
 * Creates new multipolygon from selected ways.
 * Choose relation afterwards.
 *
 * @author Zverik
 */
public class CreateMultipolygonAction extends JosmAction {
    private static final String PREF_MULTIPOLY = "reltoolbox.multipolygon.";
    protected ChosenRelation chRel;

    public CreateMultipolygonAction(ChosenRelation chRel) {
        super("Multi", "data/multipolygon", tr("Create a multipolygon from selected objects"),
                Shortcut.registerShortcut("reltoolbox:multipolygon", tr("Relation Toolbox: {0}", tr("Create multipolygon")),
                        KeyEvent.VK_A, Shortcut.ALT_CTRL), false);
        this.chRel = chRel;
        updateEnabledState();
    }

    public CreateMultipolygonAction() {
        this(null);
    }

    public static boolean getDefaultPropertyValue(String property) {
        if (property.equals("boundary"))
            return false;
        else if (property.equals("boundaryways"))
            return true;
        else if (property.equals("tags"))
            return true;
        else if (property.equals("alltags"))
            return false;
        else if (property.equals("single"))
            return true;
        else if (property.equals("allowsplit"))
            return false;
        throw new IllegalArgumentException(property);
    }

    private boolean getPref(String property) {
        return Config.getPref().getBoolean(PREF_MULTIPOLY + property, getDefaultPropertyValue(property));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isBoundary = getPref("boundary");
        DataSet ds = getLayerManager().getEditDataSet();
        Collection<Way> selectedWays = ds.getSelectedWays();
        if (!isBoundary && getPref("tags")) {
            List<Relation> rels = null;
            if (getPref("allowsplit") || selectedWays.size() == 1) {
                if (SplittingMultipolygons.canProcess(selectedWays)) {
                    rels = SplittingMultipolygons.process(ds.getSelectedWays());
                }
            } else {
                if (TheRing.areAllOfThoseRings(selectedWays)) {
                    List<Command> commands = new ArrayList<>();
                    rels = TheRing.makeManySimpleMultipolygons(ds.getSelectedWays(), commands);
                    if (!commands.isEmpty()) {
                        UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Create multipolygons from rings"), commands));
                    }
                }
            }
            if (rels != null && !rels.isEmpty()) {
                if (chRel != null) {
                    chRel.set(rels.size() == 1 ? rels.get(0) : null);
                }
                if (rels.size() == 1) {
                    ds.setSelected(rels);
                } else {
                    ds.clearSelection();
                }
                return;
            }
        }

        // for now, just copying standard action
        MultipolygonBuilder mpc = new MultipolygonBuilder();
        String error = mpc.makeFromWays(ds.getSelectedWays());
        if (error != null) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), error);
            return;
        }
        Relation rel = new Relation();
        if (isBoundary) {
            rel.put("type", "boundary");
            rel.put("boundary", "administrative");
        } else {
            rel.put("type", "multipolygon");
        }
        for (MultipolygonBuilder.JoinedPolygon poly : mpc.outerWays) {
            for (Way w : poly.ways) {
                rel.addMember(new RelationMember("outer", w));
            }
        }
        for (MultipolygonBuilder.JoinedPolygon poly : mpc.innerWays) {
            for (Way w : poly.ways) {
                rel.addMember(new RelationMember("inner", w));
            }
        }
        List<Command> list = removeTagsFromInnerWays(rel);
        if (!list.isEmpty() && isBoundary) {
            UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Move tags from ways to relation"), list));
            list = new ArrayList<>();
        }
        if (isBoundary) {
            if (!askForAdminLevelAndName(rel))
                return;
            addBoundaryMembers(rel);
            if (getPref("boundaryways")) {
                list.addAll(fixWayTagsForBoundary(rel));
            }
        }
        list.add(new AddCommand(ds, rel));
        UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Create multipolygon"), list));

        if (chRel != null) {
            chRel.set(rel);
        }

        ds.setSelected(rel);
    }

    @Override
    protected void updateEnabledState() {
        if (getLayerManager().getEditDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getLayerManager().getEditDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        boolean isEnabled = true;
        if (selection == null || selection.isEmpty()) {
            isEnabled = false;
        } else {
            if (!getPref("boundary")) {
                for (OsmPrimitive p : selection) {
                    if (!(p instanceof Way)) {
                        isEnabled = false;
                        break;
                    }
                }
            }
        }
        setEnabled(isEnabled);
    }

    /**
     * Add selected nodes and relations with corresponding roles.
     */
    private void addBoundaryMembers(Relation rel) {
        for (OsmPrimitive p : getLayerManager().getEditDataSet().getSelected()) {
            String role = null;
            if (p.getType().equals(OsmPrimitiveType.RELATION)) {
                role = "subarea";
            } else if (p.getType().equals(OsmPrimitiveType.NODE)) {
                Node n = (Node) p;
                if (!n.isIncomplete()) {
                    if (n.hasKey("place")) {
                        role = "admin_centre";
                    } else {
                        role = "label";
                    }
                }
            }
            if (role != null) {
                rel.addMember(new RelationMember(role, p));
            }
        }
    }

    /**
     * For all untagged ways in relation, add tags boundary and admin_level.
     */
    private List<Command> fixWayTagsForBoundary(Relation rel) {
        List<Command> commands = new ArrayList<>();
        if (!rel.hasKey("boundary") || !rel.hasKey("admin_level"))
            return commands;
        String adminLevelStr = rel.get("admin_level");
        int adminLevel = 0;
        try {
            adminLevel = Integer.parseInt(adminLevelStr);
        } catch (NumberFormatException e) {
            return commands;
        }
        Set<OsmPrimitive> waysBoundary = new HashSet<>();
        Set<OsmPrimitive> waysAdminLevel = new HashSet<>();
        for (OsmPrimitive p : rel.getMemberPrimitives()) {
            if (p instanceof Way) {
                int count = 0;
                if (p.hasKey("boundary") && p.get("boundary").equals("administrative")) {
                    count++;
                }
                if (p.hasKey("admin_level")) {
                    count++;
                }
                if (p.keySet().size() - count == 0) {
                    if (!p.hasKey("boundary")) {
                        waysBoundary.add(p);
                    }
                    if (!p.hasKey("admin_level")) {
                        waysAdminLevel.add(p);
                    } else {
                        try {
                            int oldAdminLevel = Integer.parseInt(p.get("admin_level"));
                            if (oldAdminLevel > adminLevel) {
                                waysAdminLevel.add(p);
                            }
                        } catch (NumberFormatException e) {
                            waysAdminLevel.add(p); // some garbage, replace it
                        }
                    }
                }
            }
        }
        if (!waysBoundary.isEmpty()) {
            commands.add(new ChangePropertyCommand(waysBoundary, "boundary", "administrative"));
        }
        if (!waysAdminLevel.isEmpty()) {
            commands.add(new ChangePropertyCommand(waysAdminLevel, "admin_level", adminLevelStr));
        }
        return commands;
    }

    public static final List<String> DEFAULT_LINEAR_TAGS = Arrays.asList(new String[] {"barrier", "source"});

    private static final Set<String> REMOVE_FROM_BOUNDARY_TAGS = new TreeSet<>(Arrays.asList(new String[] {
            "boundary", "boundary_type", "type", "admin_level"
    }));

    /**
     * This method removes tags/value pairs from inner ways that are present in relation or outer ways.
     * It was copypasted from the standard {@link org.openstreetmap.josm.actions.CreateMultipolygonAction}.
     * Todo: rewrite it.
     */
    private List<Command> removeTagsFromInnerWays(Relation relation) {
        Map<String, String> values = new HashMap<>();

        if (relation.hasKeys()) {
            for (String key : relation.keySet()) {
                values.put(key, relation.get(key));
            }
        }

        List<Way> innerWays = new ArrayList<>();
        List<Way> outerWays = new ArrayList<>();

        Set<String> conflictingKeys = new TreeSet<>();

        for (RelationMember m : relation.getMembers()) {

            if (m.hasRole() && "inner".equals(m.getRole()) && m.isWay() && m.getWay().hasKeys()) {
                innerWays.add(m.getWay());
            }

            if (m.hasRole() && "outer".equals(m.getRole()) && m.isWay() && m.getWay().hasKeys()) {
                Way way = m.getWay();
                outerWays.add(way);
                for (String key : way.keySet()) {
                    if (!values.containsKey(key)) { //relation values take precedence
                        values.put(key, way.get(key));
                    } else if (!relation.hasKey(key) && !values.get(key).equals(way.get(key))) {
                        conflictingKeys.add(key);
                    }
                }
            }
        }

        // filter out empty key conflicts - we need second iteration
        boolean isBoundary = getPref("boundary");
        if (isBoundary || !getPref("alltags")) {
            for (RelationMember m : relation.getMembers()) {
                if (m.hasRole() && m.getRole().equals("outer") && m.isWay()) {
                    for (String key : values.keySet()) {
                        if (!m.getWay().hasKey(key) && !relation.hasKey(key)) {
                            conflictingKeys.add(key);
                        }
                    }
                }
            }
        }

        for (String key : conflictingKeys) {
            values.remove(key);
        }

        for (String linearTag : Config.getPref().getList(PREF_MULTIPOLY + "lineartags", DEFAULT_LINEAR_TAGS)) {
            values.remove(linearTag);
        }

        if (values.containsKey("natural") && values.get("natural").equals("coastline")) {
            values.remove("natural");
        }

        String name = values.get("name");
        if (isBoundary) {
            Set<String> keySet = new TreeSet<>(values.keySet());
            for (String key : keySet) {
                if (!REMOVE_FROM_BOUNDARY_TAGS.contains(key)) {
                    values.remove(key);
                }
            }
        }

        values.put("area", "yes");

        List<Command> commands = new ArrayList<>();
        boolean moveTags = getPref("tags");

        for (String key : values.keySet()) {
            List<OsmPrimitive> affectedWays = new ArrayList<>();
            String value = values.get(key);

            for (Way way : innerWays) {
                if (way.hasKey(key) && (isBoundary || value.equals(way.get(key)))) {
                    affectedWays.add(way);
                }
            }

            if (moveTags) {
                // remove duplicated tags from outer ways
                for (Way way : outerWays) {
                    if (way.hasKey(key)) {
                        affectedWays.add(way);
                    }
                }
            }

            if (affectedWays.size() > 0) {
                commands.add(new ChangePropertyCommand(affectedWays, key, null));
            }
        }

        if (moveTags) {
            // add those tag values to the relation
            if (isBoundary) {
                values.put("name", name);
            }
            boolean fixed = false;
            Relation r2 = new Relation(relation);
            for (String key : values.keySet()) {
                if (!r2.hasKey(key) && !key.equals("area")
                        && (!isBoundary || key.equals("admin_level") || key.equals("name"))) {
                    if (relation.isNew()) {
                        relation.put(key, values.get(key));
                    } else {
                        r2.put(key, values.get(key));
                    }
                    fixed = true;
                }
            }
            if (fixed && !relation.isNew()) {
                commands.add(new ChangeCommand(relation, r2));
            }
        }

        return commands;
    }

    /**
     *
     * @param rel relation
     * @return false if user pressed "cancel".
     */
    private boolean askForAdminLevelAndName(Relation rel) {
        String relAL = rel.get("admin_level");
        String relName = rel.get("name");
        if (relAL != null && relName != null)
            return true;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Enter admin level and name for the border relation:")), GBC.eol().insets(0, 0, 0, 5));

        final JTextField admin = new JTextField();
        admin.setText(relAL != null ? relAL : Config.getPref().get(PREF_MULTIPOLY + "lastadmin", ""));
        panel.add(new JLabel(tr("Admin level")), GBC.std());
        panel.add(Box.createHorizontalStrut(10), GBC.std());
        panel.add(admin, GBC.eol().fill(GBC.HORIZONTAL).insets(0, 0, 0, 5));

        final JTextField name = new JTextField();
        if (relName != null) {
            name.setText(relName);
        }
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
        final JDialog dlg = optionPane.createDialog(MainApplication.getMainFrame(), tr("Create a new relation"));
        dlg.setModalityType(ModalityType.DOCUMENT_MODAL);

        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.setVisible(false);
                optionPane.setValue(JOptionPane.OK_OPTION);
            }
        });

        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if (answer == null || answer == JOptionPane.UNINITIALIZED_VALUE
                || (answer instanceof Integer && (Integer) answer != JOptionPane.OK_OPTION))
            return false;

        String admin_level = admin.getText().trim();
        String new_name = name.getText().trim();
        if (admin_level.equals("10") || (admin_level.length() == 1 && Character.isDigit(admin_level.charAt(0)))) {
            rel.put("admin_level", admin_level);
            Config.getPref().put(PREF_MULTIPOLY + "lastadmin", admin_level);
        }
        if (new_name.length() > 0) {
            rel.put("name", new_name);
        }
        return true;
    }
}
