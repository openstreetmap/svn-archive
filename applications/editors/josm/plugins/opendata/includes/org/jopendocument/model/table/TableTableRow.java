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

package org.jopendocument.model.table;

import java.util.Vector;

/**
 * 
 */
public class TableTableRow {
    static int count = 0;


    Vector<TableTableCell> cells = new Vector<TableTableCell>();

    int id = 0;

    protected int tableNumberRowsRepeated = 1;

    public TableTableRow() {
        this.id = count;
        count++;
    }

    public void addCell(final TableTableCell c) {
        this.cells.add(c);

    }

    /**
     * Gets the value of the tableNumberRowsRepeated property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public int getTableNumberRowsRepeated() {

        return this.tableNumberRowsRepeated;

    }

    // public List<TableTableCell> getCells() {
    // return cells;
    // }


    public String getText() {
        String t = "";
        for (int index = 0; index < this.cells.size(); index++) {
            final TableTableCell c = this.cells.get(index);
            t += c.getTextP();
        }
        return t;
    }

    /**
     * Sets the value of the tableNumberRowsRepeated property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTableNumberRowsRepeated(final String value) {
        if (value != null) {
            this.tableNumberRowsRepeated = Integer.valueOf(value).intValue();

        }
    }

    @Override
    public String toString() {
        return "TableRow" + this.id;
    }
}
