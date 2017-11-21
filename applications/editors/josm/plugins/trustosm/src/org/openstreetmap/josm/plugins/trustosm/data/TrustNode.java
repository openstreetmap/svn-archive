// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.trustosm.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.openpgp.PGPSignature;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.conversion.DecimalDegreesCoordinateFormat;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class TrustNode extends TrustOsmPrimitive {

    public static Node generateNodeFromSigtext(String sigtext) {
        Pattern p = Pattern.compile("^(\\d*)\\((\\d*\\.?\\d*),(\\d*\\.?\\d*)\\)");
        Matcher m = p.matcher(sigtext);
        if (m.matches()) {
            Node node = new Node(Long.parseLong(m.group(1)));
            node.setCoor(new LatLon(Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3))));
            return node;
        }
        return null;
    }

    public static String generateNodeSigtext(Node node) {
        LatLon point = node.getCoor();
        String sigtext = node.getUniqueId() + "(";
        sigtext += DecimalDegreesCoordinateFormat.INSTANCE.latToString(point) + ",";
        sigtext += DecimalDegreesCoordinateFormat.INSTANCE.lonToString(point) + ")";
        return sigtext;
    }

    private TrustSignatures ratings;

    public TrustNode(Node osmItem) {
        super(osmItem);
    }

    @Override
    public void setOsmPrimitive(OsmPrimitive osmItem) {
        if (osmItem instanceof Node) {
            osm = osmItem;
        } else {
            System.err.println("Error while creating TrustNode: OsmPrimitive "+osmItem.getUniqueId()+" is not a Node!");
        }
    }

    public void storeNodeSig(PGPSignature sig) {
        if (ratings == null) {
            ratings = new TrustSignatures(sig, TrustNode.generateNodeSigtext((Node) osm), TrustSignatures.SIG_VALID);
        } else {
            ratings.addSignature(sig, TrustNode.generateNodeSigtext((Node) osm));
        }
    }

    public void setNodeRatings(TrustSignatures ratings) {
        this.ratings = ratings;
    }

    public TrustSignatures getNodeSigs() {
        return ratings;
    }
}
