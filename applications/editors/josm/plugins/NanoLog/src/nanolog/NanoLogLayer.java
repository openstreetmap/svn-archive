package nanolog;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.actions.SaveAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.JumpToMarkerActions;
import org.openstreetmap.josm.gui.layer.Layer;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 *
 * @author zverik
 */
public class NanoLogLayer extends Layer implements JumpToMarkerActions.JumpToMarkerLayer {

    private List<NanoLogEntry> log;
    private int selectedEntry;
    
    public NanoLogLayer( File file ) throws IOException {
        super(tr("NanoLog"));
        log = readNanoLog(file);
        selectedEntry = -1;
    }
    
    public static List<NanoLogEntry> readNanoLog( File file ) throws IOException {
        List<NanoLogEntry> result = new ArrayList<NanoLogEntry>();
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        while( r.ready() ) {
            String line = r.readLine();
            if( line != null ) {
                // parse it
            }
        }
        r.close();
        return result;
    }

    @Override
    public void paint( Graphics2D g, MapView mv, Bounds box ) {
        // todo
        int radius = 4;
        int width = Main.map.mapView.getWidth();
        int height = Main.map.mapView.getHeight();
        Rectangle clip = g.getClipBounds();
        for( NanoLogEntry entry : log ) {
            if( entry.getPos() != null ) {
                Point p = mv.getPoint(entry.getPos());
                g.setColor(Color.green);
                g.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
            }
        }
        if( selectedEntry >= 0 && selectedEntry < log.size() ) {
            Point p = mv.getPoint(log.get(selectedEntry).getPos());
            g.setColor(Color.red);
            g.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
        }
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("nanolog.png");
    }

    @Override
    public String getToolTipText() {
        return tr("NanoLog of {0} entries", log.size());
    }

    @Override
    public void mergeFrom( Layer from ) {
        // todo
    }

    @Override
    public boolean isMergable( Layer other ) {
        return other instanceof NanoLogLayer;
    }

    @Override
    public void visitBoundingBox( BoundingXYVisitor v ) {
        for( NanoLogEntry entry : log )
            v.visit(entry.getPos());
    }

    @Override
    public Object getInfoComponent() {
        StringBuilder b = new StringBuilder();
        int cnt = 0;
        for( NanoLogEntry e : log )
            if( e.getPos() != null )
                cnt++;
        b.append(tr("NanoLog of {0} lines, {1} of them with coordinates.", log.size(), cnt));
        return b.toString();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[] {
            LayerListDialog.getInstance().createShowHideLayerAction(),
            LayerListDialog.getInstance().createDeleteLayerAction(),
            new RenameLayerAction(null, this),
            SeparatorLayerAction.INSTANCE,
            new CorrelateEntries(),
            new SaveLayer(),
            SeparatorLayerAction.INSTANCE,
            new LayerListPopup.InfoAction(this)
        };
    }

    public void jumpToNextMarker() {
        selectedEntry++;
        if( selectedEntry < 0 )
            selectedEntry = 0;
        else if( selectedEntry >= log.size() )
            selectedEntry = log.size() - 1;
        Main.map.repaint();
    }

    public void jumpToPreviousMarker() {
        selectedEntry--;
        if( selectedEntry < 0 )
            selectedEntry = 0;
        else if( selectedEntry >= log.size() )
            selectedEntry = log.size() - 1;
        Main.map.repaint();
    }
    
    private class CorrelateEntries extends AbstractAction {

        public void actionPerformed( ActionEvent e ) {
            // todo
        }
    }
    
    private class SaveLayer extends AbstractAction {

        public void actionPerformed( ActionEvent e ) {
            // todo
        }
    }
}
