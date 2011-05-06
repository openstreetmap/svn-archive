// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.openstreetmap.josm.data.validation.util.MultipleNameVisitor;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Tree renderer for displaying errors
 * @author frsantos
 */
public class ProblemTreeRenderer extends DefaultTreeCellRenderer
{

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        Object nodeInfo = node.getUserObject();

        if (nodeInfo instanceof Severity)
        {
            Severity s = (Severity)nodeInfo;
            setIcon(ImageProvider.get("data", s.getIcon()));
        }
        else if (nodeInfo instanceof LicenseProblem)
        {
            LicenseProblem error = (LicenseProblem)nodeInfo;
            MultipleNameVisitor v = new MultipleNameVisitor();
            v.visit(error.getPrimitives());
            setText(v.getText());
            setIcon(v.getIcon());
        }

        return this;
    }
}
