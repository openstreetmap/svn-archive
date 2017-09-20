// License: GPL. For details, see LICENSE file.
package org.wikipedia.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicArrowButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.preferences.AbstractProperty;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.download.AbstractDownloadSourcePanel;
import org.openstreetmap.josm.gui.download.DownloadSettings;
import org.openstreetmap.josm.gui.download.DownloadSource;
import org.openstreetmap.josm.gui.download.DownloadSourceSizingPolicy;
import org.openstreetmap.josm.gui.download.DownloadSourceSizingPolicy.AdjustableDownloadSizePolicy;
import org.openstreetmap.josm.gui.download.UserQueryList;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.gui.widgets.JosmTextArea;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.wikipedia.io.WikosmDownloadReader;

/**
 * Class defines the way data is fetched from Wikosm API.
 */
public class WikosmDownloadSource implements DownloadSource<WikosmDownloadSource.WikosmDownloadData> {

    @Override
    public AbstractDownloadSourcePanel<WikosmDownloadData> createPanel() {
        return new WikosmDownloadSourcePanel(this);
    }

    @Override
    public void doDownload(WikosmDownloadData data, DownloadSettings settings) {
        Bounds area = settings.getDownloadBounds().orElse(new Bounds(0, 0, 0, 0));
        DownloadOsmTask task = new DownloadOsmTask();
        task.setZoomAfterDownload(settings.zoomToData());
        Future<?> future = task.download(
                new WikosmDownloadReader(area, WikosmDownloadReader.WIKOSM_SERVER.get(), data.getQuery(),
                        settings.asNewLayer(), data.getDownloadReferrers(), data.getDownloadFull()),

                settings.asNewLayer(), area, null);
        MainApplication.worker.submit(new PostDownloadHandler(task, future, data.getErrorReporter()));
    }

    @Override
    public String getLabel() {
        return tr("Download from Wikosm API");
    }

    @Override
    public boolean onlyExpert() {
        return true;
    }

    /**
     * The GUI representation of the Wikosm download source.
     */
    public static class WikosmDownloadSourcePanel extends AbstractDownloadSourcePanel<WikosmDownloadData> {

        private static final String HELP_PAGE = "https://wiki.openstreetmap.org/wiki/Wikidata%2BOSM_SPARQL_query_service";
        private static final String SIMPLE_NAME = "wikosmdownloadpanel";
        private static final AbstractProperty<Integer> PANEL_SIZE_PROPERTY =
                new IntegerProperty(TAB_SPLIT_NAMESPACE + SIMPLE_NAME, 150).cached();
        private static final BooleanProperty WIKOSM_QUERY_LIST_OPENED =
                new BooleanProperty("download.wikosm.query-list.opened", false);
        private static final String ACTION_IMG_SUBDIR = "dialogs";

        private final JosmTextArea wikosmQuery;
        private final UserQueryList wikosmQueryList;
        private final JCheckBox referrers;
        private final JCheckBox fullRel;

        /**
         * Create a new {@code WikosmDownloadSourcePanel}
         * @param ds The download source to create the panel for
         */
        public WikosmDownloadSourcePanel(WikosmDownloadSource ds) {
            super(ds);
            setLayout(new BorderLayout());

            String queryText = "# " +
                    tr("Find places of education at least 2km, and at most 3km from the center of the selection") +
                    "\n" +
                    "SELECT ?osmid WHERE {\n" +
                    "  VALUES ?amenity { \"kindergarten\" \"school\" \"university\" \"college\" }\n" +
                    "  ?osmid osmt:amenity ?amenity ;\n" +
                    "         osmm:loc ?location .\n" +
                    "  BIND(geof:distance({{center}}, ?location) as ?distance)\n" +
                    "  FILTER(?distance > 2 && ?distance < 3)\n" +
                    "}";

            this.wikosmQuery = new JosmTextArea(queryText, 8, 80);

            this.wikosmQuery.setFont(GuiHelper.getMonospacedFont(wikosmQuery));
            this.wikosmQuery.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    wikosmQuery.selectAll();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    // ignored
                }
            });


            this.wikosmQueryList = new UserQueryList(this, this.wikosmQuery, "download.wikosm.query");
            this.wikosmQueryList.setPreferredSize(new Dimension(350, 300));

            EditSnippetAction edit = new EditSnippetAction();
            RemoveSnippetAction remove = new RemoveSnippetAction();
            this.wikosmQueryList.addSelectionListener(edit);
            this.wikosmQueryList.addSelectionListener(remove);

            JPanel listPanel = new JPanel(new GridBagLayout());
            listPanel.add(new JLabel(tr("Your saved queries:")), GBC.eol().insets(2).anchor(GBC.CENTER));
            listPanel.add(this.wikosmQueryList, GBC.eol().fill(GBC.BOTH));
            listPanel.add(new JButton(new AddSnippetAction()), GBC.std().fill(GBC.HORIZONTAL));
            listPanel.add(new JButton(edit), GBC.std().fill(GBC.HORIZONTAL));
            listPanel.add(new JButton(remove), GBC.std().fill(GBC.HORIZONTAL));
            listPanel.setVisible(WIKOSM_QUERY_LIST_OPENED.get());

            JScrollPane scrollPane = new JScrollPane(wikosmQuery);
            BasicArrowButton arrowButton = new BasicArrowButton(listPanel.isVisible()
                    ? BasicArrowButton.EAST
                    : BasicArrowButton.WEST);
            arrowButton.setToolTipText(tr("Show/hide Wikosm snippet list"));
            arrowButton.addActionListener(e -> {
                if (listPanel.isVisible()) {
                    listPanel.setVisible(false);
                    arrowButton.setDirection(BasicArrowButton.WEST);
                    WIKOSM_QUERY_LIST_OPENED.put(Boolean.FALSE);
                } else {
                    listPanel.setVisible(true);
                    arrowButton.setDirection(BasicArrowButton.EAST);
                    WIKOSM_QUERY_LIST_OPENED.put(Boolean.TRUE);
                }
            });

            // TODO: Once new core is widely avialable, replace:
            //   Main.pref.getBoolean --> Config.getPref().getBoolean
            //   Main.pref.put --> Config.getPref().putBoolean

            referrers = new JCheckBox(tr("Download referrers (parent relations)"));
            referrers.setToolTipText(tr("Select if the referrers of the object should be downloaded as well, i.e.,"
                    + "parent relations and for nodes, additionally, parent ways"));
            referrers.setSelected(Main.pref.getBoolean("wikosm.downloadprimitive.referrers", true));
            referrers.addActionListener(e -> Main.pref.put("wikosm.downloadprimitive.referrers", referrers.isSelected()));

            fullRel = new JCheckBox(tr("Download relation members"));
            fullRel.setToolTipText(tr("Select if the members of a relation should be downloaded as well"));
            fullRel.setSelected(Main.pref.getBoolean("wikosm.downloadprimitive.full", true));
            fullRel.addActionListener(e -> Main.pref.put("wikosm.downloadprimitive.full", fullRel.isSelected()));

            // https://stackoverflow.com/questions/527719/how-to-add-hyperlink-in-jlabel
            JButton helpLink = new JButton();
            helpLink.setText("<HTML><FONT color=\"#000099\"><U>"+tr("help")+"</U></FONT></HTML>");
            helpLink.setToolTipText(HELP_PAGE);
            helpLink.setHorizontalAlignment(SwingConstants.LEFT);
            helpLink.setBorderPainted(false);
            helpLink.setOpaque(false);
            helpLink.setBackground(Color.WHITE);
            helpLink.addActionListener(e -> OpenBrowser.displayUrl(HELP_PAGE));

            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.add(scrollPane, GBC.eol().fill(GBC.BOTH));
            centerPanel.add(referrers, GBC.std().anchor(GBC.WEST).insets(5, 5, 5, 5));
            centerPanel.add(fullRel, GBC.std().anchor(GBC.WEST).insets(15, 5, 5, 5));
            centerPanel.add(helpLink, GBC.std().anchor(GBC.WEST).insets(10, 5, 5, 5));


            JPanel innerPanel = new JPanel(new BorderLayout());
            innerPanel.add(centerPanel, BorderLayout.CENTER);
            innerPanel.add(arrowButton, BorderLayout.EAST);

            add(innerPanel, BorderLayout.CENTER);
            add(listPanel, BorderLayout.EAST);

            setMinimumSize(new Dimension(450, 240));
        }

        @Override
        public WikosmDownloadData getData() {
            String query = wikosmQuery.getText();
            /*
             * A callback that is passed to PostDownloadReporter that is called once the download task
             * has finished. According to the number of errors happened, their type we decide whether we
             * want to save the last query in WikosmQueryList.
             */
            Consumer<Collection<Object>> errorReporter = errors -> {

                boolean onlyNoDataError = errors.size() == 1 &&
                        errors.contains("No data found in this area.");

                if (errors.isEmpty() || onlyNoDataError) {
                    wikosmQueryList.saveHistoricItem(query);
                }
            };

            return new WikosmDownloadData(query, referrers.isSelected(), fullRel.isSelected(), errorReporter);
        }

        @Override
        public void rememberSettings() {
            // nothing
        }

        @Override
        public void restoreSettings() {
            // nothing
        }

        @Override
        public boolean checkDownload(DownloadSettings settings) {
            String query = getData().getQuery();

            /*
             * Absence of the selected area can be justified only if the Wikosm query
             * is not restricted to bbox.
             */
            if (!settings.getDownloadBounds().isPresent() && (
                    query.contains("{{boxParams}}") ||
                    query.contains("{{center}}")
            )) {
                JOptionPane.showMessageDialog(
                        this.getParent(),
                        tr("Please select a download area first."),
                        tr("Error"),
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }

            return true;
        }

        @Override
        public Icon getIcon() {
            return ImageProvider.get(ACTION_IMG_SUBDIR, "wikosm");
        }

        @Override
        public String getSimpleName() {
            return SIMPLE_NAME;
        }

        @Override
        public DownloadSourceSizingPolicy getSizingPolicy() {
            return new AdjustableDownloadSizePolicy(PANEL_SIZE_PROPERTY);
        }

        /**
         * Action that delegates snippet creation to {@link UserQueryList#createNewItem()}.
         */
        private class AddSnippetAction extends AbstractAction {

            /**
             * Constructs a new {@code AddSnippetAction}.
             */
            AddSnippetAction() {
                super();
                putValue(SMALL_ICON, ImageProvider.get(ACTION_IMG_SUBDIR, "add"));
                putValue(SHORT_DESCRIPTION, tr("Add new snippet"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                wikosmQueryList.createNewItem();
            }
        }

        /**
         * Action that delegates snippet removal to {@link UserQueryList#removeSelectedItem()}.
         */
        private class RemoveSnippetAction extends AbstractAction implements ListSelectionListener {

            /**
             * Constructs a new {@code RemoveSnippetAction}.
             */
            RemoveSnippetAction() {
                super();
                putValue(SMALL_ICON, ImageProvider.get(ACTION_IMG_SUBDIR, "delete"));
                putValue(SHORT_DESCRIPTION, tr("Delete selected snippet"));
                checkEnabled();
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                wikosmQueryList.removeSelectedItem();
            }

            /**
             * Disables the action if no items are selected.
             */
            void checkEnabled() {
                setEnabled(wikosmQueryList.getSelectedItem().isPresent());
            }

            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkEnabled();
            }
        }

        /**
         * Action that delegates snippet edit to {@link UserQueryList#editSelectedItem()}.
         */
        private class EditSnippetAction extends AbstractAction implements ListSelectionListener {

            /**
             * Constructs a new {@code EditSnippetAction}.
             */
            EditSnippetAction() {
                super();
                putValue(SMALL_ICON, ImageProvider.get(ACTION_IMG_SUBDIR, "edit"));
                putValue(SHORT_DESCRIPTION, tr("Edit selected snippet"));
                checkEnabled();
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                wikosmQueryList.editSelectedItem();
            }

            /**
             * Disables the action if no items are selected.
             */
            void checkEnabled() {
                setEnabled(wikosmQueryList.getSelectedItem().isPresent());
            }

            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkEnabled();
            }
        }
    }

    /**
     * Encapsulates data that is required to preform download from Wikosm API.
     */
    static class WikosmDownloadData {
        private final String query;
        private final boolean downloadReferrers;
        private final boolean downloadFull;
        private final Consumer<Collection<Object>> errorReporter;

        WikosmDownloadData(String query, boolean downloadReferrers, boolean downloadFull, Consumer<Collection<Object>> errorReporter) {
            this.query = query;
            this.downloadReferrers = downloadReferrers;
            this.downloadFull = downloadFull;
            this.errorReporter = errorReporter;
        }

        String getQuery() {
            return this.query;
        }

        boolean getDownloadReferrers() {
            return this.downloadReferrers;
        }

        boolean getDownloadFull() { return this.downloadFull; }

        Consumer<Collection<Object>> getErrorReporter() {
            return this.errorReporter;
        }
    }
}
