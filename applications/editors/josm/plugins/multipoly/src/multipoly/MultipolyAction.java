// License: GPL.
package multipoly;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JOptionPane;

import multipoly.Multipolygon.JoinedPolygon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Create multipolygon from selected ways automatically.
 *
 * New relation with type=multipolygon is created
 *
 * If one or more of ways is already in relation with type=multipolygon or the
 * way is not closed, then error is reported and no relation is created
 *
 * The "inner" and "outer" roles are guessed automatically. First, bbox is
 * calculated for each way. then the largest area is assumed to be outside and
 * the rest inside In cases with one "outside" area and several cut-ins, the
 * guess should be always good ... In more complex (multiple outer areas) or
 * buggy (inner and outer ways intersect) scenarios the result is likely to be
 * wrong.
 */
public class MultipolyAction extends JosmAction {

  public MultipolyAction() {
    super(tr("Create multipolygon"), "multipoly_create", tr("Create multipolygon."),
      Shortcut.registerShortcut("tools:multipoly", tr("Tool: {0}", tr("Create multipolygon")),
      KeyEvent.VK_M, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
  }
  /**
   * The action button has been clicked
   *
   * @param e Action Event
   */
  public void actionPerformed(ActionEvent e) {
    if (Main.main.getEditLayer() == null) {
      JOptionPane.showMessageDialog(Main.parent, tr("No data loaded."));
      return;
    }

    Collection < Way > selectedWays = Main.main.getCurrentDataSet().getSelectedWays();

    if (selectedWays.size() < 1) {
      // Sometimes it make sense creating multipoly of only one way (so it will form outer way)
      // and then splitting the way later (so there are multiple ways forming outer way)
      JOptionPane.showMessageDialog(Main.parent, tr("You must select at least one way."));
      return;
    }

    Multipolygon polygon = this.analyzeWays(selectedWays);

    if (polygon == null) {
      return;                   //could not make multipolygon.
    }

    Relation relation = this.createRelation(polygon);

    if (Main.pref.getBoolean("multipoly.show-relation-editor", false)) {
      //Open relation edit window, if set up in preferences
      RelationEditor editor = RelationEditor.getEditor(Main.main.getEditLayer(), relation, null);
      editor.setVisible(true);
    } else {
      //Just add the relation
      Main.main.undoRedo.add(new AddCommand(relation));
      Main.map.repaint();
    }
  }

  /** Enable this action only if something is selected */
  @Override protected void updateEnabledState() {
    if (getCurrentDataSet() == null) {
      setEnabled(false);
    } else {
      updateEnabledState(getCurrentDataSet().getSelected());
    }
  }

  /** Enable this action only if something is selected */
  @Override protected void updateEnabledState(Collection < ? extends OsmPrimitive > selection) {
    setEnabled(selection != null && !selection.isEmpty());
  }

  /**
   * This method analyzes ways and creates multipolygon.
   * @param selectedWays
   * @return null, if there was a problem with the ways.
   */
  private Multipolygon analyzeWays(Collection < Way > selectedWays) {

    Multipolygon pol = new Multipolygon();
    String error = pol.makeFromWays(selectedWays);

    if (error != null) {
      JOptionPane.showMessageDialog(Main.parent, error);
      return null;
    } else {
      return pol;
    }
  }

  /**
   * Builds a relation from polygon ways.
   * @param pol 
   * @return
   */
  private Relation createRelation(Multipolygon pol) {
    // Create new relation
    Relation rel = new Relation();
    rel.put("type", "multipolygon");
    // Add ways to it
    for (JoinedPolygon jway:pol.outerWays) {
      for (Way way:jway.ways) {
          rel.addMember(new RelationMember("outer", way));
      }
    }

    for (JoinedPolygon jway:pol.innerWays) {
      for (Way way:jway.ways) {
          rel.addMember(new RelationMember("inner", way));
      }
    }
    return rel;
  }
}
