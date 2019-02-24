// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.Splinex;

import static org.openstreetmap.josm.plugins.Splinex.SplinexPlugin.EPSILON;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.tools.ImageProvider;

public class Spline {
    public static IntegerProperty PROP_SPLINEPOINTS = new IntegerProperty("edit.spline.num_points", 10);

    public static class SNode {
        public final Node node; // Endpoint
        public EastNorth cprev, cnext; // Relative offsets of control points

        public SNode(Node node) {
            this.node = node;
            cprev = new EastNorth(0, 0);
            cnext = new EastNorth(0, 0);
        }
    }

    private final ArrayList<SNode> nodes = new ArrayList<SNode>();

    public SNode getFirstSegment() {
        if (nodes.isEmpty())
            return null;
        return nodes.get(0);
    }

    public SNode getLastSegment() {
        if (nodes.isEmpty())
            return null;
        return nodes.get(nodes.size() - 1);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public int nodeCount() {
        return nodes.size();
    }

    public boolean isClosed() {
        if (nodes.size() < 2)
            return false;
        return nodes.get(0) == nodes.get(nodes.size() - 1);
    }

    //int chkTime;

    public void paint(Graphics2D g, MapView mv, Color curveColor, Color ctlColor, Point helperEndpoint, short direction) {
        if (nodes.isEmpty())
            return;
        final GeneralPath curv = new GeneralPath();
        final GeneralPath ctl = new GeneralPath();

        Point2D cbPrev = null;
        if (helperEndpoint != null && direction == -1) {
            cbPrev = new Point2D.Double(helperEndpoint.x, helperEndpoint.y);
            curv.moveTo(helperEndpoint.x, helperEndpoint.y);
        }
        for (SNode sn : nodes) {
            Point2D pt = mv.getPoint2D(sn.node);
            EastNorth en = sn.node.getEastNorth();

            Point2D ca = mv.getPoint2D(en.add(sn.cprev));
            Point2D cb = mv.getPoint2D(en.add(sn.cnext));

            if (cbPrev != null || !isClosed()) {
                ctl.moveTo(ca.getX(), ca.getY());
                ctl.lineTo(pt.getX(), pt.getY());
                ctl.lineTo(cb.getX(), cb.getY());
            }

            if (cbPrev == null)
                curv.moveTo(pt.getX(), pt.getY());
            else
                curv.curveTo(cbPrev.getX(), cbPrev.getY(), ca.getX(), ca.getY(), pt.getX(), pt.getY());
            cbPrev = cb;
        }
        if (helperEndpoint != null && direction == 1) {
            curv.curveTo(cbPrev.getX(), cbPrev.getY(), helperEndpoint.getX(), helperEndpoint.getY(),
                    helperEndpoint.getX(), helperEndpoint.getY());
        }
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(curveColor);
        g.draw(curv);
        g.setStroke(new BasicStroke(1));
        g.setColor(ctlColor);
        g.draw(ctl);
        /* if (chkTime > 0 || sht.chkCnt > 0) {
            g.drawString(tr("Check count: {0}", sht.chkCnt), 10, 60);
            g.drawString(tr("Check time: {0} us", chkTime), 10, 70);
        }
        chkTime = 0;
        sht.chkCnt = 0; */
    }

    public enum SplinePoint {
        ENDPOINT, CONTROL_PREV, CONTROL_NEXT
    }

    public class PointHandle {
        public final int idx;
        public final SNode sn;
        public final SplinePoint point;

        public PointHandle(int idx, SplinePoint point) {
            if (point == null)
                throw new IllegalArgumentException("Invalid SegmentPoint passed for PointHandle contructor");
            this.idx = idx;
            this.sn = nodes.get(idx);
            this.point = point;
        }

        public PointHandle otherPoint(SplinePoint point) {
            return new PointHandle(idx, point);
        }

        public Spline getSpline() {
            return Spline.this;
        }

        public EastNorth getPoint() {
            EastNorth en = sn.node.getEastNorth();
            switch (point) {
            case ENDPOINT:
                return en;
            case CONTROL_PREV:
                return en.add(sn.cprev);
            case CONTROL_NEXT:
                return en.add(sn.cnext);
            }
            throw new AssertionError();
        }

        public void movePoint(EastNorth en) {
            switch (point) {
            case ENDPOINT:
                sn.node.setEastNorth(en);
                return;
            case CONTROL_PREV:
                sn.cprev = en.subtract(sn.node.getEastNorth());
                return;
            case CONTROL_NEXT:
                sn.cnext = en.subtract(sn.node.getEastNorth());
                return;
            }
            throw new AssertionError();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof PointHandle))
                return false;
            PointHandle o = (PointHandle) other;
            return this.sn == o.sn && this.point == o.point;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sn, point);
        }
    }

    public PointHandle getNearestPoint(MapView mv, Point2D point) {
        PointHandle bestPH = null;
        double bestDistSq = NavigatableComponent.PROP_SNAP_DISTANCE.get();
        bestDistSq = bestDistSq * bestDistSq;
        for (int i = 0; i < nodes.size(); i++) {
            for (SplinePoint sp : SplinePoint.values()) {
                PointHandle ph = new PointHandle(i, sp);
                double distSq = point.distanceSq(mv.getPoint2D(ph.getPoint()));
                if (distSq < bestDistSq) {
                    bestPH = ph;
                    bestDistSq = distSq;
                }
            }
        }
        return bestPH;
    }

    SplineHitTest sht = new SplineHitTest();

    public boolean doesHit(double x, double y, MapView mv) {
        //long start = System.nanoTime();
        //sht.chkCnt = 0;
        sht.setCoord(x, y, NavigatableComponent.PROP_SNAP_DISTANCE.get());
        Point2D prev = null;
        Point2D cbPrev = null;
        for (SNode sn : nodes) {
            Point2D pt = mv.getPoint2D(sn.node);
            EastNorth en = sn.node.getEastNorth();
            Point2D ca = mv.getPoint2D(en.add(sn.cprev));

            if (cbPrev != null)
                if (sht.checkCurve(prev.getX(), prev.getY(), cbPrev.getX(), cbPrev.getY(), ca.getX(), ca.getY(),
                        pt.getX(), pt.getY()))
                    return true;
            cbPrev = mv.getPoint2D(en.add(sn.cnext));
            prev = pt;
        }
        //chkTime = (int) ((System.nanoTime() - start) / 1000);
        return false;
    }

    public void finishSpline() {
        if (nodes.isEmpty())
            return;
        int detail = PROP_SPLINEPOINTS.get();
        Way w = new Way();
        List<Command> cmds = new LinkedList<>();
        Iterator<SNode> it = nodes.iterator();
        SNode sn = it.next();
        if (sn.node.isDeleted())
            cmds.add(new UndeleteNodeCommand(sn.node));
        w.addNode(sn.node);
        EastNorth a = sn.node.getEastNorth();
        EastNorth ca = a.add(sn.cnext);
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        while (it.hasNext()) {
            sn = it.next();
            if (sn.node.isDeleted() && sn != nodes.get(0))
                cmds.add(new UndeleteNodeCommand(sn.node));
            EastNorth b = sn.node.getEastNorth();
            EastNorth cb = b.add(sn.cprev);
            if (!a.equalsEpsilon(ca, EPSILON) || !b.equalsEpsilon(cb, EPSILON))
                for (int i = 1; i < detail; i++) {
                    Node n = new Node(ProjectionRegistry.getProjection().eastNorth2latlon(
                            cubicBezier(a, ca, cb, b, (double) i / detail)));
                    if (n.getCoor().isOutSideWorld()) {
                        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Spline goes outside of the world."));
                        return;
                    }
                    cmds.add(new AddCommand(ds, n));
                    w.addNode(n);
                }
            w.addNode(sn.node);
            a = b;
            ca = a.add(sn.cnext);
        }
        if (!cmds.isEmpty()) {
            cmds.add(new AddCommand(ds, w));
            UndoRedoHandler.getInstance().add(new FinishSplineCommand(cmds));
        }
    }

    /**
     * A cubic bezier method to calculate the point at t along the Bezier Curve
     * give
     */
    public static EastNorth cubicBezier(EastNorth a0, EastNorth a1, EastNorth a2, EastNorth a3, double t) {
        return new EastNorth(cubicBezierPoint(a0.getX(), a1.getX(), a2.getX(), a3.getX(), t), cubicBezierPoint(
                a0.getY(), a1.getY(), a2.getY(), a3.getY(), t));
    }

    /**
     * The cubic Bezier equation.
     */
    private static double cubicBezierPoint(double a0, double a1, double a2, double a3, double t) {
        return Math.pow(1 - t, 3) * a0 + 3 * Math.pow(1 - t, 2) * t * a1 + 3 * (1 - t) * Math.pow(t, 2) * a2
                + Math.pow(t, 3) * a3;
    }

    public class AddSplineNodeCommand extends Command {
        private final SNode sn;
        private final boolean existing;
        private final int idx;
        boolean affected;

        public AddSplineNodeCommand(SNode sn, boolean existing, int idx) {
            super(MainApplication.getLayerManager().getEditDataSet());
            this.sn = sn;
            this.existing = existing;
            this.idx = idx;
        }

        public AddSplineNodeCommand(SNode sn, boolean existing) {
            this(sn, existing, nodes.size() - 1);
        }

        @Override
        public boolean executeCommand() {
            nodes.add(idx, sn);
            if (!existing) {
                getAffectedDataSet().addPrimitive(sn.node);
                sn.node.setModified(true);
                affected = true;
            }
            return true;
        }

        @Override
        public void undoCommand() {
            if (!existing)
                getAffectedDataSet().removePrimitive(sn.node);
            nodes.remove(idx);
            affected = false;
        }

        @Override
        public String getDescriptionText() {
            if (existing)
                return tr("Add an existing node to spline: {0}",
                        sn.node.getDisplayName(DefaultNameFormatter.getInstance()));
            return tr("Add a new node to spline: {0}", sn.node.getDisplayName(DefaultNameFormatter.getInstance()));
        }

        @Override
        public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
                Collection<OsmPrimitive> added) {
            if (!existing)
                added.add(sn.node);
        }

        @Override
        public Icon getDescriptionIcon() {
            return ImageProvider.get("data", "node");
        }

        @Override
        public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
            return affected ? Collections.singleton(sn.node) : super.getParticipatingPrimitives();
        }
    }

    public class DeleteSplineNodeCommand extends Command {
        int idx;
        SNode sn;
        boolean wasDeleted;
        boolean affected;

        public DeleteSplineNodeCommand(int idx) {
            super(MainApplication.getLayerManager().getEditDataSet());
            this.idx = idx;
        }

        private boolean deleteUnderlying() {
            return !sn.node.hasKeys() && sn.node.getReferrers().isEmpty() && (!isClosed() || idx < (nodes.size() - 1));
        }

        @Override
        public boolean executeCommand() {
            if (isClosed() && idx == 0)
                idx = nodes.size() - 1;
            sn = nodes.get(idx);
            wasDeleted = sn.node.isDeleted();
            if (deleteUnderlying()) {
                sn.node.setDeleted(true);
                affected = true;
            }
            nodes.remove(idx);
            return true;
        }

        @Override
        public void undoCommand() {
            affected = false;
            sn.node.setDeleted(wasDeleted);
            nodes.add(idx, sn);
        }

        @Override
        public String getDescriptionText() {
            return tr("Delete spline node {0}", sn.node.getDisplayName(DefaultNameFormatter.getInstance()));
        }

        @Override
        public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
                Collection<OsmPrimitive> added) {
            if (deleteUnderlying())
                deleted.add(sn.node);
        }

        @Override
        public Icon getDescriptionIcon() {
            return ImageProvider.get("data", "node");
        }

        @Override
        public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
            return affected ? Collections.singleton(sn.node) : super.getParticipatingPrimitives();
        }
    }

    public static class EditSplineCommand extends Command {
        EastNorth cprev;
        EastNorth cnext;
        SNode sn;

        public EditSplineCommand(SNode sn) {
            super(MainApplication.getLayerManager().getEditDataSet());
            this.sn = sn;
            cprev = sn.cprev.add(0, 0);
            cnext = sn.cnext.add(0, 0);
        }

        @Override
        public boolean executeCommand() {
            EastNorth en = sn.cprev;
            sn.cprev = this.cprev;
            this.cprev = en;
            en = sn.cnext;
            sn.cnext = this.cnext;
            this.cnext = en;
            return true;
        }

        @Override
        public void undoCommand() {
            executeCommand();
        }

        @Override
        public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
                Collection<OsmPrimitive> added) {
            // This command doesn't touches OSM data
        }

        @Override
        public String getDescriptionText() {
            return "Edit spline";
        }

        @Override
        public Icon getDescriptionIcon() {
            return ImageProvider.get("data", "node");
        }
    }

    public class CloseSplineCommand extends Command {
        
        public CloseSplineCommand() {
            super(MainApplication.getLayerManager().getEditDataSet());
        }

        @Override
        public boolean executeCommand() {
            nodes.add(nodes.get(0));
            return true;
        }

        @Override
        public void undoCommand() {
            nodes.remove(nodes.size() - 1);
        }

        @Override
        public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
                Collection<OsmPrimitive> added) {
            // This command doesn't touches OSM data
        }

        @Override
        public String getDescriptionText() {
            return "Close spline";
        }

        @Override
        public Icon getDescriptionIcon() {
            return ImageProvider.get("aligncircle");
        }
    }

    public List<OsmPrimitive> getNodes() {
        ArrayList<OsmPrimitive> result = new ArrayList<>(nodes.size());
        for (SNode sn : nodes) {
            result.add(sn.node);
        }
        return result;
    }

    public class FinishSplineCommand extends SequenceCommand {
        public SNode[] saveSegments;

        public FinishSplineCommand(Collection<Command> sequenz) {
            super(tr("Finish spline"), sequenz);
        }

        @Override
        public boolean executeCommand() {
            saveSegments = new SNode[nodes.size()];
            int i = 0;
            for (SNode sn : nodes) {
                saveSegments[i++] = sn;
            }
            nodes.clear();
            return super.executeCommand();
        }

        @Override
        public void undoCommand() {
            super.undoCommand();
            nodes.clear();
            nodes.addAll(Arrays.asList(saveSegments));
        }
    }
}
