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

import java.util.HashSet;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;

public class ImmutableDocStyledNode<S extends StyleStyle, D extends ODDocument> extends StyledNode<S, D> {

    private static final Set<Document> getDocuments(final ODPackage pkg) {
        final Set<Document> res = new HashSet<Document>();
        for (final String entry : pkg.getEntries()) {
            final ODPackageEntry e = pkg.getEntry(entry);
            if (e.getData() instanceof ODXMLDocument)
                res.add(pkg.getDocument(entry));
        }
        return res;
    }

    private final D parent;

    /**
     * Create a new instance. We used to find the {@link StyleStyle} class with reflection but this
     * was slow.
     * 
     * @param parent the parent document.
     * @param local our XML model.
     * @param styleClass our class of style, cannot be <code>null</code>.
     */
    public ImmutableDocStyledNode(D parent, Element local, final Class<S> styleClass) {
        super(local, styleClass);
        this.parent = parent;
        assert getDocuments(this.parent.getPackage()).contains(local.getDocument()) : "Local not in parent: " + parent;
    }

    @Override
    public final D getODDocument() {
        return this.parent;
    }
}