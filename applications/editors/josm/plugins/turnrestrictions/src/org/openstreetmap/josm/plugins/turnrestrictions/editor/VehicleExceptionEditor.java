package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * VehicleExceptionEditor is UI widget for editing exceptions to a turn restriction
 * based on vehicle types.
 *  
 */
public class VehicleExceptionEditor extends JPanel implements Observer{
    //static private final Logger logger = Logger.getLogger(VehicleExceptionEditor.class.getName());
    
    private TurnRestrictionEditorModel model;
    private JCheckBox cbPsv;
    private JCheckBox cbBicyle;
    private JCheckBox cbHgv;
    private JCheckBox cbMotorcar;
    private JTextField tfNonStandardValue;
    private ButtonGroup bgStandardOrNonStandard;
    private JRadioButton rbStandardException;
    private JRadioButton rbNonStandardException;
    private JPanel pnlStandard;
    private JPanel pnlNonStandard;
    private ExceptValueModel exceptValue = new ExceptValueModel();
    
    private StandardVehicleTypeChangeListener svtChangeListener;
    
    private JPanel buildMessagePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        HtmlPanel msg = new HtmlPanel();
        pnl.add(msg, BorderLayout.CENTER);
        msg.setText(
                "<html><body>"
                + tr("Select the vehicle types this turn restriction is <strong>not</strong> applicable for.")
                + "</body></html>"
        );
        return pnl;
    }
    
    private JPanel buildStandardInputPanel() {
        if (pnlStandard != null)
            return pnlStandard;
        
        svtChangeListener = new StandardVehicleTypeChangeListener();
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        
        pnlStandard = new JPanel(new GridBagLayout());
        JLabel lbl;
        cbPsv = new JCheckBox();
        cbPsv.addItemListener(svtChangeListener);
        lbl = new JLabel();
        lbl.setText(tr("Public Service Vehicles"));
        lbl.setToolTipText(tr("Public service vehicles like buses, tramways, etc."));
        lbl.setIcon(ImageProvider.get("vehicle", "psv"));
        
        gc.weightx = 0.0;
        pnlStandard.add(cbPsv, gc);
        gc.weightx = 1.0;
        gc.gridx++;
        pnlStandard.add(lbl, gc);
        
        cbHgv = new JCheckBox();
        cbHgv.addItemListener(svtChangeListener);
        lbl = new JLabel();
        lbl.setText(tr("Heavy Goods Vehicles"));
        lbl.setIcon(ImageProvider.get("vehicle", "hgv"));

        gc.weightx = 0.0;
        gc.gridx++;
        pnlStandard.add(cbHgv, gc);
        gc.weightx = 1.0;
        gc.gridx++;
        pnlStandard.add(lbl, gc);

        cbMotorcar = new JCheckBox();
        cbMotorcar.addItemListener(svtChangeListener);
        lbl = new JLabel();
        lbl.setText(tr("Motorcars"));
        lbl.setIcon(ImageProvider.get("vehicle", "motorcar"));
        
        gc.weightx = 0.0;
        gc.gridx = 0;
        gc.gridy = 1;
        pnlStandard.add(cbMotorcar, gc);
        gc.weightx = 1.0;
        gc.gridx++;
        pnlStandard.add(lbl, gc);
        
        cbBicyle = new JCheckBox();
        cbBicyle.addItemListener(svtChangeListener);
        lbl = new JLabel();
        lbl.setText(tr("Bicycles"));
        lbl.setIcon(ImageProvider.get("vehicle", "bicycle"));
        

        gc.weightx = 0.0;
        gc.gridx++;
        pnlStandard.add(cbBicyle, gc);
        gc.weightx = 1.0;
        gc.gridx++;
        pnlStandard.add(lbl, gc);
        
        return pnlStandard;
    }
    
    private JPanel buildNonStandardInputPanel() {
        if (pnlNonStandard != null)
            return pnlNonStandard;
        pnlNonStandard = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.0;
        gc.insets = new Insets(0, 0, 4, 0);
        gc.gridx = 0;
        gc.gridy = 0;
        
        pnlNonStandard.add(new JLabel(tr("Value:")), gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        pnlNonStandard.add(tfNonStandardValue = new JTextField(), gc);
        SelectAllOnFocusGainedDecorator.decorate(tfNonStandardValue);
        
        NonStandardVehicleTypesHandler inputChangedHandler = new NonStandardVehicleTypesHandler();
        tfNonStandardValue.addActionListener(inputChangedHandler);
        tfNonStandardValue.addFocusListener(inputChangedHandler);
        return pnlNonStandard;
    }
        
    /**
     * Builds the UI for entering standard values 
     */
    protected void buildStandard() {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        add(buildMessagePanel(), gc);
        
        gc.gridy=1;
        add(buildStandardInputPanel(), gc);     
    }
    
    /**
     * Builds the UI for entering either standard or non-standard values 
     */
    protected void buildNonStandard() {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        add(buildMessagePanel(), gc);
                
        gc.gridx=0;
        gc.gridy=1;
        gc.insets = new Insets(0,0,0,0);
        add(rbStandardException = new JRadioButton(tr("Use standard exceptions")), gc);

        gc.gridx=0;
        gc.gridy=2;
        gc.insets = new Insets(0, 20, 0,0);
        add(buildStandardInputPanel(), gc);

        gc.gridx=0;
        gc.gridy=3;
        gc.insets = new Insets(0,0,0,0);
        add(rbNonStandardException = new JRadioButton(tr("Use non-standard exceptions")), gc);

        gc.gridx=0;
        gc.gridy=4;
        gc.insets = new Insets(0, 20, 0,0);
        add(buildNonStandardInputPanel(), gc);
        
        bgStandardOrNonStandard = new ButtonGroup();
        bgStandardOrNonStandard.add(rbNonStandardException);
        bgStandardOrNonStandard.add(rbStandardException);
        
        StandardNonStandardChangeHandler changeHandler = new StandardNonStandardChangeHandler();
        rbNonStandardException.addItemListener(changeHandler);
        rbStandardException.addItemListener(changeHandler);
    }
    
    protected void build() {
        removeAll();
        buildNonStandardInputPanel();
        buildStandardInputPanel();
        if (exceptValue.isStandard()){
            buildStandard();
        } else {
            buildNonStandard();
        }
        init();
        invalidate();
    }
    
    protected void init() {
    	try {
    		// temporarily disable the checkbox listeners while initializing the
    		// checkboxes with the input value
    		this.svtChangeListener.setEnabled(false);
	        cbPsv.setSelected(exceptValue.isVehicleException("psv"));
	        cbBicyle.setSelected(exceptValue.isVehicleException("bicycle"));
	        cbMotorcar.setSelected(exceptValue.isVehicleException("motorcar"));
	        cbHgv.setSelected(exceptValue.isVehicleException("hgv"));
    	} finally {
    		this.svtChangeListener.setEnabled(true);
    	}
        if (!exceptValue.isStandard()){
            rbNonStandardException.setSelected(true);
            tfNonStandardValue.setText(exceptValue.getValue());
            setEnabledNonStandardInputPanel(true);
            setEnabledStandardInputPanel(false);
        } else {
            setEnabledNonStandardInputPanel(false);
            setEnabledStandardInputPanel(true);
        }
    }
    
    protected void setEnabledStandardInputPanel(boolean enabled) {
        for (Component c: pnlStandard.getComponents()){
            c.setEnabled(enabled);
        }
    }
    
    protected void setEnabledNonStandardInputPanel(boolean enabled) {
        for (Component c: pnlNonStandard.getComponents()){
            c.setEnabled(enabled);
        }
    }

    
    /**
     * Creates the editor 
     * 
     * @param model the editor model. Must not be null.
     * @throws IllegalArgumentException thrown if {@code model} is null
     */
    public VehicleExceptionEditor(TurnRestrictionEditorModel model) throws IllegalArgumentException {
        CheckParameterUtil.ensureParameterNotNull(model, "model");
        this.model = model;
        build();
        model.addObserver(this);
    }
    
    /* ------------------------------------------------------------------------------------ */
    /* interface Observer                                                                   */
    /* ------------------------------------------------------------------------------------ */
    public void update(Observable o, Object arg) {
        if (!this.exceptValue.equals(model.getExcept())) {
            this.exceptValue = model.getExcept();
            build();
        }
    }

    /* ------------------------------------------------------------------------------------ */
    /* inner classes                                                                        */
    /* ------------------------------------------------------------------------------------ */
    class StandardNonStandardChangeHandler implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (rbNonStandardException.isSelected()){
                setEnabledNonStandardInputPanel(true);
                setEnabledStandardInputPanel(false);
                exceptValue.setStandard(false);
            } else {
                setEnabledNonStandardInputPanel(false);
                setEnabledStandardInputPanel(true);
                exceptValue.setStandard(true);
            }
            model.setExcept(exceptValue);
        }
    }
    
    class StandardVehicleTypeChangeListener implements ItemListener {
    	private boolean enabled = true;
    	
    	public void setEnabled(boolean enabled){
    		this.enabled = enabled;
    	}
    	
        public void itemStateChanged(ItemEvent e) {        	
        	if (!enabled) return;
            exceptValue.setVehicleException("bicycle", cbBicyle.isSelected());
            exceptValue.setVehicleException("hgv", cbHgv.isSelected());
            exceptValue.setVehicleException("psv", cbPsv.isSelected());
            exceptValue.setVehicleException("motorcar", cbMotorcar.isSelected());            
            model.setExcept(exceptValue);
        }
    }
    
    class NonStandardVehicleTypesHandler implements ActionListener, FocusListener {
        public void persist() {
            exceptValue.setValue(tfNonStandardValue.getText());
            model.setExcept(exceptValue);
        }
        
        public void focusGained(FocusEvent e) {}
        public void focusLost(FocusEvent e) {
            persist();
        }

        public void actionPerformed(ActionEvent e) {
            persist();          
        }
    }
}
