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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jopendocument.dom.ODDocument;
import org.jopendocument.util.Tuple2;

/**
 * A single sheet in a spreadsheet.
 * 
 * @author Sylvain
 * @param <D> type of table parent
 */
public class Table<D extends ODDocument> extends TableCalcNode<TableStyle, D> {

    static final String getName(final Element elem) {
        return elem.getAttributeValue("name", elem.getNamespace("table"));
    }

    // ATTN Row have their index as attribute
    private final List<Row<D>> rows;
    private final List<Column<D>> cols;

    public Table(D parent, Element local) {
        super(parent, local, TableStyle.class);

        this.rows = new ArrayList<Row<D>>();
        this.cols = new ArrayList<Column<D>>();

        this.readColumns();
        this.readRows();
    }

    private void readColumns() {
        this.read(true);
    }

    private final void readRows() {
        this.read(false);
    }

    private final void read(final boolean col) {
        final Tuple2<List<Element>, Integer> r = flatten(col);
        (col ? this.cols : this.rows).clear();
        for (final Element clone : r.get0()) {
            if (col)
                this.addCol(clone);
            else
                this.addRow(clone);
        }
    }

    private final void addCol(Element clone) {
        this.cols.add(new Column<D>(this, clone));
    }

    private Tuple2<List<Element>, Integer> flatten(boolean col) {
        final List<Element> res = new ArrayList<Element>();
        final Element header = this.getElement().getChild("table-header-" + getName(col) + "s", getTABLE());
        if (header != null)
            res.addAll(flatten(header, col));
        final int headerCount = res.size();

        res.addAll(flatten(getElement(), col));

        return Tuple2.create(res, headerCount);
    }

    @SuppressWarnings("unchecked")
    private List<Element> flatten(final Element elem, boolean col) {
        final String childName = getName(col);
        final List<Element> children = elem.getChildren("table-" + childName, getTABLE());
        // not final, since iter.add() does not work consistently, and
        // thus we must recreate an iterator each time
        ListIterator<Element> iter = children.listIterator();
        while (iter.hasNext()) {
            final Element row = iter.next();
            final Attribute repeatedAttr = row.getAttribute("number-" + childName + "s-repeated", getTABLE());
            if (repeatedAttr != null) {
                row.removeAttribute(repeatedAttr);
                final int index = iter.previousIndex();
                int repeated = Integer.parseInt(repeatedAttr.getValue());
                if (repeated > 60000) {
                    repeated = 10;
                }
                // -1 : we keep the original row
                for (int i = 0; i < repeated - 1; i++) {
                    final Element clone = (Element) row.clone();
                    // cannot use iter.add() since on JDOM 1.1 if row is the last table-column
                    // before table-row the clone is added at the very end
                    children.add(index, clone);
                }
                // restart after the added rows
                iter = children.listIterator(index + repeated);
            }
        }

        return children;
    }

    public void detach() {
        this.getElement().detach();
    }

    private final String getName(boolean col) {
        return col ? "column" : "row";
    }


    private synchronized void addRow(Element child) {
        this.rows.add(new Row<D>(this, child, this.rows.size()));
    }


    // *** get count

    public final Column<D> getColumn(int i) { // NO_UCD
        return this.cols.get(i);
    }

    public final int getRowCount() { // NO_UCD
        return this.rows.size();
    }

    public final int getColumnCount() { // NO_UCD
        return this.cols.size();
    }
}