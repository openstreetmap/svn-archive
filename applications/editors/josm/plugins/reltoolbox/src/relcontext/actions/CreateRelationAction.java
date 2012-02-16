package relcontext.actions;

import java.awt.Dialog.ModalityType;
import java.awt.GridBagLayout;
import java.util.Collection;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;
import relcontext.ChosenRelation;

/**
 * Simple create relation with no tags and all selected objects in it with no roles.
 * Choose relation afterwards.
 *
 * @author Zverik
 */
public class CreateRelationAction extends JosmAction {
    private static final String PREF_LASTTYPE = "reltoolbox.createrelation.lasttype";
    protected ChosenRelation chRel;

    public CreateRelationAction( ChosenRelation chRel ) {
        super(tr("New"), "data/relation", tr("Create a relation from selected objects"),
                Shortcut.registerShortcut("reltoolbox:create", tr("Relation Toolbox: {0}", tr("Create a new relation")),
                KeyEvent.VK_N, Shortcut.GROUP_HOTKEY+Shortcut.GROUPS_ALT2), true);
        this.chRel = chRel;
        updateEnabledState();
    }

    public CreateRelationAction() {
        this(null);
    }

    public void actionPerformed( ActionEvent e ) {
        String type = askForType();
        if( type == null )
            return;

        Relation rel = new Relation();
        if( type.length() > 0 )
            rel.put("type", type);
        for( OsmPrimitive selected : getCurrentDataSet().getSelected() )
            rel.addMember(new RelationMember("", selected));

        Main.main.undoRedo.add(new AddCommand(rel));

        if( chRel != null ) {
            chRel.set(rel);
        }
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
        setEnabled(selection != null && !selection.isEmpty());
    }

    // Thanks to TagInfo for the list
    private static final List<String> RELATION_TYPES = Arrays.asList(new String[] {
        "multipolygon", "boundary", "route", "site", "restriction", "associatedStreet", "public_transport",
        "street", "collection", "address", "enforcement", "destination_sign", "route_master", "junction",
        "waterway", "bridge", "tunnel", "surveillance"
    });

    private String askForType() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Choose a type for the relation:")), GBC.eol().insets(0, 0, 0, 5));

        final AutoCompletingComboBox keys = new AutoCompletingComboBox();
        keys.setPossibleItems(RELATION_TYPES);
        keys.setEditable(true);
        keys.getEditor().setItem(Main.pref.get(PREF_LASTTYPE, "multipolygon"));

        panel.add(new JLabel(tr("Type")), GBC.std());
        panel.add(Box.createHorizontalStrut(10), GBC.std());
        panel.add(keys, GBC.eol().fill(GBC.HORIZONTAL));

        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            @Override
            public void selectInitialValue() {
                keys.requestFocusInWindow();
                keys.getEditor().selectAll();
            }
        };
        final JDialog dlg = optionPane.createDialog(Main.parent, tr("Create a new relation"));
        dlg.setModalityType(ModalityType.DOCUMENT_MODAL);

        keys.getEditor().addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dlg.setVisible(false);
                optionPane.setValue(JOptionPane.OK_OPTION);
            }
        });

        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if( answer == null || answer == JOptionPane.UNINITIALIZED_VALUE
                || (answer instanceof Integer && (Integer)answer != JOptionPane.OK_OPTION) ) {
            return null;
        }

        String result = keys.getEditor().getItem().toString().trim();
        Main.pref.put(PREF_LASTTYPE, result);
        return result;
    }
}
