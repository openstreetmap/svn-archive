// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.openstreetmap.josm.plugins.fixAddresses.IOSMEntity;
import org.openstreetmap.josm.plugins.fixAddresses.OSMAddress;
import org.openstreetmap.josm.plugins.fixAddresses.OSMStreet;

/**
 * Address Edit model.
 */
public class AddressEditModel {
    private List<OSMStreet> streets;
    private List<OSMAddress> unresolvedAddresses;
    private List<OSMAddress> incompleteAddresses = new ArrayList<>();
    private DefaultMutableTreeNode streetRoot;
    private DefaultMutableTreeNode unresolvedRoot;
    private DefaultMutableTreeNode incompleteRoot;

    public AddressEditModel(List<OSMStreet> streets,
            List<OSMAddress> unresolvedAddresses) {
        super();
        this.streets = streets;
        this.unresolvedAddresses = unresolvedAddresses;
    }

    public TreeNode getStreetsTree() {
        if (streets == null) return new DefaultMutableTreeNode(tr("(No data)"));

        if (streetRoot == null) {
            streetRoot = new DefaultMutableTreeNode();
            for (OSMStreet sNode : streets) {
                DefaultMutableTreeNode treeStreetNode = new DefaultMutableTreeNode(sNode);

                DefaultMutableTreeNode segmentsNode = new DefaultMutableTreeNode(tr("Segments"));
                treeStreetNode.add(segmentsNode);

                // Add street segment(s)
                for (IOSMEntity child : sNode.getChildren()) {
                    segmentsNode.add(new DefaultMutableTreeNode(child));
                }

                if (sNode.hasAddresses()) {
                    // Add address nodes
                    DefaultMutableTreeNode addressNode = new DefaultMutableTreeNode(tr("Addresses"));
                    treeStreetNode.add(addressNode);

                    for (OSMAddress addr : sNode.getAddresses()) {
                        addressNode.add(new DefaultMutableTreeNode(addr));
                        if (!addr.isComplete()) {
                            incompleteAddresses.add(addr);
                        }
                    }
                }
                streetRoot.add(treeStreetNode);
            }
        }

        return streetRoot;
    }

    /**
     * Gets the tree node containing all unresolved addresses.
     * @return tree node containing all unresolved addresses
     */
    public TreeNode getUnresolvedAddressesTree() {
        if (unresolvedAddresses == null) return new DefaultMutableTreeNode(tr("(No data)"));

        if (unresolvedRoot == null) {
            unresolvedRoot = new DefaultMutableTreeNode();

            for (OSMAddress addr : unresolvedAddresses) {
                // Add address nodes
                unresolvedRoot.add(new DefaultMutableTreeNode(addr));
            }
        }

        return unresolvedRoot;
    }

    /**
     * Gets the tree node containing all incomplete addresses.
     * @return tree node containing all incomplete addresses
     */
    public TreeNode getIncompleteAddressesTree() {
        if (incompleteAddresses == null) return new DefaultMutableTreeNode(tr("(No data)"));

        if (incompleteRoot == null) {
            incompleteRoot = new DefaultMutableTreeNode();

            for (OSMAddress addr : incompleteAddresses) {
                // Add address nodes
                incompleteRoot.add(new DefaultMutableTreeNode(addr));
            }
        }

        return incompleteRoot;
    }
}
