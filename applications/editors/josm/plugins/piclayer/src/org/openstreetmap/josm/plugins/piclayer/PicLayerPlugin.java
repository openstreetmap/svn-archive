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
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

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
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;

/**
 * Main Plugin class.
 */
public class PicLayerPlugin extends Plugin implements LayerChangeListener {

    public static List<PicToggleButton> buttonList = null;

    // Plugin menu
    private JMenu menu = null;

    /**
     * Constructor...
     */
    public PicLayerPlugin(PluginInformation info) {
        super(info);

        // Create menu entry
        if ( Main.main.menu != null ) {
            menu = Main.main.menu.addMenu(marktr("PicLayer") , KeyEvent.VK_Y, Main.main.menu.defaultMenuPos, ht("/Plugin/PicLayer"));
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

            buttonList = new ArrayList<PicToggleButton>(7);
            buttonList.add(new PicToggleButton(movePictureAction, tr("Move Picture"), "piclayer.actionvisibility.move", true));
            buttonList.add(new PicToggleButton(movePointAction, tr("Move Point"), "piclayer.actionvisibility.movepoint", true));
            buttonList.add(new PicToggleButton(transformPointAction, tr("Transform Point"), "piclayer.actionvisibility.transformpoint", true));
            buttonList.add(new PicToggleButton(rotatePictureAction, tr("Rotate"), "piclayer.actionvisibility.rotate", false));
            buttonList.add(new PicToggleButton(scaleXYPictureAction, tr("Scale"), "piclayer.actionvisibility.scale", false));
            buttonList.add(new PicToggleButton(scaleXPictureAction, tr("Scale X"), "piclayer.actionvisibility.scalex", false));
            buttonList.add(new PicToggleButton(scaleYPictureAction, tr("Scale Y"), "piclayer.actionvisibility.scaley", false));
            buttonList.add(new PicToggleButton(shearPictureAction, tr("Shear"), "piclayer.actionvisibility.shear", false));

            for(IconToggleButton btn : buttonList) {
                newFrame.addMapMode(btn);
            }
        }
    }

    /**
     * The toolbar buttons shall be active and visible only when the PicLayer is active.
     */
    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        boolean oldPic = oldLayer instanceof PicLayerAbstract;
        boolean newPic = newLayer instanceof PicLayerAbstract;
        // actually that should be not enough - JOSM should hide all buttons that are disabled for current layer!
        if (oldPic && !newPic || oldLayer == null && !newPic) { // leave picture layer - hide all controls
            for (PicToggleButton btn : buttonList) {
                btn.writeVisible();
                btn.setVisible(false);
            }
            if (oldLayer != null)
                ((PicLayerAbstract)oldLayer).setDrawPoints(false);
        }
        if (!oldPic && newPic) { // enter picture layer - reset visibility of controls
            for (PicToggleButton btn : buttonList)
                btn.readVisible();
        }
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
