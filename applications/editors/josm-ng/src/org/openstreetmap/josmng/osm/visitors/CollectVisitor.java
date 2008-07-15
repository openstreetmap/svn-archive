/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.osm.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.Relation;
import org.openstreetmap.josmng.osm.Visitor;
import org.openstreetmap.josmng.osm.Way;

/**
 * A visitor that collects all the visited primitives and sorts them
 * according to their type into separate collections.
 * 
 * @author nenik
 */
public final class CollectVisitor extends Visitor {
    private Collection<Node> nodes = new ArrayList<Node>();
    private Collection<Way> ways = new ArrayList<Way>();
    private Collection<Relation> relations = new ArrayList<Relation>();

    protected @Override void visit(Node n) {
        nodes.add(n);
    }

    protected @Override void visit(Way w) {
        ways.add(w);
    }

    protected @Override void visit(Relation r) {
        relations.add(r);
    }
    
    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes);
    }
    
    public Collection<Way> getWays() {
        return Collections.unmodifiableCollection(ways);
    }
        
    public Collection<Relation> getRelations() {
        return Collections.unmodifiableCollection(relations);
    }
}
