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

/*
 * Créé le 28 oct. 2004
 */
package org.jopendocument.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * An OpenDocument XML document, like content.xml ou styles.xml.
 * 
 * @author Sylvain CUAZ
 */
public class ODXMLDocument {

    /**
     * All top-level elements that an office document may contain. Note that only the single xml
     * representation (office:document) contains all of them.
     */
    private static final Map<XMLVersion, List<Element>> ELEMS_ORDER;
    static {
        ELEMS_ORDER = new HashMap<XMLVersion, List<Element>>(2);
        ELEMS_ORDER.put(XMLVersion.getOOo(), createChildren(XMLVersion.getOOo()));
        ELEMS_ORDER.put(XMLVersion.getOD(), createChildren(XMLVersion.getOD()));
    }

    private static final List<Element> createChildren(XMLVersion ins) {
        final Namespace ns = ins.getOFFICE();
        final List<Element> res = new ArrayList<Element>(8);
        res.add(new Element("meta", ns));
        res.add(new Element("settings", ns));
        res.add(new Element("script", ns));
        res.add(new Element("font-decls", ns));
        res.add(new Element("styles", ns));
        res.add(new Element("automatic-styles", ns));
        res.add(new Element("master-styles", ns));
        res.add(new Element("body", ns));
        return res;
    }

    // namespaces for the name attributes
    static private final Map<String, String> namePrefixes;
    static {
        namePrefixes = new HashMap<String, String>();
        namePrefixes.put("table:table", "table");
        namePrefixes.put("text:a", "office");
        namePrefixes.put("draw:text-box", "draw");
        namePrefixes.put("draw:image", "draw");
        namePrefixes.put("draw:frame", "draw");
    }

    private final Document content;
    private final XMLVersion version;
    private final ChildCreator childCreator;

    // before making it public, assure that content is really of version "version"
    // eg by checking some namespace
    protected ODXMLDocument(final Document content, final XMLVersion version) {
        if (content == null)
            throw new NullPointerException("null document");
        this.content = content;
        this.version = version;
        this.childCreator = new ChildCreator(this.content.getRootElement(), ELEMS_ORDER.get(this.getVersion()));
    }

    public ODXMLDocument(Document content) {
        this(content, XMLVersion.getVersion(content.getRootElement()));
    }

    public ODXMLDocument(ODXMLDocument doc) {
        this((Document) doc.content.clone(), doc.version);
    }

    public Document getDocument() {
        return this.content;
    }

    public final XMLVersion getVersion() {
        return this.version;
    }

    // *** children
    
    public final Element getChild(String childName) {
        return this.getChild(childName, false);
    }

    /**
     * Return the asked child, optionally creating it.
     * 
     * @param childName the name of the child.
     * @param create whether it should be created in case it doesn't exist.
     * @return the asked child or <code>null</code> if it doesn't exist and create is
     *         <code>false</code>
     */
    public Element getChild(String childName, boolean create) {
        return this.childCreator.getChild(this.getVersion().getOFFICE(), childName, create);
    }
}