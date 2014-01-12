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
 * Created on Apr 1, 2011 by Bea
 * Modified on $DateTime$ by $Author$

 */
package org.openstreetmap.josm.plugins.mapdust.gui.component.slider;


import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Change listener for the <code>RelevanceSlider</code> object.
 *
 * @author Bea
 * @version $Revision$
 */
public class RelevanceChangeListener implements ChangeListener {

    /** The <code>RelevanceSliderUI</code> object */
    private final RelevanceSliderUI sliderUI;

    /**
     * Builds a <code>RelevanceChangeListener</code> object based on the given
     * argument
     *
     * @param sliderUI The <code>RelevanceSliderUI</code> object
     */
    public RelevanceChangeListener(RelevanceSliderUI sliderUI) {
        this.sliderUI = sliderUI;
    }

    /**
     * Listens for slider thumb specific change events ( the lower or upper
     * thumb of the <code>RelevanceSlider</code> was moved), and updates the
     * current <code>RelevanceSlider</code> state to the new state.
     *
     * @param event A <code>ChangeEvent</code> object
     */
    @Override
    public void stateChanged(ChangeEvent event) {
        if (event != null) {
            sliderUI.changeSliderState();
        }
    }

}
