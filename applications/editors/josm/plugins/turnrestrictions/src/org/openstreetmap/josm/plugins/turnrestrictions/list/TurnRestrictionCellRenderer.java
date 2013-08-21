package org.openstreetmap.josm.plugins.turnrestrictions.list;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.JMultilineLabel;
import org.openstreetmap.josm.plugins.turnrestrictions.preferences.PreferenceKeys;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This is a cell renderer for turn restrictions.
 * 
 * It can be used a cell renderer in lists of turn restrictions and as cell renderer in
 * {@link JTable}s displaying turn restrictions. 
 * 
 */
public class TurnRestrictionCellRenderer extends JPanel implements ListCellRenderer, TableCellRenderer{
    //static private final Logger logger = Logger.getLogger(TurnRestrictionCellRenderer.class.getName());
    
    /** the names of restriction types */
    static private Set<String> RESTRICTION_TYPES = new HashSet<String>(
            Arrays.asList(new String[] {
                    "no_left_turn",
                    "no_right_turn",
                    "no_straight_on",
                    "no_u_turn",
                    "only_left_turn",
                    "only_right_turn",
                    "only_straight_on"
            })
    );
    
    /** components used to render the turn restriction */
    private JLabel icon;
    private JLabel from;
    private JLabel to;
    private String iconSet = "set-a";
    
    public TurnRestrictionCellRenderer() {
        build();
    }

    /**
     * Replies true if {@code restrictionType} is a valid restriction
     * type.
     * 
     * @param restrictionType the restriction type 
     * @return true if {@code restrictionType} is a valid restriction
     * type
     */
    protected boolean isValidRestrictionType(String restrictionType) {
        if (restrictionType == null) return false;
        restrictionType = restrictionType.trim().toLowerCase();
        return RESTRICTION_TYPES.contains(restrictionType);
    }
    
    /**
     * Builds the icon name for a given restriction type 
     * 
     * @param restrictionType the restriction type 
     * @return the icon name 
     */
    protected String buildImageName(String restrictionType) {
        return "types/" + iconSet + "/" + restrictionType;
    }
    
    /**
     * Replies the icon for a given restriction type 
     * @param restrictionType the restriction type 
     * @return the icon 
     */
    protected ImageIcon getIcon(String restrictionType) {
        if (!isValidRestrictionType(restrictionType)) {
            return ImageProvider.get("types", "non-standard-type");
        }
        return ImageProvider.get(buildImageName(restrictionType));
    }
    
    /**
     * Builds the UI used to render turn restrictions 
     */
    protected void build() {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        
        // the turn restriction icon        
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.0;
        gc.gridheight = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(0,0,2,2);
        add(icon = new JLabel(), gc);
        
        
        // the name of the way with role "from"
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.gridx = 1;
        gc.gridheight = 1;
        gc.weightx = 0.0;
        add(new JMultilineLabel("<html><strong>" + trc("turnrestrictions","From:") + "</strong></html>"), gc);
        
        gc.gridx = 2;
        gc.weightx = 1.0; 
        add(from = new JLabel(), gc);
        
        // the name of the way with role "to"
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.gridx = 1;
        gc.gridy = 1;
        gc.weightx = 0.0;
        add(new JMultilineLabel("<html><strong>" + trc("turnrestriction", "To:")  + "</strong></html>"), gc);
        
        gc.gridx = 2;
        gc.weightx = 1.0;
        add(to = new JLabel(), gc);
    }

    /**
     * Renders the icon for the turn restriction  
     * 
     * @param tr the turn restriction
     */
    protected void renderIcon(Relation tr) {
        String restrictionType = tr.get("restriction");
        icon.setIcon(getIcon(restrictionType));
    }

    /**
     * Replies a way participating in this turn restriction in a given role
     * 
     * @param tr the turn restriction 
     * @param role the role (either "from" or "to")
     * @return the participating way; null, if no way is participating in this role
     */
    private Way getParticipatingWay(Relation tr, String role){
        for(RelationMember rm: tr.getMembers()){
            if (rm.getRole().trim().toLowerCase().equals(role) && rm.getType().equals(OsmPrimitiveType.WAY)) {
                return (Way)rm.getMember();
            }
        }
        return null;
    }
    
    protected void renderFrom(Relation tr) {
        Way from = getParticipatingWay(tr, "from");
        if (from == null) {
            // FIXME: render as warning/error (red background?)
            this.from.setText(tr("no participating way with role ''from''"));
            return;
        } 
        this.from.setText(DefaultNameFormatter.getInstance().format(from));
    }

    protected void renderTo(Relation tr) {
        Way to = getParticipatingWay(tr, "to");
        if (to == null) {
            // FIXME: render as warning/error (red background?)
            this.to.setText(tr("no participating way with role ''to''"));
            return;
        } 
        this.to.setText(DefaultNameFormatter.getInstance().format(to));
    }

    /**
     * Renders the foreground and background color depending on whether
     * the turn restriction is selected
     * 
     * @param isSelected true if the turn restriction is selected; false,
     * otherwise
     */
    protected void renderColor(boolean isSelected) {
        Color bg;
        Color fg;
        if (isSelected) {
            bg = UIManager.getColor("List.selectionBackground");
            fg = UIManager.getColor("List.selectionForeground");
        } else {
            bg = UIManager.getColor("background");
            fg = UIManager.getColor("foreground");
        }
        setBackground(bg);
        this.icon.setBackground(bg);
        this.from.setBackground(bg);
        this.to.setBackground(bg);
        
        setForeground(fg);
        this.icon.setForeground(fg);
        this.from.setForeground(fg);
        this.to.setForeground(fg);
    }

    /**
     * Initializes the set of icons used from the preference key
     * {@link PreferenceKeys#ROAD_SIGNS}.
     * 
     * @param prefs the JOSM preferences 
     */
    public void initIconSetFromPreferences(Preferences prefs){
        
        iconSet = prefs.get(PreferenceKeys.ROAD_SIGNS, "set-a");
        iconSet = iconSet.trim().toLowerCase();
        if (!iconSet.equals("set-a") && !iconSet.equals("set-b")) {
            iconSet = "set-a";
        }
    }
    
    /* ---------------------------------------------------------------------------------- */
    /* interface ListCellRenderer                                                         */
    /* ---------------------------------------------------------------------------------- */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        renderColor(isSelected);
        Relation tr = (Relation)value;
        renderIcon(tr);
        renderFrom(tr);
        renderTo(tr);       
        return this;
    }

    /* ---------------------------------------------------------------------------------- */
    /* interface TableCellRenderer                                                        */
    /* ---------------------------------------------------------------------------------- */
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        renderColor(isSelected);        
        Relation tr = (Relation)value;
        renderIcon(tr);
        renderFrom(tr);
        renderTo(tr);       
        return this;
    }   
}
