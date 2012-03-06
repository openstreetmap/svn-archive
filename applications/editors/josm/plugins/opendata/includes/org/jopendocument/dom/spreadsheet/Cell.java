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
 * Cell created on 10 décembre 2005
 */
package org.jopendocument.dom.spreadsheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jopendocument.dom.ODDocument;
import org.jopendocument.dom.ODValueType;
import org.jopendocument.dom.XMLVersion;
import org.jopendocument.util.CollectionUtils;

/**
 * A cell in a calc document. If you want to change a cell value you must obtain a MutableCell.
 * 
 * @author Sylvain
 * @param <D> type of document
 */
public class Cell<D extends ODDocument> extends TableCalcNode<CellStyle, D> {

    // see 5.1.1
    private static final Pattern multiSpacePattern = Pattern.compile("[\t\r\n ]+");
    private static boolean OO_MODE = true;

    /**
     * Set whether {@link #getTextValue()} parses strings using the standard way or using the
     * OpenOffice.org way.
     * 
     * @param ooMode <code>true</code> if strings should be parsed the OO way.
     * @see #getTextValue(boolean)
     */
    /*public static void setTextValueMode(boolean ooMode) {
        OO_MODE = ooMode;
    }*/

    public static boolean getTextValueMode() {
        return OO_MODE;
    }

    private final Row<D> row;

    Cell(Row<D> parent, Element elem) {
        super(parent.getODDocument(), elem, CellStyle.class);
        this.row = parent;
    }

    protected final Row<D> getRow() { // NO_UCD
        return this.row;
    }

    protected final XMLVersion getNS() {
        return this.getODDocument().getVersion();
    }

    protected final Namespace getValueNS() {
        final XMLVersion ns = this.getNS();
        return ns == XMLVersion.OD ? ns.getOFFICE() : ns.getTABLE();
    }

    protected final String getType() {
        return this.getElement().getAttributeValue("value-type", getValueNS());
    }

    public final ODValueType getValueType() {
        final String type = this.getType();
        return type == null ? null : ODValueType.get(type);
    }

    // cannot resolve our style since a single instance of Cell is used for all
    // repeated and thus if we need to check table-column table:default-cell-style-name
    // we wouldn't know which column to check.
    @Override
    protected String getStyleName() {
        throw new UnsupportedOperationException("cannot resolve our style, use MutableCell");
    }

    private final String getValue(String attrName) {
        return this.getElement().getAttributeValue(attrName, getValueNS());
    }

    public Object getValue() { // NO_UCD
        final ODValueType vt = this.getValueType();
        if (vt == null || vt == ODValueType.STRING) {
            // ATTN oo generates string value-types w/o any @string-value
            final String attr = vt == null ? null : this.getValue(vt.getValueAttribute());
            if (attr != null)
                return attr;
            else {
                return getTextValue();
            }
        } else {
            return vt.parse(this.getValue(vt.getValueAttribute()));
        }
    }

    /**
     * Calls {@link #getTextValue(boolean)} using {@link #getTextValueMode()}.
     * 
     * @return a string for the content of this cell.
     */
    public String getTextValue() {
        return this.getTextValue(getTextValueMode());
    }

    /**
     * Return the text value of this cell. This is often the formatted string of a value, e.g.
     * "11 novembre 2009" for a date. This method doesn't just return the text content it also
     * parses XML elements (like paragraphs, tabs and line-breaks). For the differences between the
     * OO way (as of 3.1) and the OpenDocument way see section 5.1.1 White-space Characters of
     * OpenDocument-v1.0-os and OpenDocument-v1.2-part1. In essence OpenOffice never trim strings.
     * 
     * @param ooMode whether to use the OO way or the standard way.
     * @return a string for the content of this cell.
     */
    public String getTextValue(final boolean ooMode) {
        final List<String> ps = new ArrayList<String>();
        for (final Object o : this.getElement().getChildren()) {
            final Element child = (Element) o;
            if ((child.getName().equals("p") || child.getName().equals("h")) && child.getNamespacePrefix().equals("text")) {
                ps.add(getStringValue(child, ooMode));
            }
        }
        return CollectionUtils.join(ps, "\n");
    }

    private String getStringValue(final Element pElem, final boolean ooMode) {
        final StringBuilder sb = new StringBuilder();
        final Namespace textNS = pElem.getNamespace();
        // true if the string ends with a space that wasn't expanded from an XML element (e.g.
        // <tab/> or <text:s/>)
        boolean spaceSuffix = false;
        final Iterator<?> iter = pElem.getDescendants();
        while (iter.hasNext()) {
            final Object o = iter.next();
            if (o instanceof Text) {
                final String text = multiSpacePattern.matcher(((Text) o).getText()).replaceAll(" ");
                // trim leading
                if (!ooMode && text.startsWith(" ") && (spaceSuffix || sb.length() == 0))
                    sb.append(text.substring(1));
                else
                    sb.append(text);
                spaceSuffix = text.endsWith(" ");
            } else if (o instanceof Element) {
                final Element elem = (Element) o;
                if (elem.getName().equals("tab") && elem.getNamespace().equals(textNS)) {
                    sb.append("\t");
                } else if (elem.getName().equals("line-break") && elem.getNamespace().equals(textNS)) {
                    sb.append("\n");
                } else if (elem.getName().equals("s") && elem.getNamespace().equals(textNS)) {
                    final int count = Integer.valueOf(elem.getAttributeValue("c", textNS, "1"));
                    final char[] toAdd = new char[count];
                    Arrays.fill(toAdd, ' ');
                    sb.append(toAdd);
                }
            }
        }
        // trim trailing
        if (!ooMode && spaceSuffix)
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}