// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PrimitiveRenderer;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

import relcontext.ChosenRelation;

/**
 * Opens a list of all relations with keyword search field. Choose selected relation.
 *
 * @author Zverik
 */
public class FindRelationAction extends JosmAction {
    protected ChosenRelation chRel;

    public FindRelationAction(ChosenRelation chRel) {
        super(tr("Find"), "relcontext/find", tr("Find a relation"),
                Shortcut.registerShortcut("reltoolbox:find", tr("Relation Toolbox: {0}", tr("Find a relation")),
                        KeyEvent.VK_F, Shortcut.ALT_CTRL), false);
        this.chRel = chRel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JPanel panel = new JPanel(new BorderLayout());
        final JTextField searchField = new JTextField();
        panel.add(searchField, BorderLayout.NORTH);
        final FindRelationListModel relationsData = new FindRelationListModel();
        final JList<Relation> relationsList = new JList<>(relationsData);
        relationsList.setSelectionModel(relationsData.getSelectionModel());
        relationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        relationsList.setCellRenderer(new PrimitiveRenderer());
        panel.add(new JScrollPane(relationsList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(400, 400));

        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            @Override
            public void selectInitialValue() {
                searchField.requestFocusInWindow();
            }
        };
        final JDialog dlg = optionPane.createDialog(MainApplication.getMainFrame(), tr("Find a relation"));
        dlg.setModalityType(ModalityType.DOCUMENT_MODAL);

        relationsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && !relationsList.isSelectionEmpty()) {
                    dlg.setVisible(false);
                    optionPane.setValue(JOptionPane.OK_OPTION);
                }
            }
        });

        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!relationsList.isSelectionEmpty()) {
                    dlg.setVisible(false);
                    optionPane.setValue(JOptionPane.OK_OPTION);
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateRelationData(relationsData, searchField.getText());
                    }
                });
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (shouldForward(e)) {
                    relationsList.dispatchEvent(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (shouldForward(e)) {
                    relationsList.dispatchEvent(e);
                }
            }

            private boolean shouldForward(KeyEvent e) {
                return e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
                        || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_UP
                        || e.getKeyCode() == KeyEvent.VK_HOME || e.getKeyCode() == KeyEvent.VK_END;
            }
        });

        updateRelationData(relationsData, null);
        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if (answer == null || answer == JOptionPane.UNINITIALIZED_VALUE
                || (answer instanceof Integer && (Integer) answer != JOptionPane.OK_OPTION))
            return;

        Relation r = relationsList.getSelectedValue();
        if (r != null) {
            chRel.set(r);
        }
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditDataSet() != null);
    }

    protected void updateRelationData(FindRelationListModel data, String filter) {
        String[] keywords = filter == null ? new String[0] : filter.split("\\s+");
        if (keywords.length > 0) {
            List<String> filteredKeywords = new ArrayList<>(keywords.length);
            for (String s : keywords) {
                if (s.length() > 0) {
                    filteredKeywords.add(s.trim().toLowerCase());
                }
            }
            keywords = filteredKeywords.toArray(new String[0]);
        }

        if (Logging.isDebugEnabled()) {
            Logging.debug("keywords.length = " + keywords.length);
            for (int i = 0; i < keywords.length; i++) {
                Logging.debug("keyword["+i+"] = " + keywords[i]);
            }
        }

        List<Relation> relations = new ArrayList<>();
        if (getLayerManager().getEditLayer() != null) {
            for (Relation r : getLayerManager().getEditLayer().data.getRelations()) {
                if (!r.isDeleted() && r.isVisible() && !r.isIncomplete()) {
                    boolean add = true;
                    for (int i = 0; i < keywords.length && add; i++) {
                        boolean ok = false;
                        if (String.valueOf(r.getPrimitiveId().getUniqueId()).contains(keywords[i])) {
                            ok = true;
                        } else {
                            for (String key : r.keySet()) {
                                if (key.contains(keywords[i]) || r.get(key).toLowerCase().contains(keywords[i])
                                        || tr(r.get(key)).toLowerCase().contains(keywords[i])) {
                                    ok = true;
                                    break;
                                }
                            }
                        }
                        if (!ok) {
                            add = false;
                        }
                    }
                    if (add) {
                        relations.add(r);
                    }
                }
            }
        }

        Collections.sort(relations, DefaultNameFormatter.getInstance().getRelationComparator());
        data.setRelations(relations);
    }

    /**
     * I admit, some of it was copypasted from {@link org.openstreetmap.josm.gui.dialogs.RelationListDialog.RelationListModel}.
     */
    protected static class FindRelationListModel extends AbstractListModel<Relation> {
        private final ArrayList<Relation> relations = new ArrayList<>();
        private DefaultListSelectionModel selectionModel;

        public FindRelationListModel(DefaultListSelectionModel selectionModel) {
            super();
            this.selectionModel = selectionModel;
        }

        public FindRelationListModel() {
            this(new DefaultListSelectionModel());
        }

        public DefaultListSelectionModel getSelectionModel() {
            return selectionModel;
        }

        @Override
        public int getSize() {
            return relations.size();
        }

        @Override
        public Relation getElementAt(int index) {
            return relations.get(index);
        }

        public void setRelations(Collection<Relation> relations) {
            int selectedIndex = selectionModel.getMinSelectionIndex();
            Relation sel = selectedIndex < 0 ? null : getElementAt(selectedIndex);

            this.relations.clear();
            selectionModel.clearSelection();
            if (relations != null) {
                this.relations.addAll(relations);
            }
            fireIntervalAdded(this, 0, getSize());

            if (sel != null) {
                selectedIndex = this.relations.indexOf(sel);
                if (selectedIndex >= 0) {
                    selectionModel.addSelectionInterval(selectedIndex, selectedIndex);
                }
            }
            if (selectionModel.isSelectionEmpty() && !this.relations.isEmpty()) {
                selectionModel.addSelectionInterval(0, 0);
            }
        }
    }
}
