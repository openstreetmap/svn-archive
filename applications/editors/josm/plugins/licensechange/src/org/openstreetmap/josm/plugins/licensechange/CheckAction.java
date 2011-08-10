// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.data.validation.util.AggregatePrimitivesVisitor;
import org.openstreetmap.josm.tools.Shortcut;
import org.xml.sax.SAXException;

/**
 * The action that starts the license check.
 */
public class CheckAction extends JosmAction 
{
    private LicenseChangePlugin plugin;

    /** Last selection used to validate */
    private Collection<OsmPrimitive> lastSelection;

    /**
     * Constructor
     */
    public CheckAction(LicenseChangePlugin plugin) 
    {
        super(tr("License Check"), "licensechange", tr("Performs the license check"),
        Shortcut.registerShortcut("tools:licensechange", tr("Tool: {0}", tr("License Check")), KeyEvent.VK_C, Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);
        this.plugin = plugin;
    }

    public void actionPerformed(ActionEvent ev) 
    {
        doCheck(ev);
    }

    /**
     * Does the validation.
     * <p>
     *
     * @param ev The event
     */
    public void doCheck(ActionEvent ev)
    {
        if (plugin.checkAction == null || Main.map == null || !Main.map.isVisible())
            return;

        plugin.initializeProblemLayer();

        Collection<OsmPrimitive> selection;
        selection = Main.main.getCurrentDataSet().getSelected();
        if (selection.isEmpty()) 
        {
            selection = Main.main.getCurrentDataSet().allNonDeletedPrimitives();
            lastSelection = null;
        } 
        else 
        {
            AggregatePrimitivesVisitor v = new AggregatePrimitivesVisitor();
            selection = v.visit(selection);
            lastSelection = selection;
        }

        CheckTask task = new CheckTask(selection, lastSelection);
        Main.worker.submit(task);
    }

    @Override
    public void updateEnabledState() 
    {
        setEnabled(getEditLayer() != null);
    }

    class CheckTask extends PleaseWaitRunnable 
    {
        private Check licenseCheck;
        private Collection<OsmPrimitive> validatedPrimitives;
        private Collection<OsmPrimitive> formerValidatedPrimitives;
        private boolean canceled;
        private List<LicenseProblem> problems;

        /**
         *
         * @param validatedPrimitives the collection of primitives to validate.
         * @param formerValidatedPrimitives the last collection of primitives being validates. May be null.
         */
        public CheckTask(Collection<OsmPrimitive> validatedPrimitives, Collection<OsmPrimitive> formerValidatedPrimitives) 
        {
            super(tr("Loading"), false /*don't ignore exceptions */);

            this.validatedPrimitives  = validatedPrimitives;
            this.formerValidatedPrimitives = formerValidatedPrimitives;
            this.licenseCheck = new BasicLicenseCheck(plugin);
        }

        @Override
        protected void cancel() 
        {
            this.canceled = true;
        }

        @Override
        protected void finish() 
        {
            if (canceled) return;

            // update GUI on Swing EDT
            //
            Runnable r = new Runnable()  {
                public void run() {
                    plugin.problemDialog.tree.setErrors(problems);
                    plugin.problemDialog.setVisible(true);
                    Main.main.getCurrentDataSet().fireSelectionChanged();
                }
            };
            if (SwingUtilities.isEventDispatchThread()) 
            {
                r.run();
            } 
            else 
            {
                SwingUtilities.invokeLater(r);
            }
        }

        @Override
        protected void realRun() throws SAXException, IOException,
                OsmTransferException 
        {
            getProgressMonitor().indeterminateSubTask(tr("Loading from Quick History Service..."));
            plugin.loadDataFromQuickHistoryService(validatedPrimitives);
            problems = new ArrayList<LicenseProblem>(200);
            int testCounter = 0;
            getProgressMonitor().indeterminateSubTask(tr("Analyzing..."));
            //licenseCheck.startCheck(getProgressMonitor().createSubTaskMonitor(validatedPrimitives.size(), false));
            licenseCheck.startCheck(getProgressMonitor());
            licenseCheck.visit(validatedPrimitives);
            licenseCheck.endCheck();
            problems.addAll(licenseCheck.getProblems());
        }
    }
}
