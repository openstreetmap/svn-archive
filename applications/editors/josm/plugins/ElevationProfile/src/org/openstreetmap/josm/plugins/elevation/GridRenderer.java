package org.openstreetmap.josm.plugins.elevation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;

public class GridRenderer extends PleaseWaitRunnable {
    private Bounds box;
    private IEleRenderingListener listener;
    
    private BlockingDeque<EleVertex> toDo = new LinkedBlockingDeque<EleVertex>();
    private BlockingDeque<EleVertex> vertices = new LinkedBlockingDeque<EleVertex>();
    
    private volatile boolean stop = false;
    
    public GridRenderer(String title, Bounds box, IEleRenderingListener listener) {
	this(title, NullProgressMonitor.INSTANCE, true, box, listener);
    }

    public GridRenderer(String title, boolean ignoreException, Bounds box, IEleRenderingListener listener) {
	this(title, NullProgressMonitor.INSTANCE, ignoreException, box, listener);
    }

    public GridRenderer(Component parent, String title,
	    boolean ignoreException, Bounds box) throws IllegalArgumentException {
	super(parent, title, ignoreException);
	
	this.box = box;
	initQueue();
    }

    public GridRenderer(String title, ProgressMonitor progressMonitor,
	    boolean ignoreException, Bounds box, IEleRenderingListener listener) {
	super(title, progressMonitor, ignoreException);

	this.box = box;
	this.listener = listener;
	initQueue();
    }
    
    /**
     * Inits the 'todo' queue with the initial vertices.
     */
    private void initQueue() {
	LatLon min = box.getMin();
	LatLon max = box.getMax();
	
	// compute missing coordinates
	LatLon h1 = new LatLon(min.lat(), max.lon()); 
	LatLon h2 = new LatLon(max.lat(), min.lon());
	
	// compute elevation coords
	EleCoordinate p0 = new EleCoordinate(min, ElevationHelper.getElevation(min));	
	EleCoordinate p1 = new EleCoordinate(h1, ElevationHelper.getElevation(h1));
	EleCoordinate p2 = new EleCoordinate(max, ElevationHelper.getElevation(max));
	EleCoordinate p3 = new EleCoordinate(h2, ElevationHelper.getElevation(h2));
		
	// compute initial vertices
	EleVertex v1 = new EleVertex(p0, p1, p2);
	EleVertex v2 = new EleVertex(p2, p3, p0);
	// enqueue vertices
	toDo.add(v1);
	toDo.add(v2);
	
	System.out.println("Inited queue");
    }

    public BlockingDeque<EleVertex> getVertices() {
        return vertices;
    }

    @Override
    protected void cancel() {
	stop = true;
    }

    @Override
    protected void realRun() throws SAXException, IOException,
	    OsmTransferException {
	
	if (stop) return;
	
	super.getProgressMonitor().indeterminateSubTask(tr("Render vertices..."));
	System.out.println("Queue size " + toDo.size());
	// 
	while (toDo.size() > 0) {
	    if (stop) break;
	    
	    EleVertex vertex = toDo.poll();
	    
	    if (vertex.isFinished()) {		
		vertices.add(vertex);
		if (listener !=null) {
		    listener.finished(vertex);
		}
	    } else {
		List<EleVertex> newV = vertex.divide();
		for (EleVertex eleVertex : newV) {
		    System.out.print(".");
		    toDo.add(eleVertex);
		}
	    }
	}
	
	if (listener !=null) {
	    listener.finishedAll();
	}
    }

    @Override
    protected void finish() {
	// TODO Auto-generated method stub

    }

}
