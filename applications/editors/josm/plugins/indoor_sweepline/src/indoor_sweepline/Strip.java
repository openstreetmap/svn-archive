// License: GPL. For details, see LICENSE file.
package indoor_sweepline;

import java.util.Vector;

import org.openstreetmap.josm.data.osm.DataSet;

public class Strip {

    public Strip(DataSet dataSet) {
        width = 10.;
        parts = new Vector<>();
        partsGeography = new Vector<>();
        lhs = new Vector<>();
        rhs = new Vector<>();

        this.dataSet = dataSet;
    }

    public void setCorridorPartType(int partIndex, CorridorPart.Type type) {
        while (parts.size() <= partIndex) {
            parts.add(new CorridorPart(0., CorridorPart.Type.WALL,
                    parts.size() % 2 == 0 ? CorridorPart.ReachableSide.FRONT :
                        CorridorPart.ReachableSide.BACK));
            partsGeography.add(new CorridorGeography(dataSet));
        }
        parts.elementAt(partIndex).setType(type, CorridorPart.ReachableSide.ALL);
    }

    public CorridorPart partAt(int i) {
        while (parts.size() <= i) {
            parts.add(new CorridorPart(0., CorridorPart.Type.WALL,
                    parts.size() % 2 == 0 ? CorridorPart.ReachableSide.FRONT :
                        CorridorPart.ReachableSide.BACK));
            partsGeography.add(new CorridorGeography(dataSet));
        }
        return parts.elementAt(i);
    }

    public CorridorGeography geographyAt(int i) {
        while (parts.size() <= i) {
            parts.add(new CorridorPart(0., CorridorPart.Type.WALL,
                    parts.size() % 2 == 0 ? CorridorPart.ReachableSide.FRONT :
                        CorridorPart.ReachableSide.BACK));
            partsGeography.add(new CorridorGeography(dataSet));
        }
        return partsGeography.elementAt(i);
    }

    public double width;
    public Vector<CorridorPart> parts;
    public Vector<CorridorGeography> partsGeography;
    public Vector<Double> lhs;
    public Vector<Double> rhs;

    private DataSet dataSet;
}
