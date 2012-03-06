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

import org.jopendocument.model.text.TextP;

public class TableTableCell {

    private TableTableColumn column;

    protected String tableStyleName;

    private TextP textP;

    private void computeStyle() {
        if (this.column == null) {
            return;
        }
        String styleName = this.getStyleName();

        if (styleName == null) {
            styleName = this.column.getTableDefaultCellStyleName();
        }

    }

    /**
     * Gets the value of the tableStyleName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getStyleName() {
        return this.tableStyleName;
    }

    public TextP getTextP() {
        return this.textP;
    }

    /**
     * Sets the value of the tableStyleName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTableStyleName(final String value) {

        this.tableStyleName = value;
        this.computeStyle();

    }


    public void setTextP(final TextP p) {
        if (this.textP != null) {
        	if (p != null && !p.isEmpty()) {
        		System.err.println("TableTableCell: Warning: no support for multiple TextP in a Cell (current="+this.textP+") (tried="+p+")");
        	}
        } else {
            this.textP = p;
        }

    }

    @Override
    public String toString() {
        return "Cell: style:" + this.getStyleName() + " TestP:" + this.getTextP();
    }

}
