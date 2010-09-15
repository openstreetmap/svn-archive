package org.openstreetmap.josm.plugins.videomapping.video;
import java.io.File;

// a specific synced video
public class GPSVideoFile extends File{
    public long offset; //time difference in ms between GPS and Video track
    
    public GPSVideoFile(File f, long offset) {
        super(f.getAbsoluteFile().toString());
        this.offset=offset;
    }

}
