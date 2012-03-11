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
package org.openstreetmap.josm.plugins.opendata;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MenuScroller;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.actions.DownloadDataAction;
import org.openstreetmap.josm.plugins.opendata.core.actions.DownloadDataTask;
import org.openstreetmap.josm.plugins.opendata.core.actions.OpenPreferencesActions;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.DataSetCategory;
import org.openstreetmap.josm.plugins.opendata.core.gui.OdDialog;
import org.openstreetmap.josm.plugins.opendata.core.gui.OdPreferenceSetting;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.XmlImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.ZipImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.KmlKmzImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifTabImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.ShpImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.CsvImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.OdsImporter;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.XlsImporter;
import org.openstreetmap.josm.plugins.opendata.core.modules.Module;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleHandler;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.tools.Pair;

public final class OdPlugin extends Plugin implements OdConstants {

	private static OdPlugin instance;
	
	public final XmlImporter xmlImporter;
	
	private final JMenu menu;
	
	public OdPlugin(PluginInformation info) { // NO_UCD
		super(info);
		if (instance == null) {
			instance = this;
		} else {
			throw new IllegalAccessError("Cannot instantiate plugin twice !");
		}
        // Allow JOSM to import more files
		for (AbstractImporter importer : Arrays.asList(new AbstractImporter[]{
				new CsvImporter(), new OdsImporter(), new XlsImporter(), // Tabular file formats
				new KmlKmzImporter(), new ShpImporter(), new MifTabImporter(), // Geographic file formats
				new ZipImporter(), // Archive containing any of the others
				xmlImporter = new XmlImporter() // Generic importer for XML files (currently used for Neptune files)
		})) {
			ExtensionFileFilter.importers.add(0, importer);
		}
        // Load modules
        loadModules();
        // Add menu
        menu = Main.main.menu.addMenu(marktr("Open Data"), KeyEvent.VK_O, Main.main.menu.defaultMenuPos, ht("/Plugin/OpenData"));
        buildMenu();
        // Add download task
        Main.main.menu.openLocation.addDownloadTaskClass(DownloadDataTask.class);
	}
	
	public static final OdPlugin getInstance() {
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
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        for (Module module : ModuleHandler.moduleList) {
        	Map<DataSetCategory, JMenu> catMenus = new HashMap<DataSetCategory, JMenu>();
        	JMenu moduleMenu = null;
        	for (AbstractDataSetHandler handler: module.getHandlers()) {
        		if (handler.getDataURL() != null || (handler.getDataURLs() != null && !handler.getDataURLs().isEmpty())) {
        			if (moduleMenu == null) {
        				moduleMenu = getModuleMenu(module);
        			}
        			DataSetCategory cat = handler.getCategory();
        			JMenu endMenu = null;
        			if (cat != null) {
        				if ((endMenu = catMenus.get(cat)) == null) {
        					catMenus.put(cat, endMenu = new JMenu(cat.getName()));
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
        			if (handler.getDataURL() != null) {
        				endMenu.add(new DownloadDataAction(handlerName, handler.getDataURL()));
        			} else if (handler.getDataURLs() != null) {
        				JMenu handlerMenu = new JMenu(handlerName);
        				JMenuItem item = null;
        				for (Pair<String, URL> pair : handler.getDataURLs()) {
        					if (pair != null && pair.a != null && pair.b != null) {
        						item = handlerMenu.add(new DownloadDataAction(pair.a, pair.b));
        					}
        				}
        				if (item != null) {
        					MenuScroller.setScrollerFor(handlerMenu, (screenHeight / item.getPreferredSize().height)-3);
        					endMenu.add(handlerMenu);
        				}
        			}
        		}
        	}
        	if (moduleMenu != null) {
        		//MenuScroller.setScrollerFor(moduleMenu, screenHeight / moduleMenu.getItem(0).getPreferredSize().height);
        		menu.add(moduleMenu);
        	}
        }
        menu.addSeparator();
        /*JMenuItem itemIcon =*/ MainMenu.add(menu, new OpenPreferencesActions());
        //MenuScroller.setScrollerFor(menu, screenHeight / itemIcon.getPreferredSize().height);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.Plugin#mapFrameInitialized(org.openstreetmap.josm.gui.MapFrame, org.openstreetmap.josm.gui.MapFrame)
	 */
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (newFrame != null) {
			newFrame.addToggleDialog(new OdDialog());
		}
	}
	
    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.Plugin#getPreferenceSetting()
     */
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new OdPreferenceSetting();
    }
    
    private final void loadModules() {
        List<ModuleInformation> modulesToLoad = ModuleHandler.buildListOfModulesToLoad(Main.parent);
        if (!modulesToLoad.isEmpty() && ModuleHandler.checkAndConfirmModuleUpdate(Main.parent)) {
            modulesToLoad = ModuleHandler.updateModules(Main.parent, modulesToLoad, null);
        }

        ModuleHandler.installDownloadedModules(true);
    	ModuleHandler.loadModules(Main.parent, modulesToLoad, null);
    }
    
    private final File getSubDirectory(String name) {
    	File dir = new File(getPluginDir()+File.separator+name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    
    public final File getModulesDirectory() {
    	return getSubDirectory("modules");
    }

    public final File getResourcesDirectory() {
    	return getSubDirectory("resources");
    }

    /*
    private static final void fixUcDetectorTest() {
    	FilterFactoryImpl n1 = new FilterFactoryImpl();
    	DatumAliases n2 = new DatumAliases();
    	EPSGCRSAuthorityFactory n3 = new EPSGCRSAuthorityFactory();
    	DefaultFunctionFactory n4 = new DefaultFunctionFactory();
    	ShapefileDirectoryFactory n5 = new ShapefileDirectoryFactory();
    	ReferencingObjectFactory n6 = new ReferencingObjectFactory();
    	BufferedCoordinateOperationFactory n7 = new BufferedCoordinateOperationFactory();
    }*/
}
