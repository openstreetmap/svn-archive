// License: GPL. For details, see LICENSE file.
package indoor_sweepline;

public class CorridorPart {

    public enum Type {
        VOID,
        PASSAGE,
        WALL,
        STAIRS_UP,
        STAIRS_DOWN,
        ESCALATOR_UP_LEAVING,
        ESCALATOR_UP_ARRIVING,
        ESCALATOR_UP_BIDIRECTIONAL,
        ESCALATOR_DOWN_LEAVING,
        ESCALATOR_DOWN_ARRIVING,
        ESCALATOR_DOWN_BIDIRECTIONAL,
        ELEVATOR
    }

    public enum ReachableSide {
        ALL,
        FRONT,
        BACK,
        LEFT,
        RIGHT
    }

    public CorridorPart(double width, Type type, ReachableSide side) {
        this.width = width;
        this.type = type;
        this.side = side;
    }

    public boolean isObstacle(ReachableSide beamSide) {
        if (type == Type.VOID)
            return false;
        if (type == Type.PASSAGE)
            return (beamSide != ReachableSide.ALL);
        return true;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type, ReachableSide beamSide) {
        this.type = type;
        adjustSideType(beamSide);
    }

    public ReachableSide getSide() {
        return side;
    }

    public void setSide(ReachableSide side, ReachableSide beamSide) {
        this.side = side;
        adjustSideType(beamSide);
    }

    public double width;
    private Type type;
    private ReachableSide side;

    private void adjustSideType(ReachableSide beamSide) {
        if (type == Type.PASSAGE || type == Type.VOID)
            side = ReachableSide.ALL;
        else if (side == ReachableSide.ALL) {
            if (beamSide == ReachableSide.RIGHT)
                side = ReachableSide.RIGHT;
            else
                side = ReachableSide.LEFT;
        }
    }
}
