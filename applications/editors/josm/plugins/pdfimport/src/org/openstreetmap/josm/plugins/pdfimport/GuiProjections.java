// License: GPL. For details, see LICENSE file.

package org.openstreetmap.josm.plugins.pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.preferences.projection.CodeProjectionChoice;
import org.openstreetmap.josm.gui.preferences.projection.CustomProjectionChoice;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionChoice;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionPreference;
import org.openstreetmap.josm.gui.preferences.projection.SingleProjectionChoice;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

public class GuiProjections {
    /*
     * provide a component let the user select a projection
     * TODO: allow preferences for sub-projections like UTM, Gauss-Krüger, etc.
     * TODO: allow selection of projection by code (EPSG)
     * TODO: allow selection of custom projection
     */
    private GuiPanel panel = null;
    private Chooser chooser = null;
    private JLabel pCode = null;
    private JLabel pName = null;
    private JLabel pInfo = null;
    private Projection projection = null;

    private class Chooser extends JComboBox<ProjectionChoice> {
        /*
         * Component to choose a Projection
         */
        public Chooser() {
            setEditable(false);
            setToolTipText(tr("Projection of the PDF-Document"));
             Monitor monitor = new Monitor();
             for (ProjectionChoice p : ProjectionPreference.getProjectionChoices()) {
                 if ((p instanceof CodeProjectionChoice)) continue;    // can not handle this projection for now
                 if ((p instanceof CustomProjectionChoice)) continue;    // can not handle this projection for now
                 addItem(p);
            }
            addActionListener(monitor);
            setProjection (ProjectionRegistry.getProjection());
        }

        public void setProjection (Projection p) {
            /*
             * set current Projection to @p
             * update internal variables
             */
            if (p==null) return;    // better keep the old one
            projection = p;
            pName.setText(p.toString());
            pCode.setText(p.toCode());
            pInfo.setText(userHints(p));
            /*
             * find projectionChoice that matches current code
             */
            final String projectionCode = p.toCode();
            for (ProjectionChoice projectionChoice : ProjectionPreference.getProjectionChoices()) {
                for (String code : projectionChoice.allCodes()) {
                    if (code.equals(projectionCode)) {
                        setSelectedItem(projectionChoice);
                        return;    // stop searching
                    }
                }
            }
            /*
             * Not found ==> search in combobox
             */
            final String localId = "PdfImport:" + projectionCode;
            for (int i=getItemCount()-1; i>=0; i--) {
                ProjectionChoice projectionChoice = getItemAt(i);
                if (!(projectionChoice instanceof SingleProjectionChoice)) continue;
                if (localId.equals(projectionChoice.getId())) {
                    setSelectedItem(projectionChoice);
                    return;    // stop searching
                }
            }
            /*
             * Still not found ==> add it now
             */
            Logging.debug("New projection encountered");
            ProjectionChoice px = new SingleProjectionChoice(p.toString(), localId, projectionCode) ;
            addItem(px);
            setSelectedItem(px);
        }

        private class Monitor implements ActionListener {
            /*
             * (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             *
             * monitor user selection and set internal var accordingly
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                ProjectionChoice pc = (ProjectionChoice)((Chooser) e.getSource()).getSelectedItem();
                setProjection(pc.getProjection());
                } catch (Exception X) {
                }
            }
        }
    }

    public GuiProjections(){
        build();
    }

    public static String getDefault() {
        /*
         * provide default projection
         */
        return Config.getPref().get("projection.default");
    }

    public JPanel getPanel() {
        return panel;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection p) {
        chooser.setProjection(p);
    }

    private String userHints(Projection p) {
        /*
         * Provide some hints about projection @p
         */
        String s="";
        ProjectionBounds bd;
        try {
            bd=p.getWorldBoundsBoxEastNorth();
            s += String.format("(%3$.0f %4$.0f) : (%5$.0f %6$.0f)", bd.getCenter().east(),bd.getCenter().north(), bd.getMin().east(),bd.getMin().north(),bd.getMax().east(),bd.getMax().north());
        } catch (Exception e) {
            e.toString();
            // Leave it, if we cant get it
        }
        return s;
    }

    private void build() {
        pCode = new JLabel("code",SwingConstants.RIGHT);
        pName = new JLabel("Name",SwingConstants.RIGHT);
        pInfo = new JLabel("Info",SwingConstants.RIGHT);
        chooser = new Chooser();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(1, 1, 1, 4);
        c.anchor = GridBagConstraints.LINE_END;

        panel = new GuiPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Projection:"),SwingConstants.RIGHT),c);
        panel.add(pCode,c);
        c.weightx = 1.0; c.gridx = 2; panel.add(chooser,c);
        c.weightx = 0.0; c.gridy = 1; c.gridx = 0; c.gridwidth = 3; c.anchor = GridBagConstraints.LINE_END;
        panel.add(pInfo,c);
    }
}
