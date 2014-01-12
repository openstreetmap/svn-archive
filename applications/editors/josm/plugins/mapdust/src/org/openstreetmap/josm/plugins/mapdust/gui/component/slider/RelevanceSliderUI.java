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
 * Created on Apr 7, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.gui.component.slider;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * This class defines a customized <code>BasicSliderUI</code> for the
 * <code>RelevanceSlider</code> object.
 *
 * @author Bea
 * @version $Revision$
 */
public class RelevanceSliderUI extends BasicSliderUI {

    /** The rectangle of the upper thumb */
    private Rectangle upperRect;

    /** Flag indicating if the upper thumb is selected or not */
    private boolean isUpperSelected;

    /** Flag indicating if the lower thumb is dragging or not */
    private transient boolean isLowerDragging;

    /** Flag indicating if the upper thumb is dragging or not */
    private transient boolean isUpperDragging;

    /** The icon used for representing the upper and lower thumbs */
    private final Icon sliderIcon;

    /**
     * Builds a new <code>RelevanceSliderUI</code> object based on the given
     * arguments.
     *
     * @param relevanceSlider The <code>RelevanceSlider</code> object
     */
    public RelevanceSliderUI(RelevanceSlider relevanceSlider) {
        super(relevanceSlider);
        this.sliderIcon = ImageProvider.get("slider/thumb.png");
    }

    /**
     * Installs the UI on the given component.
     *
     * @param component A <code>JComponent</code> object
     */
    @Override
    public void installUI(JComponent component) {
        upperRect = new Rectangle();
        super.installUI(component);
    }

    /**
     * Changes the slider current state shown on the GUI with a new state.
     * Basically moves the upper and lower thumbs on the slider, based on a
     * previous user action.
     */
    public void changeSliderState() {
        if (!isLowerDragging && !isUpperDragging) {
            calculateThumbLocation();
            slider.repaint();
        } else {
            if (isUpperSelected) {
                calculateThumbLocation();
                slider.repaint();
            }
        }
    }

    /**
     * Creates a new custom <code>TrackListener</code> object for the slider.
     *
     * @param slider The <code>JSlider</code> object
     * @return A new <code>RangeTrackListener</code> object
     */
    @Override
    protected TrackListener createTrackListener(JSlider slider) {
        return new RangeTrackListener();
    }

    /**
     * Creates a new custom <code>ChangeListener</code> object for the slider.
     *
     * @param slider The <code>JSlider</code> object
     * @return A new <code>RelevanceChangeListener</code> object
     */
    @Override
    protected ChangeListener createChangeListener(JSlider slider) {
        return new RelevanceChangeListener(this);
    }

    /**
     * Calculates the slider thumb sizes. Also sets the size for the upper and
     * lower thumb.
     *
     */
    @Override
    protected void calculateThumbSize() {
        super.calculateThumbSize();
        upperRect.setSize(sliderIcon.getIconWidth(), sliderIcon.getIconHeight());

    }

    /**
     * Calculates the locations of the upper and lower thumbs.
     */
    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();
        if (getSlider().getSnapToTicks()) {
            int upperValue = getSlider().getUpperValue();
            int snappedValue = upperValue;
            int majorTickSpacing = getSlider().getMajorTickSpacing();
            int minorTickSpacing = getSlider().getMinorTickSpacing();
            int tickSpacing = 0;
            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }
            if (tickSpacing != 0) {
                int min = getSlider().getMinimum();
                if ((upperValue - min) % tickSpacing != 0) {
                    float temp = upperValue - min;
                    temp /= tickSpacing;
                    int whichTick = Math.round(temp);
                    snappedValue = min + (whichTick * tickSpacing);
                }
                if (snappedValue != upperValue) {
                    int extent = snappedValue - getSlider().getLowerValue();
                    getSlider().setExtent(extent);
                }
            }
        }
        int value = getSlider().getLowerValue() + getSlider().getExtent();
        int upperPosition = xPositionForValue(value);
        upperRect.x = upperPosition - (upperRect.width / 2);
        upperRect.y = trackRect.y;
    }

    /**
     * Returns the size of the thumbs. The dimension is computed based on the
     * thumb icon dimension.
     *
     * @return A <code>Dimension</code> object.
     */
    @Override
    protected Dimension getThumbSize() {
        return new Dimension(sliderIcon.getIconWidth(),
                sliderIcon.getIconHeight());
    }

    /**
     * Paints the given components.
     *
     * @param g The <code>Graphics</code> object
     * @param c The <code>JComponent</code> object
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Rectangle clipRect = g.getClipBounds();
        if (isUpperSelected) {
            if (clipRect.intersects(thumbRect)) {
                sliderIcon.paintIcon(getSlider(), g, thumbRect.x, thumbRect.y);
            }
            if (clipRect.intersects(upperRect)) {
                sliderIcon.paintIcon(getSlider(), g, upperRect.x, upperRect.y);
            }
        } else {
            if (clipRect.intersects(upperRect)) {
                sliderIcon.paintIcon(getSlider(), g, upperRect.x, upperRect.y);
            }
            if (clipRect.intersects(thumbRect)) {
                sliderIcon.paintIcon(getSlider(), g, thumbRect.x, thumbRect.y);
            }
        }
    }

    @Override
    public void paintThumb(Graphics g) {}

    /**
     * Paints the tack of the slider. The selected part of the slider will be
     * painted with orange.
     *
     * @param g The <code>Graphics</code> object
     */
    @Override
    public void paintTrack(Graphics g) {
        super.paintTrack(g);
        Rectangle trackBounds = trackRect;
        int lowerX = thumbRect.x + (thumbRect.width / 2);
        int upperX = upperRect.x + (upperRect.width / 2);
        int cy = (trackBounds.height / 2) - 2;
        Color oldColor = g.getColor();
        g.translate(trackBounds.x, trackBounds.y + cy);
        g.setColor(Color.orange);
        for (int y = 0; y <= 3; y++) {
            g.drawLine(lowerX - trackBounds.x, y, upperX - trackBounds.x, y);
        }
        g.translate(-trackBounds.x, -(trackBounds.y + cy));
        g.setColor(oldColor);
    }

    /**
     * Moves the selected thumb in the specified direction by a block increment.
     *
     * @param direction The direction
     */
    @Override
    public void scrollByBlock(int direction) {
        synchronized (getSlider()) {
            int min = getSlider().getMinimum();
            int max = getSlider().getMaximum();
            int incr = (max - min) / 10;
            if (incr <= 0 && max > min) {
                incr = 1;
            }
            int delta = incr  * ((direction > 0) ? POSITIVE_SCROLL
                    : NEGATIVE_SCROLL);
            if (isUpperSelected) {
                int oldValue = getSlider().getUpperValue();
                getSlider().setUpperValue(oldValue + delta);
            } else {
                int oldValue = getSlider().getLowerValue();
                getSlider().setLowerValue(oldValue + delta);
            }
        }
    }

    /**
     * Moves the selected thumb in the specified direction by a unit increment.
     *
     * @param direction The direction
     */
    @Override
    public void scrollByUnit(int direction) {
        synchronized (getSlider()) {
            int delta = 1 * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);
            if (isUpperSelected) {
                int oldValue = getSlider().getUpperValue();
                getSlider().setUpperValue(oldValue + delta);
            } else {
                int oldValue = getSlider().getLowerValue();
                getSlider().setLowerValue(oldValue + delta);
            }
        }
    }

    /**
     * Returns the <code>RelevanceSlider</code> object
     *
     * @return relevance slider
     */
    protected RelevanceSlider getSlider() {
        return (RelevanceSlider) slider;
    }

    /**
     * Returns the thumb rectangle. This method returns the parent class
     * thumbRect field.
     *
     * @return A <code>Rectangle</code> object
     */
    protected Rectangle getThumbRect() {
        return super.thumbRect;
    }

    /**
     * Returns the track rectangle. This method returns the parent class
     * trackRect field.
     *
     * @return A <code>Rectangle</code> object
     */
    protected Rectangle getTrackRect() {
        return super.trackRect;
    }

    /**
     * Returns the upper thumb rectangle.
     *
     * @return A <code>Rectangle</code> object
     */
    public Rectangle getUpperRect() {
        return upperRect;
    }

    /**
     * This method it is used for accessing the base class 'drawInverted'
     * method.
     *
     * @return boolean
     */
    @Override
    protected boolean drawInverted() {
        return super.drawInverted();
    }

    /**
     * This method it is used for accessing the base class 'xPositionForValue'
     * method.
     *
     * @param value The integer value
     * @return the x position for the given value
     */
    @Override
    protected int xPositionForValue(int value) {
        return super.xPositionForValue(value);
    }

    /**
     * Returns the isUpperSelected field value.
     *
     * @return the isUpperSelected
     */
    public boolean getIsUpperSelected() {
        return isUpperSelected;
    }

    /**
     * Sets the isUpperSelected field to a new value
     *
     * @param isUpperSelected the isUpperSelected to set
     */
    public void setIsUpperSelected(boolean isUpperSelected) {
        this.isUpperSelected = isUpperSelected;
    }

    /**
     * Returns the isLowerDragging field value.
     *
     * @return the isLowerDragging
     */
    public boolean getIsLowerDragging() {
        return isLowerDragging;
    }

    /**
     * Sets the isLowerDragging field value
     *
     * @param isLowerDragging the isLowerDragging to set
     */
    public void setIsLowerDragging(boolean isLowerDragging) {
        this.isLowerDragging = isLowerDragging;
    }

    /**
     * Returns the isUpperDragging field value
     *
     * @return the isUpperDragging
     */
    public boolean getIsUpperDragging() {
        return isUpperDragging;
    }

    /**
     * Sets the isUpperDragging field value
     *
     * @param isUpperDragging the isUpperDragging to set
     */
    public void setIsUpperDragging(boolean isUpperDragging) {
        this.isUpperDragging = isUpperDragging;
    }

    /**
     * Custom <code>TrackListener</code> inner class for the
     * <code>RelevanceSliderUI</code> object.
     *
     */
    class RangeTrackListener extends TrackListener {

        /**
         * Listens and handles the mouse pressed event.
         *
         * @param event The <code>MouseEvent</code> object
         */
        @Override
        public void mousePressed(MouseEvent event) {
            if (!getSlider().isEnabled()) {
                return;
            }
            currentMouseX = event.getX();
            currentMouseY = event.getY();
            if (getSlider().isRequestFocusEnabled()) {
                getSlider().requestFocus();
            }
            boolean lowerPressed = false;
            boolean upperPressed = false;
            if (getIsUpperSelected()) {
                if (getUpperRect().contains(currentMouseX, currentMouseY)) {
                    upperPressed = true;
                } else {
                    if (getThumbRect().contains(currentMouseX, currentMouseY)) {
                        lowerPressed = true;
                    }
                }
            } else {
                if (getThumbRect().contains(currentMouseX, currentMouseY)) {
                    lowerPressed = true;
                } else {
                    if (getUpperRect().contains(currentMouseX, currentMouseY)) {
                        upperPressed = true;
                    }
                }
            }

            /* lower thumb was pressed */
            if (lowerPressed) {
                offset = currentMouseX - getThumbRect().x;
                setIsUpperSelected(false);
                setIsLowerDragging(true);
                return;
            }
            setIsLowerDragging(false);

            /* upper thumb was pressed */
            if (upperPressed) {
                offset = currentMouseX - getUpperRect().x;
                setIsUpperSelected(true);
                setIsUpperDragging(true);
                return;
            }
            setIsUpperDragging(false);
        }

        /**
         * Listens and handles the mouse released event.
         *
         * @param event The <code>MouseEvent</code> object
         */
        @Override
        public void mouseReleased(MouseEvent event) {
            setIsLowerDragging(false);
            setIsUpperDragging(false);
            getSlider().setValueIsAdjusting(false);
            super.mouseReleased(event);
        }

        /**
         * Listens and handles the mouse dragged event.
         *
         * @param event The <code>MouseEvent</code> object
         */
        @Override
        public void mouseDragged(MouseEvent event) {
            if (!getSlider().isEnabled()) {
                return;
            }
            currentMouseX = event.getX();
            currentMouseY = event.getY();
            if (getIsLowerDragging()) {
                getSlider().setValueIsAdjusting(true);
                moveLowerThumb(event);

            } else if (getIsUpperDragging()) {
                getSlider().setValueIsAdjusting(true);
                moveUpperThumb(event);
            }
        }

        @Override
        public boolean shouldScroll(int direction) {
            return false;
        }

        /**
         * Moves the location of the lower thumb, and sets its corresponding
         * value in the slider.
         *
         * @param event The <code>MouseEvent</code> object
         */
        private void moveLowerThumb(MouseEvent event) {
            int halfThumbWidth = getThumbRect().width / 2;
            int thumbLeft = currentMouseX - offset;
            int trackLeft = getTrackRect().x;
            int trackRight = getTrackRect().x + (getTrackRect().width - 1);
            int hMax = xPositionForValue(getSlider().getLowerValue()
                    + getSlider().getExtent());
            if (drawInverted()) {
                trackLeft = hMax;
            } else {
                trackRight = hMax;
            }
            thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
            thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);
            /* set location & repaint */
            setThumbLocation(thumbLeft, getThumbRect().y);
            /* set lower value */
            getSlider().setLowerValue(getSnappedValue(event));
        }

        /**
         * Moves the location of the upper thumb, and sets its corresponding
         * value in the slider.
         *
         * @param event The <code>MouseEvent</code> object
         */
        private void moveUpperThumb(MouseEvent event) {
            int halfThumbWidth = getThumbRect().width / 2;
            int thumbLeft = currentMouseX - offset;
            int trackLeft = getTrackRect().x;
            int trackRight = getTrackRect().x + (getTrackRect().width - 1);
            int hMin = xPositionForValue((getSlider()).getLowerValue());
            if (drawInverted()) {
                trackRight = hMin;
            } else {
                trackLeft = hMin;
            }
            thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
            thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);
            /* set location & repaint */
            Rectangle upperUnionRect = new Rectangle();
            upperUnionRect.setBounds(getUpperRect());
            getUpperRect().setLocation(thumbLeft, getThumbRect().y);
            SwingUtilities.computeUnion(getUpperRect().x, getUpperRect().y,
                    getUpperRect().width, getUpperRect().height,
                    upperUnionRect);
            getSlider().repaint(upperUnionRect.x, upperUnionRect.y,
                    upperUnionRect.width, upperUnionRect.height);
            /* set the new upper value */
            getSlider().setUpperValue(getSnappedValue(event));
        }

        /**
         * Computes the snapped values for the upper/lower relevance slider
         * value. If the value of the slider is not on a tick, than its value
         * will be adjusted according to the nearest left or right tick.
         *
         * @param evt The <code>MouseEvent</code>
         * @return The snapped value
         */
        private int getSnappedValue(MouseEvent evt) {
            int pozX = valueForXPosition(evt.getX());
            int pozY = valueForYPosition(evt.getY());
            int value = getSlider().getOrientation() ==
                SwingConstants.HORIZONTAL ? pozX : pozY;
            int snappedValue = value;
            int tickSpacing = 0;
            int majorTickSpacing = getSlider().getMajorTickSpacing();
            int minorTickSpacing = getSlider().getMinorTickSpacing();
            if (minorTickSpacing > 0)
                tickSpacing = minorTickSpacing;
            else if (majorTickSpacing > 0)
                tickSpacing = majorTickSpacing;
            /* If it's not on a tick, change the value */
            if (tickSpacing != 0) {
                if ((value - getSlider().getMinimum()) % tickSpacing != 0) {
                    float temp = (float) (value - getSlider().getMinimum())
                            / (float) tickSpacing;
                    snappedValue = getSlider().getMinimum()+ (Math.round(temp)
                            * tickSpacing);
                }
            }
            return snappedValue;
        }
    }

}