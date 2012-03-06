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
import java.util.List;

/**
 * 
 */
public class TableTable {

    // Une colonne ou ligne repeated est dupliquée dans la liste
    ArrayList<TableTableColumn> columns = new ArrayList<TableTableColumn>();

    private int printStartCol = 0;

    private int printStartRow = 0;

    private int printStopCol = 0;

    private int printStopRow = 0;

    ArrayList<TableTableRow> rows = new ArrayList<TableTableRow>();

    public void addColumn(final TableTableColumn col) {
        for (int i = 0; i < col.getTableNumberColumnsRepeated(); i++) {
            this.columns.add(col);
        }

        col.setTable(this);

    }

    public void addRow(final TableTableRow r) {
        for (int i = 0; i < r.getTableNumberRowsRepeated(); i++) {
            this.rows.add(r);
        }
    }

    public int getPrintStartCol() {
        return this.printStartCol;
    }

    public int getPrintStartRow() {
        return this.printStartRow;
    }

    public int getPrintStopCol() {
        return this.printStopCol;
    }

    public int getPrintStopRow() {
        return this.printStopRow;
    }


    /**
     * Return all the rows (duplicated if repeated)
     */
    public List<TableTableRow> getRows() {
        return this.rows;
    }

    /**
     * Sets the value of the tablePrintRanges property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTablePrintRanges(final String value) {
        if (value == null || !value.contains(":")) {
            throw new IllegalArgumentException("ranges is null");
        }
        //this.tablePrintRanges = value;
        final int s = value.indexOf(':');
        final String l = value.substring(0, s);
        final String r = value.substring(s + 1);

        String vl = l.substring(l.indexOf('.') + 1);
        String vr = r.substring(r.indexOf('.') + 1);
        vl = removeDollars(vl);
        vr = removeDollars(vr);

        {
            int j = 0;
            for (int i = vl.length() - 1; i >= 0; i--) {
                final int c = vl.charAt(i);
                if (Character.isLetter(c)) {
                    final int val = c - 'A' + 1;
                    this.printStartCol += val * Math.pow(26, j);
                    j++;
                } else {
                    this.printStartRow = i;
                }
            }
            final String substring = vl.substring(j);
            this.printStartRow = Integer.valueOf(substring) - 1;
            this.printStartCol--;
        }
        {
            int j = 0;
            for (int i = vr.length() - 1; i >= 0; i--) {
                final int c = vr.charAt(i);
                if (Character.isLetter(c)) {
                    final int val = c - 'A' + 1;
                    this.printStopCol += val * Math.pow(26, j);
                    j++;
                } else {
                    this.printStopRow = i;
                }
            }
            final String substring = vr.substring(j);
            this.printStopRow = Integer.valueOf(substring) - 1;
            this.printStopCol--;
        }
    }

    private final String removeDollars(String s) {
        final int length = s.length();
        final StringBuilder t = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c != '$') {
                t.append(c);
            }
        }
        return t.toString();
    }

    @Override
    public String toString() {

        return "TableTable: print:" + this.getPrintStartCol() + "," + this.getPrintStartRow() + " : " + this.getPrintStopCol() + "," + this.getPrintStopRow();
    }

}
