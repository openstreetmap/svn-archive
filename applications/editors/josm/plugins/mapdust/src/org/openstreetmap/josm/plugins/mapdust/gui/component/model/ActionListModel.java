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


import java.util.List;
import javax.swing.AbstractListModel;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustAction;


/**
 * The list model for the <code>MapdustAction</code> objects.
 *
 * @author Bea
 * @version $Revision$
 */
public class ActionListModel extends AbstractListModel {

    /** The serial version UID */
    private static final long serialVersionUID = -4919264544683489112L;

    /**
     * The list of <code>MapdustAction</code> objects
     */
    private final List<MapdustAction> list;

    /**
     * Builds an empty <code>MapdustAction</code> object
     */
    public ActionListModel() {
        this.list = null;
    }

    /**
     * Builds a <code>MapdustAction</code> object based on the given argument
     *
     * @param list A list of <code>MapdustAction</code> objects
     */
    public ActionListModel(List<MapdustAction> list) {
        this.list = list;
    }

    /**
     * Returns the <code>MapdustAction</code> from the given position.
     *
     * @param index The position of the element
     * @return <code>MapdustAction</code> from the given position
     */
    @Override
    public Object getElementAt(int index) {
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    /**
     * Returns the size of the list of objects.
     *
     * @return size
     */
    @Override
    public int getSize() {
        return (list != null ? list.size() : 0);
    }

}
