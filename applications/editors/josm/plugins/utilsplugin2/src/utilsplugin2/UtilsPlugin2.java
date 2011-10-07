// License: GPL v2 or later. See LICENSE file for details.
package utilsplugin2;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import utilsplugin2.selection.*;
import utilsplugin2.dumbutils.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import static org.openstreetmap.josm.tools.I18n.marktr;

public class UtilsPlugin2 extends Plugin {
    JMenuItem unglueRelation;
    JMenuItem symmetry;
    JMenuItem addIntersections;
    JMenuItem splitObject;
    JMenuItem selectWayNodes;
    JMenuItem adjNodes;
    JMenuItem unsNodes;
    JMenuItem midNodes;
    JMenuItem adjWays;
    JMenuItem adjWaysAll;
    JMenuItem intWays;
    JMenuItem intWaysR;
    JMenuItem allInside;
    JMenuItem undoSelection;
    JMenuItem extractPoint;
    JMenuItem wiki;
    
    JMenuItem replaceGeometry;
    JMenuItem tagBuffer;
    JMenuItem sourceTag;
    JMenuItem pasteRelations;
    JMenuItem alignWayNodes;
    JMenuItem splitOnIntersections;
    JMenuItem selModifiedNodes;
    JMenuItem selModifiedWays;
    JMenuItem selectHighway;
    
    JMenuItem selectURL;
    
    public UtilsPlugin2(PluginInformation info) {
        super(info);

        JMenu toolsMenu = Main.main.menu.addMenu(marktr("More tools"), KeyEvent.VK_R, 4, "help");
        unglueRelation = MainMenu.add(toolsMenu, new UnGlueRelationAction());
        addIntersections = MainMenu.add(toolsMenu, new AddIntersectionsAction());
        splitObject = MainMenu.add(toolsMenu, new SplitObjectAction());
        
        toolsMenu.addSeparator();
        replaceGeometry = MainMenu.add(toolsMenu, new ReplaceGeometryAction());
        tagBuffer = MainMenu.add(toolsMenu, new TagBufferAction());
        sourceTag = MainMenu.add(toolsMenu, new TagSourceAction());
        pasteRelations = MainMenu.add(toolsMenu, new PasteRelationsAction());
        alignWayNodes = MainMenu.add(toolsMenu, new AlignWayNodesAction());
        splitOnIntersections = MainMenu.add(toolsMenu, new SplitOnIntersectionsAction());
        extractPoint = MainMenu.add(toolsMenu, new ExtractPointAction());
        symmetry = MainMenu.add(toolsMenu, new SymmetryAction());
        wiki = MainMenu.add(toolsMenu, new OpenPageAction());

        JMenu selectionMenu = Main.main.menu.addMenu(marktr("Selection"), KeyEvent.VK_N, Main.main.menu.defaultMenuPos, "help");
        selectWayNodes = MainMenu.add(selectionMenu, new SelectWayNodesAction());
        adjNodes = MainMenu.add(selectionMenu, new AdjacentNodesAction());
        unsNodes = MainMenu.add(selectionMenu, new UnselectNodesAction());
        midNodes = MainMenu.add(selectionMenu, new MiddleNodesAction());
        adjWays = MainMenu.add(selectionMenu, new AdjacentWaysAction());
        adjWaysAll = MainMenu.add(selectionMenu, new ConnectedWaysAction());
        intWays = MainMenu.add(selectionMenu, new IntersectedWaysAction());
        intWaysR = MainMenu.add(selectionMenu, new IntersectedWaysRecursiveAction());
        allInside = MainMenu.add(selectionMenu, new SelectAllInsideAction());
        selModifiedNodes = MainMenu.add(selectionMenu, new SelectModNodesAction());
        selModifiedWays = MainMenu.add(selectionMenu, new SelectModWaysAction());
        undoSelection = MainMenu.add(selectionMenu, new UndoSelectionAction());
        selectHighway = MainMenu.add(selectionMenu, new SelectHighwayAction());
        
        selectURL = MainMenu.add(toolsMenu, new ChooseURLAction());
        

    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        boolean enabled = newFrame != null;
        enabled = false;
        unglueRelation.setEnabled(enabled);
        addIntersections.setEnabled(enabled);
        splitObject.setEnabled(enabled);

        replaceGeometry.setEnabled(enabled);
        tagBuffer.setEnabled(enabled);
        sourceTag.setEnabled(enabled);
        pasteRelations.setEnabled(enabled);
        alignWayNodes.setEnabled(enabled);
        splitOnIntersections.setEnabled(enabled);
        wiki.setEnabled(enabled);

        selectWayNodes.setEnabled(enabled);
        adjNodes.setEnabled(enabled);
        unsNodes.setEnabled(enabled);
        midNodes.setEnabled(enabled);
        adjWays.setEnabled(enabled);
        adjWaysAll.setEnabled(enabled);
        intWays.setEnabled(enabled);
        intWaysR.setEnabled(enabled);
        selModifiedNodes.setEnabled(enabled);
        selModifiedWays.setEnabled(enabled);
        undoSelection.setEnabled(enabled);
        selectURL.setEnabled(enabled);
        allInside.setEnabled(enabled);
    }
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new UtilsPluginPreferences();
    }

}
