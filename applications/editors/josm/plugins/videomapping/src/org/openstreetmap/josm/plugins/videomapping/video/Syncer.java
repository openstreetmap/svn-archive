package org.openstreetmap.josm.plugins.videomapping.video;

import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.player.MediaPlayer;

//Syncs all UI components to the playback using the SWING thread
final class Syncer implements Runnable {

    private final SimpleVideoPlayer pl;
    
    Syncer(SimpleVideoPlayer pl) {
      this.pl = pl;
    }
    

    public void run() {
      SwingUtilities.invokeLater(new Runnable() {
    	  //here we update
        public void run() {
        	pl.updateTime();

        }
      });
    }
  }
