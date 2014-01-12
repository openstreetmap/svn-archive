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

import java.util.HashMap;
import java.util.Map;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Encapsulate all namespaces for a particular version of xml.
 * 
 * @author ILM Informatique 26 juil. 2004
 */
public enum XMLVersion {

    // OpenOffice.org 1.x.
    OOo("OpenOffice.org", Namespace.getNamespace("manifest", "http://openoffice.org/2001/manifest")) {
        {
            this.putMandatory(OFFICE_1, STYLE_1, TEXT_1, TABLE_1);
            this.put("number", NUMBER_1);
            this.put("draw", DRAW_1);
            this.put("number", NUMBER_1);
            this.put("fo", FO_1);
            this.put("form", "http://openoffice.org/2000/form");
            this.put("xlink", "http://www.w3.org/1999/xlink");
            this.put("script", "http://openoffice.org/2000/script");
            this.put("svg", "http://www.w3.org/2000/svg");
            this.put("meta", "http://openoffice.org/2000/meta");
            this.put("dc", "http://purl.org/dc/elements/1.1/");
        }
    },
    // OpenDocument 1.x/OpenOffice.org 2.x.
    OD("OpenDocument", Namespace.getNamespace("manifest", "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0")) {
        {
            this.putMandatory(OFFICE_2, STYLE_2, TEXT_2, TABLE_2);
            this.put("number", NUMBER_2);
            this.put("draw", DRAW_2);
            this.put("number", NUMBER_2);
            this.put("fo", FO_2);
            this.put("form", "urn:oasis:names:tc:opendocument:xmlns:form:1.0");
            this.put("xlink", "http://www.w3.org/1999/xlink");
            this.put("script", "urn:oasis:names:tc:opendocument:xmlns:script:1.0");
            this.put("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0");
            this.put("meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
            this.put("dc", "http://purl.org/dc/elements/1.1/");
        }
    };

    private static final String OFFICE_1 = "http://openoffice.org/2000/office";
    private static final String STYLE_1 = "http://openoffice.org/2000/style";
    private static final String TEXT_1 = "http://openoffice.org/2000/text";
    private static final String NUMBER_1 = "http://openoffice.org/2000/datastyle";
    private static final String TABLE_1 = "http://openoffice.org/2000/table";
    private static final String DRAW_1 = "http://openoffice.org/2000/drawing";
    private static final String FO_1 = "http://www.w3.org/1999/XSL/Format";

    private static final String OFFICE_2 = "urn:oasis:names:tc:opendocument:xmlns:office:1.0";
    private static final String STYLE_2 = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
    private static final String TEXT_2 = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
    private static final String NUMBER_2 = "urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0";
    private static final String TABLE_2 = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";
    private static final String DRAW_2 = "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0";
    private static final String FO_2 = "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0";

    private final Map<String, Namespace> nss;

    private XMLVersion(String name, Namespace manifest) {
        this.nss = new HashMap<String, Namespace>(16);
    }

    protected final void putMandatory(String office, String style, String text, String table) {
        this.put("office", office);
        this.put("style", style);
        this.put("text", text);
        this.put("table", table);
    }

    protected final void put(String prefix, String uri) {
        this.nss.put(prefix, Namespace.getNamespace(prefix, uri));
    }

    public final Namespace getNS(String prefix) {
        if (!this.nss.containsKey(prefix))
            throw new IllegalStateException("unknown " + prefix + " : " + this.nss.keySet());
        return this.nss.get(prefix);
    }

    public Namespace getOFFICE() {
        return this.getNS("office");
    }

    public Namespace getSTYLE() { // NO_UCD
        return this.getNS("style");
    }

    public Namespace getTEXT() { // NO_UCD
        return this.getNS("text");
    }

    public Namespace getTABLE() {
        return this.getNS("table");
    }

    public Namespace getMETA() {
        return this.getNS("meta");
    }

    // *** static public

    /**
     * Namespaces for OpenOffice.org 1.x.
     * 
     * @return namespaces for OO.o 1.
     */
    public static final XMLVersion getOOo() {
        return OOo;
    }

    /**
     * Namespaces for OpenDocument/OpenOffice.org 2.x.
     * 
     * @return namespaces for OpenDocument.
     */
    public static final XMLVersion getOD() {
        return OD;
    }

    /**
     * Find the NS to which belongs the passed namespace.
     * 
     * @param ns the namespace, eg office=http://openoffice.org/2000/office.
     * @return the matching NS, eg NS.getOOo(), or <code>null</code> if none is found.
     */
    public static final XMLVersion getParent(Namespace ns) {
        for (XMLVersion v : values()) {
            if (v.getNS(ns.getPrefix()).equals(ns))
                return v;
        }
        return null;
    }

    /**
     * Infer the version of an XML element from its namespace.
     * 
     * @param elem the element to be tested, eg &lt;text:line-break/&gt;.
     * @return the version.
     * @throws IllegalArgumentException if the namespace is unknown.
     */
    public static final XMLVersion getVersion(Element elem) {
        final XMLVersion parent = getParent(elem.getNamespace());
        if (parent == null)
            throw new IllegalArgumentException(elem + " is not an OpenOffice element.");
        return parent;
    }
}
