package org.openstreetmap.josm.plugins.czechaddress.gui.utils;

import javax.swing.ImageIcon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.AddressElement;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.House;
import org.openstreetmap.josm.plugins.czechaddress.intelligence.Reasoner;
import org.openstreetmap.josm.plugins.czechaddress.proposal.AddKeyValueProposal;
import org.openstreetmap.josm.plugins.czechaddress.proposal.KeyValueChangeProposal;
import org.openstreetmap.josm.plugins.czechaddress.proposal.ProposalContainer;
import org.openstreetmap.josm.plugins.czechaddress.proposal.RemoveKeyProposal;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Helper for getting icons and texts for {@code Universal*} renderers.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public abstract class UniversalRenderer {
    private static final ImageIcon iconAdd    = ImageProvider.get("actions", "add.png");
    private static final ImageIcon iconEdit   = ImageProvider.get("actions", "edit.png");
    private static final ImageIcon iconRemove = ImageProvider.get("actions", "remove.png");

    private static final ImageIcon nodeIcon      = ImageProvider.get("data/node.png");
    private static final ImageIcon wayIcon       = ImageProvider.get("data/way.png");
    private static final ImageIcon relationIcon  = ImageProvider.get("data/relation.png");

    private static final ImageIcon envelopeNormIcon = ImageProvider.get("envelope-closed-small.png");
    private static final ImageIcon envelopeStarIcon = ImageProvider.get("envelope-closed-star-small.png");
    private static final ImageIcon envelopeExclIcon = ImageProvider.get("envelope-closed-exclamation-small.png");

    public static ImageIcon getIcon(Object value) {
        
             if (value instanceof AddKeyValueProposal)    return iconAdd;
        else if (value instanceof KeyValueChangeProposal) return iconEdit;
        else if (value instanceof RemoveKeyProposal)      return iconRemove;

        if (value instanceof House) {
            House house = (House) value;

            if (Reasoner.getInstance().inConflict(house))
                return envelopeExclIcon;

            if (Reasoner.getInstance().translate(house) == null)
                return envelopeStarIcon;

            return envelopeNormIcon;

        } else if (value instanceof Node)
            return nodeIcon;

        else if (value instanceof Relation)
            return relationIcon;

        else if (value instanceof Way)
//          return ((Way) value).isClosed() ? closedWayIcon : wayIcon;
            return wayIcon;

        return null;
    }

    public static String getText(Object value) {
        if (value instanceof ProposalContainer)
            value = ((ProposalContainer) value).getTarget();

        if (value instanceof AddressElement)
            return ((AddressElement) value).getName();
                
        if (value instanceof OsmPrimitive)
            return AddressElement.getName(value);

        if (value == null)
            return "";
        
        return value.toString();
    }

}
