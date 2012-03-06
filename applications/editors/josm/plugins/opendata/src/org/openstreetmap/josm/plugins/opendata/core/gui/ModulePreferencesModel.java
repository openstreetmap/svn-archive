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
package org.openstreetmap.josm.plugins.opendata.core.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleException;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;

/**
 * TODO
 *
 */
public class ModulePreferencesModel extends Observable implements OdConstants {
    private final ArrayList<ModuleInformation> availableModules = new ArrayList<ModuleInformation>();
    private final ArrayList<ModuleInformation> displayedModules = new ArrayList<ModuleInformation>();
    private final HashMap<ModuleInformation, Boolean> selectedModulesMap = new HashMap<ModuleInformation, Boolean>();
    private Set<String> pendingDownloads = new HashSet<String>();
    private String filterExpression;
    private final Set<String> currentActiveModules;

    protected Collection<String> getModules(Collection<String> def) {
    	return Main.pref.getCollection(PREF_MODULES, def);
    }
    
    public ModulePreferencesModel() {
    	currentActiveModules = new HashSet<String>();
    	currentActiveModules.addAll(getModules(currentActiveModules));
    }

    public void filterDisplayedModules(String filter) {
        if (filter == null) {
            displayedModules.clear();
            displayedModules.addAll(availableModules);
            this.filterExpression = null;
            return;
        }
        displayedModules.clear();
        for (ModuleInformation pi: availableModules) {
            if (pi.matches(filter)) {
                displayedModules.add(pi);
            }
        }
        filterExpression = filter;
        clearChanged();
        notifyObservers();
    }

    public void setAvailableModules(Collection<ModuleInformation> available) {
        availableModules.clear();
        if (available != null) {
            availableModules.addAll(available);
        }
        sort();
        filterDisplayedModules(filterExpression);
        Set<String> activeModules = new HashSet<String>();
        activeModules.addAll(getModules(activeModules));
        for (ModuleInformation pi: availableModules) {
            if (selectedModulesMap.get(pi) == null) {
                if (activeModules.contains(pi.name)) {
                    selectedModulesMap.put(pi, true);
                }
            }
        }
        clearChanged();
        notifyObservers();
    }

    protected  void updateAvailableModule(ModuleInformation other) {
        if (other == null) return;
        ModuleInformation pi = getModuleInformation(other.name);
        if (pi == null) {
            availableModules.add(other);
            return;
        }
        pi.updateFromModuleSite(other);
    }

    /**
     * Updates the list of module information objects with new information from
     * module update sites.
     *
     * @param fromModuleSite module information read from module update sites
     */
    public void updateAvailableModules(Collection<ModuleInformation> fromModuleSite) {
        for (ModuleInformation other: fromModuleSite) {
            updateAvailableModule(other);
        }
        sort();
        filterDisplayedModules(filterExpression);
        Set<String> activeModules = new HashSet<String>();
        activeModules.addAll(getModules(activeModules));
        for (ModuleInformation pi: availableModules) {
            if (selectedModulesMap.get(pi) == null) {
                if (activeModules.contains(pi.name)) {
                    selectedModulesMap.put(pi, true);
                }
            }
        }
        clearChanged();
        notifyObservers();
    }

    /**
     * Replies the list of selected module information objects
     *
     * @return the list of selected module information objects
     */
    public List<ModuleInformation> getSelectedModules() {
        List<ModuleInformation> ret = new LinkedList<ModuleInformation>();
        for (ModuleInformation pi: availableModules) {
            if (selectedModulesMap.get(pi) == null) {
                continue;
            }
            if (selectedModulesMap.get(pi)) {
                ret.add(pi);
            }
        }
        return ret;
    }

    /**
     * Replies the list of selected module information objects
     *
     * @return the list of selected module information objects
     */
    public Set<String> getSelectedModuleNames() {
        Set<String> ret = new HashSet<String>();
        for (ModuleInformation pi: getSelectedModules()) {
            ret.add(pi.name);
        }
        return ret;
    }

    /**
     * Sorts the list of available modules
     */
    protected void sort() {
        Collections.sort(
                availableModules,
                new Comparator<ModuleInformation>() {
                    public int compare(ModuleInformation o1, ModuleInformation o2) {
                        String n1 = o1.getName() == null ? "" : o1.getName().toLowerCase();
                        String n2 = o2.getName() == null ? "" : o2.getName().toLowerCase();
                        return n1.compareTo(n2);
                    }
                }
        );
    }

    /**
     * Replies the list of module informations to display
     *
     * @return the list of module informations to display
     */
    public List<ModuleInformation> getDisplayedModules() {
        return displayedModules;
    }


    /**
     * Replies the list of modules waiting for update or download
     *
     * @return the list of modules waiting for update or download
     */
    /*public List<ModuleInformation> getModulesScheduledForUpdateOrDownload() {
        List<ModuleInformation> ret = new ArrayList<ModuleInformation>();
        for (String module: pendingDownloads) {
            ModuleInformation pi = getModuleInformation(module);
            if (pi == null) {
                continue;
            }
            ret.add(pi);
        }
        return ret;
    }*/

    /**
     * Sets whether the module is selected or not.
     *
     * @param name the name of the module
     * @param selected true, if selected; false, otherwise
     */
    public void setModuleSelected(String name, boolean selected) {
        ModuleInformation pi = getModuleInformation(name);
        if (pi != null) {
            selectedModulesMap.put(pi,selected);
            if (pi.isUpdateRequired()) {
                pendingDownloads.add(pi.name);
            }
        }
        if (!selected) {
            pendingDownloads.remove(name);
        }
    }

    /**
     * Removes all the module in {@code modules} from the list of modules
     * with a pending download
     *
     * @param modules the list of modules to clear for a pending download
     */
    public void clearPendingModules(Collection<ModuleInformation> modules){
        if (modules == null || modules.isEmpty()) return;
        for(ModuleInformation pi: modules) {
            pendingDownloads.remove(pi.name);
        }
    }

    /**
     * Replies the module info with the name <code>name</code>. null, if no
     * such module info exists.
     *
     * @param name the name. If null, replies null.
     * @return the module info.
     */
    public ModuleInformation getModuleInformation(String name) {
        for (ModuleInformation pi: availableModules) {
            if (pi.getName() != null && pi.getName().equals(name))
                return pi;
        }
        return null;
    }

    /**
     * Initializes the model from preferences
     */
    /*public void initFromPreferences() {
        Collection<String> enabledModules = getModules(null);
        if (enabledModules == null) {
            this.selectedModulesMap.clear();
            return;
        }
        for (String name: enabledModules) {
            ModuleInformation pi = getModuleInformation(name);
            if (pi == null) {
                continue;
            }
            setModuleSelected(name, true);
        }
    }*/

    /**
     * Replies true if the module with name <code>name</code> is currently
     * selected in the module model
     *
     * @param name the module name
     * @return true if the module is selected; false, otherwise
     */
    public boolean isSelectedModule(String name) {
        ModuleInformation pi = getModuleInformation(name);
        if (pi == null) return false;
        if (selectedModulesMap.get(pi) == null) return false;
        return selectedModulesMap.get(pi);
    }

    /**
     * Replies the set of modules which have been added by the user to
     * the set of activated modules.
     *
     * @return the set of newly deactivated modules
     */
    /*public List<ModuleInformation> getNewlyActivatedModules() {
        List<ModuleInformation> ret = new LinkedList<ModuleInformation>();
        for (Entry<ModuleInformation, Boolean> entry: selectedModulesMap.entrySet()) {
            ModuleInformation pi = entry.getKey();
            boolean selected = entry.getValue();
            if (selected && ! currentActiveModules.contains(pi.name)) {
                ret.add(pi);
            }
        }
        return ret;
    }*/

    /**
     * Replies the set of modules which have been removed by the user from
     * the set of activated modules.
     *
     * @return the set of newly deactivated modules
     */
    /*public List<ModuleInformation> getNewlyDeactivatedModules() {
        List<ModuleInformation> ret = new LinkedList<ModuleInformation>();
        for (ModuleInformation pi: availableModules) {
            if (!currentActiveModules.contains(pi.name)) {
                continue;
            }
            if (selectedModulesMap.get(pi) == null || ! selectedModulesMap.get(pi)) {
                ret.add(pi);
            }
        }
        return ret;
    }*/

    /**
     * Replies the set of module names which have been added by the user to
     * the set of activated modules.
     *
     * @return the set of newly activated module names
     */
    /*public Set<String> getNewlyActivatedModuleNames() {
        Set<String> ret = new HashSet<String>();
        List<ModuleInformation> modules = getNewlyActivatedModules();
        for (ModuleInformation pi: modules) {
            ret.add(pi.name);
        }
        return ret;
    }*/

    /**
     * Replies true if the set of active modules has been changed by the user
     * in this preference model. He has either added modules or removed modules
     * being active before.
     *
     * @return true if the collection of active modules has changed
     */
    public boolean isActiveModulesChanged() {
        Set<String> newActiveModules = getSelectedModuleNames();
        return ! newActiveModules.equals(currentActiveModules);
    }

    /**
     * Refreshes the local version field on the modules in <code>modules</code> with
     * the version in the manifest of the downloaded "jar.new"-file for this module.
     *
     * @param modules the collections of modules to refresh
     */
    public void refreshLocalModuleVersion(Collection<ModuleInformation> modules) {
        if (modules == null) return;
        File moduleDir = OdPlugin.getInstance().getModulesDirectory();
        for (ModuleInformation pi : modules) {
            // Find the downloaded file. We have tried to install the downloaded modules
            // (ModuleHandler.installDownloadedModules). This succeeds depending on the
            // platform.
            File downloadedModuleFile = new File(moduleDir, pi.name + ".jar.new");
            if (!(downloadedModuleFile.exists() && downloadedModuleFile.canRead())) {
                downloadedModuleFile = new File(moduleDir, pi.name + ".jar");
                if (!(downloadedModuleFile.exists() && downloadedModuleFile.canRead())) {
                    continue;
                }
            }
            try {
                ModuleInformation newinfo = new ModuleInformation(downloadedModuleFile, pi.name);
                ModuleInformation oldinfo = getModuleInformation(pi.name);
                if (oldinfo == null) {
                    // should not happen
                    continue;
                }
                oldinfo.localversion = newinfo.version;
            } catch(ModuleException e) {
                e.printStackTrace();
            }
        }
    }
}
