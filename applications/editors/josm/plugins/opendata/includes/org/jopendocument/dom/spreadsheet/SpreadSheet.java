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

package org.jopendocument.dom.spreadsheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jdom.Document;
import org.jdom.Element;
import org.jopendocument.dom.ODDocument;
import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.XMLVersion;

/**
 * A calc document.
 * 
 * @author Sylvain
 */
public class SpreadSheet implements ODDocument {

    private final ODPackage originalFile;
    private final Map<Element, Sheet> sheets;

    private SpreadSheet(final Document doc, final Document styles, final ODPackage orig) {
        if (orig != null) {
            // ATTN OK because this is our private instance (see createFromFile())
            this.originalFile = orig;
        } else {
            this.originalFile = new ODPackage();
        }
        this.originalFile.putFile("content.xml", doc);
        if (styles != null)
            this.originalFile.putFile("styles.xml", styles);

        // map Sheet by XML elements so has not to depend on ordering or name
        this.sheets = new HashMap<Element, Sheet>();
    }

    final Document getContent() {
        return this.getPackage().getContent().getDocument();
    }

    @Override
    public final XMLVersion getVersion() {
        return this.getPackage().getVersion();
    }

    private Element getBody() {
        final Element body = this.getContent().getRootElement().getChild("body", this.getVersion().getOFFICE());
        if (this.getVersion().equals(XMLVersion.OOo))
            return body;
        else
            return body.getChild("spreadsheet", this.getVersion().getOFFICE());
    }


    // query directly the DOM, that way don't need to listen to it (eg for name, size or order
    // change)
    @SuppressWarnings("unchecked")
    private final List<Element> getTables() {
        return this.getBody().getChildren("table", this.getVersion().getTABLE());
    }

    public int getSheetCount() { // NO_UCD
        return this.getTables().size();
    }

    public Sheet getSheet(int i) { // NO_UCD
        return this.getSheet(getTables().get(i));
    }

    public Sheet getSheet(String name) { // NO_UCD
        return this.getSheet(name, false);
    }

    /**
     * Return the first sheet with the passed name.
     * 
     * @param name the name of a sheet.
     * @param mustExist what to do when no match is found : <code>true</code> to throw an exception,
     *        <code>false</code> to return null.
     * @return the first matching sheet, <code>null</code> if <code>mustExist</code> is
     *         <code>false</code> and no match is found.
     * @throws NoSuchElementException if <code>mustExist</code> is <code>true</code> and no match is
     *         found.
     */
    public Sheet getSheet(String name, final boolean mustExist) throws NoSuchElementException {
        for (final Element table : getTables()) {
            if (name.equals(Table.getName(table)))
                return getSheet(table);
        }
        if (mustExist)
            throw new NoSuchElementException("no such sheet: " + name);
        else
            return null;
    }

    private final Sheet getSheet(Element table) {
        Sheet res = this.sheets.get(table);
        if (res == null) {
            res = new Sheet(this, table);
            this.sheets.put(table, res);
        }
        return res;
    }

    void invalidate(Element element) {
        this.sheets.remove(element);
    }

    // *** Files

    @Override
    public final ODPackage getPackage() {
        return this.originalFile;
    }
}