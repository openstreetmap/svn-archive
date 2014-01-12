/*
 * Copyright (c) 2010, skobbler GmbH
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
 *
 * Created on Feb 14, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.gui.component.util;


import java.awt.Rectangle;
import javax.swing.JCheckBox;
import javax.swing.JLabel;


/**
 * This is a helper class for the customized <code>JCheckBox</code> object.
 *
 * @author Bea
 * @version $Revision$
 */
public class FilterCheckBox {

    /** The identifier of the check box */
    private Object id;

    /** The <code>JLabel</code> of the filter */
    private JLabel lblFilter;

    /** The <code>JCheckBox</code> of the filter */
    private JCheckBox chbFilter;

    /**
     * Builds a <code>FilterCheckBox</code> object
     */
    public FilterCheckBox() {}

    /**
     * Builds a <code>FilterCheckBox</code> object based on the given parameters
     *
     * @param id The identifier of the object
     * @param chbBounds The bounds of the check box
     * @param iconName The name of the icon
     * @param text The text of the filter
     * @param lblBounds The bounds of the label
     */
    public FilterCheckBox(Object id, Rectangle chbBounds, String iconName,
            String text, Rectangle lblBounds) {
        this.id = id;
        this.chbFilter = ComponentUtil.createJCheckBox(chbBounds);
        this.lblFilter = ComponentUtil.createJLabel(text, iconName, lblBounds);
    }

    /**
     * Returns the id
     *
     * @return the id
     */
    public Object getId() {
        return id;
    }

    /**
     * Returns the filter label
     *
     * @return the lblFilter
     */
    public JLabel getLblFilter() {
        return lblFilter;
    }

    /**
     * Returns the filter check box
     *
     * @return the chbFilter
     */
    public JCheckBox getChbFilter() {
        return chbFilter;
    }

}
