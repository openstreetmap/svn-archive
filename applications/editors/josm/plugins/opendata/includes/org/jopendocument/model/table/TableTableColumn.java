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


public class TableTableColumn {

    protected String tableDefaultCellStyleName;

    protected String tableNumberColumnsRepeated;

    protected String tableStyleName;

    /**
     * Gets the value of the tableDefaultCellStyleName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTableDefaultCellStyleName() {
        return this.tableDefaultCellStyleName;
    }

    /**
     * Gets the value of the tableNumberColumnsRepeated property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public int getTableNumberColumnsRepeated() {
        if (this.tableNumberColumnsRepeated == null) {
            return 1;
        }
        return Integer.valueOf(this.tableNumberColumnsRepeated).intValue();

    }

    /**
     * Gets the value of the tableStyleName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTableStyleName() {
        return this.tableStyleName;
    }

    public void setTable(final TableTable t) {
    }

    /**
     * Sets the value of the tableDefaultCellStyleName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTableDefaultCellStyleName(final String value) {
        this.tableDefaultCellStyleName = value;
    }

    /**
     * Sets the value of the tableNumberColumnsRepeated property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTableNumberColumnsRepeated(final String value) {
        this.tableNumberColumnsRepeated = value;
    }

    /**
     * Sets the value of the tableStyleName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTableStyleName(final String value) {
        this.tableStyleName = value;
    }

    /*
     * private void setColumnStyle(StyleStyle style) { this.columnStyle=style;
     * this.width=columnStyle.getWidth(); }
     */


    @Override
    public String toString() {

        return "TableColumn: style:" + this.getTableStyleName() + " defaultCellStyle:" + this.tableDefaultCellStyleName;
    }
}
