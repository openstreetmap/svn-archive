package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.validator.util.AgregatePrimitivesVisitor;

/**
 * The action that does the validate thing.
 * <p>
 * This action iterates through all active tests and give them the data, so that
 * each one can test it.
 *
 * @author frsantos
 */
public class ValidateAction extends JosmAction {
    private OSMValidatorPlugin plugin;

    /** Serializable ID */
    private static final long serialVersionUID = -2304521273582574603L;

    /** Last selection used to validate */
    private Collection<OsmPrimitive> lastSelection;

    /**
     * Constructor
     */
    public ValidateAction(OSMValidatorPlugin plugin) {
        super(tr("Validation"), "validator", tr("Performs the data validation"), KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK
                + KeyEvent.ALT_MASK, true);
        this.plugin = plugin;
    }

    public void actionPerformed(ActionEvent ev) {
        doValidate(ev, true);
    }

    /**
     * Does the validation.
     * <p>
     * If getSelectedItems is true, the selected items (or all items, if no one
     * is selected) are validated. If it is false, last selected items are
     * revalidated
     *
     * @param ev The event
     * @param getSelectedItems If selected or last selected items must be validated
     */
    public void doValidate(ActionEvent ev, boolean getSelectedItems) {
        if (plugin.validateAction == null || Main.map == null || !Main.map.isVisible())
            return;

        OSMValidatorPlugin.plugin.initializeErrorLayer();

        Collection<Test> tests = OSMValidatorPlugin.getEnabledTests(false);
        if (tests.isEmpty())
            return;

        Collection<OsmPrimitive> selection;
        if (getSelectedItems) {
            selection = Main.ds.getSelected();
            if (selection.isEmpty()) {
                selection = Main.ds.allNonDeletedPrimitives();
                lastSelection = null;
            } else {
                AgregatePrimitivesVisitor v = new AgregatePrimitivesVisitor();
                selection = v.visit(selection);
                lastSelection = selection;
            }
        } else {
            if (lastSelection == null)
                selection = Main.ds.allNonDeletedPrimitives();
            else
                selection = lastSelection;
        }

        List<TestError> errors = new ArrayList<TestError>();
        for (Test test : tests) {
            test.setPartialSelection(lastSelection != null);
            test.startTest();
            test.visit(selection);
            test.endTest();
            errors.addAll(test.getErrors());
        }
        tests = null;
        if (Main.pref.getBoolean(PreferenceEditor.PREF_USE_IGNORE, true)) {
            for (TestError error : errors) {
                List<String> s = new ArrayList<String>();
                s.add(error.getIgnoreState());
                s.add(error.getIgnoreGroup());
                s.add(error.getIgnoreSubGroup());
                for (String state : s) {
                    if (state != null && plugin.ignoredErrors.contains(state)) {
                        error.setIgnored(true);
                    }
                }
            }
        }

        plugin.validationDialog.tree.setErrors(errors);
        plugin.validationDialog.setVisible(true);
        DataSet.fireSelectionChanged(Main.ds.getSelected());
    }
}
