// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

/**
 * An action to add or remove (or both) member(s) from the chosen relation.
 * In time should be able to determine correct position for new members.
 * Also, there should be some support for entering a role for new members.
 *
 * @author Zverik
 */
public class AddRemoveMemberAction extends JosmAction implements ChosenRelationListener {
    private ChosenRelation rel;
    private SortAndFixAction sortAndFix;

    public AddRemoveMemberAction(ChosenRelation rel, SortAndFixAction sortAndFix) {
        super(null, "relcontext/addremove", tr("Add/remove members from the chosen relation"),
                Shortcut.registerShortcut("reltoolbox:addremove", tr("Relation Toolbox: {0}", tr("Add/remove members from the chosen relation")),
                        KeyEvent.VK_EQUALS, Shortcut.DIRECT), false);
        this.rel = rel;
        this.sortAndFix = sortAndFix;
        rel.addChosenRelationListener(this);
        updateEnabledState();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (rel.get() == null)
            return;

        Relation r = new Relation(rel.get());

        Collection<OsmPrimitive> toAdd = new ArrayList<>(getLayerManager().getEditDataSet().getSelected());
        toAdd.remove(rel.get());
        toAdd.removeAll(r.getMemberPrimitives());

        // 0. check if relation is broken (temporary)
        boolean isBroken = !toAdd.isEmpty() && sortAndFix.needsFixing(r);

        // 1. remove all present members
        r.removeMembersFor(getLayerManager().getEditDataSet().getSelected());

        // 2. add all new members
        for (OsmPrimitive p : toAdd) {
            int pos = -1; //p instanceof Way ? findAdjacentMember(p, r) : -1;
            if (pos < 0) {
                r.addMember(new RelationMember("", p));
            } else {
                r.addMember(pos, new RelationMember("", p));
            }
        }

        // 3. check for roles again (temporary)
        Command roleFix = !isBroken && sortAndFix.needsFixing(r) ? sortAndFix.fixRelation(r) : null;
        if (roleFix != null) {
            roleFix.executeCommand();
        }

        if (!r.getMemberPrimitives().equals(rel.get().getMemberPrimitives())) {
            UndoRedoHandler.getInstance().add(new ChangeCommand(rel.get(), r));
        }
    }

    /**
     * Finds two relation members between which to place given way. Incomplete.
     * @see org.openstreetmap.josm.gui.dialogs.relation.MemberTableModel#determineDirection
     */
    protected int findAdjacentMember(Way w, Relation r) {
        Node firstNode = w.firstNode();
        Node lastNode = w.lastNode();

        if (firstNode != null && !firstNode.equals(lastNode)) {
            for (int i = 0; i < r.getMembersCount(); i++) {
                if (r.getMember(i).getType().equals(OsmPrimitiveType.WAY)) {
                    Way rw = (Way) r.getMember(i).getMember();
                    Node firstNodeR = rw.firstNode();
                    Node lastNodeR = rw.lastNode();
                    if (firstNode.equals(firstNodeR) || firstNode.equals(lastNodeR) || lastNode.equals(firstNodeR) || lastNode.equals(lastNodeR))
                        return i + 1;
                }
            }
        }
        return -1;
    }

    @Override
    public void chosenRelationChanged(Relation oldRelation, Relation newRelation) {
        updateEnabledState();
    }

    @Override
    protected void updateEnabledState() {
        updateEnabledState(getLayerManager().getEditDataSet() == null ? null : getLayerManager().getEditDataSet().getSelected());
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        updateIcon();
        if (rel == null || rel.get() == null || selection == null || selection.isEmpty()) {
            setEnabled(false);
            return;
        }
        if (selection.size() == 1 && selection.contains(rel.get())) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
    }

    protected void updateIcon() {
        // todo: change icon based on selection
        final int state; // 0=unknown, 1=add, 2=remove, 3=both
        DataSet ds = getLayerManager().getEditDataSet();
        if (ds == null || ds.getSelected() == null
                || ds.getSelected().isEmpty() || rel == null || rel.get() == null) {
            state = 0;
        } else {
            Collection<OsmPrimitive> toAdd = new ArrayList<>(ds.getSelected());
            toAdd.remove(rel.get());
            int selectedSize = toAdd.size();
            if (selectedSize == 0) {
                state = 0;
            } else {
                toAdd.removeAll(rel.get().getMemberPrimitives());
                if (toAdd.isEmpty()) {
                    state = 2;
                } else if (toAdd.size() < selectedSize) {
                    state = 3;
                } else {
                    state = 1;
                }
            }
        }
        GuiHelper.runInEDT(new Runnable() {
            @Override
            public void run() {
                if (state == 0) {
                    putValue(LARGE_ICON_KEY, ImageProvider.get("relcontext", "addremove"));
                } else {
                    String iconName = state == 1 ? "add" : state == 2 ? "remove" : "addremove";
                    putValue(NAME, null);
                    putValue(LARGE_ICON_KEY, ImageProvider.get("relcontext", iconName));
                }
            }
        });
    }
}
