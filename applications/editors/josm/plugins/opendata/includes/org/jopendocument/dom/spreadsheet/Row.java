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
 * Row created on 10 décembre 2005
 */
package org.jopendocument.dom.spreadsheet;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jopendocument.dom.ODDocument;

/**
 * A row in a Calc document. This class will only break "repeated" attributes on demand (eg for
 * setting a value).
 * 
 * @author Sylvain
 * @param <D> type of document
 */
public class Row<D extends ODDocument> extends TableCalcNode<RowStyle, D> {

    private final Table<D> parent;
    private final int index;
    // the same immutable cell instance is repeated, but each MutableCell is only once
    // ATTN MutableCell have their index as attribute
    private final List<Cell<D>> cells;

    Row(Table<D> parent, Element tableRowElem, int index) {
        super(parent.getODDocument(), tableRowElem, RowStyle.class);
        this.parent = parent;
        this.index = index;
        this.cells = new ArrayList<Cell<D>>();
        for (final Element cellElem : this.getCellElements()) {
            addCellElem(cellElem);
        }
    }

    protected final Table<D> getSheet() {
        return this.parent;
    }

    final int getY() { // NO_UCD
        return this.index;
    }

    private void addCellElem(final Element cellElem) {
        final Cell<D> cell = new Cell<D>(this, cellElem);
        this.cells.add(cell);

        final String repeatedS = cellElem.getAttributeValue("number-columns-repeated", this.getSheet().getTABLE());
        if (repeatedS != null) {
            final int toRepeat = Integer.parseInt(repeatedS) - 1;
            for (int i = 0; i < toRepeat; i++) {
                this.cells.add(cell);
            }
        }
    }

    /**
     * All cells of this row.
     * 
     * @return cells of this row, only "table-cell" and "covered-table-cell".
     */
    @SuppressWarnings("unchecked")
    private List<Element> getCellElements() {
        // seuls table-cell et covered-table-cell sont légaux
        return this.getElement().getChildren();
    }

    protected final Cell<D> getCellAt(int col) { // NO_UCD
        return this.cells.get(col);
    }
}
