/*
 *      PrintDialog.java
 *      
 *      Copyright 2011 Kai Pastor
 *      
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *      
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 *      
 *      
 */

package org.openstreetmap.josm.plugins.print;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.*;

import java.text.ParseException;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.WindowGeometry;

/**
 * A print dialog with preview
 */
public class PrintDialog extends JDialog implements ActionListener {

    /**
     * The printer name
     */
    protected JTextField printerField;
    
    /**
     * The media format name
     */
    protected JTextField paperField;
    
    /**
     * The media orientation
     */
    protected JTextField orientationField;

    /**
     * The preview toggle checkbox
     */
    protected JCheckBox previewCheckBox;
    
    /**
     * The resolution in dpi for printing/preview
     */
    protected SpinnerNumberModel resolutionModel;
    
    /**
     * The page preview
     */
    protected PrintPreview printPreview;
    
    /**
     * The printer job
     */
    protected PrinterJob job;
    
    /**
     * The custom printer job attributes
     */
    PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
    
    /** 
     * Create a new print dialog
     * 
     * @param parent the parent component
     */
    public PrintDialog(Component parent) {
        super(JOptionPane.getFrameForComponent(parent), tr("Print the Map"), ModalityType.DOCUMENT_MODAL);
        job = PrinterJob.getPrinterJob();
        job.setJobName("JOSM Map");
        build();
        updateFields();
        pack();
        //setMinimumSize(getPreferredSize());
        setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
    }
    
    /**
     * Show or hide the dialog
     * 
     * Set the dialog size to reasonable values.
     * 
     * @param visible a flag indication the visibility of the dialog
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            // Make the dialog at most as large as the parent JOSM window
            // Have to take window decorations into account or the windows will
            // be too large
            Insets i = this.getParent().getInsets();
            Dimension p = this.getParent().getSize();
            p = new Dimension(Math.min(p.width-i.left-i.right, 1000),
                    Math.min(p.height-i.top-i.bottom, 700));
            new WindowGeometry(
                    getClass().getName() + ".geometry",
                    WindowGeometry.centerInWindow(
                            getParent(),
                            p
                    )
            ).applySafe(this);
        } else if (!visible && isShowing()){
            new WindowGeometry(this).remember(getClass().getName() + ".geometry");
            Main.pref.put("print.preview.enabled",previewCheckBox.isSelected());
        }
        super.setVisible(visible);
    }

    /**
     * Construct the dialog from components
     */
    public void build() {
        setLayout(new GridBagLayout());
        final GBC std = GBC.std().insets(0,5,5,0);
        std.fill = GBC.HORIZONTAL;
        final GBC twocolumns  = GBC.std().insets(0,5,5,0).span(2).fill(GBC.HORIZONTAL);
        
        JLabel caption;
        
        int row = 0;
        caption = new JLabel(tr("Printer:"));
        add(caption, std.grid(2, row));
        printerField = new JTextField();
        printerField.setEditable(false);
        add(printerField, std.grid(3, row));

        row++;
        caption = new JLabel(tr("Paper:"));
        add(caption, std.grid(2, row));
        paperField = new JTextField();
        paperField.setEditable(false);
        add(paperField, std.grid(3, row));

        row++;
        caption = new JLabel(tr("Orientation:"));
        add(caption, std.grid(2, row));
        orientationField = new JTextField();
        orientationField.setEditable(false);
        add(orientationField, std.grid(3, row));

        row++;
        JButton printerButton = new JButton(tr("Printer settings..."));
        printerButton.setActionCommand("printer-dialog");
        printerButton.addActionListener(this);
        add(printerButton, twocolumns.grid(2, row));

        row++;
        add(GBC.glue(5,10), GBC.std(1,row).fill(GBC.VERTICAL));
        
        row++;
        caption = new JLabel(tr("Resolution (dpi):"));
        add(caption, std.grid(2, row));
        resolutionModel = new SpinnerNumberModel(
          (int)Main.pref.getInteger("print.resolution.dpi", PrintPlugin.DEF_RESOLUTION_DPI),
          30, 1200, 10 );
        final JSpinner resolutionField = new JSpinner(resolutionModel);
        resolutionField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            resolutionField.commitEdit();
                            Main.pref.put("print.resolution.dpi",resolutionModel.getNumber().toString());
                            printPreview.repaint();
                        }
                        catch (ParseException pe) {
                            ; // NOP
                        }
                    }
                });
            }
        });
        add(resolutionField, std.grid(3, row));

        row++;
        caption = new JLabel(tr("Attribution:"));
        add(caption, std.grid(2, row));

        row++;
        final JTextField attributionField = new JTextField();
        attributionField.setText(Main.pref.get("print.attribution", PrintPlugin.DEF_ATTRIBUTION));
        attributionField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Main.pref.put("print.attribution", attributionField.getText());
                        printPreview.repaint();
                    }
                });
            }
            public void removeUpdate(DocumentEvent evt) {
                this.insertUpdate(evt);
            }
            public void changedUpdate(DocumentEvent evt) {
                ; // NOP
            }
        });
        add(attributionField, twocolumns.grid(2, row));

        row++;
        add(GBC.glue(5,10), GBC.std(1,row).fill(GBC.VERTICAL));
        
        row++;
        previewCheckBox = new JCheckBox(tr("Map Preview"));
        previewCheckBox.setSelected(Main.pref.getBoolean("print.preview.enabled",false));
        previewCheckBox.setActionCommand("toggle-preview");
        previewCheckBox.addActionListener(this);
        add(previewCheckBox, twocolumns.grid(2, row));

        row++;
        JButton zoomInButton = new JButton(tr("Zoom In"));
        zoomInButton.setActionCommand("zoom-in");
        zoomInButton.addActionListener(this);
        add(zoomInButton, twocolumns.grid(2, row));

        row++;
        JButton zoomOutButton = new JButton(tr("Zoom Out"));
        zoomOutButton.setActionCommand("zoom-out");
        zoomOutButton.addActionListener(this);
        add(zoomOutButton, twocolumns.grid(2, row));
        
        row++;
        JButton zoomToPageButton = new JButton(tr("Zoom To Page"));
        zoomToPageButton.setActionCommand("zoom-to-page");
        zoomToPageButton.addActionListener(this);
        add(zoomToPageButton, twocolumns.grid(2, row));
        
        row++;
        JButton zoomToActualSize = new JButton(tr("Zoom To Actual Size"));
        zoomToActualSize.setActionCommand("zoom-to-actual-size");
        zoomToActualSize.addActionListener(this);
        add(zoomToActualSize, twocolumns.grid(2, row));
        
        printPreview = new PrintPreview();
        if (previewCheckBox.isSelected()) {
            printPreview.setPrintable(new PrintableMapView());
        }
        JScrollPane previewPane = new JScrollPane(printPreview, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        previewPane.setPreferredSize(Main.main != null ? Main.main.map.mapView.getSize() : new Dimension(210,297));
        add(previewPane, GBC.std(0,0).span(1, GBC.RELATIVE).fill().weight(5.0,5.0));

        row++;
        JPanel actionPanel = new JPanel();
        JButton printButton = new JButton(tr("Print"));
        printButton.setActionCommand("print");
        printButton.addActionListener(this);
        actionPanel.add(printButton);
        JButton cancelButton = new JButton(tr("Cancel"));
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        actionPanel.add(cancelButton);
        add(actionPanel, GBC.std(0,row).insets(5,5,5,5).span(GBC.REMAINDER).fill(GBC.HORIZONTAL));
    }
    
    /**
     * Update the dialog fields from the underlying model
     */
    protected void updateFields() {
        PrintService service = job.getPrintService();
        if (service == null) {
            printerField.setText("-");
            paperField.setText("-");
            orientationField.setText("-");
        }
        else {
            printerField.setText(service.getName());
            if (! attrs.containsKey(Media.class)) {
                attrs.add((Attribute) service.getDefaultAttributeValue(Media.class));
            } 
            if (attrs.containsKey(Media.class)) {
                paperField.setText(attrs.get(Media.class).toString());
            }

            if (! attrs.containsKey(OrientationRequested.class)) {
                attrs.add((Attribute) service.getDefaultAttributeValue(OrientationRequested.class));
            }
            if (attrs.containsKey(OrientationRequested.class)) {
                orientationField.setText(attrs.get(OrientationRequested.class).toString());
            }

            if (! attrs.containsKey(MediaPrintableArea.class)) {
                PageFormat pf = job.defaultPage();
                attrs.add(new MediaPrintableArea(
                  (float)pf.getImageableX()/72,(float)pf.getImageableY()/72,
                  (float)pf.getImageableWidth()/72,(float)pf.getImageableHeight()/72,
                  MediaPrintableArea.INCH) );
            }
            
            PageFormat pf = job.getPageFormat(attrs);
            printPreview.setPageFormat(pf);
        }
    }
    
    /**
     * Handle user input
     * 
     * @param e an ActionEvent with one of the known commands
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("printer-dialog")) {
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService svc = PrintServiceLookup.lookupDefaultPrintService();
            if (job.printDialog(attrs)) {
                updateFields();
            }
        }
        else if (cmd.equals("toggle-preview")) {
            Main.pref.put("print.preview.enabled", previewCheckBox.isSelected());
            if (previewCheckBox.isSelected() == true) {
                printPreview.setPrintable(new PrintableMapView());
            }
            else {
                printPreview.setPrintable(null);
            }
        }
        else if (cmd.equals("zoom-in")) {
            printPreview.zoomIn();
        }
        else if (cmd.equals("zoom-out")) {
            printPreview.zoomOut();
        }
        else if (cmd.equals("zoom-to-page")) {
            printPreview.zoomToPage();
        }
        else if (cmd.equals("zoom-to-actual-size")) {
            printPreview.setZoom(1.0);
        }
        else if (cmd.equals("print")) {
            try {
                job.setPrintable(new PrintableMapView());
                job.print(attrs);
            }
            catch (PrinterAbortException ex) {
                String msg = ex.getLocalizedMessage();
                if (msg.length() == 0) {
                    msg = tr("Printing has been cancelled.");
                }
                JOptionPane.showMessageDialog(Main.main.parent, msg,
                  tr("Printing stopped"),
                  JOptionPane.WARNING_MESSAGE);
            }
            catch (PrinterException ex) {
                String msg = ex.getLocalizedMessage();
                if (msg.length() == 0) {
                    msg = tr("Printing has failed.");
                }
                JOptionPane.showMessageDialog(Main.main.parent, msg,
                  tr("Printing stopped"),
                  JOptionPane.ERROR_MESSAGE);
            }
            dispose();
        }
        else if (cmd.equals("cancel")) {
            dispose();
        }
    }
    
}

