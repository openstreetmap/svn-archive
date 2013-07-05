// License: GPL v2 or later. See LICENSE file for details.
package org.openstreetmap.josm.plugins.utilsplugin2;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.search.PushbackTokenizer;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.actions.search.SearchCompiler.Match;
import org.openstreetmap.josm.actions.search.SearchCompiler.ParseError;
import org.openstreetmap.josm.actions.search.SearchCompiler.UnaryMatch;
import org.openstreetmap.josm.actions.search.SearchCompiler.UnaryMatchFactory;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.utilsplugin2.actions.*;
import org.openstreetmap.josm.plugins.utilsplugin2.curves.CurveAction;
import org.openstreetmap.josm.plugins.utilsplugin2.customurl.ChooseURLAction;
import org.openstreetmap.josm.plugins.utilsplugin2.customurl.OpenPageAction;
import org.openstreetmap.josm.plugins.utilsplugin2.customurl.UtilsPluginPreferences;
import org.openstreetmap.josm.plugins.utilsplugin2.latlon.LatLonAction;
import org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry.ReplaceGeometryAction;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.*;
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
    JMenuItem latlon;
    
    JMenuItem replaceGeometry;
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

        JMenu toolsMenu = Main.main.menu.addMenu(marktr("More tools"), KeyEvent.VK_Q, 4, "help");
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
        latlon = MainMenu.add(toolsMenu, new LatLonAction());

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
        selectAreaBoundary = MainMenu.add(selectionMenu, new SelectBoundaryAction());
        
        selectURL = MainMenu.add(toolsMenu, new ChooseURLAction());
	drawArc = MainMenu.add(toolsMenu, new CurveAction());

        // register search operators
        SearchCompiler.addMatchFactory(new UtilsUnaryMatchFactory());
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

        drawArc.setEnabled(enabled);
    }
    
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new UtilsPluginPreferences();
    }
    
    public static class UtilsUnaryMatchFactory implements UnaryMatchFactory {
        private static Collection<String> keywords = Arrays.asList("inside",
                "intersecting", "allintersecting", "adjacent", "connected");
        
        @Override
        public UnaryMatch get(String keyword, Match matchOperand, PushbackTokenizer tokenizer) throws ParseError {
            if ("inside".equals(keyword))
                return new InsideMatch(matchOperand);
            else if ("adjacent".equals(keyword))
                return new ConnectedMatch(matchOperand, false);
            else if ("connected".equals(keyword))
                return new ConnectedMatch(matchOperand, true);
            else if ("intersecting".equals(keyword))
                return new IntersectingMatch(matchOperand, false);
            else if ("allintersecting".equals(keyword))
                return new IntersectingMatch(matchOperand, true);
            return null;
        }

        @Override
        public Collection<String> getKeywords() {
            return keywords;
        }
    }

    /**
     * Matches all objects contained within the match expression.
     */
    public static class InsideMatch extends UnaryMatch {
        private Collection<OsmPrimitive> inside = null;
        
        public InsideMatch(Match match) {
            super(match);
        }
        
        /**
         * Find all objects inside areas which match the expression
         */
        private void init() {
            Collection<OsmPrimitive> matchedAreas = new HashSet<OsmPrimitive>();

            // find all ways that match the expression
            Collection<Way> ways = Main.main.getCurrentDataSet().getWays();
            for (Way way : ways) {
                if (match.match(way))
                    matchedAreas.add(way);
            }
            
            // find all relations that match the expression
            Collection<Relation> rels = Main.main.getCurrentDataSet().getRelations();
            for (Relation rel : rels) {
                if (match.match(rel))
                    matchedAreas.add(rel);
            }
            
            inside = NodeWayUtils.selectAllInside(matchedAreas, Main.main.getCurrentDataSet());
        }

        @Override
        public boolean match(OsmPrimitive osm) {
            if (inside == null)
                init(); // lazy initialization

            return inside.contains(osm);
        }
    }
    
    public static class IntersectingMatch extends UnaryMatch {
        private Collection<Way> intersecting = null;
        boolean all;
        
        public IntersectingMatch(Match match, boolean all) {
            super(match);
            this.all=all;
            //init(all);
        }   
        
        /**
         * Find (all) ways intersecting ways which match the expression.
         */
        private void init(boolean all) {
            Collection<Way> matchedWays = new HashSet<Way>();
            
            // find all ways that match the expression
            Collection<Way> allWays = Main.main.getCurrentDataSet().getWays();
            for (Way way : allWays) {
                if (match.match(way))
                    matchedWays.add(way);
            }
            
            Set<Way> newWays = new HashSet<Way>();
            if (all)
                NodeWayUtils.addWaysIntersectingWaysRecursively(allWays, matchedWays, newWays);
            else
                NodeWayUtils.addWaysIntersectingWays(allWays, matchedWays, newWays);
            intersecting = newWays;
        }
        
        @Override
        public boolean match(OsmPrimitive osm) {
            if (intersecting==null) init(all); // lazy initialization
            if (osm instanceof Way)
                return intersecting.contains((Way)osm);
            return false;
        }
    }
    
    public static class ConnectedMatch extends UnaryMatch {
        private Collection<Way> connected = null;
        boolean all;
        
        public ConnectedMatch(Match match, boolean all) {
            super(match);
            this.all=all;
        }   
        
        /**
         * Find (all) ways intersecting ways which match the expression.
         */
        private void init(boolean all) {
            Collection<Way> matchedWays = new HashSet<Way>();
            Set<Node> matchedNodes = new HashSet<Node>();
            
            // find all ways that match the expression
            Collection<Way> allWays = Main.main.getCurrentDataSet().getWays();
            for (Way way : allWays) {
                if (match.match(way))
                    matchedWays.add(way);
            }
            
            // find all nodes that match the expression
            Collection<Node> allNodes = Main.main.getCurrentDataSet().getNodes();
            for (Node node: allNodes) {
                if (match.match(node))
                    matchedNodes.add(node);
            }
            
            Set<Way> newWays = new HashSet<Way>();
            if (all) {
                NodeWayUtils.addWaysConnectedToNodes(matchedNodes, newWays);
                NodeWayUtils.addWaysConnectedToWaysRecursively(matchedWays, newWays);
            } else {
                NodeWayUtils.addWaysConnectedToNodes(matchedNodes, newWays);
                NodeWayUtils.addWaysConnectedToWays(matchedWays, newWays);
            }
            connected = newWays;
        }
        
        @Override
        public boolean match(OsmPrimitive osm) {
            if (connected==null) init(all); // lazy initialization
            if (osm instanceof Way)
                return connected.contains((Way)osm);
            return false;
        }
    }   
    
}
