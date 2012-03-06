package org.openstreetmap.josm.plugins.opendata.core.gui;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleDownloadTask;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.core.modules.ReadLocalModuleInformationTask;
import org.openstreetmap.josm.plugins.opendata.core.modules.ReadRemoteModuleInformationTask;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

public class ModulePreference implements SubPreferenceSetting, OdConstants {
    /*public static class Factory implements PreferenceSettingFactory {
        public PreferenceSetting createPreferenceSetting() {
            return new ModulePreference();
        }
    }*/
    
    public static String buildDownloadSummary(ModuleDownloadTask task) {
        Collection<ModuleInformation> downloaded = task.getDownloadedModules();
        Collection<ModuleInformation> failed = task.getFailedModules();
        StringBuilder sb = new StringBuilder();
        if (! downloaded.isEmpty()) {
            sb.append(trn(
                    "The following module has been downloaded <strong>successfully</strong>:",
                    "The following {0} modules have been downloaded <strong>successfully</strong>:",
                    downloaded.size(),
                    downloaded.size()
            ));
            sb.append("<ul>");
            for(ModuleInformation pi: downloaded) {
                sb.append("<li>").append(pi.name).append(" (").append(pi.version).append(")").append("</li>");
            }
            sb.append("</ul>");
        }
        if (! failed.isEmpty()) {
            sb.append(trn(
                    "Downloading the following module has <strong>failed</strong>:",
                    "Downloading the following {0} modules has <strong>failed</strong>:",
                    failed.size(),
                    failed.size()
            ));
            sb.append("<ul>");
            for(ModuleInformation pi: failed) {
                sb.append("<li>").append(pi.name).append("</li>");
            }
            sb.append("</ul>");
        }
        return sb.toString();
    }

    private JTextField tfFilter;
    private ModuleListPanel pnlModulePreferences;
    private ModulePreferencesModel model;
    private JScrollPane spModulePreferences;

    /**
     * is set to true if this preference pane has been selected
     * by the user
     */
    private boolean modulePreferencesActivated = false;

    protected JPanel buildSearchFieldPanel() {
        JPanel pnl  = new JPanel(new GridBagLayout());
        pnl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        GridBagConstraints gc = new GridBagConstraints();

        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.0;
        gc.insets = new Insets(0,0,0,3);
        pnl.add(new JLabel(tr("Search:")), gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        pnl.add(tfFilter = new JTextField(), gc);
        tfFilter.setToolTipText(tr("Enter a search expression"));
        SelectAllOnFocusGainedDecorator.decorate(tfFilter);
        tfFilter.getDocument().addDocumentListener(new SearchFieldAdapter());
        return pnl;
    }

    protected JPanel buildActionPanel() {
        JPanel pnl = new JPanel(new GridLayout(1,3));

        pnl.add(new JButton(new DownloadAvailableModulesAction()));
        pnl.add(new JButton(new UpdateSelectedModulesAction()));
        pnl.add(new JButton(new ConfigureSitesAction()));
        return pnl;
    }

    protected JPanel buildModuleListPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(buildSearchFieldPanel(), BorderLayout.NORTH);
        model  = new ModulePreferencesModel();
        spModulePreferences = new JScrollPane(pnlModulePreferences = new ModuleListPanel(model));
        spModulePreferences.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spModulePreferences.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        spModulePreferences.getVerticalScrollBar().addComponentListener(
                new ComponentAdapter(){
                    @Override
                    public void componentShown(ComponentEvent e) {
                        spModulePreferences.setBorder(UIManager.getBorder("ScrollPane.border"));
                    }
                    @Override
                    public void componentHidden(ComponentEvent e) {
                        spModulePreferences.setBorder(null);
                    }
                }
        );

        pnl.add(spModulePreferences, BorderLayout.CENTER);
        pnl.add(buildActionPanel(), BorderLayout.SOUTH);
        return pnl;
    }

    public void addGui(final PreferenceTabbedPane gui) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.BOTH;
        OdPreferenceSetting settings = gui.getSetting(OdPreferenceSetting.class);
        settings.tabPane.addTab(tr("Modules"), buildModuleListPanel());
        pnlModulePreferences.refreshView();
        gui.addChangeListener(new ModulePreferenceActivationListener(settings.masterPanel));
    }

    private void configureSites() {
        ButtonSpec[] options = new ButtonSpec[] {
                new ButtonSpec(
                        tr("OK"),
                        ImageProvider.get("ok"),
                        tr("Accept the new module sites and close the dialog"),
                        null /* no special help topic */
                ),
                new ButtonSpec(
                        tr("Cancel"),
                        ImageProvider.get("cancel"),
                        tr("Close the dialog"),
                        null /* no special help topic */
                )
        };
        ModuleConfigurationSitesPanel pnl = new ModuleConfigurationSitesPanel();

        int answer = HelpAwareOptionPane.showOptionDialog(
                pnlModulePreferences,
                pnl,
                tr("Configure Module Sites"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0],
                null /* no help topic */
        );
        if (answer != 0 /* OK */)
            return;
        List<String> sites = pnl.getUpdateSites();
        OdPreferenceSetting.setModuleSites(sites);
    }

    /**
     * Replies the list of modules waiting for update or download
     *
     * @return the list of modules waiting for update or download
     */
    public List<ModuleInformation> getModulesScheduledForUpdateOrDownload() {
        return model != null ? model.getModulesScheduledForUpdateOrDownload() : null;
    }

    public boolean ok() {
        if (! modulePreferencesActivated)
            return false;
        if (model.isActiveModulesChanged()) {
            LinkedList<String> l = new LinkedList<String>(model.getSelectedModuleNames());
            Collections.sort(l);
            Main.pref.putCollection(PREF_MODULES, l);
            return true;
        }
        return false;
    }

    /**
     * Reads locally available information about modules from the local file system.
     * Scans cached module lists from module download sites and locally available
     * module jar files.
     *
     */
    public void readLocalModuleInformation() {
        final ReadLocalModuleInformationTask task = new ReadLocalModuleInformationTask();
        Runnable r = new Runnable() {
            public void run() {
                if (task.isCanceled()) return;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        model.setAvailableModules(task.getAvailableModules());
                        pnlModulePreferences.refreshView();
                    }
                });
            }
        };
        Main.worker.submit(task);
        Main.worker.submit(r);
    }

    /**
     * The action for downloading the list of available modules
     *
     */
    class DownloadAvailableModulesAction extends AbstractAction {

        public DownloadAvailableModulesAction() {
            putValue(NAME,tr("Download list"));
            putValue(SHORT_DESCRIPTION, tr("Download the list of available modules"));
            putValue(SMALL_ICON, ImageProvider.get("download"));
        }

        public void actionPerformed(ActionEvent e) {
            final ReadRemoteModuleInformationTask task = new ReadRemoteModuleInformationTask(OdPreferenceSetting.getModuleSites());
            Runnable continuation = new Runnable() {
                public void run() {
                    if (task.isCanceled()) return;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            model.updateAvailableModules(task.getAvailabeModules());
                            pnlModulePreferences.refreshView();
                        }
                    });
                }
            };
            Main.worker.submit(task);
            Main.worker.submit(continuation);
        }
    }

    /**
     * The action for downloading the list of available modules
     *
     */
    class UpdateSelectedModulesAction extends AbstractAction {
        public UpdateSelectedModulesAction() {
            putValue(NAME,tr("Update modules"));
            putValue(SHORT_DESCRIPTION, tr("Update the selected modules"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "refresh"));
        }

        protected void notifyDownloadResults(ModuleDownloadTask task) {
            Collection<ModuleInformation> downloaded = task.getDownloadedModules();
            Collection<ModuleInformation> failed = task.getFailedModules();
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append(buildDownloadSummary(task));
            if (!downloaded.isEmpty()) {
                sb.append(tr("Please restart JOSM to activate the downloaded modules."));
            }
            sb.append("</html>");
            HelpAwareOptionPane.showOptionDialog(
                    pnlModulePreferences,
                    sb.toString(),
                    tr("Update modules"),
                    !failed.isEmpty() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE,
                            HelpUtil.ht("/Preferences/Modules")
            );
        }

        protected void alertNothingToUpdate() {
            HelpAwareOptionPane.showOptionDialog(
                    pnlModulePreferences,
                    tr("All installed modules are up to date. JOSM does not have to download newer versions."),
                    tr("Modules up to date"),
                    JOptionPane.INFORMATION_MESSAGE,
                    null // FIXME: provide help context
            );
        }

        public void actionPerformed(ActionEvent e) {
            final List<ModuleInformation> toUpdate = model.getSelectedModules();
            // the async task for downloading modules
            final ModuleDownloadTask moduleDownloadTask = new ModuleDownloadTask(
                    pnlModulePreferences,
                    toUpdate,
                    tr("Update modules")
            );
            // the async task for downloading module information
            final ReadRemoteModuleInformationTask moduleInfoDownloadTask = new ReadRemoteModuleInformationTask(OdPreferenceSetting.getModuleSites());

            // to be run asynchronously after the module download
            //
            final Runnable moduleDownloadContinuation = new Runnable() {
                public void run() {
                    if (moduleDownloadTask.isCanceled())
                        return;
                    notifyDownloadResults(moduleDownloadTask);
                    model.refreshLocalModuleVersion(moduleDownloadTask.getDownloadedModules());
                    model.clearPendingModules(moduleDownloadTask.getDownloadedModules());
                    pnlModulePreferences.refreshView();
                }
            };

            // to be run asynchronously after the module list download
            //
            final Runnable moduleInfoDownloadContinuation = new Runnable() {
                public void run() {
                    if (moduleInfoDownloadTask.isCanceled())
                        return;
                    model.updateAvailableModules(moduleInfoDownloadTask.getAvailabeModules());
                    // select modules which actually have to be updated
                    //
                    Iterator<ModuleInformation> it = toUpdate.iterator();
                    while(it.hasNext()) {
                        ModuleInformation pi = it.next();
                        if (!pi.isUpdateRequired()) {
                            it.remove();
                        }
                    }
                    if (toUpdate.isEmpty()) {
                        alertNothingToUpdate();
                        return;
                    }
                    moduleDownloadTask.setModulesToDownload(toUpdate);
                    Main.worker.submit(moduleDownloadTask);
                    Main.worker.submit(moduleDownloadContinuation);
                }
            };

            Main.worker.submit(moduleInfoDownloadTask);
            Main.worker.submit(moduleInfoDownloadContinuation);
        }
    }


    /**
     * The action for configuring the module download sites
     *
     */
    class ConfigureSitesAction extends AbstractAction {
        public ConfigureSitesAction() {
            putValue(NAME,tr("Configure sites..."));
            putValue(SHORT_DESCRIPTION, tr("Configure the list of sites where modules are downloaded from"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "settings"));
        }

        public void actionPerformed(ActionEvent e) {
            configureSites();
        }
    }

    /**
     * Listens to the activation of the module preferences tab. On activation it
     * reloads module information from the local file system.
     *
     */
    class ModulePreferenceActivationListener implements ChangeListener {
        private Component pane;
        public ModulePreferenceActivationListener(Component preferencesPane) {
            pane = preferencesPane;
        }

        public void stateChanged(ChangeEvent e) {
            JTabbedPane tp = (JTabbedPane)e.getSource();
            if (tp.getSelectedComponent() == pane) {
                readLocalModuleInformation();
                modulePreferencesActivated = true;
            }
        }
    }

    /**
     * Applies the current filter condition in the filter text field to the
     * model
     */
    class SearchFieldAdapter implements DocumentListener {
        public void filter() {
            String expr = tfFilter.getText().trim();
            if (expr.equals("")) {
                expr = null;
            }
            model.filterDisplayedModules(expr);
            pnlModulePreferences.refreshView();
        }

        public void changedUpdate(DocumentEvent arg0) {
            filter();
        }

        public void insertUpdate(DocumentEvent arg0) {
            filter();
        }

        public void removeUpdate(DocumentEvent arg0) {
            filter();
        }
    }

    static private class ModuleConfigurationSitesPanel extends JPanel {

        private DefaultListModel model;

        protected void build() {
            setLayout(new GridBagLayout());
            add(new JLabel(tr("Add Open Data Module description URL.")), GBC.eol());
            model = new DefaultListModel();
            for (String s : OdPreferenceSetting.getModuleSites()) {
                model.addElement(s);
            }
            final JList list = new JList(model);
            add(new JScrollPane(list), GBC.std().fill());
            JPanel buttons = new JPanel(new GridBagLayout());
            buttons.add(new JButton(new AbstractAction(tr("Add")){
                public void actionPerformed(ActionEvent e) {
                    String s = JOptionPane.showInputDialog(
                            JOptionPane.getFrameForComponent(ModuleConfigurationSitesPanel.this),
                            tr("Add Open Data Module description URL."),
                            tr("Enter URL"),
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (s != null) {
                        model.addElement(s);
                    }
                }
            }), GBC.eol().fill(GBC.HORIZONTAL));
            buttons.add(new JButton(new AbstractAction(tr("Edit")){
                public void actionPerformed(ActionEvent e) {
                    if (list.getSelectedValue() == null) {
                        JOptionPane.showMessageDialog(
                                JOptionPane.getFrameForComponent(ModuleConfigurationSitesPanel.this),
                                tr("Please select an entry."),
                                tr("Warning"),
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    String s = (String)JOptionPane.showInputDialog(
                            Main.parent,
                            tr("Edit Open Data Module description URL."),
                            tr("Open Data Module description URL"),
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            list.getSelectedValue()
                    );
                    if (s != null) {
                        model.setElementAt(s, list.getSelectedIndex());
                    }
                }
            }), GBC.eol().fill(GBC.HORIZONTAL));
            buttons.add(new JButton(new AbstractAction(tr("Delete")){
                public void actionPerformed(ActionEvent event) {
                    if (list.getSelectedValue() == null) {
                        JOptionPane.showMessageDialog(
                                JOptionPane.getFrameForComponent(ModuleConfigurationSitesPanel.this),
                                tr("Please select an entry."),
                                tr("Warning"),
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                    model.removeElement(list.getSelectedValue());
                }
            }), GBC.eol().fill(GBC.HORIZONTAL));
            add(buttons, GBC.eol());
        }

        public ModuleConfigurationSitesPanel() {
            build();
        }

        public List<String> getUpdateSites() {
            if (model.getSize() == 0) return Collections.emptyList();
            List<String> ret = new ArrayList<String>(model.getSize());
            for (int i=0; i< model.getSize();i++){
                ret.add((String)model.get(i));
            }
            return ret;
        }
    }

	@Override
	public boolean isExpert() {
		return false;
	}

	@Override
	public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
		return gui.getSetting(OdPreferenceSetting.class);
	}
}
