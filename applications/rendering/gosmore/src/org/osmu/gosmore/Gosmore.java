package org.osmu.gosmore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Gosmore extends Application {
    public Bitmap icons;
    public class Settings implements Serializable {
    	static final long serialVersionUID = 6618718669L;
    	ArrayList<Place> recent;
    	String search;
        Place searchResult; // This is where the app stores the current place
    }
    public Settings s;
    public int updateProgress = -1;
    public String updateMsg;
    public boolean cancelUpdate = true;
    public ProgressBar updateBar;
    public TextView updateMsgView;
    public Button updateCancel;
    static {
        System.loadLibrary("gosmore");
    }
    @Override 
    public void onCreate() {
    	File f = new File(Environment.getExternalStorageDirectory(), "/gosmore/icons.png");
    	InputStream is;
    	try {
    		is = new FileInputStream(f);
    	} catch (FileNotFoundException e) {
           is = getApplicationContext().getResources().openRawResource(R.drawable.mappaint);
    	}
        try {
            icons = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
            	Log.w ("Gosmore", "Cannot read icons.png");
                // Ignore.
            }
        } 
   		try {
   			FileInputStream fis = openFileInput ("settings");
   			ObjectInputStream in = new ObjectInputStream (fis);
   			try {
   				s = new Settings();
   				s.recent = (ArrayList<Place>) in.readObject();
   				s.search = (String) in.readObject();
   				s.searchResult = (Place) in.readObject();
   			}
    		catch (ClassNotFoundException ex) {Log.w("Gosmore", "Settings file is corrupt");}
   			in.close();
   		} catch (IOException ex) {}//Log.d("gosmore", "Settings file not found");}
   		  finally {
   			if (s == null) {
   				s = new Settings();
   				s.searchResult = new Place();
   				s.searchResult.lat = 0; // Set up reasonable defaults
   				s.searchResult.lon = 0;
   				s.searchResult.zoom = 1000000;
   				s.searchResult.dir = 0;
   				s.recent = new ArrayList<Place> ();
   			}
   		}
    }
}
  