// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata;

import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MenuScroller;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.opendata.core.actions.DownloadDataAction;
import org.openstreetmap.josm.plugins.opendata.core.actions.DownloadDataTask;
import org.openstreetmap.josm.plugins.opendata.core.actions.OpenPreferencesActions;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.DataSetCategory;
import org.openstreetmap.josm.plugins.opendata.core.gui.OdDialog;
import org.openstreetmap.josm.plugins.opendata.core.gui.OdPreferenceSetting;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.XmlImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.SevenZipImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.ZipImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.GmlImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.KmlKmzImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifTabImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.ShpImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.CsvImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.OdsImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.XlsImporter;
import org.openstreetmap.josm.plugins.opendata.core.modules.Module;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleHandler;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.tools.Pair;

public final class OdPlugin extends Plugin {

    private static OdPlugin instance;

    public final XmlImporter xmlImporter = new XmlImporter();

    private final JMenu menu;

    private OdDialog dialog;

    public OdPlugin(PluginInformation info) {
        super(info);
        if (instance == null) {
            instance = this;
        } else {
            throw new IllegalAccessError("Cannot instantiate plugin twice !");
        }
        // Allow JOSM to import more files
        for (AbstractImporter importer : Arrays.asList(new AbstractImporter[]{
                new CsvImporter(), new OdsImporter(), new XlsImporter(), // Tabular file formats
                new KmlKmzImporter(), new ShpImporter(), new MifTabImporter(), new GmlImporter(), // Geographic file formats
                new ZipImporter(), // Zip archive containing any of the others
                new SevenZipImporter(), // 7Zip archive containing any of the others
                xmlImporter // Generic importer for XML files (currently used for Neptune files)
        })) {
            ExtensionFileFilter.addImporterFirst(importer);
        }

        menu = Main.main.menu.dataMenu;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Load modules in new thread
                loadModules();
                // Add menu in EDT
                GuiHelper.runInEDT(new Runnable() {
                    @Override
                    public void run() {
                        buildMenu();
                    }
                });
            }
        }).start();

        // Add download task
        Main.main.menu.openLocation.addDownloadTaskClass(DownloadDataTask.class);
        // Delete previous temp dirs if any (old plugin versions did not remove them correctly)
        OdUtils.deletePreviousTempDirs();
    }

    public static OdPlugin getInstance() {
        return instance;
    }

    private JMenu getModuleMenu(Module module) {
        String moduleName = module.getDisplayedName();
        if (moduleName == null || moduleName.isEmpty()) {
            moduleName = module.getModuleInformation().getName();
        }
        JMenu moduleMenu = new JMenu(moduleName);
        moduleMenu.setIcon(module.getModuleInformation().getScaledIcon());
        return moduleMenu;
    }

    private void buildMenu() {
        for (Module module : ModuleHandler.moduleList) {
            Map<DataSetCategory, JMenu> catMenus = new HashMap<>();
            JMenu moduleMenu = null;
            for (AbstractDataSetHandler handler: module.getNewlyInstanciatedHandlers()) {
                URL dataURL = handler.getDataURL();
                List<Pair<String, URL>> dataURLs = handler.getDataURLs();
                if (dataURL != null || (dataURLs != null && !dataURLs.isEmpty())) {
                    if (moduleMenu == null) {
                        moduleMenu = getModuleMenu(module);
                    }
                    DataSetCategory cat = handler.getCategory();
                    JMenu endMenu = null;
                    if (cat != null) {
                        if ((endMenu = catMenus.get(cat)) == null) {
                            catMenus.put(cat, endMenu = new JMenu(cat.getName()));
                            setMenuItemIcon(cat.getIcon(), endMenu);
                            moduleMenu.add(endMenu);
                        }
                    }
                    if (endMenu == null) {
                        endMenu = moduleMenu;
                    }
                    String handlerName = handler.getName();
                    if (handlerName == null || handlerName.isEmpty()) {
                        handlerName = handler.getClass().getName();
                    }
                    JMenuItem handlerItem = null;
                    if (dataURL != null) {
                        handlerItem = endMenu.add(new DownloadDataAction(module, handlerName, dataURL));
                    } else if (dataURLs != null) {
                        JMenu handlerMenu = new JMenu(handlerName);
                        JMenuItem item = null;
                        for (Pair<String, URL> pair : dataURLs) {
                            if (pair != null && pair.a != null && pair.b != null) {
                                item = handlerMenu.add(new DownloadDataAction(module, pair.a, pair.b));
                            }
                        }
                        if (item != null) {
                            MenuScroller.setScrollerFor(handlerMenu);
                            handlerItem = endMenu.add(handlerMenu);
                        }
                    }
                    if (handlerItem != null) {
                        setMenuItemIcon(handler.getMenuIcon(), handlerItem);
                    }
                }
            }
            if (moduleMenu != null) {
                menu.add(moduleMenu);
            }
        }
        menu.addSeparator();
        MainMenu.add(menu, new OpenPreferencesActions());
    }

    private void setMenuItemIcon(ImageIcon icon, JMenuItem menuItem) {
        if (icon != null) {
            if (icon.getIconHeight() != 16 || icon.getIconWidth() != 16) {
                icon = new ImageIcon(icon.getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
            }
            menuItem.setIcon(icon);
        }
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            newFrame.addToggleDialog(dialog = new OdDialog());
        } else {
            dialog = null;
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new OdPreferenceSetting();
    }

    private void loadModules() {
        List<ModuleInformation> modulesToLoad = ModuleHandler.buildListOfModulesToLoad(Main.parent);
        if (!modulesToLoad.isEmpty() && ModuleHandler.checkAndConfirmModuleUpdate(Main.parent)) {
            modulesToLoad = ModuleHandler.updateModules(Main.parent, modulesToLoad, null);
        }

        ModuleHandler.installDownloadedModules(true);
        ModuleHandler.loadModules(Main.parent, modulesToLoad, null);
    }

    private File getSubDirectory(String name) {
        File dir = new File(getPluginDir()+File.separator+name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public File getModulesDirectory() {
        return getSubDirectory("modules");
    }

    public File getResourcesDirectory() {
        return getSubDirectory("resources");
    }

    public OdDialog getDialog() {
        return dialog;
    }
}
