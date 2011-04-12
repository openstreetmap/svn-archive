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
package org.openstreetmap.josm.plugins.mapdust.gui.component.util;


import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import org.openstreetmap.josm.plugins.mapdust.gui.component.model.ActionListModel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.model.BugsListModel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.model.TypesListModel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.renderer.ActionListCellRenderer;
import org.openstreetmap.josm.plugins.mapdust.gui.component.renderer.BugListCellRenderer;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustAction;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * Helper class, used for creating the GUI elements.
 *
 * @author Bea
 * @version $Revision$
 */
public class ComponentUtil {

    /**
     * Creates a <code>JLabel</code> object with the given properties.
     *
     * @param text The text of the label
     * @param font The font of the label
     * @param bounds The bounds of the element
     * @param color The color of the element
     * @return A <code>JLabel</code> object
     */
    public static JLabel createJLabel(String text, Font font, Rectangle bounds,
            Color color) {
        JLabel jLabel = new JLabel();
        if (bounds != null) {
            jLabel.setBounds(bounds);
        }
        jLabel.setText(text);
        jLabel.setFont(font);
        if (color != null) {
            jLabel.setForeground(color);
        }
        jLabel.setVerticalTextPosition(SwingConstants.TOP);
        return jLabel;
    }


    /**
     * Creates a <code>JLabel</code> object with the given properties.
     *
     * @param text The text of the label
     * @param iconName The name of the label icon
     * @param bounds The dimension of the label
     * @return A <code>JLabel</code> object
     */
    public static JLabel createJLabel(String text, String iconName,
            Rectangle bounds) {
        JLabel jLabel = new JLabel(text, ImageProvider.get(iconName),
                SwingConstants.LEFT);
        jLabel.setBounds(bounds);
        jLabel.setFont(new Font("Times New Roman", Font.BOLD, 12));
        return jLabel;
    }

    /**
     * Creates a new <code>JButton</code> object with the given properties.
     *
     * @param text The text which appears on the button
     * @param bounds The position and dimension attributes
     * @param action The action of the button
     * @return A <code>JButton</code> object
     */
    public static JButton createJButton(String text, Rectangle bounds,
            Action action) {
        JButton jButton = null;
        if (action != null) {
            jButton = new JButton(action);
        } else {
            jButton = new JButton();
        }
        jButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        jButton.setText(text);
        jButton.setBounds(bounds);
        return jButton;
    }

    /**
     * Creates a new <code>JToggleButton</code> object with the given
     * properties.
     *
     * @param text The text of the button
     * @param toolTipText The tool tip text
     * @param iconName The name of the icon
     * @param action The action of the button
     * @return A <code>JToggleButton</code> object
     */
    public static JToggleButton createJButton(String text, String toolTipText,
            String iconName, AbstractAction action) {
        JToggleButton jButton = new JToggleButton(tr(text));
        if (action != null) {
            jButton.setAction(action);
        }
        if (toolTipText != null) {
            jButton.setToolTipText(tr(toolTipText));
        }
        if (iconName != null) {
            jButton.setIcon(ImageProvider.get(iconName));
        } else {
            jButton.setText(tr(text));
            jButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        }
        return jButton;
    }

    /**
     * Creates a new <code>JTextField</code> object with the given properties.
     *
     * @param bounds The position and dimension attributes
     * @return A <code>JTextField</code> object
     */
    public static JTextField createJTextField(Rectangle bounds) {
        JTextField txtField = new JTextField();
        txtField.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        txtField.setBounds(bounds);
        return txtField;
    }

    /**
     * Creates a new <code>JScrollPane</code> object with the given properties.
     *
     * @param component The component of the scroll pane
     * @param bounds The dimension of the component
     * @param backgroundColor The color of the background
     * @param noBorder if true then the scroll pane is without border otherwise
     * the scroll pane will have also a border
     * @param visible if true then the scroll pane will be visible otherwise the
     * scroll pane will be invisible
     * @return A <code>JScrollPane</code> object
     */
    public static JScrollPane createJScrollPane(Component component,
            Rectangle bounds, Color backgroundColor, boolean noBorder,
            boolean visible) {
        JScrollPane pane = new JScrollPane();
        if (bounds != null) {
            pane.setBounds(bounds);
        }
        pane.setBackground(backgroundColor);
        pane.setViewportView(component);
        if (noBorder) {
            pane.setBorder(null);
        }
        if (!visible) {
            pane.setVisible(false);
        }
        return pane;
    }

    /**
     * Creates a new <code>JScrollPane</code> object with the given properties.
     *
     * @param list The list of the scroll pane
     * @return A <code>JScrollPane</code> object
     */
    public static JScrollPane createJScrollPane(JList list) {
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(list);
        jScrollPane.setAutoscrolls(true);
        return jScrollPane;
    }

    /**
     * Creates a new <code>JList</code> object with the given properties.
     *
     * @param list The list of <code>MapdustAction</code> objects
     * @return A <code>JList</code> object
     */
    public static JList createJList(List<MapdustAction> list) {
        final JList jList = new JList(new ActionListModel(list));
        jList.setBorder(new LineBorder(Color.black, 1, false));
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setCellRenderer(new ActionListCellRenderer());
        jList.setAutoscrolls(true);
        return jList;
    }

    /**
     * Creates a new <code>JList</code> object with the given properties.
     *
     * @param bugsList The list of <code>MapdustBug</code> objects
     * @param menu The menu
     * @return A <code>JList</code>
     */
    public static JList createJList(List<MapdustBug> bugsList,
            final JPopupMenu menu) {
        final JList jList = new JList(new BugsListModel(bugsList));
        jList.setBorder(new LineBorder(Color.black, 1, false));
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setCellRenderer(new BugListCellRenderer());
        jList.setAutoscrolls(true);
        return jList;
    }

    /**
     * Creates a new <code>JMenuItem</code> object with the given properties.
     *
     * @param action the Action of the menu item
     * @param title The title of the menu item
     * @param iconPath The path of the icon
     * @return A <code>JMenuItem</code> object
     */
    public static JMenuItem createJMenuItem(Action action, String title,
            String iconPath) {
        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setText(title);
        menuItem.setIcon(ImageProvider.get(iconPath));
        menuItem.setFont(new Font("Times New Roman", Font.BOLD, 12));
        return menuItem;
    }

    /**
     * Creates a new <code>JTextPane</code> object with the given properties.
     *
     * @param text The text which will appear in the text pane
     * @param backgroundColor The background color
     * @return A <code>JTextPane</code> object
     */
    public static JTextPane createJTextPane(String text, Color backgroundColor) {
        JTextPane jTextPane = new JTextPane();
        jTextPane.setBorder(null);
        jTextPane.setEditable(false);
        jTextPane.setBackground(backgroundColor);
        jTextPane.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        if (text != null) {
            jTextPane.setText(text);
        }
        jTextPane.setVerifyInputWhenFocusTarget(false);
        jTextPane.setAutoscrolls(false);
        return jTextPane;
    }

    /**
     * Creates a new <code>JComboBox</code> object with the given properties.
     *
     * @param bounds The dimension of the combo box
     * @param renderer The <code>ListCellRenderer</code> object
     * @param backgroundColor The background color
     * @return A <code>JComboBox</code> object
     */
    public static JComboBox createJComboBox(Rectangle bounds,
            ListCellRenderer renderer, Color backgroundColor) {
        JComboBox jComboBox = new JComboBox(new TypesListModel());
        jComboBox.setSelectedIndex(0);
        jComboBox.setBackground(backgroundColor);
        jComboBox.setFont(new Font("Times New Roman", Font.BOLD, 12));
        jComboBox.setDoubleBuffered(false);
        jComboBox.setBorder(null);
        jComboBox.setBounds(bounds);
        if (renderer != null) {
            jComboBox.setRenderer(renderer);
            jComboBox.setMaximumRowCount(7);
        }
        return jComboBox;
    }

    /**
     * Creates a new <code>JCheckBox</code> object with the given properties.
     *
     * @param bounds The dimension of the check box
     * @return A <code>JCheckBox</code> object
     */
    public static JCheckBox createJCheckBox(Rectangle bounds) {
        JCheckBox jCheckBox = new JCheckBox();
        jCheckBox.setBounds(bounds);
        return jCheckBox;
    }

}
