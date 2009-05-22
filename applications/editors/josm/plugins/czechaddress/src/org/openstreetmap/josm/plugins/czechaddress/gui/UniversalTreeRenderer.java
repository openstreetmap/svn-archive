package org.openstreetmap.josm.plugins.czechaddress.gui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.proposal.AddKeyValueProposal;
import org.openstreetmap.josm.plugins.czechaddress.proposal.KeyValueChangeProposal;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalContainer;
import org.openstreetmap.josm.plugins.czechaddress.proposal.RemoveKeyProposal;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Renderer for rendering trees with {@link OsmPrimitive}s and
 * {@link AddressElement}s.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class UniversalTreeRenderer extends DefaultTreeCellRenderer {
    static ImageIcon iconAdd    = ImageProvider.get("actions", "add.png");
    static ImageIcon iconEdit   = ImageProvider.get("actions", "edit.png");
    static ImageIcon iconRemove = ImageProvider.get("actions", "remove.png");

    static ImageIcon nodeIcon      = ImageProvider.get("Mf_node.png");
    static ImageIcon wayIcon       = ImageProvider.get("Mf_way.png");
    static ImageIcon closedWayIcon = ImageProvider.get("Mf_closedway.png");
    static ImageIcon relationIcon  = ImageProvider.get("Mf_relation.png");
    
    static ImageIcon envelopeNormIcon = ImageProvider.get("envelope-closed-small.png");
    static ImageIcon envelopeStarIcon = ImageProvider.get("envelope-closed-star-small.png");
    static ImageIcon envelopeExclIcon = ImageProvider.get("envelope-closed-exclamation-small.png");

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {

        Component c = super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row, hasFocus);

        /*if (plainFont == null) plainFont = getFont().deriveFont(Font.PLAIN);
        if ( boldFont == null)  boldFont = getFont().deriveFont(Font.BOLD);*/
        Reasoner r = CzechAddressPlugin.getReasoner();


        if (value instanceof ProposalContainer) {
            value = ((ProposalContainer) value).getTarget();
        }

        if ((value instanceof AddressElement) || (value instanceof OsmPrimitive)) {
            setText(AddressElement.getName(value));
        }

             if (value instanceof AddKeyValueProposal)    setIcon(iconAdd);
        else if (value instanceof KeyValueChangeProposal) setIcon(iconEdit);
        else if (value instanceof RemoveKeyProposal)      setIcon(iconRemove);


        if (value instanceof House) {
            House house = (House) value;

            setIcon(envelopeNormIcon);
            if ( r.getConflicts(house) != null )
                setIcon(envelopeExclIcon);
            else if ( r.translate(house) == null)
                setIcon(envelopeStarIcon);
    
        } else if (value instanceof Node) {
            setIcon(nodeIcon);
        } else if (value instanceof Relation) {
            setIcon(relationIcon);
        } else if (value instanceof Way) {
            if (((Way) value).isClosed()) {
                setIcon(closedWayIcon);
            } else {
                setIcon(wayIcon);
            }
        }
        
        return c;
    }
}
