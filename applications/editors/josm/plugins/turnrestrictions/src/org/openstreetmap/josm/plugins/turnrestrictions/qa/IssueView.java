package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * An IssueView is a view on an individual {@see Issue}.
 */
public class IssueView extends JPanel{

    private HtmlPanel pnlMessage;
    private JPanel pnlActions;
    private Issue issue;
    private JLabel lblIcon;
    private StyleSheet styleSheet;
    
     /**
     * Builds the style sheet used in the internal help browser
     *
     * @return the style sheet
     */
    protected void initStyleSheet(HtmlPanel view) {
        StyleSheet ss = ((HTMLEditorKit)view.getEditorPane().getEditorKit()).getStyleSheet();
        ss.addRule("em {font-style: italic}");
        ss.addRule("tt {font-family: Courier New}");
        ss.addRule(".object-name {background-color:rgb(240,240,240); color: blue;}");
    }
    
    protected void build() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        
        // add the icon for the severity 
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.VERTICAL;
        gc.gridheight = 2;
        gc.weightx = 0.0;
        gc.weighty = 1.0;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(2,2,2,2);
        add(lblIcon = new JLabel(), gc);
        lblIcon.setVerticalAlignment(SwingConstants.TOP);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

        // add the html panel with the issue description 
        gc.insets = new Insets(0,0,0,0);
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 1;
        gc.gridy = 0;
        gc.gridheight = 1;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        add(pnlMessage = new HtmlPanel(), gc);
        initStyleSheet(pnlMessage);
        pnlMessage.setBackground(Color.white);
        pnlMessage.setText("<html><body>" + issue.getText() + "</html></bod>");

        
        // if there are any actions available to resolve the issue, add a panel with action buttons 
        if (!issue.getActions().isEmpty()) {
            pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlActions.setBackground(Color.WHITE);
            for (Action action: issue.getActions()){
                JButton btn = new JButton(action);
                pnlActions.add(btn);                
            }
            
            gc.gridx = 1;           
            gc.gridy = 1;           
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weighty = 0.0;
            add(pnlActions,gc);
        }   
        
        // set the severity icon 
        switch(issue.getSeverity()){
        case WARNING: 
            lblIcon.setIcon(ImageProvider.get("warning-small"));
            break;
        case ERROR:
            lblIcon.setIcon(ImageProvider.get("error"));
            break;
        }       
    }
    
    /**
     * Creates an issue view for an issue.
     * 
     * @param issue the issue. Must not be null.
     * @throws IllegalArgumentException thrown if issue is null.
     */
    public IssueView(Issue issue) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(issue, "issue");
        this.issue = issue;
        build();        
    }

    @Override
    public Dimension getMinimumSize() {
        return super.getPreferredSize();
    }
}
