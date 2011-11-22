package org.osmu.gosmore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

public class Update extends Activity implements LocationListener {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.update);
        setProgressBarVisibility(true);
          
        String code = currentBbox ();

        //if (!code.equals("default")) ((TextView) findViewById(R.id.update_description)).setText (code);
        if (((Gosmore) getApplication()).updateProgress >= 0) {
    		((TextView) findViewById(R.id.update_msg)).setText( 
    				((Gosmore) getApplication()).updateMsg);
        }
        else if (code.equals("")) {
        	((Gosmore)getApplication()).updateMsg = "Cannot update a custom map";
        }
        else {
            //else ((TextView) findViewById(R.id.update_description)).setText (R.string.default_update);
        	final UpdateTask dft;
            dft = new UpdateTask ();
            dft.gosmore = (Gosmore) getApplication();
            dft.gosmore.updateProgress = 0;
            dft.gosmore.cancelUpdate = false;
            setProgress(0); // Redundant ?
            if (dft.gosmore.s.searchResult.lon == 0 && dft.gosmore.s.searchResult.lat == 0) {
            	// New install. Center the view in anticipation of the initial download.
                ((LocationManager) getSystemService(LOCATION_SERVICE)).requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10f, this);
            }
          
            dft.notification = new Notification(R.drawable.icon, "Downloading Map", System.currentTimeMillis());
            dft.notification.flags = dft.notification.flags | Notification.FLAG_ONGOING_EVENT;
            dft.notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.download_progress);
            dft.notification.contentIntent = /*pendingIntent;*/ PendingIntent.getActivity(this, 0, new Intent(this, Update.class), 0);
            //notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_menu_save);
            dft.notification.contentView.setTextViewText(R.id.status_text, "Downloading map");
            //notification.contentView.setProgressBar(R.id.status_progress, 100, progress, false);
          
            //notification.setLatestEventInfo(getApplicationContext(), "Downloading", "Map", contentIntent);
            dft.nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            dft.nm.notify (1, dft.notification);
            dft.execute(code);
        }
        ((Button)findViewById(R.id.cancel_update)).setOnClickListener(new Button.OnClickListener() {
        	public void onClick (View v) {
        		((Gosmore)getApplication()).cancelUpdate = true;
        	}
        });
    }
    // ---- Location Listener methods ----
    public void onLocationChanged(Location location) {
    	((LocationManager) getSystemService(LOCATION_SERVICE)).removeUpdates(this);
    	((Gosmore) getApplication()).s.searchResult.lon = location.getLongitude();
    	((Gosmore) getApplication()).s.searchResult.lat = location.getLatitude();
    	((Gosmore) getApplication()).s.searchResult.zoom = 4000000;
    }
    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    // ---- Location Listener methods ----

    protected void onResume()
    {
    	super.onResume();
    	ProgressBar pb = (ProgressBar) findViewById(R.id.update_progress);
    	pb.setProgress(((Gosmore)getApplication()).updateProgress);
    	((Gosmore)getApplication()).updateBar = pb;
    	((Gosmore)getApplication()).updateMsgView = ((TextView) findViewById(R.id.update_msg));
    	((Gosmore)getApplication()).updateMsgView.setText(((Gosmore)getApplication()).updateMsg);
    	((Gosmore)getApplication()).updateCancel = ((Button) findViewById(R.id.cancel_update));
    	if (((Gosmore)getApplication()).cancelUpdate) ((Gosmore)getApplication()).updateCancel.setEnabled(false);
    }
    protected void onDestroy ()
    {
    	((Gosmore)getApplication()).updateBar = null;
    	((Gosmore)getApplication()).updateMsgView = null;
    	((Gosmore)getApplication()).updateCancel = null;
    	super.onDestroy ();
    }
    private native String currentBbox ();
}

class UpdateTask extends AsyncTask<String, Integer, String> {
	public Gosmore gosmore;
	Notification notification;
	NotificationManager nm;
    protected String doInBackground(String... code) {
    	PipedInputStream in = new PipedInputStream();
        File sd = Environment.getExternalStorageDirectory();
        final long modifiedTm = (new File (sd, "/gosmore/" + code[0] + ".pak")).lastModified();
        try {
        	final String myurl = "http://dev.openstreetmap.de/gosmore/" + code[0] + ".zip";
      	  	final PipedOutputStream out = new PipedOutputStream(in);
        	new Thread(
        	    new Runnable(){
        	        public void run(){
        	            for (int downloaded = 0, l = 1, count; l > 0 && !gosmore.cancelUpdate;) {
        	        		try {
        	        			URL u = new URL(myurl);
        	        			URLConnection cx = u.openConnection();
        	                	cx.setRequestProperty("Range", "bytes=" + downloaded + "-");
        	                	cx.setReadTimeout(60000); 
        	                	cx.setConnectTimeout(60000);
        	                	//cx.connect(); // getContentLen will do it for us.
      	                		if ((l = cx.getContentLength()) > 0) {
      	                			Log.d("Gosmore", "Start "+cx.getLastModified()+" len "+modifiedTm);
            	                	if (cx.getLastModified() < modifiedTm) {
            	                		l = 0;
            	                		break;
            	                	}
      	                			//Log.d("Gosmore", "Start "+downloaded+" len "+l + " "+ cx.getHeaderField("Accept-Ranges")+ " " + cx.getResponseCode());
      	                			InputStream is = cx.getInputStream();
      	                			byte[] buf = new byte[1500]; 
      	                			for (; (count=is.read(buf, 0, 1500)) != -1; l-=count) {
      	                				if(count > 0) out.write(buf,0,count);
      	                				downloaded += count;
      	                			}
       	                		}
      	                		else l = 1;
        	                } catch (MalformedURLException mue) {
        	                	gosmore.updateMsg = "Bad URL. Retrying.";
        	                } catch (IOException ioe) {
        	                	gosmore.updateMsg = "Network Error. Retrying.";
        	                } catch (SecurityException se) {
        	                	gosmore.updateMsg = "Security error. Retrying.";
        	                }
        	                try {
        	                	if (l > 0) Thread.sleep (1);
        	                } catch(InterruptedException e) {}
        	        	}
                		Log.d("Gosmore", "Leaving download thread "+gosmore.cancelUpdate);
                		try { out.close(); } catch (IOException ioe) {}
        	        }
        	    }
        	).start();
        } catch(IOException a) {Log.d ("Gosmore", "1st");}
        Log.d ("Gosmore", "a");
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream dis = new ZipInputStream (in);
            ZipEntry entry;

            int length, done = 0;
            Log.d ("Gosmore", "b");
            if ((entry = dis.getNextEntry()) != null) {
                Log.d ("Gosmore", "c");
              StatFs stat = new StatFs (sd.getPath());
              if (stat.getAvailableBlocks() * (long) stat.getBlockSize() <
            		  entry.getSize()) {
            	  gosmore.updateMsg = "Error: At least " + 
            	    (entry.getSize() / 1024/1024) + "MB storage space is needed";
            	  publishProgress(-1);
              }
              else {
                  gosmore.updateMsg = "" + (int)(entry.getSize()/(2048*1024))+ " MB";
                  publishProgress(-1);
            	  File f = new File(sd, "/gosmore/tmp.pak");
            	  FileOutputStream fos = new FileOutputStream(f);
            	  while (!gosmore.cancelUpdate && (length = dis.read(buffer)) != -1) {
            		  fos.write(buffer, 0, length);
            		  if ((done >> 20) < ((done + length) >> 20)) {
            			  publishProgress(done / (int)(entry.getSize() / 100));
            			  notification.contentView.setProgressBar(R.id.status_progress, 100, done / (int)(entry.getSize() / 100), false);
            			  nm.notify(1, notification);
            		  }
            		  done += length;
            	  }
            	  fos.close();
            	  //Log.d("Gosmore", "k" + done + " " + entry.getSize());
            	  if (done == entry.getSize()) {
            		  File to = new File (sd, "/gosmore/" + code[0] + ".pak");
            		  f.renameTo(to);
            		  gosmore.updateMsg = "Download successful.";
              	  }
              	  else gosmore.updateMsg = "Download incomplete. Aborting";
                  Log.d ("Gosmore", "a");
              } // There was enough space with we started.
            } // The zip looks valid
            else gosmore.updateMsg = gosmore.cancelUpdate ? "Update cancelled" :
            	"No new map available."; // Empty file.
        } catch (IOException ioe) {
        	gosmore.updateMsg = "File Error";
        } catch (SecurityException se) {
        	gosmore.updateMsg = "Security error";
        }
        //publishProgress(100);
        nm.cancel (1);
        gosmore.updateProgress = -1; // Ready for a new update
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
    	if (progress[0] >= 0) {
    		if (gosmore.updateBar != null) gosmore.updateBar.setProgress(progress[0]);
    		gosmore.updateProgress = progress[0];
    	}
    	else if (gosmore.updateMsgView != null) {
    		gosmore.updateMsgView.setText(gosmore.updateMsg);
    	}
    }

    protected void onPostExecute(String result) {
  		Log.d ("Gosmore", gosmore.updateMsg + (gosmore.updateMsgView != null ? " view" : " noview"));
    	if (gosmore.updateMsgView != null) gosmore.updateMsgView.setText(gosmore.updateMsg);
    	if (gosmore.updateCancel != null) gosmore.updateCancel.setEnabled(false);
    	gosmore.cancelUpdate = true;
    }
}
