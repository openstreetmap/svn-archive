package org.openstreetmap.josm.plugins.contourmerge;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * <p>ContourMergeMode</p> is the {@link MapMode} for mergin the contours of two areas.</p>
 *
 */
public class ContourMergeMode extends MapMode {
	static private final Logger logger = Logger.getLogger(ContourMergeMode.class.getName());

	private Collection<OsmPrimitive> selection;
	
	public ContourMergeMode(MapFrame mapFrame) {
		super(
				tr("Contour Merge"),  // name
				"contourmerge",       // icon name 
				tr("Merge the contour of an area with the contour of another area"), // tooltip 
				mapFrame,
				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
		);
		putValue("help", HelpUtil.ht("Plugin/ContourMerge"));
	}
	
	protected MapView getMapView(){
		return Main.map.mapView;
	}

	@Override
	public void enterMode() {
		super.enterMode();
        getMapView().addMouseListener(this);
        getMapView().addMouseMotionListener(this);
        ContourMergePlugin.setEnabled(true);
        ContourMergeModel model = ContourMergePlugin.getModelManager().getActiveModel();
        if (model != null) {
        	model.reset();
        }
        /*
         * Remind the current selection and clear it; otherwise the rendered selection
         * might interfere with our understanding of "selected" nodes and way slices in
         * this map mode.
         */
        selection = model.getLayer().data.getSelected();
        model.getLayer().data.clearSelection();
	}

	@Override
	public void exitMode() {
		super.exitMode();
		getMapView().removeMouseListener(this);
        getMapView().removeMouseMotionListener(this);
		ContourMergePlugin.setEnabled(false);
	    ContourMergeModel model = ContourMergePlugin.getModelManager().getActiveModel();
	    if (model != null) {
        	model.reset();
    	    /*
    	     * Restore the last selection.
    	     */    	 
        	model.getLayer().data.setSelected(selection);
     	    selection = null;
        }
	}

	@Override
	public boolean layerIsSupported(Layer l) {
		return l instanceof OsmDataLayer;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (! ContourMergePlugin.isEnabled()) return;
		onDrop(e.getPoint());		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (! ContourMergePlugin.isEnabled()) return;
		ContourMergeModel model = ContourMergePlugin.getModelManager().getActiveModel();
		if (model == null) return;
		onStartDrag(e.getPoint());
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {		
		if (! ContourMergePlugin.isEnabled()) return;
		if (e.getButton() != MouseEvent.BUTTON1) return;		
		ContourMergeModel model = ContourMergePlugin.getModelManager().getActiveModel();	
		if (model == null) return;
		
		List<Node> candidates = getMapView().getNearestNodes(e.getPoint(), OsmPrimitive.isSelectablePredicate);
		if (!candidates.isEmpty()){
			if (!OsmPrimitive.getFilteredList(candidates.get(0).getReferrers(), Way.class).isEmpty()) {					
				/*
				 * clicked on a node which isn't isolated ? => toggle its selected state
				 */
				model.toggleSelected(candidates.get(0));
			}
		}
		Main.map.mapView.repaint();
	}

	protected BBox buildSnapBBox(Point p){
		MapView mv = Main.map.mapView;
		LatLon ll = mv.getLatLon(p.x -3, p.y - 3);
		LatLon ur = mv.getLatLon(p.x + 3, p.y + 3);
		return new BBox(ll, ur);
	}
	
	protected void showHelpText(String text){
		Main.map.statusLine.setHelpText(text);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (! ContourMergePlugin.isEnabled()) return;
		if (ContourMergePlugin.getModelManager().getActiveModel() == null) return;
		if (e.getButton() != MouseEvent.NOBUTTON) return;
		List<Node> candidates = getMapView().getNearestNodes(e.getPoint(), OsmPrimitive.isSelectablePredicate);
		ContourMergeModel model = ContourMergePlugin.getModelManager().getActiveModel();
		showHelpText("");
		if (candidates.isEmpty()){
			model.setFeedbackNode(null);
			WaySegment ws = getMapView().getNearestWaySegment(e.getPoint(),OsmPrimitive.isSelectablePredicate);
			if (ws == null){
				getMapView().setCursor(Cursor.getDefaultCursor());			
				model.setDragStartFeedbackWaySegment(null);				
			} else {
				showHelpText(tr("Drag/drop: drag the way segment an drop it on a target segment"));
				getMapView().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));	
				model.setDragStartFeedbackWaySegment(ws);
			}
		} else {
			if (model.isSelected(candidates.get(0))) {
				showHelpText(tr("Left-Click: deselect node"));
				getMapView().setCursor(ImageProvider.getCursor("normal","deselect_node"));
			} else {
				if (OsmPrimitive.getFilteredList(candidates.get(0).getReferrers(), Way.class).isEmpty()) {
					showHelpText(tr("Can''t select an isolated node"));
					getMapView().setCursor(DragSource.DefaultMoveNoDrop);
				} else {
					showHelpText(tr("Left-Click: select node"));
					getMapView().setCursor(ImageProvider.getCursor("normal","select_node"));
				}
			}			
			model.setFeedbackNode(candidates.get(0));			
		}
		Main.map.mapView.repaint();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {/* ignore */}
	@Override
	public void mouseExited(MouseEvent e) {/* ignore */}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		onStepDrag(e.getPoint());
	}
	
	/* -------------------------------------------------------------------------------------- */
	/* drag and drop                                                                          */
	/* -------------------------------------------------------------------------------------- */
	protected Point dragStart = null;
	
	protected void onStartDrag(Point start) {
		ContourMergeModel model = ContourMergePlugin.getModelManager().getActiveModel();
		WaySegment ws = getMapView().getNearestWaySegment(start,OsmPrimitive.isSelectablePredicate);
		if (ws != null && model.isWaySegmentDragable(ws)) {
			this.dragStart = start;
			getMapView().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			showHelpText(tr("Drag the way segment and drop it on a target segment"));
			model.setDragOffset(new Point(0,0));
			model.setDragStartFeedbackWaySegment(ws);
			model.setDropFeedbackSegment(null);
		}
	}
	
	protected void onStepDrag(Point current){		
		if (dragStart == null) return;  // drag initiated outside of map view ?
		WaySegment ws = getMapView().getNearestWaySegment(current,OsmPrimitive.isSelectablePredicate);
		ContourMergeModel model = ContourMergePlugin.getModelManager().getActiveModel();
		WaySegment newDropTargetFeedbackSegment;
		if (ws == null){
			/*
			 * mouse pointer isn't close to another way, continue dragging
			 */
			getMapView().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			showHelpText(tr("Drag the way segment and drop it on a target segment"));
			newDropTargetFeedbackSegment = null;
		} else if (! model.isPotentialDropTarget(ws)) {
			/*
			 * mouse pointer is close to a way segment which isn't part of
			 * a potential target way slice
			 */
			getMapView().setCursor(DragSource.DefaultLinkNoDrop);
			showHelpText(tr("Drag the way segment and drop it on a target segment"));
			newDropTargetFeedbackSegment = null;
		} else {
			/*
			 * mouse pointer is close to a way segment which is part of a potential
			 * target way slice
			 */
			getMapView().setCursor(DragSource.DefaultLinkDrop);
			showHelpText(tr("Drop to align to the the target segment"));
			newDropTargetFeedbackSegment = ws;
		}
		Point offset = new Point(current.x - dragStart.x, current.y - dragStart.y);
		model.setDragOffset(offset);
		model.setDropFeedbackSegment(newDropTargetFeedbackSegment);
		Main.map.mapView.repaint();
	}
		
	protected void onDrop(Point target){
		if (dragStart == null) return;  // drag initiated outside of map view ?
		WaySegment ws = getMapView().getNearestWaySegment(target,OsmPrimitive.isSelectablePredicate);
		ContourMergeModel model = ContourMergePlugin.getModelManager().getActiveModel();
		if (model.isPotentialDropTarget(ws)){
			/*
			 * Merge the way slice given by the drag source onto the way
			 * slice given by the drop target.
			 */
			getMapView().setCursor(Cursor.getDefaultCursor());
			Command cmd = model.buildContourAlignCommand();
			if (cmd != null){
				Main.main.undoRedo.add(cmd);
			}
		}
		
		/*
		 * Reset the drag state
		 */
		getMapView().setCursor(Cursor.getDefaultCursor());
		showHelpText(tr("Left-Click: on node to select/unselect; Drag: drag way slice"));  			
		this.dragStart = null;
		model.setDragStartFeedbackWaySegment(null);
		model.setDropFeedbackSegment(null);
		model.setDragOffset(null);
		Main.map.mapView.repaint();
	}	
}
