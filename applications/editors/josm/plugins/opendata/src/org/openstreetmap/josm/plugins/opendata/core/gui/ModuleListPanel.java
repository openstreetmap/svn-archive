// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.tools.OpenBrowser;

/**
 * TODO
 *
 */
public class ModuleListPanel extends VerticallyScrollablePanel{
    private ModulePreferencesModel model;

    public ModuleListPanel() {
        model = new ModulePreferencesModel();
        setLayout(new GridBagLayout());
    }

    public ModuleListPanel(ModulePreferencesModel model) {
        this.model = model;
        setLayout(new GridBagLayout());
    }

    protected String formatModuleRemoteVersion(ModuleInformation pi) {
        StringBuilder sb = new StringBuilder();
        if (pi.version == null || pi.version.trim().equals("")) {
            sb.append(tr("unknown"));
        } else {
            sb.append(pi.version);
        }
        return sb.toString();
    }

    protected String formatModuleLocalVersion(ModuleInformation pi) {
        if (pi == null) return tr("unknown");
        if (pi.localversion == null || pi.localversion.trim().equals(""))
            return tr("unknown");
        return pi.localversion;
    }

    protected String formatCheckboxTooltipText(ModuleInformation pi) {
        if (pi == null) return "";
        if (pi.downloadlink == null)
            return tr("Module bundled with opendata plugin");
        else
            return pi.downloadlink;
    }

    public void displayEmptyModuleListInformation() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(40,0,40,0);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        HtmlPanel hint = new HtmlPanel();
        hint.setText(
                "<html>"
                + tr("Please click on <strong>Download list</strong> to download and display a list of available modules.")
                + "</html>"
        );
        add(hint, gbc);
    }

    public void refreshView() {
        List<ModuleInformation> displayedModules = model.getDisplayedModules();
        removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        if (displayedModules.isEmpty()) {
            displayEmptyModuleListInformation();
            return;
        }

        int row = -1;
        for (final ModuleInformation pi : displayedModules) {
            boolean selected = model.isSelectedModule(pi.getName());
            String remoteversion = formatModuleRemoteVersion(pi);
            String localversion = formatModuleLocalVersion(model.getModuleInformation(pi.getName()));

            final JCheckBox cbModule = new JCheckBox();
            cbModule.setSelected(selected);
            cbModule.setToolTipText(formatCheckboxTooltipText(pi));
            cbModule.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    model.setModuleSelected(pi.getName(), cbModule.isSelected());
                }
            });
            JLabel lblModule = new JLabel(
                    tr("{0}: Version {1} (local: {2})", pi.getName(), remoteversion, localversion),
                    pi.getScaledIcon(),
                    SwingConstants.LEFT);

            gbc.gridx = 0;
            gbc.gridy = ++row;
            gbc.insets = new Insets(5,5,0,5);
            gbc.weighty = 0.0;
            gbc.weightx = 0.0;
            add(cbModule, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            add(lblModule, gbc);

            HtmlPanel description = new HtmlPanel();
            description.setText(pi.getDescriptionAsHtml());
            description.getEditorPane().addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if(e.getEventType() == EventType.ACTIVATED) {
                        OpenBrowser.displayUrl(e.getURL().toString());
                    }
                }
            });

            gbc.gridx = 1;
            gbc.gridy = ++row;
            gbc.insets = new Insets(3,25,5,5);
            gbc.weighty = 1.0;
            add(description, gbc);
        }
        revalidate();
        repaint();
    }
}
