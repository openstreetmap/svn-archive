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

package org.jopendocument.model.office;

import java.util.List;
import java.util.Vector;

import org.jopendocument.model.table.TableTable;

public class OfficeSpreadsheet {
    List<TableTable> tables = new Vector<TableTable>();

    public void addTable(final TableTable table) {
        this.tables.add(table);
    }

    public List<TableTable> getTables() {
        return this.tables;
    }

    @Override
    public String toString() {

        return "OfficeSpreadsheet: " + this.tables.size() + " tables";
    }
}
