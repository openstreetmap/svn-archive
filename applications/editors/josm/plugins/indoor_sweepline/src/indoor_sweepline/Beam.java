// License: GPL. For details, see LICENSE file.
package indoor_sweepline;

import java.util.Vector;

public class Beam {

    public Beam(Vector<Double> blueprint, double blueprintOffset, CorridorPart.ReachableSide defaultSide) {
        offset = blueprintOffset;
        parts = new Vector<>();

        setDefaultSide_(defaultSide);
        if (defaultSide == CorridorPart.ReachableSide.RIGHT) {
            for (int i = 1; i < blueprint.size(); i += 2) {
                addCorridorPart_(true, CorridorPart.Type.WALL,
                        blueprint.elementAt(i).doubleValue() - blueprint.elementAt(i-1).doubleValue());
                if (i+1 < blueprint.size())
                    addCorridorPart_(true, CorridorPart.Type.VOID,
                            blueprint.elementAt(i+1).doubleValue() - blueprint.elementAt(i).doubleValue());
            }
        } else {
            for (int i = 1; i < blueprint.size(); i += 2) {
                addCorridorPart_(true, CorridorPart.Type.PASSAGE,
                        blueprint.elementAt(i).doubleValue() - blueprint.elementAt(i-1).doubleValue());
                if (i+1 < blueprint.size())
                    addCorridorPart_(true, CorridorPart.Type.VOID,
                            blueprint.elementAt(i+1).doubleValue() - blueprint.elementAt(i).doubleValue());
            }
        }
        adjustStripCache();
    }

    private void setDefaultSide_(CorridorPart.ReachableSide defaultSide) {
        this.defaultSide = defaultSide;
    }

    public void setDefaultSide(CorridorPart.ReachableSide defaultSide) {
        setDefaultSide_(defaultSide);
        adjustStripCache();
    }

    public Vector<CorridorPart> getBeamParts() {
        return parts;
    }

    public double getBeamOffset() {
        return offset;
    }

    public void setBeamOffset(double beamOffset) {
        offset = beamOffset;
    }

    private void addCorridorPart_(boolean append, CorridorPart.Type type, double width) {
        CorridorPart.ReachableSide side = defaultSide == CorridorPart.ReachableSide.RIGHT ?
                defaultSide : CorridorPart.ReachableSide.ALL;

        if (append)
            parts.add(new CorridorPart(width, type, side));
        else
            parts.add(0, new CorridorPart(width, type, side));
    }

    public void addCorridorPart(boolean append, double width) {
        addCorridorPart_(append,
                defaultSide == CorridorPart.ReachableSide.RIGHT ? CorridorPart.Type.WALL : CorridorPart.Type.PASSAGE,
                        width);
        adjustStripCache();
    }

    public void setCorridorPartWidth(int partIndex, double value) {
        parts.elementAt(partIndex).width = value;
        adjustStripCache();
    }

    public void setCorridorPartType(int partIndex, CorridorPart.Type type) {
        parts.elementAt(partIndex).setType(type, defaultSide);
        enforceSideCoherence();
        adjustStripCache();
    }

    public void setCorridorPartSide(int partIndex, CorridorPart.ReachableSide side) {
        parts.elementAt(partIndex).setSide(side, defaultSide);
        enforceSideCoherence();
        adjustStripCache();
    }

    private void enforceSideCoherence() {
        for (int i = 1; i < parts.size(); ++i) {
            if (parts.elementAt(i).getSide() != CorridorPart.ReachableSide.ALL
                    && parts.elementAt(i-1).getSide() != CorridorPart.ReachableSide.ALL)
                parts.elementAt(i).setSide(parts.elementAt(i-1).getSide(), defaultSide);
        }
    }

    private boolean isVoidAbove(int i) {
        return i == 0 || parts.elementAt(i-1).getType() == CorridorPart.Type.VOID
                || (parts.elementAt(i-1).getSide() == CorridorPart.ReachableSide.RIGHT
                && defaultSide == CorridorPart.ReachableSide.LEFT)
                || (parts.elementAt(i-1).getSide() == CorridorPart.ReachableSide.LEFT
                && defaultSide == CorridorPart.ReachableSide.RIGHT);
    }

    private boolean isVoidBelow(int i) {
        return i == parts.size() || parts.elementAt(i).getType() == CorridorPart.Type.VOID
                || (parts.elementAt(i).getSide() == CorridorPart.ReachableSide.RIGHT
                && defaultSide == CorridorPart.ReachableSide.LEFT)
                || (parts.elementAt(i).getSide() == CorridorPart.ReachableSide.LEFT
                && defaultSide == CorridorPart.ReachableSide.RIGHT);
    }

    private boolean isPassageAbove(int i) {
        return i > 0
                && parts.elementAt(i-1).getType() == CorridorPart.Type.PASSAGE
                && defaultSide == CorridorPart.ReachableSide.ALL;
    }

    private boolean isPassageBelow(int i) {
        return i < parts.size()
                && parts.elementAt(i).getType() == CorridorPart.Type.PASSAGE
                && defaultSide == CorridorPart.ReachableSide.ALL;
    }

    private boolean isReachableLeft(int i) {
        if (defaultSide == CorridorPart.ReachableSide.RIGHT)
            return false;
        if (parts.elementAt(i).getSide() == CorridorPart.ReachableSide.LEFT)
            return true;
        return defaultSide == CorridorPart.ReachableSide.LEFT;
    }

    private void connectTwoPos(StripPosition newPos, boolean toLeft) {
        StripPosition other = null;
        if (rhsStrips.size() > 0 && rhsStrips.elementAt(rhsStrips.size()-1).connectedTo == -1) {
            newPos.connectedToSameSide = !toLeft;
            newPos.connectedTo = rhsStrips.size()-1;
            other = rhsStrips.elementAt(rhsStrips.size()-1);
        } else {
            newPos.connectedToSameSide = toLeft;
            newPos.connectedTo = lhsStrips.size()-1;
            other = lhsStrips.elementAt(lhsStrips.size()-1);
        }

        other.connectedToSameSide = newPos.connectedToSameSide;
        if (toLeft) {
            other.connectedTo = lhsStrips.size();
            lhsStrips.add(newPos);
        } else {
            other.connectedTo = rhsStrips.size();
            rhsStrips.add(newPos);
        }
    }

    private static class StripPosition {
        StripPosition(int nodeIndex, double offset) {
            this.nodeIndex = nodeIndex;
            this.offset = offset;
            connectedTo = -1;
            connectedToSameSide = false;
        }

        public int nodeIndex;
        public double offset;
        public int connectedTo;
        public boolean connectedToSameSide;
    }

    private double offset;
    private Vector<CorridorPart> parts;
    private Vector<StripPosition> lhsStrips;
    private Vector<StripPosition> rhsStrips;

    private void adjustStripCache() {
        lhsStrips = new Vector<>();
        rhsStrips = new Vector<>();

        double offset = 0;

        for (int i = 0; i <= parts.size(); ++i) {
            if (isVoidBelow(i)) {
                if (isPassageAbove(i)) {
                    StripPosition lhs = new StripPosition(i, offset);
                    StripPosition rhs = new StripPosition(i, offset);

                    lhs.connectedToSameSide = false;
                    lhs.connectedTo = rhsStrips.size();
                    rhs.connectedToSameSide = false;
                    rhs.connectedTo = lhsStrips.size();

                    lhsStrips.add(lhs);
                    rhsStrips.add(rhs);
                } else if (!isVoidAbove(i))
                    connectTwoPos(new StripPosition(i, offset), isReachableLeft(i-1));
            } else if (isPassageBelow(i)) {
                if (isVoidAbove(i)) {
                    StripPosition lhs = new StripPosition(i, offset);
                    StripPosition rhs = new StripPosition(i, offset);

                    lhs.connectedToSameSide = false;
                    lhs.connectedTo = rhsStrips.size();
                    rhs.connectedToSameSide = false;
                    rhs.connectedTo = lhsStrips.size();

                    lhsStrips.add(lhs);
                    rhsStrips.add(rhs);
                } else if (!isPassageAbove(i))
                    connectTwoPos(new StripPosition(i, offset), !isReachableLeft(i-1));
            } else {
                if (isVoidAbove(i)) {
                    if (isReachableLeft(i))
                        lhsStrips.add(new StripPosition(i, offset));
                    else
                        rhsStrips.add(new StripPosition(i, offset));
                } else if (isPassageAbove(i)) {
                    if (isReachableLeft(i))
                        rhsStrips.add(new StripPosition(i, offset));
                    else
                        lhsStrips.add(new StripPosition(i, offset));
                }
            }

            if (i < parts.size())
                offset += parts.elementAt(i).width;
        }
    }

    public Vector<Double> leftHandSideStrips() {
        Vector<Double> offsets = new Vector<>();
        for (StripPosition pos : lhsStrips) {
            offsets.add(pos.offset);
        }

        return offsets;
    }

    public Vector<Double> rightHandSideStrips() {
        Vector<Double> offsets = new Vector<>();
        for (StripPosition pos : rhsStrips) {
            offsets.add(pos.offset);
        }

        return offsets;
    }

    public int getBeamPartIndex(boolean toTheLeft, int i) {
        if (toTheLeft)
            return lhsStrips.elementAt(i).nodeIndex;
        else
            return rhsStrips.elementAt(i).nodeIndex;
    }

    public boolean appendNodes(IndoorSweeplineModel.SweepPolygonCursor cursor, boolean fromRight,
            BeamGeography geography, String level) {
        if (fromRight) {
            StripPosition pos = rhsStrips.elementAt(cursor.partIndex);
            StripPosition to = pos.connectedToSameSide ?
                    rhsStrips.elementAt(pos.connectedTo) : lhsStrips.elementAt(pos.connectedTo);

                    geography.appendNodes(pos.nodeIndex, to.nodeIndex, level);

                    if (!pos.connectedToSameSide)
                        --cursor.stripIndex;
                    cursor.partIndex = pos.connectedTo;

                    return !pos.connectedToSameSide;
        } else {
            StripPosition pos = lhsStrips.elementAt(cursor.partIndex);
            StripPosition to = pos.connectedToSameSide ?
                    lhsStrips.elementAt(pos.connectedTo) : rhsStrips.elementAt(pos.connectedTo);

                    geography.appendNodes(pos.nodeIndex, to.nodeIndex, level);

                    if (!pos.connectedToSameSide)
                        ++cursor.stripIndex;
                    cursor.partIndex = pos.connectedTo;

                    return pos.connectedToSameSide;
        }
    }

    private CorridorPart.ReachableSide defaultSide;
}
