// License: GPL. For details, see LICENSE file.
package indoor_sweepline;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/* TODO:
- focus to useful table entry after cell edit
- keyboard shortcuts
 */
public class IndoorSweeplineModel {
    public enum Type {
        CORRIDOR,
        PLATFORM
    }

    public IndoorSweeplineModel(OsmDataLayer activeLayer, LatLon center) {
        target = new ModelGeography(activeLayer.getDataSet(), center);

        beams = new Vector<>();
        strips = new Vector<>();
        type = Type.CORRIDOR;
        level = "-1";
        addBeam();
        addStrip();
        addBeam();

        structureBox = new DefaultComboBoxModel<>();
    }

    private ModelGeography target;

    public void addBeam() {
        CorridorPart.ReachableSide side = CorridorPart.ReachableSide.LEFT;
        if (beams.size() == 0)
            side = CorridorPart.ReachableSide.RIGHT;

        /*double width = 10.;
        if (beams.size() > 0) {
            width = 0;
            for (CorridorPart part : beams.elementAt(beams.size() - 1).getBeamParts())
                width += part.width;
        }

        double offset = 0;
        for (int i = 0; i < strips.size(); ++i)
            offset += strips.elementAt(i).width;*/

        if (strips.size() == 0) {
            Vector<Double> blueprint = new Vector<>();
            blueprint.addElement(0.);
            blueprint.addElement(10.);
            beams.add(new Beam(blueprint, 0., side));
        } else
            beams.add(new Beam(strips.elementAt(strips.size()-1).lhs,
                    beams.elementAt(beams.size()-1).getBeamOffset(), side));

        if (strips.size() > 0)
            strips.elementAt(beams.size()-2).rhs = beams.elementAt(beams.size()-1).leftHandSideStrips();

        updateOsmModel();
    }

    public void addStrip() {
        strips.add(new Strip(target.getDataSet()));
        if (beams.size() > 1) {
            beams.elementAt(beams.size()-1).setDefaultSide(CorridorPart.ReachableSide.ALL);
            strips.elementAt(strips.size()-2).rhs = beams.elementAt(strips.size()-1).leftHandSideStrips();
        }
        strips.elementAt(strips.size()-1).lhs = beams.elementAt(strips.size()-1).rightHandSideStrips();

        updateOsmModel();
    }

    public int leftRightCount() {
        return beams.size() + strips.size();
    }

    public DefaultComboBoxModel<String> structures() {
        structureBox.removeAllElements();
        double offset = 0;
        for (int i = 0; i < strips.size(); ++i) {
            if (i < beams.size())
                structureBox.addElement(Double.toString(offset));
            structureBox.addElement(Double.toString(offset) + " - "
                    + Double.toString(offset + strips.elementAt(i).width));
            offset += strips.elementAt(i).width;
        }
        if (strips.size() < beams.size())
            structureBox.addElement(Double.toString(offset));

        return structureBox;
    }

    public Strip getStrip(int index) {
        return strips.elementAt(index / 2);
    }

    public double getStripWidth(int index) {
        return strips.elementAt(index / 2).width;
    }

    public void setStripWidth(int index, double value) {
        strips.elementAt(index / 2).width = value;
        updateOsmModel();
    }

    public double getBeamOffset(int index) {
        return beams.elementAt(index / 2).getBeamOffset();
    }

    public void setBeamOffset(int index, double beamOffset) {
        beams.elementAt(index / 2).setBeamOffset(beamOffset);
        updateOsmModel();
    }

    public List<CorridorPart> getBeamParts(int index) {
        return beams.elementAt(index / 2).getBeamParts();
    }

    public void addCorridorPart(int beamIndex, boolean append, double value) {
        beams.elementAt(beamIndex / 2).addCorridorPart(append, value);
        if (beamIndex / 2 > 0)
            strips.elementAt(beamIndex / 2 - 1).rhs = beams.elementAt(beamIndex / 2).leftHandSideStrips();
        if (beamIndex / 2 < strips.size())
            strips.elementAt(beamIndex / 2).lhs = beams.elementAt(beamIndex / 2).rightHandSideStrips();

        updateOsmModel();
    }

    public void setCorridorPartWidth(int beamIndex, int partIndex, double value) {
        beams.elementAt(beamIndex / 2).setCorridorPartWidth(partIndex, value);
        if (beamIndex / 2 > 0)
            strips.elementAt(beamIndex / 2 - 1).rhs = beams.elementAt(beamIndex / 2).leftHandSideStrips();
        if (beamIndex / 2 < strips.size())
            strips.elementAt(beamIndex / 2).lhs = beams.elementAt(beamIndex / 2).rightHandSideStrips();

        updateOsmModel();
    }

    public void setCorridorPartType(int beamIndex, int partIndex, CorridorPart.Type type) {
        if (beamIndex % 2 == 0) {
            beams.elementAt(beamIndex / 2).setCorridorPartType(partIndex, type);
            if (beamIndex / 2 > 0)
                strips.elementAt(beamIndex / 2 - 1).rhs = beams.elementAt(beamIndex / 2).leftHandSideStrips();
            if (beamIndex / 2 < strips.size())
                strips.elementAt(beamIndex / 2).lhs = beams.elementAt(beamIndex / 2).rightHandSideStrips();
        } else {
            if (type != CorridorPart.Type.PASSAGE && type != CorridorPart.Type.VOID)
                strips.elementAt(beamIndex / 2).setCorridorPartType(partIndex, type);
        }

        updateOsmModel();
    }

    public void setCorridorPartSide(int beamIndex, int partIndex, CorridorPart.ReachableSide side) {
        beams.elementAt(beamIndex / 2).setCorridorPartSide(partIndex, side);
        if (beamIndex / 2 > 0)
            strips.elementAt(beamIndex / 2 - 1).rhs = beams.elementAt(beamIndex / 2).leftHandSideStrips();
        if (beamIndex / 2 < strips.size())
            strips.elementAt(beamIndex / 2).lhs = beams.elementAt(beamIndex / 2).rightHandSideStrips();

        updateOsmModel();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        updateOsmModel();
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
        updateOsmModel();
    }

    private Vector<Beam> beams;
    private Vector<Strip> strips;
    private Type type;
    private String level;

    DefaultComboBoxModel<String> structureBox;

    private void updateOsmModel() {
        distributeWays();
        MainApplication.getMap().mapView.repaint();
    }

    public static class SweepPolygonCursor {
        public int stripIndex;
        public int partIndex;

        public SweepPolygonCursor(int stripIndex, int partIndex) {
            this.stripIndex = stripIndex;
            this.partIndex = partIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(stripIndex, partIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            SweepPolygonCursor other = (SweepPolygonCursor) obj;
            return Objects.equals(partIndex, other.partIndex)
                    && Objects.equals(stripIndex, other.stripIndex);
        }
    }

    private void distributeWays() {
        target.startGeographyBuild(beams, strips);

        Vector<Vector<Boolean>> stripRefs = new Vector<>();
        for (Strip strip : strips) {
            Vector<Boolean> refs = new Vector<>();
            if (strip.lhs.size() < strip.rhs.size())
                refs.setSize(strip.rhs.size());
            else
                refs.setSize(strip.lhs.size());
            stripRefs.add(refs);
        }

        Boolean truePtr = Boolean.TRUE;
        for (int i = 0; i < stripRefs.size(); ++i) {
            Vector<Boolean> refs = stripRefs.elementAt(i);
            for (int j = 0; j < refs.size(); ++j) {
                if (refs.elementAt(j) == null) {
                    target.startWay();

                    SweepPolygonCursor cursor = new SweepPolygonCursor(i, j);

                    boolean toTheLeft = true;
                    while (stripRefs.elementAt(cursor.stripIndex).elementAt(cursor.partIndex) == null) {
                        stripRefs.elementAt(cursor.stripIndex).setElementAt(truePtr, cursor.partIndex);
                        if (toTheLeft && cursor.partIndex < strips.elementAt(cursor.stripIndex).lhs.size()) {
                            target.appendCorridorPart(
                                    strips.elementAt(cursor.stripIndex).partAt(cursor.partIndex),
                                    strips.elementAt(cursor.stripIndex).geographyAt(cursor.partIndex),
                                    cursor.stripIndex,
                                    beams.elementAt(cursor.stripIndex).getBeamPartIndex(!toTheLeft, cursor.partIndex),
                                    level);
                            toTheLeft = beams.elementAt(cursor.stripIndex).appendNodes(
                                    cursor, toTheLeft, target.beamAt(cursor.stripIndex), level);
                        } else if (!toTheLeft && cursor.partIndex < strips.elementAt(cursor.stripIndex).rhs.size()) {
                            target.appendCorridorPart(
                                    strips.elementAt(cursor.stripIndex).partAt(cursor.partIndex),
                                    strips.elementAt(cursor.stripIndex).geographyAt(cursor.partIndex),
                                    cursor.stripIndex + 1,
                                    beams.elementAt(cursor.stripIndex + 1).getBeamPartIndex(!toTheLeft, cursor.partIndex),
                                    level);
                            toTheLeft = beams.elementAt(cursor.stripIndex + 1).appendNodes(
                                    cursor, toTheLeft, target.beamAt(cursor.stripIndex + 1), level);
                        } else
                            toTheLeft = appendUturn(cursor, toTheLeft);
                    }

                    target.finishWay(strips.elementAt(cursor.stripIndex), cursor.partIndex, j % 2 == 0, level);
                }
            }
        }

        target.finishGeographyBuild(type, level);
    }

    private boolean appendUturn(SweepPolygonCursor cursor, boolean toTheLeft) {
        Strip strip = strips.elementAt(cursor.stripIndex);
        target.appendUturnNode(strip, cursor.partIndex, cursor.stripIndex,
                beams.elementAt(toTheLeft ? cursor.stripIndex + 1 : cursor.stripIndex).
                getBeamPartIndex(toTheLeft, cursor.partIndex),
                toTheLeft, level);

        if (cursor.partIndex % 2 == 0)
            ++cursor.partIndex;
        else
            --cursor.partIndex;
        return !toTheLeft;
    }
}
