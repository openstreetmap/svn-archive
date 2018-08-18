// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.JunctionChecker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.reader.ColorSchemeXMLReader;
import org.openstreetmap.josm.plugins.JunctionChecker.util.RelationProducer;

/**
 *
 * This plugins helps to create a channel digraph und check channels in this network if it is a
 * junction or searches in a subgraph after junctions.
 * @author Jörg Possin (joerg.possin@uni-muenster.de)
 */
public class JunctionCheckerPlugin extends Plugin implements LayerChangeListener {

    private static final String COLORSCHEMEFILTERFILE = "/resources/xml/colorscheme.xml";
    private JunctionCheckDialog junctionCheckDialog;
    private File pathDir;
    private final RelationProducer relationproducer;
    //Die benötigten Layer für JOSM
    private OsmDataLayer osmlayer; //IN diesem Layer sind die Originaldaten gespiechert, aus denen der Channel-Digraph erzeugt wird
    private ChannelDiGraphLayer channelDigraphLayer;
    private final ColorSchemeXMLReader cXMLReaderMK;
    private ChannelDiGraph channelDigraph;
    private final JunctionCheckerMapMode jcMapMode;
    private MapMode normalMapMode;

    public JunctionCheckerPlugin(PluginInformation info) {
        super(info);
        jcMapMode = new JunctionCheckerMapMode("junctionchecking", tr("construct channel digraph and search for junctions"));
        relationproducer = new RelationProducer(this);
        cXMLReaderMK = new ColorSchemeXMLReader(COLORSCHEMEFILTERFILE);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        jcMapMode.setFrame(newFrame);
        if (newFrame != null) {
            junctionCheckDialog = new JunctionCheckDialog(this);
            newFrame.addToggleDialog(junctionCheckDialog);
            MainApplication.getLayerManager().addLayerChangeListener(this);
        } else
            MainApplication.getLayerManager().removeLayerChangeListener(this);
    }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        if (newLayer instanceof OsmDataLayer) {
            this.getJunctionCheckDialog().setActivateCreateDigraph(true);
            this.getJunctionCheckDialog().setActivateJunctionCheckOrSearch(false);
            if (normalMapMode != null) {
                MainApplication.getMap().selectMapMode(normalMapMode);
            }
        } else if (newLayer instanceof ChannelDiGraphLayer) {
            this.getJunctionCheckDialog().setActivateCreateDigraph(false);
            this.getJunctionCheckDialog().setActivateJunctionCheckOrSearch(true);
            MainApplication.getMap().selectMapMode(jcMapMode);
        }
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (e.getRemovedLayer() == channelDigraphLayer) {
            channelDigraphLayer = null;
            this.getJunctionCheckDialog().setActivateJunctionCheckOrSearch(false);
            return;
        } else {
            this.getJunctionCheckDialog().setActivateCreateDigraph(false);
        }
    }

    public ChannelDiGraphLayer getChannelDigraphLayer() {
        if (channelDigraphLayer == null) {
            channelDigraphLayer = new ChannelDiGraphLayer(cXMLReaderMK);
        }
        return channelDigraphLayer;
    }

    public JunctionCheckDialog getJunctionCheckDialog() {
        return junctionCheckDialog;
    }

    public File getPathDir() {
        return pathDir;
    }

    public OsmDataLayer getOsmlayer() {
        return osmlayer;
    }

    public void setOsmlayer(OsmDataLayer osmlayer) {
        this.osmlayer = osmlayer;
    }

    public RelationProducer getRelationProducer() {
        return relationproducer;
    }

    public ChannelDiGraph getChannelDigraph() {
        return channelDigraph;
    }

    public void setChannelDigraph(ChannelDiGraph channelDigraph) {
        this.channelDigraph = channelDigraph;
    }

    public JunctionCheckerMapMode getJcMapMode() {
        return jcMapMode;
    }

    public void setNormalMapMode(MapMode mm) {
        normalMapMode = mm;
    }
}
