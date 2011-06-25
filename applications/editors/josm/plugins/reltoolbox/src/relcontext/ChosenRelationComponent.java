package relcontext;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import org.openstreetmap.josm.data.osm.Relation;

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
/*        setBackground(Color.white);
        setOpaque(true);
        setBorder(new LineBorder(Color.black, 1, true));*/
        this.chRel = rel;
        rel.addChosenRelationListener(this);
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setText(prepareText(newRelation));
        repaint();
    }
    
    private final static String[] TYPE_KEYS = new String[] {
        "natural", "landuse", "place", "waterway", "leisure", "amenity", "restriction", "public_transport", "route", "enforcement"
    };

    private final static String[] NAMING_TAGS = new String[] {
        "name", "place_name", "ref", "destination", "note"
    };

    protected String prepareText( Relation rel ) {
        if( rel == null )
            return "";

        String type = rel.get("type");
        if( type == null || type.length() == 0 )
            type = "-";

        String tag = null;
        for( int i = 0; i < TYPE_KEYS.length && tag == null; i++ )
            if( rel.hasKey(TYPE_KEYS[i]))
                tag = TYPE_KEYS[i];
        if( tag != null )
            tag = tag.substring(0, 2) + "=" + rel.get(tag);

        String name = null;
        for( int i = 0; i < NAMING_TAGS.length && name == null; i++ )
            if( rel.hasKey(NAMING_TAGS[i]) )
                name = rel.get(NAMING_TAGS[i]);

        StringBuilder sb = new StringBuilder();
        sb.append(type.substring(0, 1));
        if( type.equals("boundary") && rel.hasKey("admin_level") )
            sb.append(rel.get("admin_level"));
        if( name != null )
            sb.append(" \"").append(name).append('"');
        if( tag != null )
            sb.append("; ").append(tag);
        sb.append(" (").append(rel.getMembersCount()).append(')');

        return sb.toString();
    }
}
