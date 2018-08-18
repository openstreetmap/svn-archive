// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.JunctionChecker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.JunctionChecker.connectedness.DiGraphSealer;
import org.openstreetmap.josm.plugins.JunctionChecker.connectedness.StrongConnectednessCalculator;
import org.openstreetmap.josm.plugins.JunctionChecker.converting.ChannelDigraphBuilder;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.filter.ExecuteFilter;
import org.openstreetmap.josm.plugins.JunctionChecker.reader.XMLFilterReader;
import org.xml.sax.SAXException;

/**
 * Diese Klasse erstellt aus den Daten eines OSMDatenLayers den Channel-Digraphen
 * Die dafür notwendigen Schritte werden von dieser Klasse angestoßen
 * @author  joerg
 */
public class DigraphCreationTask extends PleaseWaitRunnable {

    private final JunctionCheckerPlugin plugin;
    private final boolean sealGraph;
    private boolean canceled;
    private final boolean calculateSCC;

    private static final String WAYFILTERFILE = "/resources/xml/waysfilter.xml";

    public DigraphCreationTask(JunctionCheckerPlugin plugin, boolean sealGraph, boolean calculateSCC) {
        super(tr("Create Channel Digraph"), false);
        this.plugin = plugin;
        this.sealGraph = sealGraph;
        this.calculateSCC = calculateSCC;
    }

    @Override
    protected void cancel() {
        canceled = true;
        progressMonitor.cancel();
    }

    @Override
    protected void finish() {
        if (canceled) {
            removeDigraphLayer();
        }
    }

    private void removeDigraphLayer() {
        ChannelDiGraphLayer layer = plugin.getChannelDigraphLayer();
        if (MainApplication.getLayerManager().containsLayer(layer)) {
            MainApplication.getLayerManager().removeLayer(layer);
        }
    }

    @Override
    protected void realRun() throws SAXException, IOException,
    OsmTransferException {
        //Prüfen, ob der ausgewählte Layer ein OSMDataLayer ist
        if (MainApplication.getMap() == null
                || !MainApplication.getMap().isVisible() || !(MainApplication.getLayerManager().getActiveLayer() instanceof OsmDataLayer)) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("this layer is no osm data layer"));
            return;
        }
        removeDigraphLayer();
        int tickscounter = 4;
        if (sealGraph) {
            tickscounter++;
        }
        if (calculateSCC) {
            tickscounter++;
        }
        getProgressMonitor().setTicksCount(tickscounter);
        tickscounter = 1;
        getProgressMonitor().subTask(tr("Converting OSM graph into Channel Digraph"));
        getProgressMonitor().setTicks(tickscounter++);

        OSMGraph graph = new OSMGraph();
        //Der vom Benutzer in JOSM ausgewählte, zur Zeit aktive Layer wird der PLugin-OSM-Layer
        plugin.setOsmlayer((OsmDataLayer) MainApplication.getLayerManager().getActiveLayer());
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        Iterator<Node> it = ds.getNodes().iterator();
        while (it.hasNext()) {
            graph.addNode(it.next());
        }
        Iterator<Way> itway = ds.getWays().iterator();
        while (itway.hasNext()) {
            graph.addWay(itway.next());
        }
        Iterator<Relation> itrel = ds.getRelations().iterator();
        while (itrel.hasNext()) {
            graph.addRelation(itrel.next());
        }
        Iterator<DataSource> itdata = ds.getDataSources().iterator();
        while (itdata.hasNext()) {
            Bounds b = itdata.next().bounds;
            graph.setBbbottom(b.getMin().getY());
            graph.setBbleft(b.getMin().getX());
            graph.setBbright(b.getMax().getX());
            graph.setBbtop(b.getMax().getY());
        }
        getProgressMonitor().subTask(tr("filtering ways"));
        getProgressMonitor().setTicks(tickscounter++);
        // Filter mit gewünschten Ways laden
        XMLFilterReader reader = new XMLFilterReader(
                WAYFILTERFILE);
        reader.parseXML();
        // gewünschte Ways filtern
        ExecuteFilter ef = new ExecuteFilter(reader.getFilters(), graph);
        ef.filter();
        getProgressMonitor().subTask(tr("creating Channel-Digraph"));
        getProgressMonitor().setTicks(tickscounter++);
        // ChannelDiGraphen erzeugen
        ChannelDigraphBuilder cdgb = new ChannelDigraphBuilder(ef.getOutgoinggraph());
        cdgb.buildChannelDigraph();
        StrongConnectednessCalculator scc = new StrongConnectednessCalculator(cdgb.getDigraph());
        // DiGraph "versiegeln"
        if (sealGraph) {
            getProgressMonitor().subTask(tr("sealing Digraph"));
            getProgressMonitor().setTicks(tickscounter++);
            DiGraphSealer sealer = new DiGraphSealer(cdgb.getDigraph(), cdgb
                    .getNewid());
            sealer.sealingGraph();
        }
        //Digraph starke Zusammenhangskomponenten berechnen
        if (calculateSCC) {
            getProgressMonitor().subTask(tr("calculating Strong Connectedness"));
            getProgressMonitor().setTicks(tickscounter++);
            scc.calculateSCC();
        }
        getProgressMonitor().subTask(tr("creating DigraphLayer"));
        getProgressMonitor().setTicks(tickscounter++);
        plugin.setChannelDigraph(cdgb.getDigraph());
        plugin.getOsmlayer().setBackgroundLayer(true);
        plugin.getChannelDigraphLayer().setDigraph(cdgb.getDigraph());
        plugin.setChannelDigraph(cdgb.getDigraph());
        plugin.getJcMapMode().setDigraph(cdgb.getDigraph());
        plugin.setNormalMapMode(MainApplication.getMap().mapMode);
        MainApplication.getMap().selectMapMode(plugin.getJcMapMode());
        MainApplication.getLayerManager().addLayer(plugin.getChannelDigraphLayer());
        MainApplication.getLayerManager().setActiveLayer(plugin.getChannelDigraphLayer());
    }
}
