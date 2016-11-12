// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SelectCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.gui.dialogs.relation.GenericRelationEditor;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.pt_assistant.gui.PTAssistantLayer;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Represents tests and fixed of the PT_Assistant plugin
 *
 * @author darya
 *
 */
public abstract class Checker {

    // test which created this WayChecker:
    protected final Test test;

    // node that is checked:
    protected Node node;

    // relation that is checked:
    protected Relation relation;

    // stores all found errors:
    protected ArrayList<TestError> errors = new ArrayList<>();

    protected Checker(Node node, Test test) {
        this.node = node;
        this.test = test;
    }

    protected Checker(Relation relation, Test test) {
        this.relation = relation;
        this.test = test;
    }

    /**
     * Returns errors
     */
    public List<TestError> getErrors() {

        return errors;
    }

    /**
     * Returns a list of stop-related route relation members with corrected
     * roles (if necessary)
     *
     * @return list of stop-related route relation members
     */
    protected static List<RelationMember> listStopMembers(Relation r) {

        List<RelationMember> resultList = new ArrayList<>();

        for (RelationMember rm : r.getMembers()) {

            if (RouteUtils.isPTStop(rm)) {

                if (rm.getMember().hasTag("public_transport", "stop_position")) {
                    if (!rm.hasRole("stop") && !rm.hasRole("stop_entry_only") && !rm.hasRole("stop_exit_only")) {
                        RelationMember newMember = new RelationMember("stop", rm.getMember());
                        resultList.add(newMember);
                    } else {
                        resultList.add(rm);
                    }
                } else { // if platform
                    if (!rm.hasRole("platform") && !rm.hasRole("platform_entry_only")
                            && !rm.hasRole("platform_exit_only")) {
                        RelationMember newMember = new RelationMember("platform", rm.getMember());
                        resultList.add(newMember);
                    } else {
                        resultList.add(rm);
                    }
                }

            }
        }

        return resultList;
    }

    /**
     * Returns a list of other (not stop-related) route relation members with
     * corrected roles (if necessary)
     *
     * @return list of other (not stop-related) route relation members
     */
    protected static List<RelationMember> listNotStopMembers(Relation r) {

        List<RelationMember> resultList = new ArrayList<>();

        for (RelationMember rm : r.getMembers()) {

            if (!RouteUtils.isPTStop(rm)) {

                if (rm.hasRole("forward") || rm.hasRole("backward")) {
                    RelationMember newMember = new RelationMember("", rm.getMember());
                    resultList.add(newMember);
                } else {

                    resultList.add(rm);

                }
            }

        }

        return resultList;
    }

    protected static Command fixErrorByZooming(TestError testError) {

        if (testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_STOP_BY_STOP
                && testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_DIRECTION
                && testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_CONSTRUCTION
                && testError.getCode() != PTAssistantValidatorTest.ERROR_CODE_ROAD_TYPE) {
            return null;
        }

        Collection<? extends OsmPrimitive> primitives = testError.getPrimitives();
        Relation originalRelation = (Relation) primitives.iterator().next();
        ArrayList<OsmPrimitive> primitivesToZoom = new ArrayList<>();
        for (Object primitiveToZoom : testError.getHighlighted()) {
            primitivesToZoom.add((OsmPrimitive) primitiveToZoom);
        }

        SelectCommand command = new SelectCommand(primitivesToZoom);

        List<OsmDataLayer> listOfLayers = Main.getLayerManager().getLayersOfType(OsmDataLayer.class);
        for (OsmDataLayer osmDataLayer : listOfLayers) {
            if (osmDataLayer.data == originalRelation.getDataSet()) {

                final OsmDataLayer layerParameter = osmDataLayer;
                final Relation relationParameter = originalRelation;
                final Collection<OsmPrimitive> zoomParameter = primitivesToZoom;

                if (SwingUtilities.isEventDispatchThread()) {

                    showRelationEditorAndZoom(layerParameter, relationParameter, zoomParameter);

                } else {

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {

                            showRelationEditorAndZoom(layerParameter, relationParameter, zoomParameter);

                        }
                    });

                }

                return command;
            }
        }

        return null;

    }

    private static void showRelationEditorAndZoom(OsmDataLayer layer, Relation r, Collection<OsmPrimitive> primitives) {

        // zoom to problem:
        AutoScaleAction.zoomTo(primitives);

        // put stop-related members to the front and edit roles if necessary:
        List<RelationMember> sortedRelationMembers = listStopMembers(r);
        sortedRelationMembers.addAll(listNotStopMembers(r));
        r.setMembers(sortedRelationMembers);

        // create editor:
        GenericRelationEditor editor = (GenericRelationEditor) RelationEditor.getEditor(layer, r,
                r.getMembersFor(primitives));

        // open editor:
        editor.setVisible(true);

        // make the current relation purple in the pt_assistant layer:
        PTAssistantLayer.getLayer().repaint(r);

    }

}
