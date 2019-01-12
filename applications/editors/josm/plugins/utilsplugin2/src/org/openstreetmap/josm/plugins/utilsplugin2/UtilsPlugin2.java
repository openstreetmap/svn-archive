// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2;

import javax.swing.JMenu;

import org.openstreetmap.josm.data.osm.search.SearchCompiler;
import org.openstreetmap.josm.gui.MainApplication;
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

/**
 * Collection of utilities
 */
public class UtilsPlugin2 extends Plugin {

    private static UtilsPlugin2 instance;

    public UtilsPlugin2(PluginInformation info) {
        super(info);
        instance = this;

        JMenu editMenu = MainApplication.getMenu().editMenu;
        JMenu toolsMenu = MainApplication.getMenu().moreToolsMenu;
        JMenu dataMenu = MainApplication.getMenu().dataMenu;
        JMenu selectionMenu = MainApplication.getMenu().selectionMenu;

        MainMenu.addAfter(editMenu, new CopyTagsAction(), false, MainApplication.getMenu().copy);

        MainMenu.add(toolsMenu, new AddIntersectionsAction());
        MainMenu.add(toolsMenu, new SplitObjectAction());
        MainMenu.add(toolsMenu, new AlignWayNodesAction());
        MainMenu.add(toolsMenu, new SymmetryAction());
        MainMenu.add(toolsMenu, new SplitOnIntersectionsAction());
        MainMenu.add(toolsMenu, new UnGlueRelationAction());

        toolsMenu.addSeparator();

        MainMenu.add(toolsMenu, new ReplaceGeometryAction());
        MainMenu.add(toolsMenu, new ReplaceMembershipAction());
        MainMenu.add(toolsMenu, new ExtractPointAction());
        MainMenu.add(toolsMenu, new TagBufferAction());
        MainMenu.add(toolsMenu, new TagSourceAction());
        MainMenu.add(toolsMenu, new PasteRelationsAction());
        MainMenu.add(dataMenu, new OpenPageAction());
        MainMenu.add(toolsMenu, new LatLonAction());
        MainMenu.add(toolsMenu, new CurveAction());

        selectionMenu.addSeparator();

        MainMenu.add(selectionMenu, new SelectWayNodesAction());
        MainMenu.add(selectionMenu, new AdjacentNodesAction());
        MainMenu.add(selectionMenu, new UnselectNodesAction());
        MainMenu.add(selectionMenu, new MiddleNodesAction());
        MainMenu.add(selectionMenu, new AdjacentWaysAction());
        MainMenu.add(selectionMenu, new ConnectedWaysAction());
        MainMenu.add(selectionMenu, new IntersectedWaysAction());
        MainMenu.add(selectionMenu, new IntersectedWaysRecursiveAction());
        MainMenu.add(selectionMenu, new SelectAllInsideAction());
        MainMenu.add(selectionMenu, new SelectModNodesAction());
        MainMenu.add(selectionMenu, new SelectModWaysAction());
        MainMenu.add(selectionMenu, new UndoSelectionAction());
        MainMenu.add(selectionMenu, new SelectHighwayAction());
        MainMenu.add(selectionMenu, new SelectBoundaryAction());

        MainMenu.add(dataMenu, new ChooseURLAction());
        MainMenu.add(dataMenu, new MultiTagAction());

        // register search operators
        SearchCompiler.addMatchFactory(new UtilsUnaryMatchFactory());
        SearchCompiler.addMatchFactory(new UtilsSimpleMatchFactory());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new UtilsPluginPreferences();
    }

    public static final UtilsPlugin2 getInstance() {
        return instance;
    }
}
