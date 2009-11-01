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
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener;
import org.openstreetmap.josm.gui.MapFrame;

/**
 * Main Plugin class.
 */
public class PicLayerPlugin extends Plugin implements LayerChangeListener {

    // Plugin menu
    private JMenu m_menu = null;

    // Toolbar buttons
    private IconToggleButton m_movePictureButton = null;
    private IconToggleButton m_rotatePictureButton = null;
    private IconToggleButton m_scalexPictureButton = null;
    private IconToggleButton m_scaleyPictureButton = null;
    private IconToggleButton m_scalexyPictureButton = null;

    // Menu actions
    private NewLayerFromFileAction      m_newFromFileAction = null;
    private NewLayerFromClipboardAction m_newFromClipAction = null;

    /**
     * Constructor...
     */
    public PicLayerPlugin() {

        // Create menu entry
        if ( Main.main.menu != null ) {
            m_menu = Main.main.menu.addMenu(marktr("PicLayer") , KeyEvent.VK_I, Main.main.menu.defaultMenuPos, ht("/Plugin/PicLayer"));
        }

        // Add menu items
        if ( m_menu != null ) {
            m_menu.add( m_newFromFileAction = new NewLayerFromFileAction() );
            m_menu.add( m_newFromClipAction = new NewLayerFromClipboardAction() );
            m_menu.addSeparator();
            m_menu.add( new HelpAction() );
            m_newFromFileAction.setEnabled( false );
            m_newFromClipAction.setEnabled( false );
        }

        // Listen to layers
        Layer.listeners.add( this );

    }

    /**
     * Called when the map is created. Creates the toolbar buttons.
     */
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if(newFrame != null) {
            // Create plugin map modes
            MovePictureAction movePictureAction = new MovePictureAction(newFrame);
            RotatePictureAction rotatePictureAction = new RotatePictureAction(newFrame);
            ScaleXYPictureAction scaleXYPictureAction = new ScaleXYPictureAction(newFrame);
            ScaleXPictureAction scaleXPictureAction = new ScaleXPictureAction(newFrame);
            ScaleYPictureAction scaleYPictureAction = new ScaleYPictureAction(newFrame);
            // Create plugin buttons and add them to the toolbar
            m_movePictureButton = new IconToggleButton(movePictureAction);
            m_rotatePictureButton = new IconToggleButton(rotatePictureAction);
            m_scalexyPictureButton = new IconToggleButton(scaleXYPictureAction);
            m_scalexPictureButton = new IconToggleButton(scaleXPictureAction);
            m_scaleyPictureButton = new IconToggleButton(scaleYPictureAction);
            newFrame.addMapMode(m_movePictureButton);
            newFrame.addMapMode(m_rotatePictureButton);
            newFrame.addMapMode(m_scalexyPictureButton);
            newFrame.addMapMode(m_scalexPictureButton);
            newFrame.addMapMode(m_scaleyPictureButton);
//            newFrame.toolGroup.add(m_movePictureButton);
//            newFrame.toolGroup.add(m_rotatePictureButton);
//            newFrame.toolGroup.add(m_scalePictureButton);
            // Show them by default
            m_movePictureButton.setVisible(true);
            m_rotatePictureButton.setVisible(true);
            m_scalexyPictureButton.setVisible(true);
            m_scalexPictureButton.setVisible(true);
            m_scaleyPictureButton.setVisible(true);
        }
    }

    /**
     * The toolbar buttons shall be active only when the PicLayer is active.
     */
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
    }

    /**
     * The menu is enabled once another layer is first created. This is needed
     * because the picture must be positioned based on the current mapview (so
     * one must exist first). User should not be able to load a picture too early.
     */
    public void layerAdded(Layer arg0) {
        m_newFromFileAction.setEnabled( true );
        m_newFromClipAction.setEnabled( true );
    }

    /**
     * When all layers are gone - the menu is gone too.
     */
    public void layerRemoved(Layer arg0) {
        boolean enable = Main.map.mapView.getAllLayers().size() != 0;
        m_newFromFileAction.setEnabled( enable );
        m_newFromClipAction.setEnabled( enable );
    }
};
