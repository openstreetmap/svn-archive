/* Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.mapdust.gui.component.model;


import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.openstreetmap.josm.plugins.mapdust.service.value.BugType;


/**
 * The combo box model for the bug <code>BugType</code>s.
 *
 * @author Bea
 * @version $Revision$
 */
public class TypesListModel extends AbstractListModel implements ComboBoxModel {

    /** The serial version UID */
    private static final long serialVersionUID = 4005728061902494996L;

    /** The array of types */
    private final BugType[] types;

    /** The selected type */
    private BugType selection = null;

    /**
     * Builds a <code>TypesListModel</code> object
     */
    public TypesListModel() {
        this.types = BugType.getTypes();
    }

    /**
     * Returns the <code>BugType</code> from the given position.
     *
     * @param index The position of the element
     * @return <code>BugType</code> from the given position
     */
    @Override
    public Object getElementAt(int index) {
        return types[index];
    }

    /**
     * Returns the size of the list of objects.
     *
     * @return size
     */
    @Override
    public int getSize() {
        return types.length;
    }

    /**
     * Returns the selected <code>BugType</code> object.
     *
     * @return selected item
     */
    @Override
    public Object getSelectedItem() {
        return selection;
    }

    /**
     * Sets the selected <code>BugType</code> object.
     *
     * @param anItem The selected item
     */
    @Override
    public void setSelectedItem(Object anItem) {
        selection = (BugType) anItem;
    }
}
