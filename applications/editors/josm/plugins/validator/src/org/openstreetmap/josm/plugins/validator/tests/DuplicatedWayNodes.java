package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.Collections;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

public class DuplicatedWayNodes extends Test {
    protected static int DUPLICATE_WAY_NODE = 501;

    public DuplicatedWayNodes() {
        super(tr("Duplicated way nodes."),
            tr("Checks for ways with identical consecutive nodes."));
    }

    @Override public void visit(Way w) {
        if (w.deleted || w.incomplete) return;

        Node lastN = null;
        for (Node n : w.nodes) {
            if (lastN == null) {
                lastN = n;
                continue;
            }
            if (lastN == n) {
                errors.add(new TestError(this, Severity.ERROR, tr("Duplicated way nodes"), DUPLICATE_WAY_NODE,
                    Arrays.asList(w), Arrays.asList(n)));
                break;
            }
            lastN = n;
        }
    }

    @Override public Command fixError(TestError testError) {
        Way w = (Way) testError.getPrimitives().iterator().next();
        Way wnew = new Way(w);
        wnew.nodes.clear();
        Node lastN = null;
        for (Node n : w.nodes) {
            if (lastN == null) {
                wnew.nodes.add(n);
            } else if (n == lastN) {
                // Skip this node
            } else {
                wnew.nodes.add(n);
            }
            lastN = n;
        }
        if (wnew.nodes.size() < 2) {
            // Empty way, delete
            return DeleteCommand.delete(Main.map.mapView.getEditLayer(), Collections.singleton(w));
        } else {
            return new ChangeCommand(w, wnew);
        }
    }

    @Override public boolean isFixable(TestError testError) {
        return testError.getTester() instanceof DuplicatedWayNodes;
    }
}
