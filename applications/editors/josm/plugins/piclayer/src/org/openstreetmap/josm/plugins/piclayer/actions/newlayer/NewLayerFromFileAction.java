/***************************************************************************
 *   Copyright (C) 2009 by Tomasz Stelmach                                 *
 *   http://www.stelmach-online.net/                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package org.openstreetmap.josm.plugins.piclayer.actions.newlayer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerFromFile;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerFromKML;
import org.openstreetmap.josm.plugins.piclayer.layer.kml.KMLGroundOverlay;
import org.openstreetmap.josm.plugins.piclayer.layer.kml.KMLReader;

/**
 * Action responsible for creation of new layers based on image files.
 */
@SuppressWarnings("serial")
public class NewLayerFromFileAction extends JosmAction {

    String m_lastdirprefname = "piclayer.lastdir";

    /**
     * Provides filtering of only image files.
     */
    private class ImageFileFilter extends FileFilter {

        private String[] supportedExtensions;

        public ImageFileFilter() {
            List<String> extensions = new ArrayList<String>();
            extensions.add("zip");
            extensions.add("kml");
            for (String ext : ImageIO.getReaderFormatNames())
                extensions.add(ext);
            supportedExtensions = extensions.toArray(new String[0]);
        }

        @Override
        public boolean accept(File f) {
            if ( f.isDirectory() )
                return true;

            String fileExtension = PicLayerFromFile.getFileExtension(f);

            // Unfortunately, getReaderFormatNames does not always return ALL extensions in
            // both lower and upper case, so we can not do a search in the array
            for (String e: supportedExtensions)
                if ( e.equalsIgnoreCase(fileExtension) ) {
                    return true;
                }

            return false;
        }


        @Override
        public String getDescription() {
            return tr("Supported image files, *.zip, *.kml");
        }

    }

    /**
     * Constructor...
     */
    public NewLayerFromFileAction() {
        super(tr("New picture layer from file..."), "layericon24", null, null, false);
    }

    /**
     * Action handler
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {

        // Choose a file
        JFileChooser fc = new JFileChooser(Main.pref.get(m_lastdirprefname));
        fc.setAcceptAllFileFilterUsed( true );
        fc.setFileFilter( new ImageFileFilter() );

        fc.setMultiSelectionEnabled(true);
        int result = fc.showOpenDialog( Main.parent );

        // Create a layer?
        if ( result == JFileChooser.APPROVE_OPTION ) {
            // The first loaded layer will be placed at the top of any other layer of the same class,
            // or at the bottom of the stack if there is no such layer yet
            // The next layers we load will be placed one after the other after this first layer
            int newLayerPos = Main.map.mapView.getAllLayers().size();
            for(Layer l : Main.map.mapView.getLayersOfType(PicLayerAbstract.class)) {
                int pos = Main.map.mapView.getLayerPos(l);
                if (pos < newLayerPos) newLayerPos = pos;
            }

            for(File file : fc.getSelectedFiles() ) {
                // TODO: we need a progress bar here, it can take quite some time

                Main.pref.put(m_lastdirprefname, file.getParent());

                // Create layer from file
                if ("kml".equalsIgnoreCase(PicLayerFromFile.getFileExtension(file))) {
                    KMLReader kml = new KMLReader(file);
                    kml.process();
                    JOptionPane.showMessageDialog(null, tr("KML calibration is in beta stage and may produce incorrectly calibrated layers!\nPlease use http://josm.openstreetmap.de/ticket/5451 to upload your KMLs that were calibrated incorrectly."));
                    for (KMLGroundOverlay overlay : kml.getGroundOverlays()) {
                        //TODO: zoom to whole picture, not only the last
                        addNewLayerFromKML(file, overlay, newLayerPos);
                    }

                } else {
                addNewLayerFromFile(file, newLayerPos, fc.getSelectedFiles().length == 1);

            }
        }
    }
}

    private void addNewLayerFromFile(File file, int newLayerPos, boolean isZoomToLayer) {
        try {
            PicLayerFromFile layer = new PicLayerFromFile( file );
            layer.initialize();

            placeLayer(layer, newLayerPos, isZoomToLayer);
        }
        catch (IOException e) {
            // Failed
            System.out.println( "NewLayerFromFileAction::actionPerformed - " + e.getMessage() );
            JOptionPane.showMessageDialog(null, e.getMessage() );
        }
    }

    private void placeLayer(PicLayerAbstract layer, int newLayerPos, boolean isZoomToLayer) throws IOException {
        // Add layer only if successfully initialized

        Main.main.addLayer( layer );
        Main.map.mapView.moveLayer(layer, newLayerPos++);

        if ( isZoomToLayer && Main.pref.getInteger("piclayer.zoom-on-load", 1) != 0 ) {
            // if we are loading a single picture file, zoom on it, so that the user can see something
            BoundingXYVisitor v = new BoundingXYVisitor();
            layer.visitBoundingBox(v);
            Main.map.mapView.recalculateCenterScale(v);
        }
    }
    private void addNewLayerFromKML(File root, KMLGroundOverlay overlay, int newLayerPos) {
        try {
            PicLayerFromKML layer = new PicLayerFromKML(root, overlay);
            layer.initialize();

            placeLayer(layer, newLayerPos, true);
        } catch (IOException e) {
            // Failed
            System.out.println( "NewLayerFromFileAction::actionPerformed - " + e.getMessage() );
            JOptionPane.showMessageDialog(null, e.getMessage() );
        }
    }
}
