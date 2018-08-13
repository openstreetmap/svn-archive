// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions.upload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PrimitiveRenderer;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.tools.GBC;

/**
 * This hook is called at JOSM upload and will check if new nodes and ways provide
 * a tag "source=". If not and if auto-sourcing is enabled, it will add
 * automatically a tag "source"="Cadastre..." as defined in the plugin preferences.
 */
public class CheckSourceUploadHook implements UploadHook {

    /**
     * Add the tag "source" if it doesn't exist for all new Nodes and Ways before uploading
     */
    @Override
    public boolean checkUpload(APIDataSet apiDataSet) {
        if (CadastrePlugin.autoSourcing && CadastrePlugin.pluginUsed && !apiDataSet.getPrimitivesToAdd().isEmpty()) {
            Collection<OsmPrimitive> sel = new HashSet<>();
            for (OsmPrimitive osm : apiDataSet.getPrimitivesToAdd()) {
                if ((osm instanceof Way && (osm.getKeys().size() == 0 || !tagSourceExist(osm)))
                 || (osm instanceof Node && osm.getKeys().size() > 0 && !tagSourceExist(osm))) {
                    sel.add(osm);
                }
            }
            if (!sel.isEmpty()) {
                displaySource(sel);
            }
        }
        return true;
    }

    /**
     * Check whenever one of the keys of the object is "source"
     * @param osm primitive to check
     * @return true if one of keys is "source"
     */
    private boolean tagSourceExist(OsmPrimitive osm) {
        return osm.hasKey("source");
    }

    /**
     * Displays a screen with the list of objects which will be tagged with
     * source="cadastre.." if it is approved.
     * @param sel the list of elements added without a key "source"
     */
    private void displaySource(Collection<OsmPrimitive> sel) {
        if (!sel.isEmpty()) {
            JPanel p = new JPanel(new GridBagLayout());
            PrimitiveRenderer renderer = new PrimitiveRenderer();
            p.add(new JLabel(tr("Add \"source=...\" to elements?")), GBC.eol());
            JTextField tf = new JTextField(CadastrePlugin.source);
            p.add(tf, GBC.eol());
            JList<OsmPrimitive> l = new JList<>(sel.toArray(new OsmPrimitive[0]));
            l.setCellRenderer(renderer);
            l.setVisibleRowCount(l.getModel().getSize() < 6 ? l.getModel().getSize() : 10);
            p.add(new JScrollPane(l), GBC.eol().fill());
            boolean bContinue = JOptionPane.showConfirmDialog(MainApplication.getMainFrame(), p, tr("Add \"source=...\" to elements?"),
                   JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if (bContinue)
                UndoRedoHandler.getInstance().add(new ChangePropertyCommand(sel, "source", tf.getText()));
        }
    }
}
