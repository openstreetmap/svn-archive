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

import java.util.Arrays;

import org.jdom.Element;
import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.StyleDesc;
import org.jopendocument.dom.StyleStyle;
import org.jopendocument.dom.XMLVersion;

public class CellStyle extends StyleStyle {

    /*static public enum Side {
        TOP, BOTTOM, LEFT, RIGHT
    }*/

    static public final String STYLE_FAMILY = "table-cell";

    // from section 18.728 in v1.2-part1
    public static final StyleDesc<CellStyle> DESC = new StyleDesc<CellStyle>(CellStyle.class, XMLVersion.OD, STYLE_FAMILY, "ce", "table", Arrays.asList("table:body", "table:covered-table-cell",
            "table:even-rows", "table:first-column", "table:first-row", "table:last-column", "table:last-row", "table:odd-columns", "table:odd-rows", "table:table-cell")) {

        {
            this.getMultiRefElementsMap().putAll("table:default-cell-style-name", "table:table-column", "table:table-row");
        }

        @Override
        public CellStyle create(ODPackage pkg, Element e) {
            return new CellStyle(pkg, e);
        }
    };

    public CellStyle(final ODPackage pkg, Element tableColElem) {
        super(pkg, tableColElem);
    }
}
