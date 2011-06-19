package org.openstreetmap.josm.plugins.turnlanes.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.plugins.turnlanes.model.Issue;
import org.openstreetmap.josm.plugins.turnlanes.model.Validator;

class ValidationPanel extends JPanel {
    private static final long serialVersionUID = -1585778734201458665L;
    
    private static final String[] COLUMN_NAMES = {
        tr("Description"), tr("Type"), tr("Quick-Fix")
    };
    
    private final Action refreshAction = new JosmAction(tr("Refresh"), "dialogs/refresh",
        tr("Revalidate all turnlanes-relations."), null, false) {
        private static final long serialVersionUID = -8110599654128234810L;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            setIssues(new Validator().validate(Main.main.getCurrentDataSet()));
        }
    };
    
    private final Action fixAction = new JosmAction(tr("Fix"), "dialogs/fix", tr("Automatically fixes the issue."), null,
        false) {
        private static final long serialVersionUID = -8110599654128234810L;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selected.getQuickFix().perform()) {
                final int i = issues.indexOf(selected);
                issueModel.removeRow(i);
                issues.remove(i);
            }
        }
    };
    
    private final Action selectAction = new JosmAction(tr("Select"), "dialogs/select",
        tr("Selects the offending relation."), null, false) {
        private static final long serialVersionUID = -8110599654128234810L;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selected.getRelation() == null) {
                Main.main.getCurrentDataSet().setSelected(selected.getPrimitives());
            } else {
                Main.main.getCurrentDataSet().setSelected(selected.getRelation());
            }
        }
    };
    
    private final SideButton refreshButton = new SideButton(refreshAction);
    private final SideButton fixButton = new SideButton(fixAction);
    private final SideButton selectButton = new SideButton(selectAction);
    
    private final DefaultTableModel issueModel = new DefaultTableModel(COLUMN_NAMES, 0);
    private final List<Issue> issues = new ArrayList<Issue>();
    private final JTable issueTable = new JTable(issueModel) {
        private static final long serialVersionUID = 6323348290180585298L;
        
        public boolean isCellEditable(int row, int column) {
            return false;
        };
    };
    
    private Issue selected;
    
    public ValidationPanel() {
        super(new BorderLayout(4, 4));
        
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 4, 4));
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(fixButton);
        buttonPanel.add(selectButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(issueTable), BorderLayout.CENTER);
        
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issueTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                final int i = issueTable.getSelectedRow();
                final Issue issue = i >= 0 ? issues.get(i) : null;
                
                setSelected(issue);
            }
        });
        
        setSelected(null);
    }
    
    private void setIssues(List<Issue> issues) {
        issueModel.setRowCount(0);
        this.issues.clear();
        
        for (Issue i : issues) {
            final String[] row = {
                i.getDescription(), //
                i.getRelation() == null ? tr("(none)") : i.getRelation().get("type"), //
                i.getQuickFix().getDescription()
            };
            issueModel.addRow(row);
            this.issues.add(i);
        }
    }
    
    private void setSelected(Issue selected) {
        this.selected = selected;
        
        if (selected == null) {
            fixButton.setEnabled(false);
            selectButton.setEnabled(false);
        } else {
            fixButton.setEnabled(selected.getQuickFix() != Issue.QuickFix.NONE);
            selectButton.setEnabled(true);
        }
    }
}
