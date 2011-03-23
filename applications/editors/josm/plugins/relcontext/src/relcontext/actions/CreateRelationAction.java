package relcontext.actions;

import java.awt.Dialog.ModalityType;
import java.awt.GridBagLayout;
import java.util.Collection;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.tools.GBC;
import relcontext.ChosenRelation;

/**
 * Simple create relation with no tags and all selected objects in it with no roles.
 * Choose relation afterwards.
 *
 * @author Zverik
 */
public class CreateRelationAction extends JosmAction {
    private static final String ACTION_NAME = "Create relation";
    protected ChosenRelation chRel;

    public CreateRelationAction( ChosenRelation chRel ) {
        super("+", null, "Create a relation from selected objects", null, false);
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

    private static final List<String> RELATION_TYPES = Arrays.asList(new String[] {
        "multipolygon", "boundary", "route"
    });

    private String askForType() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Choose type for the new relation:")), GBC.eol().insets(0, 0, 0, 5));

        final AutoCompletingComboBox keys = new AutoCompletingComboBox();
        keys.setPossibleItems(RELATION_TYPES);
        keys.setEditable(true);

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
        final JDialog dlg = optionPane.createDialog(Main.parent, tr("Create relation"));
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

        return keys.getEditor().getItem().toString().trim();
    }
}
