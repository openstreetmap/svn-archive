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

/**
 * An OpenDocument package entry, ie a file or folder inside a zip.
 */
public class ODPackageEntry {

    private final String name;
    private String type;
    // either byte[] or OOXMLDocument
    private final Object data;
    private boolean compressed;

    public ODPackageEntry(String name, String type, Object data, final boolean compressed) {
        super();
        this.name = name;
        this.type = type;
        this.data = data;
        this.compressed = compressed;
    }

    public final String getName() {
        return this.name;
    }

    public final String getType() {
        return this.type;
    }

    public final Object getData() {
        return this.data;
    }

    public final boolean isCompressed() {
        return this.compressed;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + getName() + "[" + this.getType() + "]" + getData();
    }

}
