/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * A helper to create children in the schema order.
 * 
 * @author Sylvain CUAZ
 */
public class ChildCreator {

    private final Element content;
    private final List<Element> elemsOrder;

    protected ChildCreator(final Element content, final List<Element> children) {
        if (content == null)
            throw new NullPointerException("null content");
        this.content = content;
        this.elemsOrder = new ArrayList<Element>(children);
    }

    public final Element getElement() {
        return this.content;
    }

    // *** children

    public final Element getChild(Namespace childNS, String childName) {
        return this.getChild(childNS, childName, false);
    }

    private final int indexOf(Namespace childNS, String childName) {
        for (int i = 0; i < this.elemsOrder.size(); i++) {
            final Element elem = this.elemsOrder.get(i);
            if (elem.getNamespace().equals(childNS) && elem.getName().equals(childName))
                return i;
        }
        return -1;
    }

    private final int indexOf(final Element elem) {
        return this.indexOf(elem.getNamespace(), elem.getName());
    }

    /**
     * Trouve l'index ou il faut insérer le fils dans ce document.
     * 
     * @param childName le nom du fils que l'on veut insérer.
     * @return l'index ou il faut l'insérer (s'il est déjà présent son index actuel +1).
     * @throws IllegalArgumentException if childName is not in {@link #getChildren()}.
     */
    @SuppressWarnings("unchecked")
    private final int findInsertIndex(Namespace childNS, String childName) {
        // eg 6, for "master-styles"
        final int idealIndex = indexOf(childNS, childName);
        if (idealIndex == -1)
            throw new IllegalArgumentException(childName + " is unknown.");
        final Element existingChild = this.getChild(childNS, childName);
        if (existingChild != null)
            return this.getElement().getChildren().indexOf(existingChild) + 1;
        // eg [scripts, font-decls, styles, font-face-decls, automatic-styles, body]
        final List<Element> children = this.getElement().getChildren();
        final ListIterator<Element> iter = children.listIterator();
        while (iter.hasNext()) {
            final Element elem = iter.next();
            if (indexOf(elem) > idealIndex)
                // eg indexOf("body") == 7 > 6
                // eg return 5
                return iter.previousIndex();
        }
        return children.size();
    }

    /**
     * Insère cet élément à la bonne place. The child should not be already present.
     * 
     * @param child l'élément à insérer, doit être dans TOP_ELEMENTS.
     */
    @SuppressWarnings("unchecked")
    private final void insertChild(Element child) {
        // on ajoute au bon endroit
        this.getElement().getChildren().add(this.findInsertIndex(child.getNamespace(), child.getName()), child);
    }

    /**
     * Return the asked child, optionally creating it.
     * 
     * @param childNS the namespace of the child.
     * @param childName the name of the child.
     * @param create whether it should be created in case it doesn't exist.
     * @return the asked child or <code>null</code> if it doesn't exist and create is
     *         <code>false</code>
     */
    public final Element getChild(Namespace childNS, String childName, boolean create) {
        Element child = this.getElement().getChild(childName, childNS);
        if (create && child == null) {
            child = new Element(childName, childNS);
            this.insertChild(child);
        }
        return child;
    }
}