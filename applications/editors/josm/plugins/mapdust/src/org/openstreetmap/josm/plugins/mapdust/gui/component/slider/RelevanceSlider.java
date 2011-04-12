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


import java.awt.Font;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JSlider;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustRelevanceValue;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustRelevance;


/**
 * This class defines a customized relevance <code>JSlider</code> object. The
 * relevance slider is a slider with two thumbs, and with a lower and upper
 * value.
 *
 * @author Bea
 * @version $Revision$
 */
public class RelevanceSlider extends JSlider {

    /** The serial version UID */
    private static final long serialVersionUID = 3306976109770890966L;

    /** The selected lower value */
    private int lowerValue;

    /** The selected upper value */
    private int upperValue;

    /**
     * Builds a new <code>RelevanceSlider</code> with the default settings. By
     * default the <code>RelevanceSlider</code> values are from 0 (see
     * <code>MapdustRelevanceValue</code> LOW value) until 16 (see
     * <code>MapdustRelevanceValue</code> HIGH value).
     */
    public RelevanceSlider() {
        super(MapdustRelevanceValue.LOW.getSliderValue(),
                MapdustRelevanceValue.HIGH.getSliderValue());
        initSlider(MapdustRelevanceValue.LOW.getSliderValue(),
                MapdustRelevanceValue.HIGH.getSliderValue());
    }

    /**
     * Initialize the <code>RelevanceSlider</code> object.
     *
     * @param lowerValue The value of the lower thumb
     * @param upperValue The value of the upper thumb
     */
    private void initSlider(int lowerValue, int upperValue) {
        setOrientation(HORIZONTAL);
        setMajorTickSpacing(4);
        setLowerValue(lowerValue);
        setUpperValue(upperValue);
        setPaintTicks(true);
        setPaintLabels(true);
        setSnapToTicks(true);
        setFocusable(false);
        /* set label for the slider values */
        Dictionary<Integer, JLabel> values = new Hashtable<Integer, JLabel>();
        Font font = new Font("Times New Roman", Font.BOLD, 12);
        values.put(MapdustRelevanceValue.LOW.getSliderValue(), ComponentUtil
                .createJLabel(MapdustRelevance.LOW.getName(), font, null, null));
        values.put(MapdustRelevanceValue.MID_LOW.getSliderValue(),
                ComponentUtil.createJLabel(MapdustRelevance.MID_LOW.getName(),
                        font, null, null));
        values.put(MapdustRelevanceValue.MEDIUM.getSliderValue(), ComponentUtil
                .createJLabel(MapdustRelevance.MEDIUM.getName(), font, null,
                        null));
        values.put(MapdustRelevanceValue.MID_HIGH.getSliderValue(),
                ComponentUtil.createJLabel(MapdustRelevance.MID_HIGH.getName(),
                        font, null, null));
        values.put(MapdustRelevanceValue.HIGH.getSliderValue(),
                ComponentUtil.createJLabel(MapdustRelevance.HIGH.getName(),
                        font, null, null));
        setLabelTable(values);
    }

    /**
     * Updates the UI of the slider.
     */
    @Override
    public void updateUI() {
        setUI(new RelevanceSliderUI(this));
        updateLabelUIs();
    }

    /**
     * Returns the <code>RelevanceSliderUI</code> object
     *
     * @return ui
     */
    @Override
    public RelevanceSliderUI getUI() {
        return (RelevanceSliderUI) ui;
    }

    /**
     * Returns the lower value
     *
     * @return the lowerValue
     */
    public int getLowerValue() {
        return lowerValue;
    }

    /**
     * Sets the new lower value, and the range properties.
     *
     * @param lowerValue The new lower value
     */
    public void setLowerValue(int lowerValue) {
        int oldValue = getLowerValue();
        int oldExtent = getExtent();
        int newExtent = oldExtent + oldValue - lowerValue;
        getModel().setRangeProperties(lowerValue, newExtent, getMinimum(),
                getMaximum(), true);
        this.lowerValue = lowerValue;
    }

    /**
     * Returns the upper value
     *
     * @return upperValue
     */
    public int getUpperValue() {
        if (upperValue<lowerValue){
            upperValue=lowerValue;
        }
        return upperValue;
    }

    /**
     * Sets the upper value, and the extent value.
     *
     * @param upperValue The new upper value.
     */
    public void setUpperValue(int upperValue) {
        this.upperValue = upperValue;
        int newExtent = Math.min(Math.max(0, upperValue - getLowerValue()),
                getMaximum() - getLowerValue());
        setExtent(newExtent);
    }

    /**
     * Sets the extent values of the slider, and also reset the value for the
     * upperValue field.
     */
    @Override
    public void setExtent(int extent) {
        super.setExtent(extent);
        this.upperValue=getLowerValue()+getExtent();
    }

}
