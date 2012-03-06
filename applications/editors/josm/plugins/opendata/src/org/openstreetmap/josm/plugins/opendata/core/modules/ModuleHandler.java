//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.gui.JMultilineLabel;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.preferences.SourceProvider;
import org.openstreetmap.josm.gui.preferences.map.MapPaintPreference;
import org.openstreetmap.josm.gui.preferences.map.TaggingPresetPreference;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.gui.OdPreferenceSetting;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * ModuleHandler is basically a collection of static utility functions used to bootstrap
 * and manage the loaded modules.
 *
 */
public class ModuleHandler implements OdConstants {

    /**
     * All installed and loaded modules (resp. their main classes)
     */
    public final static Collection<Module> moduleList = new LinkedList<Module>();

    /**
     * Add here all ClassLoader whose resource should be searched.
     */
    private static final List<ClassLoader> sources = new LinkedList<ClassLoader>();

    static {
        try {
            sources.add(ClassLoader.getSystemClassLoader());
            sources.add(org.openstreetmap.josm.gui.MainApplication.class.getClassLoader());
        } catch (SecurityException ex) {
            sources.add(ImageProvider.class.getClassLoader());
        }
    }

    public static Collection<ClassLoader> getResourceClassLoaders() {
        return Collections.unmodifiableCollection(sources);
    }

    /**
     * Checks whether the locally available modules should be updated and
     * asks the user if running an update is OK. An update is advised if
     * JOSM was updated to a new version since the last module updates or
     * if the modules were last updated a long time ago.
     *
     * @param parent the parent window relative to which the confirmation dialog
     * is to be displayed
     * @return true if a module update should be run; false, otherwise
     */
    public static boolean checkAndConfirmModuleUpdate(Component parent) {
        String message = null;
        String togglePreferenceKey = null;
        long tim = System.currentTimeMillis();
        long last = Main.pref.getLong("opendata.modulemanager.lastupdate", 0);
        Integer maxTime = Main.pref.getInteger("opendata.modulemanager.time-based-update.interval", 60);
        long d = (tim - last) / (24 * 60 * 60 * 1000l);
        if ((last <= 0) || (maxTime <= 0)) {
            Main.pref.put("opendata.modulemanager.lastupdate", Long.toString(tim));
        } else if (d > maxTime) {
            message =
                "<html>"
                + tr("Last module update more than {0} days ago.", d)
                + "</html>";
            togglePreferenceKey = "opendata.modulemanager.time-based-update.policy";
        }
        if (message == null) return false;

        ButtonSpec [] options = new ButtonSpec[] {
                new ButtonSpec(
                        tr("Update modules"),
                        ImageProvider.get("dialogs", "refresh"),
                        tr("Click to update the activated modules"),
                        null /* no specific help context */
                ),
                new ButtonSpec(
                        tr("Skip update"),
                        ImageProvider.get("cancel"),
                        tr("Click to skip updating the activated modules"),
                        null /* no specific help context */
                )
        };

        UpdateModulesMessagePanel pnlMessage = new UpdateModulesMessagePanel();
        pnlMessage.setMessage(message);
        pnlMessage.initDontShowAgain(togglePreferenceKey);

        // check whether automatic update at startup was disabled
        //
        String policy = Main.pref.get(togglePreferenceKey, "ask");
        policy = policy.trim().toLowerCase();
        if (policy.equals("never")) {
            if ("opendata.modulemanager.time-based-update.policy".equals(togglePreferenceKey)) {
                System.out.println(tr("Skipping module update after elapsed update interval. Automatic update at startup is disabled."));
            }
            return false;
        }

        if (policy.equals("always")) {
            if ("opendata.modulemanager.time-based-update.policy".equals(togglePreferenceKey)) {
                System.out.println(tr("Running module update after elapsed update interval. Automatic update at startup is disabled."));
            }
            return true;
        }

        if (!policy.equals("ask")) {
            System.err.println(tr("Unexpected value ''{0}'' for preference ''{1}''. Assuming value ''ask''.", policy, togglePreferenceKey));
        }
        int ret = HelpAwareOptionPane.showOptionDialog(
                parent,
                pnlMessage,
                tr("Update modules"),
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0],
                null
        );

        if (pnlMessage.isRememberDecision()) {
            switch(ret) {
            case 0:
                Main.pref.put(togglePreferenceKey, "always");
                break;
            case JOptionPane.CLOSED_OPTION:
            case 1:
                Main.pref.put(togglePreferenceKey, "never");
                break;
            }
        } else {
            Main.pref.put(togglePreferenceKey, "ask");
        }
        return ret == 0;
    }

    /**
     * Checks whether all preconditions for loading the module <code>module</code> are met. The
     * current JOSM version must be compatible with the module and no other modules this module
     * depends on should be missing.
     *
     * @param modules the collection of all loaded modules
     * @param module the module for which preconditions are checked
     * @return true, if the preconditions are met; false otherwise
     */
    public static boolean checkLoadPreconditions(Component parent, Collection<ModuleInformation> modules, ModuleInformation module) {
        return true;
    }

    /**
     * Creates a class loader for loading module code.
     *
     * @param modules the collection of modules which are going to be loaded with this
     * class loader
     * @return the class loader
     */
    public static ClassLoader createClassLoader(Collection<ModuleInformation> modules) {
        // iterate all modules and collect all libraries of all modules:
        List<URL> allModuleLibraries = new LinkedList<URL>();
        File moduleDir = OdPlugin.getInstance().getModulesDirectory();
        for (ModuleInformation info : modules) {
            if (info.libraries == null) {
                continue;
            }
            allModuleLibraries.addAll(info.libraries);
            File moduleJar = new File(moduleDir, info.name + ".jar");
            I18n.addTexts(moduleJar);
            URL moduleJarUrl = ModuleInformation.fileToURL(moduleJar);
            allModuleLibraries.add(moduleJarUrl);
        }

        // create a classloader for all modules:
        return new URLClassLoader(allModuleLibraries.toArray(new URL[0]), OdPlugin.class.getClassLoader());
    }

    /**
     * Loads and instantiates the module described by <code>module</code> using
     * the class loader <code>moduleClassLoader</code>.
     *
     * @param module the module
     * @param moduleClassLoader the module class loader
     */
    public static void loadModule(Component parent, ModuleInformation module, ClassLoader moduleClassLoader) {
        String msg = tr("Could not load module {0}. Delete from preferences?", module.name);
        try {
            Class<? extends Module> klass = module.loadClass(moduleClassLoader);
            if (klass != null) {
                System.out.println(tr("loading module ''{0}'' (version {1})", module.name, module.localversion));
                Module mod = module.load(klass);
                if (moduleList.add(mod)) {
                	SourceProvider styleProvider = mod.getMapPaintStyleSourceProvider();
                	if (styleProvider != null) {
                		MapPaintPreference.registerSourceProvider(styleProvider);
                	}
                	SourceProvider presetProvider = mod.getPresetSourceProvider();
                	if (presetProvider != null) {
                		TaggingPresetPreference.registerSourceProvider(presetProvider);
                	}
                }
            }
            msg = null;
        } catch (ModuleException e) {
            e.printStackTrace();
            if (e.getCause() instanceof ClassNotFoundException) {
                msg = tr("<html>Could not load module {0} because the module<br>main class ''{1}'' was not found.<br>"
                        + "Delete from preferences?</html>", module.name, module.className);
            }
        }  catch (Throwable e) {
            e.printStackTrace();
        }
        if (msg != null && confirmDisableModule(parent, msg, module.name)) {
            Main.pref.removeFromCollection(PREF_MODULES, module.name);
        }
    }

    /**
     * Loads the module in <code>modules</code> from locally available jar files into
     * memory.
     *
     * @param modules the list of modules
     * @param monitor the progress monitor. Defaults to {@see NullProgressMonitor#INSTANCE} if null.
     */
    public static void loadModules(Component parent, Collection<ModuleInformation> modules, ProgressMonitor monitor) {
        if (monitor == null) {
            monitor = NullProgressMonitor.INSTANCE;
        }
        try {
            monitor.beginTask(tr("Loading modules ..."));
            monitor.subTask(tr("Checking module preconditions..."));
            List<ModuleInformation> toLoad = new LinkedList<ModuleInformation>();
            for (ModuleInformation pi: modules) {
                if (checkLoadPreconditions(parent, modules, pi)) {
                    toLoad.add(pi);
                }
            }
            if (toLoad.isEmpty())
                return;

            ClassLoader moduleClassLoader = createClassLoader(toLoad);
            sources.add(0, moduleClassLoader);
            monitor.setTicksCount(toLoad.size());
            for (ModuleInformation info : toLoad) {
                monitor.setExtraText(tr("Loading module ''{0}''...", info.name));
                loadModule(parent, info, moduleClassLoader);
                monitor.worked(1);
            }
        } finally {
            monitor.finishTask();
        }
    }

    /**
     * Loads locally available module information from local module jars and from cached
     * module lists.
     *
     * @param monitor the progress monitor. Defaults to {@see NullProgressMonitor#INSTANCE} if null.
     * @return the list of locally available module information
     *
     */
    private static Map<String, ModuleInformation> loadLocallyAvailableModuleInformation(ProgressMonitor monitor) {
        if (monitor == null) {
            monitor = NullProgressMonitor.INSTANCE;
        }
        try {
            ReadLocalModuleInformationTask task = new ReadLocalModuleInformationTask(monitor);
            ExecutorService service = Executors.newSingleThreadExecutor();
            Future<?> future = service.submit(task);
            try {
                future.get();
            } catch(ExecutionException e) {
                e.printStackTrace();
                return null;
            } catch(InterruptedException e) {
                e.printStackTrace();
                return null;
            }
            HashMap<String, ModuleInformation> ret = new HashMap<String, ModuleInformation>();
            for (ModuleInformation pi: task.getAvailableModules()) {
                ret.put(pi.name, pi);
            }
            return ret;
        } finally {
            monitor.finishTask();
        }
    }

    private static void alertMissingModuleInformation(Component parent, Collection<String> modules) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(trn("JOSM could not find information about the following module:",
                "JOSM could not find information about the following modules:",
                modules.size()));
        sb.append("<ul>");
        for (String module: modules) {
            sb.append("<li>").append(module).append("</li>");
        }
        sb.append("</ul>");
        sb.append(trn("The module is not going to be loaded.",
                "The modules are not going to be loaded.",
                modules.size()));
        sb.append("</html>");
        HelpAwareOptionPane.showOptionDialog(
                parent,
                sb.toString(),
                tr("Warning"),
                JOptionPane.WARNING_MESSAGE,
                HelpUtil.ht("/Module/Loading#MissingModuleInfos")
        );
    }

    /**
     * Builds the set of modules to load. Deprecated and unmaintained modules are filtered
     * out. This involves user interaction. This method displays alert and confirmation
     * messages.
     *
     * @return the set of modules to load (as set of module names)
     */
    public static List<ModuleInformation> buildListOfModulesToLoad(Component parent) {
        Set<String> modules = new HashSet<String>();
        modules.addAll(Main.pref.getCollection(PREF_MODULES,  new LinkedList<String>()));
        if (System.getProperty("josm."+PREF_MODULES) != null) {
            modules.addAll(Arrays.asList(System.getProperty("josm."+PREF_MODULES).split(",")));
        }
        Map<String, ModuleInformation> infos = loadLocallyAvailableModuleInformation(null);
        List<ModuleInformation> ret = new LinkedList<ModuleInformation>();
        for (Iterator<String> it = modules.iterator(); it.hasNext();) {
            String module = it.next();
            if (infos.containsKey(module)) {
                ret.add(infos.get(module));
                it.remove();
            }
        }
        if (!modules.isEmpty()) {
            alertMissingModuleInformation(parent, modules);
        }
        return ret;
    }

    private static void alertFailedModuleUpdate(Component parent, Collection<ModuleInformation> modules) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append(trn(
                "Updating the following module has failed:",
                "Updating the following modules has failed:",
                modules.size()
        )
        );
        sb.append("<ul>");
        for (ModuleInformation pi: modules) {
            sb.append("<li>").append(pi.name).append("</li>");
        }
        sb.append("</ul>");
        sb.append(trn(
                "Please open the Preference Dialog after JOSM has started and try to update it manually.",
                "Please open the Preference Dialog after JOSM has started and try to update them manually.",
                modules.size()
        ));
        sb.append("</html>");
        HelpAwareOptionPane.showOptionDialog(
                parent,
                sb.toString(),
                tr("Module update failed"),
                JOptionPane.ERROR_MESSAGE,
                HelpUtil.ht("/Module/Loading#FailedModuleUpdated")
        );
    }

    /**
     * Updates the modules in <code>modules</code>.
     *
     * @param parent the parent window for message boxes
     * @param modules the collection of modules to update. Must not be null.
     * @param monitor the progress monitor. Defaults to {@see NullProgressMonitor#INSTANCE} if null.
     * @throws IllegalArgumentException thrown if modules is null
     */
    public static List<ModuleInformation>  updateModules(Component parent,
            List<ModuleInformation> modules, ProgressMonitor monitor)
            throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(modules, "modules");
        if (monitor == null) {
            monitor = NullProgressMonitor.INSTANCE;
        }
        try {
            monitor.beginTask("");
            ExecutorService service = Executors.newSingleThreadExecutor();

            // try to download the module lists
            //
            ReadRemoteModuleInformationTask task1 = new ReadRemoteModuleInformationTask(
                    monitor.createSubTaskMonitor(1,false),
                    OdPreferenceSetting.getModuleSites()
            );
            Future<?> future = service.submit(task1);
            try {
                future.get();
                modules = buildListOfModulesToLoad(parent);
            } catch(ExecutionException e) {
                System.out.println(tr("Warning: failed to download module information list"));
                e.printStackTrace();
                // don't abort in case of error, continue with downloading modules below
            } catch(InterruptedException e) {
                System.out.println(tr("Warning: failed to download module information list"));
                e.printStackTrace();
                // don't abort in case of error, continue with downloading modules below
            }

            // filter modules which actually have to be updated
            //
            Collection<ModuleInformation> modulesToUpdate = new ArrayList<ModuleInformation>();
            for(ModuleInformation pi: modules) {
                if (pi.isUpdateRequired()) {
                    modulesToUpdate.add(pi);
                }
            }

            if (!modulesToUpdate.isEmpty()) {
                // try to update the locally installed modules
                //
                ModuleDownloadTask task2 = new ModuleDownloadTask(
                        monitor.createSubTaskMonitor(1,false),
                        modulesToUpdate,
                        tr("Update modules")
                );

                future = service.submit(task2);
                try {
                    future.get();
                } catch(ExecutionException e) {
                    e.printStackTrace();
                    alertFailedModuleUpdate(parent, modulesToUpdate);
                    return modules;
                } catch(InterruptedException e) {
                    e.printStackTrace();
                    alertFailedModuleUpdate(parent, modulesToUpdate);
                    return modules;
                }
                // notify user if downloading a locally installed module failed
                //
                if (! task2.getFailedModules().isEmpty()) {
                    alertFailedModuleUpdate(parent, task2.getFailedModules());
                    return modules;
                }
            }
        } finally {
            monitor.finishTask();
        }
        // remember the update because it was successful
        //
        Main.pref.put("opendata.modulemanager.lastupdate", Long.toString(System.currentTimeMillis()));
        return modules;
    }

    /**
     * Ask the user for confirmation that a module shall be disabled.
     *
     * @param reason the reason for disabling the module
     * @param name the module name
     * @return true, if the module shall be disabled; false, otherwise
     */
    public static boolean confirmDisableModule(Component parent, String reason, String name) {
        ButtonSpec [] options = new ButtonSpec[] {
                new ButtonSpec(
                        tr("Disable module"),
                        ImageProvider.get("dialogs", "delete"),
                        tr("Click to delete the module ''{0}''", name),
                        null /* no specific help context */
                ),
                new ButtonSpec(
                        tr("Keep module"),
                        ImageProvider.get("cancel"),
                        tr("Click to keep the module ''{0}''", name),
                        null /* no specific help context */
                )
        };
        int ret = HelpAwareOptionPane.showOptionDialog(
                parent,
                reason,
                tr("Disable module"),
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0],
                null // FIXME: add help topic
        );
        return ret == 0;
    }

    /*public static Module getModule(String name) {
        for (Module module : moduleList)
            if (module.getModuleInformation().name.equals(name))
                return module;
        return null;
    }*/

    /**
     * Installs downloaded modules. Moves files with the suffix ".jar.new" to the corresponding
     * ".jar" files.
     *
     * If {@code dowarn} is true, this methods emits warning messages on the console if a downloaded
     * but not yet installed module .jar can't be be installed. If {@code dowarn} is false, the
     * installation of the respective module is sillently skipped.
     *
     * @param dowarn if true, warning messages are displayed; false otherwise
     */
    public static void installDownloadedModules(boolean dowarn) {
        File moduleDir = OdPlugin.getInstance().getModulesDirectory();
        if (! moduleDir.exists() || ! moduleDir.isDirectory() || ! moduleDir.canWrite())
            return;

        final File[] files = moduleDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar.new");
            }});

        for (File updatedModule : files) {
            final String filePath = updatedModule.getPath();
            File module = new File(filePath.substring(0, filePath.length() - 4));
            String moduleName = updatedModule.getName().substring(0, updatedModule.getName().length() - 8);
            if (module.exists()) {
                if (!module.delete() && dowarn) {
                    System.err.println(tr("Warning: failed to delete outdated module ''{0}''.", module.toString()));
                    System.err.println(tr("Warning: failed to install already downloaded module ''{0}''. Skipping installation. JOSM is still going to load the old module version.", moduleName));
                    continue;
                }
            }
            if (!updatedModule.renameTo(module) && dowarn) {
                System.err.println(tr("Warning: failed to install module ''{0}'' from temporary download file ''{1}''. Renaming failed.", module.toString(), updatedModule.toString()));
                System.err.println(tr("Warning: failed to install already downloaded module ''{0}''. Skipping installation. JOSM is still going to load the old module version.", moduleName));
            }
        }
        return;
    }

    /*private static boolean confirmDeactivatingModuleAfterException(Module module) {
        ButtonSpec [] options = new ButtonSpec[] {
                new ButtonSpec(
                        tr("Disable module"),
                        ImageProvider.get("dialogs", "delete"),
                        tr("Click to disable the module ''{0}''", module.getModuleInformation().name),
                        null // no specific help context
                ),
                new ButtonSpec(
                        tr("Keep module"),
                        ImageProvider.get("cancel"),
                        tr("Click to keep the module ''{0}''",module.getModuleInformation().name),
                        null // no specific help context
                )
        };

        StringBuffer msg = new StringBuffer();
        msg.append("<html>");
        msg.append(tr("An unexpected exception occurred that may have come from the ''{0}'' module.", module.getModuleInformation().name));
        msg.append("<br>");
        if(module.getModuleInformation().author != null) {
            msg.append(tr("According to the information within the module, the author is {0}.", module.getModuleInformation().author));
            msg.append("<br>");
        }
        msg.append(tr("Try updating to the newest version of this module before reporting a bug."));
        msg.append("<br>");
        msg.append(tr("Should the module be disabled?"));
        msg.append("</html>");

        int ret = HelpAwareOptionPane.showOptionDialog(
                Main.parent,
                msg.toString(),
                tr("Update modules"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0],
                null
        );
        return ret == 0;
    }*/

    /**
     * Replies the module which most likely threw the exception <code>ex</code>.
     *
     * @param ex the exception
     * @return the module; null, if the exception probably wasn't thrown from a module
     */
    /*private static Module getModuleCausingException(Throwable ex) {
        Module err = null;
        StackTraceElement[] stack = ex.getStackTrace();
        // remember the error position, as multiple modules may be involved,
        //  we search the topmost one
        int pos = stack.length;
        for (Module p : moduleList) {
            String baseClass = p.getModuleInformation().className;
            baseClass = baseClass.substring(0, baseClass.lastIndexOf("."));
            for (int elpos = 0; elpos < pos; ++elpos) {
                if (stack[elpos].getClassName().startsWith(baseClass)) {
                    pos = elpos;
                    err = p;
                }
            }
        }
        return err;
    }*/

    /**
     * Checks whether the exception <code>e</code> was thrown by a module. If so,
     * conditionally deactivates the module, but asks the user first.
     *
     * @param e the exception
     */
    /*public static void disableModuleAfterException(Throwable e) {
        Module module = null;
        // Check for an explicit problem when calling a module function
        if (e instanceof ModuleException) {
            module = ((ModuleException) e).module;
        }
        if (module == null) {
            module = getModuleCausingException(e);
        }
        if (module == null)
            // don't know what module threw the exception
            return;

        Set<String> modules = new HashSet<String>(
                Main.pref.getCollection(PREF_MODULES, Collections.<String> emptySet())
        );
        if (! modules.contains(module.getModuleInformation().name))
            // module not activated ? strange in this context but anyway, don't bother
            // the user with dialogs, skip conditional deactivation
            return;

        if (!confirmDeactivatingModuleAfterException(module))
            // user doesn't want to deactivate the module
            return;

        // deactivate the module
        modules.remove(module.getModuleInformation().name);
        Main.pref.putCollection(PREF_MODULES, modules);
        JOptionPane.showMessageDialog(
                Main.parent,
                tr("The module has been removed from the configuration. Please restart JOSM to unload the module."),
                tr("Information"),
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }*/

    /*public static String getBugReportText() {
        String text = "";
        LinkedList <String> pl = new LinkedList<String>(Main.pref.getCollection(PREF_MODULES, new LinkedList<String>()));
        for (final Module pp : moduleList) {
            ModuleInformation pi = pp.getModuleInformation();
            pl.remove(pi.name);
            pl.add(pi.name + " (" + (pi.localversion != null && !pi.localversion.equals("")
                    ? pi.localversion : "unknown") + ")");
        }
        Collections.sort(pl);
        for (String s : pl) {
            text += "Module: " + s + "\n";
        }
        return text;
    }*/

    /*public static JPanel getInfoPanel() {
        JPanel moduleTab = new JPanel(new GridBagLayout());
        for (final Module p : moduleList) {
            final ModuleInformation info = p.getModuleInformation();
            String name = info.name
            + (info.version != null && !info.version.equals("") ? " Version: " + info.version : "");
            moduleTab.add(new JLabel(name), GBC.std());
            moduleTab.add(Box.createHorizontalGlue(), GBC.std().fill(GBC.HORIZONTAL));
            moduleTab.add(new JButton(new AbstractAction(tr("Information")) {
                public void actionPerformed(ActionEvent event) {
                    StringBuilder b = new StringBuilder();
                    for (Entry<String, String> e : info.attr.entrySet()) {
                        b.append(e.getKey());
                        b.append(": ");
                        b.append(e.getValue());
                        b.append("\n");
                    }
                    JTextArea a = new JTextArea(10, 40);
                    a.setEditable(false);
                    a.setText(b.toString());
                    JOptionPane.showMessageDialog(Main.parent, new JScrollPane(a), tr("Module information"),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }), GBC.eol());

            JTextArea description = new JTextArea((info.description == null ? tr("no description available")
                    : info.description));
            description.setEditable(false);
            description.setFont(new JLabel().getFont().deriveFont(Font.ITALIC));
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            description.setBackground(UIManager.getColor("Panel.background"));

            moduleTab.add(description, GBC.eop().fill(GBC.HORIZONTAL));
        }
        return moduleTab;
    }*/

    static private class UpdateModulesMessagePanel extends JPanel {
        private JMultilineLabel lblMessage;
        private JCheckBox cbDontShowAgain;

        protected void build() {
            setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.NORTHWEST;
            gc.fill = GridBagConstraints.BOTH;
            gc.weightx = 1.0;
            gc.weighty = 1.0;
            gc.insets = new Insets(5,5,5,5);
            add(lblMessage = new JMultilineLabel(""), gc);
            lblMessage.setFont(lblMessage.getFont().deriveFont(Font.PLAIN));

            gc.gridy = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weighty = 0.0;
            add(cbDontShowAgain = new JCheckBox(tr("Do not ask again and remember my decision (go to Preferences->Modules to change it later)")), gc);
            cbDontShowAgain.setFont(cbDontShowAgain.getFont().deriveFont(Font.PLAIN));
        }

        public UpdateModulesMessagePanel() {
            build();
        }

        public void setMessage(String message) {
            lblMessage.setText(message);
        }

        public void initDontShowAgain(String preferencesKey) {
            String policy = Main.pref.get(preferencesKey, "ask");
            policy = policy.trim().toLowerCase();
            cbDontShowAgain.setSelected(! policy.equals("ask"));
        }

        public boolean isRememberDecision() {
            return cbDontShowAgain.isSelected();
        }
    }
}
