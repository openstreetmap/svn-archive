// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.AddIntersectionsAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.AlignWayNodesAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.CopyTagsAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.ExtractPointAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.PasteRelationsAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.SplitObjectAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.SplitOnIntersectionsAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.SymmetryAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.TagBufferAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.TagSourceAction;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.UnGlueRelationAction;
import org.openstreetmap.josm.plugins.utilsplugin2.curves.CurveAction;
import org.openstreetmap.josm.plugins.utilsplugin2.customurl.ChooseURLAction;
import org.openstreetmap.josm.plugins.utilsplugin2.customurl.OpenPageAction;
import org.openstreetmap.josm.plugins.utilsplugin2.customurl.UtilsPluginPreferences;
import org.openstreetmap.josm.plugins.utilsplugin2.latlon.LatLonAction;
import org.openstreetmap.josm.plugins.utilsplugin2.multitagger.MultiTagAction;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryAction;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceMembershipAction;
import org.openstreetmap.josm.plugins.utilsplugin2.search.UtilsSimpleMatchFactory;
import org.openstreetmap.josm.plugins.utilsplugin2.search.UtilsUnaryMatchFactory;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.AdjacentNodesAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.AdjacentWaysAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.ConnectedWaysAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.IntersectedWaysAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.IntersectedWaysRecursiveAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.MiddleNodesAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.SelectAllInsideAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.SelectBoundaryAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.SelectHighwayAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.SelectModNodesAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.SelectModWaysAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.SelectWayNodesAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.UndoSelectionAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.UnselectNodesAction;

public class UtilsPlugin2 extends Plugin {

    private static UtilsPlugin2 instance;

    JMenuItem copyTags;

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
    JMenuItem latlon;
    JMenuItem multiTag;

    JMenuItem replaceGeometry;
    JMenuItem replaceMembership;
    JMenuItem tagBuffer;
    JMenuItem sourceTag;
    JMenuItem pasteRelations;
    JMenuItem alignWayNodes;
    JMenuItem splitOnIntersections;
    JMenuItem selModifiedNodes;
    JMenuItem selModifiedWays;
    JMenuItem selectHighway;
    JMenuItem selectAreaBoundary;

    JMenuItem selectURL;

    JMenuItem drawArc;

    public UtilsPlugin2(PluginInformation info) {
        super(info);
        instance = this;

        JMenu editMenu = Main.main.menu.editMenu;
        JMenu toolsMenu = Main.main.menu.moreToolsMenu;
        JMenu dataMenu = Main.main.menu.dataMenu;
        JMenu selectionMenu = Main.main.menu.selectionMenu;

        copyTags = MainMenu.addAfter(editMenu, new CopyTagsAction(), false, Main.main.menu.copy);

        addIntersections = MainMenu.add(toolsMenu, new AddIntersectionsAction());
        splitObject = MainMenu.add(toolsMenu, new SplitObjectAction());
        alignWayNodes = MainMenu.add(toolsMenu, new AlignWayNodesAction());
        symmetry = MainMenu.add(toolsMenu, new SymmetryAction());
        splitOnIntersections = MainMenu.add(toolsMenu, new SplitOnIntersectionsAction());
        unglueRelation = MainMenu.add(toolsMenu, new UnGlueRelationAction());
        toolsMenu.addSeparator();
        replaceGeometry = MainMenu.add(toolsMenu, new ReplaceGeometryAction());
        replaceMembership = MainMenu.add(toolsMenu, new ReplaceMembershipAction());
        extractPoint = MainMenu.add(toolsMenu, new ExtractPointAction());
        tagBuffer = MainMenu.add(toolsMenu, new TagBufferAction());
        sourceTag = MainMenu.add(toolsMenu, new TagSourceAction());
        pasteRelations = MainMenu.add(toolsMenu, new PasteRelationsAction());
        wiki = MainMenu.add(dataMenu, new OpenPageAction());
        latlon = MainMenu.add(toolsMenu, new LatLonAction());
        drawArc = MainMenu.add(toolsMenu, new CurveAction());

        selectionMenu.addSeparator();

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
        selectAreaBoundary = MainMenu.add(selectionMenu, new SelectBoundaryAction());

        selectURL = MainMenu.add(dataMenu, new ChooseURLAction());
        multiTag = MainMenu.add(dataMenu, new MultiTagAction());

        // register search operators
        SearchCompiler.addMatchFactory(new UtilsUnaryMatchFactory());
        SearchCompiler.addMatchFactory(new UtilsSimpleMatchFactory());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        boolean enabled = newFrame != null;
        enabled = false;
        unglueRelation.setEnabled(enabled);
        addIntersections.setEnabled(enabled);
        splitObject.setEnabled(enabled);

        replaceGeometry.setEnabled(enabled);
        replaceMembership.setEnabled(enabled);
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

        drawArc.setEnabled(enabled);
        multiTag.setEnabled(enabled);
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new UtilsPluginPreferences();
    }

    public static final UtilsPlugin2 getInstance() {
        return instance;
    }
}
