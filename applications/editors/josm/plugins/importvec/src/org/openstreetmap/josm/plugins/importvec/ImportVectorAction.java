package org.openstreetmap.josm.plugins.importvec;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Mercator;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Shortcut;

import com.kitfox.svg.Group;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.ShapeElement;

public class ImportVectorAction extends JosmAction {

    public ImportVectorAction() {
        super(tr("Import..."), "open", tr("Import vector graphics."),
                Shortcut.registerShortcut("system:import", tr("File: {0}", tr("Import...")), KeyEvent.VK_I, Shortcut.GROUP_DIRECT+Shortcut.GROUPS_ALT1), false);
        // Avoid to override "open" toolbar function
        putValue("toolbar", "importvec");
        Main.toolbar.register(this);
    }
    public static JFileChooser createAndOpenFileChooser(boolean open, boolean multiple, String title) {
        String curDir = Main.pref.get("lastDirectory");
        if (curDir.equals("")) {
            curDir = ".";
        }
        JFileChooser fc = new JFileChooser(new File(curDir));
        if (title != null) {
            fc.setDialogTitle(title);
        }

        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(multiple);
        fc.setAcceptAllFileFilterUsed(false);
        //System.out.println("opening fc for extension " + extension);
        fc.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                else {
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".svg");
                }
            }
            @Override
            public String getDescription() {
                return tr("SVG Drawings (*.svg)");
            }

        });

        int answer = open ? fc.showOpenDialog(Main.parent) : fc.showSaveDialog(Main.parent);
        if (answer != JFileChooser.APPROVE_OPTION)
            return null;

        if (!fc.getCurrentDirectory().getAbsolutePath().equals(curDir)) {
            Main.pref.put("lastDirectory", fc.getCurrentDirectory().getAbsolutePath());
        }

        if (!open) {
            File file = fc.getSelectedFile();
            if (file != null && file.exists()) {
                ExtendedDialog dialog = new ExtendedDialog(
                        Main.parent,
                        tr("Overwrite"),
                        new String[] {tr("Overwrite"), tr("Cancel")}
                );
                dialog.setContent(tr("File exists. Overwrite?"));
                dialog.setButtonIcons(new String[] {"save_as.png", "cancel.png"});
                if (dialog.getValue() != 1)
                    return null;
            }
        }

        return fc;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        JFileChooser fc = createAndOpenFileChooser(true, true, "Import vector graphics");
        if (fc == null)
            return;

        ImportDialog dlg = new ImportDialog();
        if (dlg.getValue() != 1)
            return;
        dlg.saveSettings();

        File[] files = fc.getSelectedFiles();

        Main.worker.submit(new ImportTask(Arrays.asList(files)));
    }

    static public class ImportTask extends PleaseWaitRunnable {
        LinkedList<Node> nodes = new LinkedList<Node>();
        LinkedList<Way> ways = new LinkedList<Way>();

        private List<File> files;
        private boolean canceled;

        public ImportTask(List<File> files) {
            super(tr("Importing..."), false);
            this.files = new ArrayList<File>(files);
        }
        @Override
        protected void cancel() {
            this.canceled = true;
        }

        @Override
        protected void finish() {
        }

        Mercator projection = new Mercator();
        EastNorth center;
        double scale;

        Way currentway;
        double lastX;
        double lastY;

        private void appendNode(double x, double y) throws IOException {
            if (currentway == null)
                throw new IOException("Shape is started incorectly");
            Node nd = new Node(projection.eastNorth2latlon(center.add(x*scale, -y*scale)));
            if (nd.getCoor().isOutSideWorld())
                throw new IOException("Shape goes outside the world");
            currentway.addNode(nd);
            nodes.add(nd);
            lastX = x;
            lastY = y;
        }
        private void appendNode(Point2D point) throws IOException {
            appendNode(point.getX(),point.getY());
        }

        private static double sqr(double x) {
            return x*x;
        }
        private static double cube(double x) {
            return x*x*x;
        }
        private static Point2D interpolate_quad(
                double ax,double ay,
                double bx,double by,
                double cx,double cy,
                double t) {
            return new Point2D.Double(
                    sqr(1-t)*ax+2*(1-t)*t*bx+t*t*cx,
                    sqr(1-t)*ay+2*(1-t)*t*by+t*t*cy);
        }
        private static Point2D interpolate_cubic(
                double ax,double ay,
                double bx,double by,
                double cx,double cy,
                double dx,double dy,
                double t) {
            return new Point2D.Double(
                    cube(1-t)*ax+3*sqr(1-t)*t*bx+3*(1-t)*t*t*cx+t*t*t*dx,
                    cube(1-t)*ay+3*sqr(1-t)*t*by+3*(1-t)*t*t*cy+t*t*t*dy);
        }
        private void processElement(SVGElement el, AffineTransform transform) throws IOException {
            if (el instanceof Group) {
                AffineTransform oldTransform = transform;
                AffineTransform xform = ((Group)el).getXForm();
                if (transform == null)
                {
                    transform = xform;
                } else if (xform != null) {
                    transform = new AffineTransform(transform);
                    transform.concatenate(xform);
                }
                for (Object child : ((Group)el).getChildren(null)) {
                    processElement((SVGElement)child, transform);
                }
                transform = oldTransform;
            } else if (el instanceof ShapeElement) {
                Shape shape = ((ShapeElement)el).getShape();
                if (transform != null) shape = transform.createTransformedShape(shape);
                PathIterator it = shape.getPathIterator(null);
                while (!it.isDone()) {
                    double[] coords = new double[6];
                    switch (it.currentSegment(coords)) {
                    case PathIterator.SEG_MOVETO:
                        currentway = new Way();
                        ways.add(currentway);
                        appendNode(coords[0],coords[1]);
                        break;
                    case PathIterator.SEG_LINETO:
                        appendNode(coords[0],coords[1]);
                        break;
                    case PathIterator.SEG_CLOSE:
                        if (currentway.firstNode().getCoor().equalsEpsilon(nodes.getLast().getCoor())) {
                            currentway.removeNode(nodes.removeLast());
                        }
                        currentway.addNode(currentway.firstNode());
                        break;
                    case PathIterator.SEG_QUADTO:
                        double lastx = lastX;
                        double lasty = lastY;
                        for (int i = 1;i<Settings.getCurveSteps();i++) {
                            appendNode(interpolate_quad(lastx,lasty,coords[0],coords[1],coords[2],coords[3],i/Settings.getCurveSteps()));
                        }
                        appendNode(coords[2],coords[3]);
                        break;
                    case PathIterator.SEG_CUBICTO:
                        lastx = lastX;
                        lasty = lastY;
                        for (int i = 1;i<Settings.getCurveSteps();i++) {
                            appendNode(interpolate_cubic(lastx,lasty,coords[0],coords[1],coords[2],coords[3],coords[4],coords[5],i/Settings.getCurveSteps()));
                        }
                        appendNode(coords[4],coords[5]);
                        break;
                    }
                    it.next();
                }
            }
        }
        @Override
        protected void realRun() throws IOException, OsmTransferException {
            LatLon center = Main.getProjection().eastNorth2latlon(Main.map.mapView.getCenter());
            scale = Settings.getScaleNumerator() / Settings.getScaleDivisor() / Math.cos(Math.toRadians(center.lat()));
            this.center = projection.latlon2eastNorth(center);
            try {
                SVGUniverse universe = new SVGUniverse();
                for (File f : files) {
                    if (canceled) return;
                    SVGDiagram diagram = universe.getDiagram(f.toURI());
                    ShapeElement root = diagram.getRoot();
                    if (root == null) throw new IOException("Can't find root SVG element");
                    Rectangle2D bbox = root.getBoundingBox();
                    this.center = this.center.add(-bbox.getCenterX()*scale, bbox.getCenterY()*scale);

                    processElement(root, null);
                }
            } catch(IOException e) {
                throw e;
            } catch(Exception e) {
                throw new IOException(e);
            }
            LinkedList<Command> cmds = new LinkedList<Command>();
            for (Node n : nodes) {
                cmds.add(new AddCommand(n));
            }
            for (Way w : ways) {
                cmds.add(new AddCommand(w));
            }
            Main.main.undoRedo.add(new SequenceCommand("Import primitives",cmds));
        }
    }
    @Override
    protected void updateEnabledState() {
        setEnabled(getCurrentDataSet() != null);
    }
}
