/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package relcontext;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.DefaultNameFormatter;

/**
 * Renderer for chosen relation.
 * [Icon] na=wood U
 * key is 2-letter; type = icon; to the right — symbol of relation topology (closed, lines, broken).
 *
 * @author Zverik
 */
public class ChosenRelationComponent extends JLabel implements ChosenRelationListener {

    private ChosenRelation chRel;

    public ChosenRelationComponent(ChosenRelation rel) {
        super("");
        this.chRel = rel;
        rel.addChosenRelationListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent e ) {
                if( chRel.get() != null && Main.map.mapView.getEditLayer() != null )
                    Main.map.mapView.getEditLayer().data.setSelected(chRel.get().getMemberPrimitives());
            }
        });
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setText(newRelation == null ? "" : newRelation.getDisplayName(DefaultNameFormatter.getInstance()));
        repaint();
    }
}
