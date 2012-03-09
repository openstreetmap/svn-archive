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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
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
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleHandler;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;

public final class OdPlugin extends Plugin implements OdConstants {

	private static OdPlugin instance;
	
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
				new XmlImporter() // Generic importer for XML files (currently used for Neptune files)
		})) {
			ExtensionFileFilter.importers.add(0, importer);
		}
        // Load modules
        loadModules();
	}
	
	public static final OdPlugin getInstance() {
		return instance;
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
