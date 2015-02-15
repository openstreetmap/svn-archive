package org.openstreetmap.josm.plugins.utilsplugin2.multitagger;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Shortcut;

public final class MultiTagAction extends JosmAction {

    MultiTagDialog dlg;

    public MultiTagAction() {
        super(tr("Tag multiple objects [alpha]"), (String)null, tr("Edit tags of object list in table"),
                Shortcut.registerShortcut("multitag", tr("Edit: {0}", tr("Tag multiple objects")), KeyEvent.VK_T, Shortcut.CTRL),
                true, "multitag", true);
        putValue("help", ht("/Action/MultiTag"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        dlg = new MultiTagDialog();
        dlg.selectionChanged(getCurrentDataSet().getSelected());
        dlg.showDialog();
    }


    @Override
    protected void updateEnabledState() {
        setEnabled(getEditLayer()!=null);
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(getEditLayer()!=null);
        if (dlg!=null && dlg.isVisible()) {
            dlg.selectionChanged(selection);
        }
    }
}
