// License: GPL. For details, see LICENSE file.
package CreateGridOfWaysPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Crea una grilla de vías usando como base las dos seleccionadas que tengan un nodo en común
 * y probablemente sean perpendiculares entre si , deben tener ambas vías un nodo en cada esquina
 * (Please if you understand this translate to English)
 *
 * @author Jorge Luis Chamorro
 */
@SuppressWarnings("serial")
public final class CreateGridOfWaysAction extends JosmAction {

    public CreateGridOfWaysAction() {
        super(tr("Create grid of ways"), "creategridofways", tr("Forms a grid of ways in base to two existing that have various nodes and one in common"), Shortcut.registerShortcut("tools:CreateGridOfWays", tr("Tool: {0}", tr("Create grid of ways")),
        KeyEvent.VK_G, Shortcut.SHIFT), true);
    }

    /**
     * Dadas 2 vias seleccionadas buscamos el punto en comun entre ambas y luego las recorremos para crear una grilla
     * de vias paralelas a esas dos creando los nodos que son sus puntos de union y reusando los existentes
     * (Please if you understand this translate to English)
     * They given 2 ways selected we seek the point in common between both and then we travel through them to
     * create a grid of ways parallel to those creating the nodes that are its points of union and reusing the
     * existing ones (--this is from a translation machine--)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        DataSet ds = getLayerManager().getEditDataSet();
        Collection<OsmPrimitive> sel = ds.getSelected();
        Collection<Node> nodesWay1 = new LinkedList<>();
        Collection<Node> nodesWay2 = new LinkedList<>();
        if ((sel.size() != 2) || !(sel.toArray()[0] instanceof Way) || !(sel.toArray()[1] instanceof Way)) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Select two ways with a node in common"));
            return;
        }
        nodesWay1.addAll(((Way)sel.toArray()[0]).getNodes());
        nodesWay2.addAll(((Way)sel.toArray()[1]).getNodes());
        Node nodeCommon = null;
        for (Node n : nodesWay1)
            for (Node m : nodesWay2)
                if (n.equals(m)) {
                    if ( nodeCommon != null ) {
                        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Select two ways with alone a node in common"));
                        return;
                    }
                    nodeCommon = n;
                }
        if (nodeCommon == null) {
            Logging.error("Cannot find common node");
            return;
        }
        Way w2[] = new Way[nodesWay2.size()-1];
        for (int c=0;c<w2.length;c++)
            w2[c]=new Way();
        Way w1[] = new Way[nodesWay1.size()-1];
        for (int c=0;c<w1.length;c++)
            w1[c]=new Way();
        Collection<Command> cmds = new LinkedList<>();
        int c1=0,c2;
        double latDif,lonDif;
        LatLon llc = nodeCommon.getCoor();
        for (Node n1 : nodesWay1) {
            LatLon ll1 = n1.getCoor();
            if (ll1 == null || llc == null) {
                Logging.warn("Null coordinates: {0} / {1}", n1, nodeCommon);
                continue;
            }
            latDif = ll1.lat()-llc.lat();
            lonDif = ll1.lon()-llc.lon();
            c2=0;
            for (Node n2 : nodesWay2) {
                if (n1.equals(nodeCommon) && n2.equals(nodeCommon))
                    continue;
                if (n2.equals(nodeCommon)) {
                    w1[c1].addNode(n1);
                    continue;
                }
                if (n1.equals(nodeCommon)) {
                    w2[c2++].addNode(n2);
                    continue;
                }
                LatLon ll2 = n2.getCoor();
                Node nodeOfGrid = new Node(new LatLon(ll2.lat()+latDif, ll2.lon()+lonDif));
                cmds.add(new AddCommand(ds, nodeOfGrid));
                w1[c1].addNode(nodeOfGrid);
                w2[c2++].addNode(nodeOfGrid);
            }
            if (!n1.equals(nodeCommon))
               c1++;
        }
        for (int c=0;c<w1.length;c++)
            cmds.add(new AddCommand(ds, w1[c]));
        for (int c=0;c<w2.length;c++)
            cmds.add(new AddCommand(ds, w2[c]));
        UndoRedoHandler.getInstance().add(new SequenceCommand(tr("Create a grid of ways"), cmds));
        MainApplication.getMap().repaint();
    }
}
