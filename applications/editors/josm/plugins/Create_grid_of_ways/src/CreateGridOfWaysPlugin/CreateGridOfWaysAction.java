package CreateGridOfWaysPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
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
        KeyEvent.VK_G, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
/* en mis otras pruebas esto terminaba : Shortcut.GROUP_EDIT), true);
/* le puse tecla G de shortcut pero parece que esta usado por tools:unglue , buscar otro */
    }

    /**
     * Dadas 2 vias seleccionadas buscamos el punto en comun entre ambas y luego las recorremos para crear una grilla
     * de vias paralelas a esas dos creando los nodos que son sus puntos de union y reusando los existentes
     * (Please if you understand this translate to English)
     * They given 2 ways selected we seek the point in common between both and then we travel through them to
     * create a grid of ways parallel to those creating the nodes that are its points of union and reusing the
     * existing ones (--this is from a translation machine--)
     */
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> sel = Main.main.getCurrentDataSet().getSelected();
        Collection<Node> nodesWay1 = new LinkedList<Node>();
        Collection<Node> nodesWay2 = new LinkedList<Node>();
        if ((sel.size() != 2) || !(sel.toArray()[0] instanceof Way) || !(sel.toArray()[1] instanceof Way)) {
            JOptionPane.showMessageDialog(Main.parent, tr("Select two ways with a node in common"));
            return;
        }
        nodesWay1.addAll(((Way)sel.toArray()[0]).getNodes());
        nodesWay2.addAll(((Way)sel.toArray()[1]).getNodes());
        Node nodeCommon = null;
        for (Node n : nodesWay1)
            for (Node m : nodesWay2)
                if (n.equals(m)) {
                    if ( nodeCommon != null ) {
                        JOptionPane.showMessageDialog(Main.parent, tr("Select two ways with alone a node in common"));
                        return;
                    }
                    nodeCommon = n;
                }
        Way w2[] = new Way[nodesWay2.size()-1];
        for (int c=0;c<w2.length;c++)
            w2[c]=new Way();
        Way w1[] = new Way[nodesWay1.size()-1];
        for (int c=0;c<w1.length;c++)
            w1[c]=new Way();
        Collection<Command> cmds = new LinkedList<Command>();
        int c1=0,c2;
        double latDif,lonDif;
        for (Node n1 : nodesWay1) {
            latDif = n1.getCoor().lat()-nodeCommon.getCoor().lat();
            lonDif = n1.getCoor().lon()-nodeCommon.getCoor().lon();
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
                Node nodeOfGrid = new Node(new LatLon(n2.getCoor().lat()+latDif,n2.getCoor().lon()+lonDif));
                cmds.add(new AddCommand(nodeOfGrid));
                w1[c1].addNode(nodeOfGrid);
                w2[c2++].addNode(nodeOfGrid);
            }
            if (!n1.equals(nodeCommon))
               c1++;
        }
        for (int c=0;c<w1.length;c++)
            cmds.add(new AddCommand(w1[c]));
        for (int c=0;c<w2.length;c++)
            cmds.add(new AddCommand(w2[c]));
        Main.main.undoRedo.add(new SequenceCommand(tr("Create a grid of ways"), cmds));
        Main.map.repaint();
    }
}
