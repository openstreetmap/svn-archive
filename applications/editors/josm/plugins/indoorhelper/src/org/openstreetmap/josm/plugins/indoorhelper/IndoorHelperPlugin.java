/*
 * Indoorhelper is a JOSM plug-in to support users when creating their own indoor maps.
 *  Copyright (C) 2016  Erik Gruschka
 *  Copyright (C) 2018  Rebecca Schmidt
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.indoorhelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.autofilter.AutoFilter;
import org.openstreetmap.josm.gui.autofilter.AutoFilterManager;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.MapViewPaintable.PaintableInvalidationEvent;
import org.openstreetmap.josm.gui.layer.MapViewPaintable.PaintableInvalidationListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import controller.IndoorHelperController;

/**
 * This is the main class for the indoorhelper plug-in.
 *
 * @author egru
 * @author rebsc
 *
 */
public class IndoorHelperPlugin extends Plugin implements PaintableInvalidationListener, ActiveLayerChangeListener {

    @SuppressWarnings("unused")
    private IndoorHelperController controller;
    String sep = System.getProperty("file.separator");

    /**
     * Constructor for the plug-in.
     *
     * Exports the needed files and adds them to the settings.
     *
     * @param info general information about the plug-in
     */
    public IndoorHelperPlugin(PluginInformation info) throws Exception {
        super(info);
        MainApplication.getLayerManager().addAndFireActiveLayerChangeListener(this);
        this.exportValidator("/data/indoorhelper.validator.mapcss");
        this.exportStyleFile("indoor.mapcss");
        this.exportStyleFile("entrance_door_icon.png");
        this.exportStyleFile("entrance_icon.png");
        this.exportStyleFile("elevator_icon.png");

        //this.setIndoorValidator();
    }


    /**
     * Secures that the plug-in is only loaded, if a new MapFrame is created.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);

        if (oldFrame == null && newFrame != null) {
            controller = new IndoorHelperController();
        }
    }


    /**
     * Exports the mapcss validator file to the preferences directory.
     */
    private void exportValidator(String resourceName) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;

        try {
            stream = IndoorHelperPlugin.class.getResourceAsStream(resourceName);
            if (stream == null) {
                System.out.println("Validator: stream is null");
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            String outPath;
            int readBytes;
            byte[] buffer = new byte[4096];

            String valDirPath = Main.pref.getDirs().getUserDataDirectory(true) + sep + "validator";
            File valDir = new File(valDirPath);
            valDir.mkdirs();
            outPath = valDir.getAbsolutePath() +sep+ "indoorhelper.validator.mapcss";
            System.out.println("Validator:"+outPath);

            resStreamOut = new FileOutputStream(outPath);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            resStreamOut.close();
        } catch (Exception ex) {
            throw ex;
        } finally {
            stream.close();
        }
    }

    /**
     * Exports the mapCSS file to the preferences directory.
     */
    private void exportStyleFile(String resourceName) throws Exception {
        try (InputStream stream = IndoorHelperPlugin.class.getResourceAsStream("/data/" + resourceName)) {
            if (stream == null) {
                System.out.println("MapPaint: stream is null");
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            String outPath;
            int readBytes;
            byte[] buffer = new byte[4096];

            String valDirPath = Main.pref.getDirs().getUserDataDirectory(true) + sep + "styles";
            File valDir = new File(valDirPath);
            valDir.mkdirs();
            outPath = valDir.getAbsolutePath() +sep+ resourceName;

            try (OutputStream resStreamOut = new FileOutputStream(outPath)) {
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        OsmDataLayer editLayer = MainApplication.getLayerManager().getEditLayer();
        if (editLayer != null) {
            editLayer.addInvalidationListener(this);
        }
    }

    @Override
    public void paintableInvalidated(PaintableInvalidationEvent event) {
        AutoFilter currentAutoFilter = AutoFilterManager.getInstance().getCurrentAutoFilter();
        String currentFilterValue = new String();

        if (currentAutoFilter != null) {
            currentFilterValue = currentAutoFilter.getFilter().text.split("=")[1];

            this.controller.setIndoorLevel(currentFilterValue);
            this.controller.getIndoorLevel(currentFilterValue);
            this.controller.unsetSpecificKeyFilter("repeat_on");

        } else {
            currentFilterValue = "";
            this.controller.setIndoorLevel(currentFilterValue);
            this.controller.getIndoorLevel(currentFilterValue);
        }
    }

    /**
     * Writes the indoor validator file in the user preferences if it isn't there
     * and activates it.
     */
//    private void setIndoorValidator() {
//        //get the current validator settings
//        Map<String, Setting<?>> settings =  Main.pref.getAllSettings();
//        MapListSetting mapListSetting = (MapListSetting) settings.
//                get("validator.org.openstreetmap.josm.data.validation.tests.MapCSSTagChecker.entries");
//        List<Map<String, String>> validatorMaps;
//        if (mapListSetting != null) {
//            validatorMaps = mapListSetting.getValue();
//        } else {
//            validatorMaps = new ArrayList<>();
//        }
//        boolean validatorExists = false;
//
//        //check if indoor validator is already set
//        for (Map<String, String> map : validatorMaps) {
//            if (map.containsValue("Indoor")) {
//                validatorExists = true;
//            }
//        }
//
//        //put it in the settings if not
//        if (!validatorExists) {
//            List<Map<String, String>> validatorMapsNew = new ArrayList<>();
//            if (!validatorMaps.isEmpty()) {
//                validatorMapsNew.addAll(validatorMaps);
//            }
//            Map<String, String> indoorValidator = new HashMap<>();
//            indoorValidator.put("title", "Indoor");
//            indoorValidator.put("active", "true");
//            indoorValidator.put("url", Main.pref.getUserDataDirectory()+ sep +"validator" +
//                    sep + "indoorhelper.validator.mapcss");
//
//            validatorMapsNew.add(indoorValidator);
//            Main.pref.putListOfStructs
//            ("validator.org.openstreetmap.josm.data.validation.tests.MapCSSTagChecker.entries",
//                    validatorMapsNew);
//        }
//    }
}
