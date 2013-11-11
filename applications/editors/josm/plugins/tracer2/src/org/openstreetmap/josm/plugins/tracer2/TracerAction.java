/**
 *  Tracer2 - plug-in for JOSM to capture contours
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.tracer2;

import static org.openstreetmap.josm.tools.I18n.tr;
//import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.xml.sax.SAXException;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.tracer2.preferences.ServerParam;
import org.openstreetmap.josm.plugins.tracer2.preferences.ServerParamList;
import org.openstreetmap.josm.plugins.tracer2.preferences.ServerParamSelectDialog;
import org.openstreetmap.josm.plugins.tracer2.server.GetVersion;
import org.openstreetmap.josm.plugins.tracer2.server.GetTrace;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

class TracerAction extends MapMode implements MouseListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private static boolean s_bServerVersionOK = false;
    
    protected boolean m_bCancel;
    private boolean m_bCtrl;	// if pressed no tag is added + changes and connection are made to ways without tag
    private boolean m_bAlt;		// 
    private boolean m_bShift;	// if pressed the new way will be add to the current selected
    private boolean m_bEnter = false;
    
    private TagValues m_oTagValues = new TagValues();
    
    TracerPlugin m_oPlugin;
    
    public TracerAction(MapFrame mapFrame) {
        super(tr("Tracer2"), "tracer2-sml", tr("Tracer2."), Shortcut.registerShortcut("tools:tracer2", tr("Tool: {0}", tr("Tracer2")), KeyEvent.VK_T, Shortcut.DIRECT), mapFrame, getCursor());
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
    	//System.out.println("keyPressed: key:" + e.getKeyChar() + " code" + e.getKeyCode() + " Loc" + e.getKeyLocation()+ " ID" + KeyEvent.getKeyText(e.getKeyCode()));
        
    	Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
    	List<Command> commands = new ArrayList<Command>();
    	
    	if ( checkActiveServerParam() == false ) return;
    	
    	switch (e.getKeyCode()) {
    	case 37: // left
    		m_oTagValues.left();
    		break;
    	case 38: // up
    		m_oTagValues.up();
    		break;
    	case 39: // right
    		m_oTagValues.right();
    		break;
    	case 40: // down
    		m_oTagValues.down();
    		break;
    	default:
    		return;
    	}
    	
        if (selection.isEmpty())
        {
            return;
        }
        
        String strTag = m_oTagValues.getTag();
        String strTagValue = m_oTagValues.getTagValue();
        
        if ( strTag != null && strTagValue != null ) {
	    	commands.add(new ChangePropertyCommand(selection, strTag, strTagValue));
	    	
	    	if (!commands.isEmpty()) {
	    		Main.main.undoRedo.add( new SequenceCommand( tr("Change tag {0} to {1}", strTag, strTagValue), commands ));
	        }
        }
    }
    
    @Override
    public void keyReleased ( KeyEvent e ) {
    	//System.out.println("keyReleased: key:" + e.getKeyChar() + " code" + e.getKeyCode() + " Loc" + e.getKeyLocation()+ " chra" + KeyEvent.getKeyText(e.getKeyCode()));
    }
    
    @Override
    public void keyTyped ( KeyEvent e ) {
    	//System.out.println("keyTyped: key:" + e.getKeyChar() + " code" + e.getKeyCode() + " Loc" + e.getKeyLocation()+ " ID" + KeyEvent.getKeyText(e.getKeyCode()));
    }
    
    @Override
    public void enterMode() {
    	m_bEnter = true;
    	
    	// is not working hear
    	// because if JOSM exit it is called too
    	//checkActiveServerParam();
    	
    	if (!isEnabled()) {
            return;
        }
        super.enterMode();
        Main.map.mapView.setCursor(getCursor());
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addKeyListener(this);
    }
    
    @Override
    public void exitMode() {
    	m_bEnter = false;
    	
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeKeyListener(this);
    }
    
    private static Cursor getCursor() {
        return ImageProvider.getCursor("crosshair", "tracer2-sml");
    }
    
    protected void traceAsync(Point clickPoint) {
        m_bCancel = false;
        /**
         * Positional data
         */
        final LatLon pos = Main.map.mapView.getLatLon(clickPoint.x, clickPoint.y);
        
        try {
            PleaseWaitRunnable tracerTask = new PleaseWaitRunnable(tr("Tracing")) {
                @Override
                protected void realRun() throws SAXException {
                    traceSync(pos, progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
                }
                
                @Override
                protected void finish() {
                }
                
                @Override
                protected void cancel() {
                    TracerAction.this.cancel();
                }
            };
            Thread executeTraceThread = new Thread(tracerTask);
            executeTraceThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void tagBuilding(Way way) {
        String strTag = m_oTagValues.getTag();
        String strTagValue = m_oTagValues.getTagValue();
        
        if ( strTag != null && strTagValue != null && !m_bCtrl) {
        	way.put(strTag, strTagValue);
        }
    }
    
    private boolean checkServerVersion() {
    	int nMajor = 1;
    	int nMinor = 0;
    	
    	if (s_bServerVersionOK == false) {
            GetVersion  oGetVersion = new GetVersion();
            oGetVersion.start();
            
            int nRetray = 500; // 5 seconds
            	
            while(oGetVersion.isAlive() && nRetray > 0) {
	            try {
	            	Thread.sleep(10);
	            } catch (Exception e) {
	            	break;
	            }
	            nRetray--;
	        }
            
            if (oGetVersion.m_nVersionMajor < 0 || oGetVersion.m_nVersionMinor < 0) {
            	return false;
            }
            if (oGetVersion.m_nVersionMajor != nMajor) {
                JOptionPane.showMessageDialog(Main.parent, tr("The Tracer2Server version isn''t compatible with this plugin. Please download version {0} from",nMajor + ".x" )
                		+ "\nhttp://sourceforge.net/projects/tracer2server/." , tr("Error"),  JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (oGetVersion.m_nVersionMinor < nMinor) {
                JOptionPane.showMessageDialog(Main.parent, tr("New version of Tracer2Server is avalibel. For best results please upgrade to version {0}.",nMajor + "." + nMinor ), tr("Information"),  JOptionPane.INFORMATION_MESSAGE);
            }
            s_bServerVersionOK = true;
    	}
    	return true;
    }
    
    private boolean checkActiveServerParam() {
    	if (checkServerVersion() == false) {
    		return false;
    	}
    	if ( m_bEnter == true || TracerPlugin.s_oPlugin.m_oParamList.getActivParam() == null ) {
    		
    		ServerParamList listParam = TracerPlugin.s_oPlugin.m_oParamList;
    		List<ServerParam> listEnableParam = listParam.getEnableParamList();
    		
            if (listEnableParam == null || listEnableParam.size() == 0) {
            	listParam.setActivParam(null);
                JOptionPane.showMessageDialog(Main.parent, tr("No set of parameter is activ!"), tr("Error"),  JOptionPane.ERROR_MESSAGE);
            	return false;
            }
            if ( listEnableParam.size() == 1 ) {
            	listParam.setActivParam(listEnableParam.get(0));
            	return true;
            }
            
            ServerParamSelectDialog dialog = new ServerParamSelectDialog(listEnableParam, listParam.getActivParam());
            
            if (dialog.getShow()) {
                JOptionPane pane = new JOptionPane(dialog, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                JDialog dlg = pane.createDialog(Main.parent, tr("Tracer2") + " - " + tr("Select parameter"));
            	dlg.setVisible(true);
            	Object obj = pane.getValue();
                dlg.dispose();
            	if(obj != null && ((Integer)obj) == JOptionPane.OK_OPTION) {
            		TracerPlugin.s_oPlugin.m_oParamList.setActivParam(dialog.getSelectedParam());
            	} else {
            		return false;
            	}
            }
       	}
    	ServerParam param = TracerPlugin.s_oPlugin.m_oParamList.getActivParam();
    	if ( param == null ) {
    		return false;
    	}
    	m_bEnter = false;
    	m_oTagValues.readBuildingTags(param);
    	return true;
    }
    
    private void traceSync(LatLon pos, ProgressMonitor progressMonitor) {
        Collection<Command> commands = new LinkedList<Command>();
        
        progressMonitor.beginTask(null, 3);
        try {
            ArrayList<LatLon> coordList;
            
            if ( checkActiveServerParam() == false ) return;
            
            ServerParam param = TracerPlugin.s_oPlugin.m_oParamList.getActivParam();
            GetTrace  oTraceSimple = new GetTrace(pos, param);
            oTraceSimple.start();
            try
            {
            	while(oTraceSimple.isAlive())
	            {
	            	Thread.sleep(50);
	            	if (m_bCancel == true)
	            	{
	            		oTraceSimple.interrupt();
	            		break;
	            	}
	            }
	            coordList = oTraceSimple.m_listLatLon;
        	} catch (Exception e) {
            	coordList = new ArrayList<LatLon>();
            }
           
            if (m_bCancel == true || coordList.size() == 0) {
                return;
            }
            
            // make nodes a way
            Way way = new Way();
            Node firstNode = null;
            for (LatLon coord : coordList) {
                Node node = new Node(coord);
                if (firstNode == null) {
                    firstNode = node;
                }
                commands.add(new AddCommand(node));
                way.addNode(node);
            }
            way.addNode(firstNode);
            
            tagBuilding(way);
            
            // connect to other buildings
            commands.add(ConnectWays.connect(way, pos, param, m_bCtrl, m_bAlt));
            
            if (!commands.isEmpty()) {
            	String strCommand;
            	if (ConnectWays.s_bAddNewWay == true) {
            		strCommand = tr("Tracer2 add a Way with {0} points", coordList.size());
            	} else {
            		strCommand = tr("Tracer2 modified Way to {0} points", coordList.size());
            	}
            	Main.main.undoRedo.add(new SequenceCommand(strCommand, commands));
            	
                if (m_bShift) {
                    Main.main.getCurrentDataSet().addSelected(ConnectWays.s_oWay);
                } else {
                    Main.main.getCurrentDataSet().setSelected(ConnectWays.s_oWay);
                }
            } else {
                System.out.println("Failed");
            }
            
        } finally {
            progressMonitor.finishTask();
        }
    }
    
    public void cancel() {
        m_bCancel = true;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable()) {
            return;
        }
        requestFocusInMapView();
        updateKeyModifiers(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            traceAsync(e.getPoint());
        }
    }
    
    @Override
    protected void updateKeyModifiers(MouseEvent e) {
        m_bCtrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        m_bAlt = (e.getModifiers() & (ActionEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK)) != 0;
        m_bShift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    
}

