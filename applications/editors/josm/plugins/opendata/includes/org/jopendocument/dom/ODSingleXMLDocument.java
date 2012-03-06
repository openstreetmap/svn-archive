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

/**
 * An XML document containing all of an office document, see section 2.1 of OpenDocument 1.1.
 * 
 * @author Sylvain CUAZ 24 nov. 2004
 */
public class ODSingleXMLDocument extends ODXMLDocument implements Cloneable, ODDocument {

    final static Set<String> DONT_PREFIX;
    static {
        DONT_PREFIX = new HashSet<String>();
        // don't touch to user fields and variables
        // we want them to be the same across the document
        DONT_PREFIX.add("user-field-decl");
        DONT_PREFIX.add("user-field-get");
        DONT_PREFIX.add("variable-get");
        DONT_PREFIX.add("variable-decl");
        DONT_PREFIX.add("variable-set");
    }

    /**
     * fix bug when a SingleXMLDoc is used to create a document (for example with P2 and 1_P2), and
     * then create another instance s2 with the previous document and add a second file (also with
     * P2 and 1_P2) => s2 will contain P2, 1_P2, 1_P2, 1_1_P2.
     */
    private static final String COUNT = "SingleXMLDocument_count";

    /** Le nombre de fichiers concat */
    private int numero;
    /** Les styles présent dans ce document */
    private final Set<String> stylesNames;
    /** Les styles de liste présent dans ce document */
    private final Set<String> listStylesNames;
    /** Les fichiers référencés par ce document */
    private final ODPackage pkg;
    private final ODMeta meta;

    ODSingleXMLDocument(ODSingleXMLDocument doc, ODPackage p) {
        super(doc);
        this.stylesNames = new HashSet<String>(doc.stylesNames);
        this.listStylesNames = new HashSet<String>(doc.listStylesNames);
        this.pkg = p;
        this.meta = ODMeta.create(this);
        this.setNumero(doc.numero);
    }

    @Override
    public ODSingleXMLDocument clone() {
        final ODPackage copy = new ODPackage(this.pkg);
        return (ODSingleXMLDocument) copy.getContent();
    }

    private void setNumero(int numero) {
        this.numero = numero;
        this.meta.getUserMeta(COUNT, true).setValue(this.numero);
    }

    @Override
    public ODPackage getPackage() {
        return this.pkg;
    }
}