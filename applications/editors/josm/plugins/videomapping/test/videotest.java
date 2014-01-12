
import java.io.File;
import java.text.SimpleDateFormat;

import org.openstreetmap.josm.plugins.videomapping.video.VideoEngine;
import org.openstreetmap.josm.plugins.videomapping.video.VideoPlayer;

//simple app to test videoplayer alone
public class videotest {
    public static void main(String[] args) {
    	VideoEngine.setupPlayer();
    	VideoPlayer testplayer= new VideoPlayer(new SimpleDateFormat("hh:mm:ss"));
    	testplayer.setJumpLength(1000);
    	testplayer.setLoopLength(3000);
    	testplayer.addVideo(new File("C:\\TEMP\\test.mpg"),"1");
    	testplayer.addVideo(new File("C:\\TEMP\\aoe-drachen_dvdscr.avi"),"2");
    	testplayer.enableSingleVideoMode(true);
    }
}
