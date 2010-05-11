package org.openstreetmap.josm.plugins.videomapping;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.tools.Shortcut;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.*;

public class VideoAction extends JosmAction {

	private GpxData gps;

	public VideoAction() {
		super("Sync Video","videomapping","Sync a video against this GPS track",null,true);
	}

	// Choose a file
	public void actionPerformed(ActionEvent arg0) {
	
//		JFileChooser fc = new JFileChooser();
//		fc.setAcceptAllFileFilterUsed( false );
//		fc.setFileFilter( new VideoFileFilter() );
//		if (fc.showOpenDialog( Main.parent )==JFileChooser.APPROVE_OPTION)
//		{
//			VideoWindow w = new VideoWindow(fc.getSelectedFile());
//		}
		Main.main.addLayer(new PositionLayer("test",gps));
	}
	
	//restrict the file chooser
	private class VideoFileFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
		    
		    String ext3 = ( f.getName().length() > 4 ) ?  f.getName().substring( f.getName().length() - 4 ).toLowerCase() : "";
		    String ext4 = ( f.getName().length() > 5 ) ?  f.getName().substring( f.getName().length() - 5 ).toLowerCase() : "";

		    // TODO: check what is supported by JMF or if there is a build in filter
		    return ( f.isDirectory() 
		    	||	ext3.equals( ".avi" )
		    	||	ext4.equals( ".wmv" )
		    	||	ext3.equals( ".mpg" )
		    	);
		}


		@Override
		public String getDescription() {
			return tr("Video files");
		}
		
	}
	
	public void setGps(GpxData gps) {
		this.gps = gps;
	}

}
