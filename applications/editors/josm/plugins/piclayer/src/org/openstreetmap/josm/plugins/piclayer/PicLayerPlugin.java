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

package org.openstreetmap.josm.plugins.piclayer;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.piclayer.actions.newlayer.NewLayerFromClipboardAction;
import org.openstreetmap.josm.plugins.piclayer.actions.newlayer.NewLayerFromFileAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.MovePictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.RotatePictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.ScaleXPictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.ScaleXYPictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.ScaleYPictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.ShearPictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.affine.MovePointAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.affine.TransformPointAction;

/**
 * Main Plugin class.
 */
public class PicLayerPlugin extends Plugin implements LayerChangeListener {


    // Toolbar buttons
    static IconToggleButton movePictureButton = null;
    static IconToggleButton movePointButton = null;
    static IconToggleButton transformPointButton = null;
    static IconToggleButton rotatePictureButton = null;
    static IconToggleButton scalexPictureButton = null;
    static IconToggleButton scaleyPictureButton = null;
    static IconToggleButton scalexyPictureButton = null;
    static IconToggleButton shearPictureButton = null;

    // Plugin menu
    private JMenu menu = null;
    private ActionVisibilityChangeMenu actionVisibility;

    /**
     * Constructor...
     */
    public PicLayerPlugin(PluginInformation info) {
        super(info);

        // Create menu entry
        if ( Main.main.menu != null ) {
            menu = Main.main.menu.addMenu(marktr("PicLayer") , KeyEvent.VK_I, Main.main.menu.defaultMenuPos, ht("/Plugin/PicLayer"));
        }

        // Add menu items
        if ( menu != null ) {
            menu.add(new NewLayerFromFileAction());
            menu.add(new NewLayerFromClipboardAction());
            menu.setEnabled(false);
        }

        // Listen to layers
        MapView.addLayerChangeListener(this);
    }

    /**
     * Called when the map is created. Creates the toolbar buttons.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if(newFrame != null) {
            // Create plugin map modes
            MovePictureAction movePictureAction = new MovePictureAction(newFrame);
            MovePointAction movePointAction = new MovePointAction(newFrame);
            TransformPointAction transformPointAction = new TransformPointAction(newFrame);

            RotatePictureAction rotatePictureAction = new RotatePictureAction(newFrame);
            ScaleXYPictureAction scaleXYPictureAction = new ScaleXYPictureAction(newFrame);
            ScaleXPictureAction scaleXPictureAction = new ScaleXPictureAction(newFrame);
            ScaleYPictureAction scaleYPictureAction = new ScaleYPictureAction(newFrame);
            ShearPictureAction shearPictureAction = new ShearPictureAction(newFrame);
            // Create plugin buttons and add them to the toolbar
            movePictureButton = new IconToggleButton(movePictureAction);
            movePointButton = new IconToggleButton(movePointAction);
            transformPointButton = new IconToggleButton(transformPointAction);
            rotatePictureButton = new IconToggleButton(rotatePictureAction);
            scalexyPictureButton = new IconToggleButton(scaleXYPictureAction);
            scalexPictureButton = new IconToggleButton(scaleXPictureAction);
            scaleyPictureButton = new IconToggleButton(scaleYPictureAction);
            shearPictureButton = new IconToggleButton(shearPictureAction);
            newFrame.addMapMode(movePictureButton);
            newFrame.addMapMode(movePointButton);
            newFrame.addMapMode(transformPointButton);
            newFrame.addMapMode(rotatePictureButton);
            newFrame.addMapMode(scalexyPictureButton);
            newFrame.addMapMode(scalexPictureButton);
            newFrame.addMapMode(scaleyPictureButton);
            newFrame.addMapMode(shearPictureButton);
//            newFrame.toolGroup.add(m_movePictureButton);
//            newFrame.toolGroup.add(m_rotatePictureButton);
//            newFrame.toolGroup.add(m_scalePictureButton);
            // Show them by default

            if (actionVisibility == null)
                menu.add(actionVisibility = new ActionVisibilityChangeMenu());
        }
    }

	/**
     * The toolbar buttons shall be active only when the PicLayer is active.
     */
    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
    }

    /**
     * The menu is enabled once another layer is first created. This is needed
     * because the picture must be positioned based on the current mapview (so
     * one must exist first). User should not be able to load a picture too early.
     */
    @Override
    public void layerAdded(Layer arg0) {
        menu.setEnabled(true);
    }

    /**
     * When all layers are gone - the menu is gone too.
     */
    @Override
    public void layerRemoved(Layer arg0) {
        boolean enable = Main.map.mapView.getAllLayers().size() != 0;
        menu.setEnabled(enable);
    }
};
