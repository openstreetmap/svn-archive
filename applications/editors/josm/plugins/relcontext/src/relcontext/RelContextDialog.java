package relcontext;

import java.awt.event.MouseEvent;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;

import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.OsmPrimitivRenderer;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * The new, advanced relation editing panel.
 * 
 * @author Zverik
 */
public class RelContextDialog extends ToggleDialog implements LayerChangeListener, ChosenRelationListener {
    private JList relationsList;
    private ChosenRelation chosenRelation;

    public RelContextDialog() {
        super(tr("Open Relation Editor"), "icon_relcontext",
                tr("Opens advanced relation/multipolygon editor panel"),
                Shortcut.registerShortcut("view:relcontext", tr("Toggle: {0}", tr("Open Relation Editor")),
                KeyEvent.VK_R, Shortcut.GROUP_LAYER, Shortcut.SHIFT_DEFAULT), 150);

        chosenRelation = new ChosenRelation();
        chosenRelation.addChosenRelationListener(this);
        MapView.addEditLayerChangeListener(chosenRelation);

        relationsList = new JList();
        relationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationsList.setCellRenderer(new OsmPrimitivRenderer() {
            @Override
            protected String getComponentToolTipText( OsmPrimitive value ) {
                return null;
            }
        });
        relationsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent e ) {
                if( Main.main.getEditLayer() == null ) {
                    return;
                }
                chosenRelation.set((Relation)relationsList.getSelectedValue());
            }
        });
        add(new JScrollPane(relationsList), BorderLayout.CENTER);

        // [±][X] relation U [AZ][Down][Edit]
        JPanel topLine = new JPanel(new BorderLayout());
        JPanel topLeftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel topRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topLine.add(topLeftButtons, BorderLayout.WEST);
        topLine.add(topRightButtons, BorderLayout.EAST);
        add(topLine, BorderLayout.NORTH);

        // [+][Multi] [X]Adm [X]Tags [X]1
        JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
        add(bottomLine, BorderLayout.SOUTH);

        MapView.addLayerChangeListener(this);
    }

    public ChosenRelation getChosenRelation() {
        return chosenRelation;
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        // ?
    }

    @Override
    public void activeLayerChange( Layer arg0, Layer arg1 ) {
        // TODO Auto-generated method stub
    }

    @Override
    public void layerAdded( Layer arg0 ) {
        // TODO Auto-generated method stub
    }

    @Override
    public void layerRemoved( Layer arg0 ) {
        // TODO Auto-generated method stub
    }
}
