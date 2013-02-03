package org.openstreetmap.josm.plugins.utilsplugin2.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.utilsplugin2.helper.TextTagParser;
import org.openstreetmap.josm.tools.Shortcut;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;
import org.openstreetmap.josm.tools.Utils;

public class PasteTagsExtendedAction extends JosmAction {
    public PasteTagsExtendedAction() {
        super(tr("Paste tags [testing]"), "pastetags", tr("Apply tags parsed from buffer to all selected items.."),
            Shortcut.registerShortcut("tools:pastetags", tr("Tool: {0}", tr("Paste tags")),
            KeyEvent.VK_T, Shortcut.CTRL), true); // TODO: shortcut is temporary, will be on Ctrl-Shift-V
        //putValue("help", ht("/Action/Paste"));
    }
    
    

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();

        if (selection.isEmpty())
            return;
        
        String buf = Utils.getClipboardContent();

        if (buf==null) return;
        List<Command> commands = new ArrayList<Command>();
        for (Tag tag: TextTagParser.readTagsFromText(buf)) {
            commands.add(new ChangePropertyCommand(selection, tag.getKey(), "".equals(tag.getValue())?null:tag.getValue()));
        }
        if (!commands.isEmpty()) {
            String title1 = trn("Pasting {0} tag", "Pasting {0} tags", commands.size(), commands.size());
            String title2 = trn("to {0} object", "to {0} objects", selection.size(), selection.size());
            Main.main.undoRedo.add(
                    new SequenceCommand(
                            title1 + " " + title2,
                            commands
                    ));
        }
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
}
