package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.tools.GBC;

/**
 * This hook is called at JOSM upload and will check if new nodes and ways provide
 * a tag "source=". If not and if auto-sourcing is enabled, it will add
 * automatically a tag "source"="Cadastre..." as defined in the plugin preferences.
 */
public class CheckSourceUploadHook implements UploadHook
{
    /** Serializable ID */
    private static final long serialVersionUID = -1;

    /**
     * Add the tag "source" if it doesn't exist for all new Nodes and Ways before uploading
     */
    public boolean checkUpload(APIDataSet apiDataSet) 
    {
        if (CadastrePlugin.autoSourcing && CadastrePlugin.pluginUsed && !apiDataSet.getPrimitivesToAdd().isEmpty()) {
            Collection<OsmPrimitive> sel = new HashSet<OsmPrimitive>();
            for (OsmPrimitive osm : apiDataSet.getPrimitivesToAdd()) {
                if ((osm instanceof Node || osm instanceof Way)
                        && (osm.getKeys() == null || !tagSourceExist(osm))) {
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
     * @param OsmPrimitive
     * @return true if one of keys is "source"
     */
    private boolean tagSourceExist(OsmPrimitive osm) {
        for (String key : osm.keySet()) {
            if (key.equals("source") ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Displays a screen with the list of objects which will be tagged with
     * source="cadastre.." if it is approved.
     * @param sel the list of elements added without a key "source"
     */
    private void displaySource(Collection<OsmPrimitive> sel)
    {
        if (!sel.isEmpty()) {
            JPanel p = new JPanel(new GridBagLayout());
            OsmPrimitivRenderer renderer = new OsmPrimitivRenderer();
            p.add(new JLabel(tr("Auto-tag source added:")), GBC.eol());
            JList l = new JList(sel.toArray());
            l.setCellRenderer(renderer);
            l.setVisibleRowCount(l.getModel().getSize() < 6 ? l.getModel().getSize() : 10);
            p.add(new JScrollPane(l), GBC.eol().fill());
            boolean bContinue = JOptionPane.showConfirmDialog(Main.parent, p, tr("Add \"source=...\" to elements?"),
                   JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if (bContinue)
                Main.main.undoRedo.add(new ChangePropertyCommand(sel, "source", CadastrePlugin.source));
        }

    }
}
