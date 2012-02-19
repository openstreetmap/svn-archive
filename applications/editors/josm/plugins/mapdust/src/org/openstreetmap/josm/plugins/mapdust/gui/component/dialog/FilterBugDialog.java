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
 * Created on Feb 10, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.gui.component.dialog;


import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.action.adapter.WindowClose;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteCancel;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteFilterBug;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustButtonPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.slider.RelevanceSlider;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.FilterCheckBox;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustRelevanceValue;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBugFilter;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustRelevance;
import org.openstreetmap.josm.plugins.mapdust.service.value.Status;
import org.openstreetmap.josm.plugins.mapdust.service.value.BugType;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * This class it is used for creating a dialog window for the MapDust bug
 * filtering feature.
 *
 * @author Bea
 */
public class FilterBugDialog extends AbstractDialog {
    org.openstreetmap.josm.plugins.mapdust.service.value.BugType ttt=org.openstreetmap.josm.plugins.mapdust.service.value.BugType.WRONG_TURN;

    /** The serial version UID */
    private static final long serialVersionUID = -3333642616656969760L;

    /** The <code>JLabel</code> for the status filter */
    private JLabel lblStatus;

    /** The array of <code>FilterCheckBox</cod> containing the status filters */
    private FilterCheckBox[] filterStatuses;

    /** The <ode>JLabel</code> for the type filter */
    private JLabel lblType;

    /** The array of <code>FilterCheckBox</cod> containing the type filters */
    private FilterCheckBox[] filterTypes;

    /** The <code>JLabel</code> for the description filter */
    private JLabel lblDescription;

    /** The <code>FilterCheckBox</code> containing the description filter */
    private FilterCheckBox filterDescr;

    /** The <code>JLabel</code> for the relevance filter */
    private JLabel lblRelevance;

    /** The <code>RelevanceSlider</code> representing the relevance values */
    private RelevanceSlider sliderRelevance;

    /** The apply button */
    private JButton btnApply;

    /** The cancel button */
    private JButton btnCancel;

    /** Builds a <code>FilterBugDialog</code> object */
    public FilterBugDialog() {}

    /**
     * Builds a <code>FilterBugDialog</code> object with the given parameters.
     *
     * @param title The title of the dialog
     * @param iconName The name of the icon
     * @param firedButton The button which fired this action
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public FilterBugDialog(String title, String iconName,
            JToggleButton firedButton, MapdustPlugin mapdustPlugin) {
        if (firedButton != null) {
            setFiredButton(firedButton);
        }
        setTitle(title);
        setModal(true);
        Image image = ImageProvider.get(iconName).getImage();
        setIconImage(image);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setFont(new Font("Times New Roman", Font.BOLD, 14));
        setBackground(Color.white);
        setResizable(false);
        setForeground(Color.black);
        setLayout(null);
        addComponents(mapdustPlugin);
        MapdustButtonPanel btnPanel =
                mapdustPlugin.getMapdustGUI().getPanel().getBtnPanel();
        addWindowListener(new WindowClose(this, btnPanel));
    }

    @Override
    public void addComponents(MapdustPlugin mapdustPlugin) {
        Font font = new Font("Times New Roman", Font.BOLD, 14);
        MapdustBugFilter prevFilter = mapdustPlugin.getFilter();

        /* status filter */
        if (lblStatus == null) {
            Rectangle bounds = new Rectangle(10, 10, 95, 25);
            lblStatus = ComponentUtil.createJLabel("Status", font, bounds,
                    null);
        }
        if (filterStatuses == null) {
            createStatusFilters(prevFilter);
        }

        /* type filters */
        if (filterTypes == null) {
            createTypeFilters(prevFilter);
        }
        if (lblType == null) {
            Rectangle bounds = new Rectangle(10, 90, 95, 25);
            lblType = ComponentUtil.createJLabel("Type", font, bounds, null);
        }

        /* description filter */
        if (lblDescription == null) {
            Rectangle bounds = new Rectangle(10, 230, 95, 25);
            lblDescription = ComponentUtil.createJLabel("Description", font,
                    bounds, null);
        }
        if (filterDescr == null) {
            Rectangle chbBounds = new Rectangle(110, 230, 20, 25);
            Rectangle lblBounds = new Rectangle(130, 230, 300, 25);
            String text = "Hide bugs with default description";
            String iconName = "dialogs/default_description.png";
            filterDescr = new FilterCheckBox("descr", chbBounds, iconName,
                    text, lblBounds);
            if (prevFilter != null && prevFilter.getDescr() != null
                    && prevFilter.getDescr()) {
                filterDescr.getChbFilter().setSelected(true);
            }
        }
        /* relevance filter */
        if (lblRelevance == null) {
            Rectangle bounds = new Rectangle(10, 265, 95, 25);
            lblRelevance = ComponentUtil.createJLabel("Relevance", font,
                    bounds, null);
        }
        if (sliderRelevance == null) {
            Rectangle bounds = new Rectangle(110, 265, 300, 50);
            sliderRelevance = new RelevanceSlider();
            sliderRelevance.setBounds(bounds);
            if (prevFilter != null) {
                MapdustRelevance min = prevFilter.getMinRelevance();
                MapdustRelevance max = prevFilter.getMaxRelevance();
                if (min != null) {
                    Integer value = MapdustRelevanceValue.getSliderValue(min);
                    if (value == null) {
                        value = MapdustRelevanceValue.LOW.getSliderValue();
                    }
                    sliderRelevance.setLowerValue(value);
                }
                if (max != null) {
                    Integer value = MapdustRelevanceValue.getSliderValue(max);
                    if (value == null) {
                        value = MapdustRelevanceValue.HIGH.getSliderValue();
                    }
                    sliderRelevance.setUpperValue(value);
                }
                if (max != null && max.equals(min)
                        && max.equals(MapdustRelevance.LOW)) {
                    sliderRelevance.getUI().setIsUpperSelected(true);
                }
            }
        }

        /* creates the cancel button */
        if (btnCancel == null) {
            Rectangle bounds = new Rectangle(360, 330, 90, 25);
            ExecuteCancel cancelAction = new ExecuteCancel(this,
                    mapdustPlugin.getMapdustGUI());
            btnCancel = ComponentUtil.createJButton("Cancel", bounds,
                    cancelAction);
        }
        /* creates the OK button */
        if (btnApply == null) {
            Rectangle bounds = new Rectangle(260, 330, 90, 25);
            ExecuteFilterBug applyAction = new ExecuteFilterBug(this,
                    mapdustPlugin.getMapdustGUI());
            applyAction.addObserver(mapdustPlugin);
            btnApply = ComponentUtil.createJButton("Apply", bounds,
                    applyAction);
        }
        /* add components */
        add(lblStatus);
        add(filterStatuses);
        add(lblType);
        add(filterTypes);
        add(lblDescription);
        add(filterDescr);
        add(lblRelevance);
        add(sliderRelevance);
        add(btnCancel);
        add(btnApply);
        setSize(460, 360);
    }

    /**
     * Creates the MapDust bug status filters. If there was any previous status
     * filter selected then this status filter will be selected by default.
     *
     * @param prevFilter The <code>MapdustBugFilter</code> object
     */
    private void createStatusFilters(MapdustBugFilter prevFilter) {
        filterStatuses = new FilterCheckBox[3];

        /* open status */
        filterStatuses[0] = new FilterCheckBox(Status.OPEN.getKey(),
                new Rectangle(110, 10, 20, 25), "dialogs/open_bug.png",
                Status.OPEN.getValue(), new Rectangle(130, 10, 150, 25));

        /* closed status */
        filterStatuses[1] = new FilterCheckBox(Status.INVALID.getKey(),
                new Rectangle(270, 10, 20, 25), "dialogs/invalid_bug.png",
                Status.INVALID.getValue(), new Rectangle(290, 10, 180, 25));

        /* invalid status */
        filterStatuses[2] = new FilterCheckBox(Status.FIXED.getKey(),
                new Rectangle(110, 45, 20, 25), "dialogs/fixed_bug.png",
                Status.FIXED.getValue(), new Rectangle(130, 45, 150, 25));

        if (prevFilter != null && prevFilter.getStatuses() != null) {
            for (FilterCheckBox filter : filterStatuses) {
                if (prevFilter.getStatuses().contains(filter.getId())) {
                    filter.getChbFilter().setSelected(true);
                }
            }
        }
    }

    /**
     * Creates the MapDust bug type filters. If there was any previous type
     * filter selected, then this type filter will be selected by default.
     *
     * @param prevFilter The <code>MapdustBugFilter</code> object
     */
    private void createTypeFilters(MapdustBugFilter prevFilter) {
        filterTypes = new FilterCheckBox[8];

        /* wrong_turn type */
        filterTypes[0] = new FilterCheckBox(BugType.WRONG_TURN.getKey(),
                new Rectangle(110, 90, 20, 25), "dialogs/wrong_turn.png",
                BugType.WRONG_TURN.getValue(), new Rectangle(130, 90, 120, 25));
        /* bad_routing type */
        filterTypes[1] = new FilterCheckBox(BugType.WRONG_ROUNDABOUT.getKey(),
                new Rectangle(270, 90, 20, 25), "dialogs/wrong_roundabout.png",
                BugType.WRONG_ROUNDABOUT.getValue(), new Rectangle(290, 90, 180,
                        25));
        /* oneway_road type */
        filterTypes[2] =  new FilterCheckBox(BugType.MISSING_STREET.getKey(),
                new Rectangle(110, 125, 20, 25), "dialogs/missing_street.png",
                BugType.MISSING_STREET.getValue(), new Rectangle(130, 125, 150,
                        25));
        /* blocked_street type */
        filterTypes[3] = new FilterCheckBox(BugType.BLOCKED_STREET.getKey(),
                new Rectangle(270, 125, 20, 25), "dialogs/blocked_street.png",
                BugType.BLOCKED_STREET.getValue(), new Rectangle(290, 125, 180,
                        25));
        /* missing_street type */
        filterTypes[4] = new FilterCheckBox(BugType.BAD_ROUTING.getKey(),
                new Rectangle(110, 160, 20, 25), "dialogs/bad_routing.png",
                BugType.BAD_ROUTING.getValue(), new Rectangle(130, 160, 150, 25));
        /* wrong_roundabout type */
        filterTypes[5] = new FilterCheckBox(BugType.MISSING_SPEEDLIMIT.getKey(),
                new Rectangle(270, 160, 20, 25), "dialogs/missing_speedlimit.png",
                BugType.MISSING_SPEEDLIMIT.getValue(), new Rectangle(290, 160,
                        180, 25));
        /* missing_speedlimit type */
        filterTypes[6] = new FilterCheckBox(BugType.OTHER.getKey(),
                new Rectangle(110, 195, 20, 25), "dialogs/other.png",
                BugType.OTHER.getValue(), new Rectangle(130, 195, 150, 25));
        /* other type */
        filterTypes[7] = new FilterCheckBox(BugType.ONEWAY_ROAD.getKey(),
                new Rectangle(270, 195, 20, 25), "dialogs/oneway_road.png",
                BugType.ONEWAY_ROAD.getValue(), new Rectangle(290, 195, 180, 25));

        if (prevFilter != null && prevFilter.getTypes() != null) {
            for (FilterCheckBox filter : filterTypes) {
                if (prevFilter.getTypes().contains(filter.getId())) {
                    filter.getChbFilter().setSelected(true);
                }
            }
        }
    }

    /**
     * Returns the list of selected status filter values.
     *
     * @return a list of <code>Integer</code> values, representing the selected
     * status filter IDs
     */
    public List<Integer> getCheckedStatuses() {
        List<Integer> statusList = new ArrayList<Integer>();
        for (FilterCheckBox filter : filterStatuses) {
            if (filter.getChbFilter().isSelected()) {
                statusList.add((Integer) filter.getId());
            }
        }
        return statusList;
    }

    /**
     * Returns the list of selected type filter values.
     *
     * @return a list of <code>String</code> values, representing the selected
     * type filter values
     */
    public List<String> getCheckedTypes() {
        List<String> typeList = new ArrayList<String>();
        for (FilterCheckBox filter : filterTypes) {
            if (filter.getChbFilter().isSelected()) {
                typeList.add((String) filter.getId());
            }
        }
        return typeList;
    }

    /**
     * Verifies if the description filter is selected or not.
     *
     * @return true if the description filter is selected false if the
     * description filter is not selected
     */
    public boolean isDescrFilterChecked() {
        if (filterDescr.getChbFilter().isSelected()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the selected minimum relevance value from the relevance slider.
     *
     * @return A <code>MapdustRelevance</code> object
     */
    public MapdustRelevance getSelectedMinRelevance() {
        int value = sliderRelevance.getLowerValue();
        MapdustRelevance minRelevance =
                MapdustRelevance.getMapdustRelevance(value);
        return minRelevance;
    }

    /**
     * Returns the selected maximum relevance value from the relevance slider.
     *
     * @return A <code>MapdustRelevance</code> object
     */
    public MapdustRelevance getSelectedMaxRelevance() {
        int value = sliderRelevance.getUpperValue();
        MapdustRelevance maxRelevance =
                MapdustRelevance.getMapdustRelevance(value);
        return maxRelevance;
    }

    /**
     * Add the given filter to the current dialog window.
     *
     * @param filter The <code>FilterCheckBox</code> object
     */
    private void add(FilterCheckBox filter) {
        add(filter.getChbFilter());
        add(filter.getLblFilter());
    }

    /**
     * Add the given array of filters to the current dialog window.
     *
     * @param filters The array of <code>FilterCheckBox</code> objects
     */
    private void add(FilterCheckBox[] filters) {
        for (FilterCheckBox filter : filters) {
            add(filter.getChbFilter());
            add(filter.getLblFilter());
        }
    }

}
