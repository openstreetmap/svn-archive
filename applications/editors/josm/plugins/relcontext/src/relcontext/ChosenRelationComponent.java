package relcontext;

import javax.swing.JLabel;
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
        this.chRel = rel;
        rel.addChosenRelationListener(this);
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setText(prepareText(newRelation));
        repaint();
    }
    
    private final static String[] typeKeys = new String[] {
        "natural", "landuse", "place", "waterway", "leisure", "amenity"
    };

    protected String prepareText( Relation rel ) {
        if( rel == null )
            return "";

        String type = rel.get("type");
        if( type == null || type.length() == 0 )
            type = "-";

        String tag = null;
        for( int i = 0; i < typeKeys.length && tag == null; i++ )
            if( rel.hasKey(typeKeys[i]))
                tag = typeKeys[i];
        if( tag != null )
            tag = tag.substring(0, 2) + "=" + rel.get(tag);

        String name = rel.get("name");
        if( name == null && rel.hasKey("place_name") )
            name = rel.get("place_name");

        StringBuilder sb = new StringBuilder();
        sb.append(type.substring(0, 1));
        if( type.equals("boundary") && rel.hasKey("admin_level") )
            sb.append(rel.get("admin_level"));
        if( name != null )
            sb.append(" \"").append(name).append('"');
        if( tag != null )
            sb.append("; ").append(tag);

        return sb.toString();
    }
}
