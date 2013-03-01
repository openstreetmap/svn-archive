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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

/**
 * 
 */
public class TableTableRow {
    static int count = 0;
    ArrayList<TableTableCell> allCells;

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
     * Compute AllCell except the last one
     */
    void computeAllCells() {
        this.allCells = new ArrayList<TableTableCell>();
        for (int index = 0; index < this.cells.size(); index++) {
            final TableTableCell c = this.cells.get(index);
            // for (TableTableCell c : cells) {
            //final int colPosition = this.allCells.size();
            int repeated = c.getTableNumberColumnsRepeated();
            // la derniere colonne n'est repétée que dans la limite de la zone d'impression
            // sinon, on s'en coltine des milliers
            if (index == this.cells.size() - 1) {
                //repeated = this.getTable().getPrintStopCol() - this.allCells.size() + 1;
                // Patch JOSM open data : do not care about last cell
                repeated = 0;
            }
            for (int i = 0; i < repeated; i++) {
             // Patch JOSM open data : do not care about column
                //final TableTableColumn col = this.table.getColumnAtPosition(colPosition + i);
                final TableTableCell cc = c.cloneCell();
                //cc.setRowAndColumn(this, col);
                this.allCells.add(cc);
            }
        }
        // }}
        // System.err.println("Computed:" + allCells.size() + " :" + allCells);
    }
    
    public Collection<TableTableCell> getAllCells() {

        if (this.allCells == null) {
            this.computeAllCells();
        }
        
        return this.allCells;
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
