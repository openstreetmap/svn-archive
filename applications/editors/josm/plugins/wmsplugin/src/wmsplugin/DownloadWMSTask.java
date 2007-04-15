package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAction;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;

 

public class DownloadWMSTask extends PleaseWaitRunnable {

	private WMSLayer wmsLayer;
	private double minlat, minlon, maxlat, maxlon;
	
	/* whether our layer was already added. */
	private boolean layerAdded = false;
	
	
	public DownloadWMSTask(String name, String wmsurl) {
		
		super(tr("Downloading " + name));
		
		// simply check if we already have a layer created. if not, create; if yes, reuse.
		
		if (wmsLayer == null) {
			
			if (wmsurl.matches("(?i).*layers=npeoocmap.*") || wmsurl.matches("(?i).*layers=npe.*") ){
				//then we use the OSGBLayer
				this.wmsLayer= new OSGBLayer(name, wmsurl);
			} else {			
				this.wmsLayer = new WMSLayer(name, wmsurl); 
				
			} 
		} 
	}
	
	@Override public void realRun() throws IOException {
		wmsLayer.grab(minlat,minlon,maxlat,maxlon);
	}

	@Override protected void finish() {

		// BUG if layer is deleted, wmsLayer is not null and layerAdded remains true!
		// FIXED, see below
		
		layerAdded = false;
		for (Iterator it = Main.map.mapView.getAllLayers().iterator(); it.hasNext(); ) {
        Object  element = it.next();
       
        if (element.equals(wmsLayer)) layerAdded = true;
        
		}
		 
				
		if ((wmsLayer != null) && (!layerAdded))
		{
			Main.main.addLayer(wmsLayer);
			layerAdded = true;
		}
	}

	@Override protected void cancel() {
	}

	public void download(DownloadAction action, double minlat, double minlon, double maxlat, double maxlon) {
		this.minlat=minlat;
		this.minlon=minlon;
		this.maxlat=maxlat;
		this.maxlon=maxlon;
		Main.worker.execute(this);
	}
}
