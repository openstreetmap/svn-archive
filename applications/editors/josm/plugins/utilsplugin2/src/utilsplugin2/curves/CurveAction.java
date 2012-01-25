// License: GPL. Copyright 2011 by Ole Jørgen Brønner
package utilsplugin2.curves;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;

// TODO: investigate splines

public class CurveAction extends JosmAction {

    private static final long serialVersionUID = 1L;

    private int angleSeparation = -1;

    public CurveAction() {
        super(tr("Circle arc"), "circlearc", tr("Create a circle arc"),
                Shortcut.registerShortcut("tools:createcurve", tr("Tool: {0}", tr("Create a circle arc")), KeyEvent.VK_C,
                        Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
        putValue("help", ht("/Action/CreateCircleArc"));
        updatePreferences();
    }

    private void updatePreferences() {
        // @formatter:off
        angleSeparation = Main.pref.getInteger(prefKey("circlearc.angle-separation"), 20);
        // @formatter:on
    }

    private String prefKey(String subKey) {
        return "curves." + subKey;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;

        updatePreferences();

        List<Node> selectedNodes = new ArrayList<Node>(getCurrentDataSet().getSelectedNodes());
        List<Way> selectedWays = new ArrayList<Way>(getCurrentDataSet().getSelectedWays());

        // Collection<Command> cmds = doSpline(selectedNodes, selectedWays);
        Collection<Command> cmds = CircleArcMaker.doCircleArc(selectedNodes, selectedWays, angleSeparation);
        if (cmds != null)
            Main.main.undoRedo.add(new SequenceCommand("Create a curve", cmds));
    }

    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }

    public static void main(String[] args) {
    }

}
