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
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.piclayer.actions.SavePictureCalibrationAction;
import org.openstreetmap.josm.plugins.piclayer.actions.newlayer.NewLayerFromClipboardAction;
import org.openstreetmap.josm.plugins.piclayer.actions.newlayer.NewLayerFromFileAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.MovePictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.RotatePictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.ScaleXPictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.ScaleXYPictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.ScaleYPictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.ShearPictureAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.affine.MovePointAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.affine.RemovePointAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.affine.TransformPointAction;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;

/**
 * Main Plugin class.
 */
public class PicLayerPlugin extends Plugin implements LayerChangeListener {

    public static List<IconToggleButton> buttonList = null;

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
            RemovePointAction removePointAction = new RemovePointAction(newFrame);

            RotatePictureAction rotatePictureAction = new RotatePictureAction(newFrame);
            ScaleXYPictureAction scaleXYPictureAction = new ScaleXYPictureAction(newFrame);
            ScaleXPictureAction scaleXPictureAction = new ScaleXPictureAction(newFrame);
            ScaleYPictureAction scaleYPictureAction = new ScaleYPictureAction(newFrame);
            ShearPictureAction shearPictureAction = new ShearPictureAction(newFrame);
            // Create plugin buttons and add them to the toolbar

            buttonList = new ArrayList<IconToggleButton>(7);
            buttonList.add(picLayerActionButtonFactory(movePictureAction));
            buttonList.add(picLayerActionButtonFactory(movePointAction));
            buttonList.add(picLayerActionButtonFactory(transformPointAction));
            buttonList.add(picLayerActionButtonFactory(removePointAction));
            buttonList.add(picLayerActionButtonFactory(rotatePictureAction));
            buttonList.add(picLayerActionButtonFactory(scaleXYPictureAction));
            buttonList.add(picLayerActionButtonFactory(scaleXPictureAction));
            buttonList.add(picLayerActionButtonFactory(scaleYPictureAction));
            buttonList.add(picLayerActionButtonFactory(shearPictureAction));

            for(IconToggleButton btn : buttonList) {
                newFrame.addMapMode(btn);
            }
        }
    }

    private IconToggleButton picLayerActionButtonFactory(MapMode action) {
    	IconToggleButton button = new IconToggleButton(action);
    	button.setAutoHideDisabledButton(true);
    	return button;
    }

    /**
     * The toolbar buttons shall be active and visible only when the PicLayer is active.
     */
    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        boolean oldPic = oldLayer instanceof PicLayerAbstract;
        boolean newPic = newLayer instanceof PicLayerAbstract;

        if (oldPic) {
            ((PicLayerAbstract)oldLayer).setDrawPoints(false);
        }

        if (newPic) {
        	((PicLayerAbstract)newLayer).setDrawPoints(true);
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
        if (arg0 instanceof PicLayerAbstract && ((PicLayerAbstract) arg0).getTransformer().isModified()) {
            if (JOptionPane.showConfirmDialog(Main.parent, tr("Do you want to save current calibration of layer {0}?",
                    ((PicLayerAbstract)arg0).getPicLayerName()),
                    UIManager.getString("OptionPane.titleText"),
                    JOptionPane.YES_NO_OPTION) == 0)
                new SavePictureCalibrationAction((PicLayerAbstract) arg0).actionPerformed(null);
        }
        boolean enable = Main.map.mapView.getAllLayers().size() != 0;
        menu.setEnabled(enable);
    }
};
