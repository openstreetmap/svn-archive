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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jopendocument.util.CopyUtils;
import org.jopendocument.util.FileUtils;

/**
 * An OpenDocument package, ie a zip containing XML documents and their associated files.
 * 
 * @author ILM Informatique 2 août 2004
 */
public class ODPackage {

    private static final Set<String> subdocNames;
    static {
        subdocNames = new HashSet<String>();
        // section 2.1 of OpenDocument-v1.1-os.odt
        subdocNames.add("content.xml");
        subdocNames.add("styles.xml");
        subdocNames.add("meta.xml");
        subdocNames.add("settings.xml");
    }

    private final Map<String, ODPackageEntry> files;
    private File file;

    public ODPackage() {
        this.files = new HashMap<String, ODPackageEntry>();
        this.file = null;
    }

    public ODPackage(ODPackage o) {
        this();
        // ATTN this works because, all files are read upfront
        for (final String name : o.getEntries()) {
            final ODPackageEntry entry = o.getEntry(name);
            final Object data = entry.getData();
            final Object myData;
            if (data instanceof byte[])
                // assume byte[] are immutable
                myData = data;
            else if (data instanceof ODSingleXMLDocument) {
                myData = new ODSingleXMLDocument((ODSingleXMLDocument) data, this);
            } else {
                myData = CopyUtils.copy(data);
            }
            this.putFile(name, myData, entry.getType(), entry.isCompressed());
        }
        this.file = o.file;
    }

    /**
     * The version of this package, <code>null</code> if it cannot be found (eg this package is
     * empty, or contains no xml).
     * 
     * @return the version of this package, can be <code>null</code>.
     */
    public final XMLVersion getVersion() {
        final ODXMLDocument content = this.getContent();
        if (content == null)
            return null;
        else
            return content.getVersion();
    }


    // *** getter on files

    public final Set<String> getEntries() {
        return this.files.keySet();
    }

    public final ODPackageEntry getEntry(String entry) {
        return this.files.get(entry);
    }

    protected final Object getData(String entry) {
        final ODPackageEntry e = this.getEntry(entry);
        return e == null ? null : e.getData();
    }

    public final ODXMLDocument getXMLFile(String xmlEntry) {
        return (ODXMLDocument) this.getData(xmlEntry);
    }

    public final ODXMLDocument getXMLFile(final Document doc) {
        for (final String s : subdocNames) {
            final ODXMLDocument xmlFile = getXMLFile(s);
            if (xmlFile != null && xmlFile.getDocument() == doc) {
                return xmlFile;
            }
        }
        return null;
    }

    public final ODXMLDocument getContent() {
        return this.getXMLFile("content.xml");
    }

    public final ODMeta getMeta() { // NO_UCD
        final ODMeta meta;
        if (this.getEntries().contains("meta.xml"))
            meta = ODMeta.create(this.getXMLFile("meta.xml"));
        else
            meta = ODMeta.create(this.getContent());
        return meta;
    }

    /** 
     * Return an XML document.
     * 
     * @param xmlEntry the filename, eg "styles.xml".
     * @return the matching document, or <code>null</code> if there's none.
     * @throws JDOMException if error about the XML.
     * @throws IOException if an error occurs while reading the file.
     */
    public Document getDocument(String xmlEntry) {
        final ODXMLDocument xml = this.getXMLFile(xmlEntry);
        return xml == null ? null : xml.getDocument();
    }

    // *** setter

    public void putFile(String entry, Object data) {
        this.putFile(entry, data, null);
    }

    public void putFile(final String entry, final Object data, final String mediaType) {
        this.putFile(entry, data, mediaType, true);
    }

    public void putFile(final String entry, final Object data, final String mediaType, final boolean compress) {
        if (entry == null)
            throw new NullPointerException("null name");
        final Object myData;
        if (subdocNames.contains(entry)) {
            final ODXMLDocument oodoc;
            if (data instanceof Document)
                oodoc = new ODXMLDocument((Document) data);
            else
                oodoc = (ODXMLDocument) data;
            // si le package est vide n'importe quelle version convient
            if (this.getVersion() != null && !oodoc.getVersion().equals(this.getVersion()))
                throw new IllegalArgumentException("version mismatch " + this.getVersion() + " != " + oodoc);
            myData = oodoc;
        } else if (data != null && !(data instanceof byte[]))
            throw new IllegalArgumentException("should be byte[] for " + entry + ": " + data);
        else
            myData = data;
        final String inferredType = mediaType != null ? mediaType : FileUtils.findMimeType(entry);
        this.files.put(entry, new ODPackageEntry(entry, inferredType, myData, compress));
    }
}
