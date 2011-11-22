package org.osmu.gosmore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
  
public class MapActivity extends Activity implements TextToSpeech.OnInitListener {
	private MapView mGLView;
    private native int changePak (String sd, int lat, int lon);
    public int pakType = 0; // This is really just a boolean
    @Override  
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        /*// Uncomment for full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
        	!Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
        	Log.d("Gosmore", "("+Environment.getExternalStorageState() + " " + Environment.MEDIA_MOUNTED+")"+
        			(Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED));
        	showDialog (5);
        }
        else {
        File sd = new File (Environment.getExternalStorageDirectory(), "gosmore");
        sd.mkdir (); 
        if ((pakType = changePak (sd.getAbsolutePath(), 0, 0)) == 0) {
          	Intent intent = new Intent (MapActivity.this, Update.class);
      		startActivity(intent);
            setContentView(R.layout.map_help);
      		//finish(); 
        }  
        else if (false) { // Set this to see if things are faster with ImageViews
            // Fixme and uncomment mGLView = new MapView(this);
            setContentView(mGLView);
        }  
        else { 
          setContentView(R.layout.map_view);
          mGLView = (MapView) findViewById(R.id.mapview);
          mGLView.activity = this; // So that the view can show a dialog
          //final MapRenderer mr = mGLView.mRenderer;
          ImageView.OnTouchListener otl = new ImageView.OnTouchListener() {
        	  public boolean onTouch (View v, MotionEvent m) {
        		  if (m.getAction() == MotionEvent.ACTION_DOWN) {
        			  mGLView.requestRender ();
        			  mGLView.mRenderer.zExp = v.getId() == 
        				  R.id.zoom_out ? 0.001f : -0.001f;
        			  mGLView.mRenderer.zTime = SystemClock.uptimeMillis()-50;
        		  } 
        		  if (m.getAction() == MotionEvent.ACTION_UP) {
        			  long now = SystemClock.uptimeMillis();
        			  mGLView.mRenderer.p.zoom = (int)(mGLView.mRenderer.p.zoom * Math.exp (mGLView.mRenderer.zExp * (now - mGLView.mRenderer.zTime)));
        			  mGLView.mRenderer.zTime = 0;
        		  }
        	      return false;
        	  }
          };  
          ImageView oImageView = (ImageView) findViewById (R.id.zoom_out);
          oImageView.setOnTouchListener(otl);
          ImageView iImageView = (ImageView) findViewById (R.id.zoom_in);
          iImageView.setOnTouchListener(otl);
          ImageView mImageView = (ImageView) findViewById (R.id.mylocation);
          mImageView.setOnClickListener(new ImageView.OnClickListener() {
        	  public void onClick (View v) {    
        		  mGLView.mRenderer.follow = true;
       	          mGLView.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, mGLView);
       	          mGLView.mSensorManager.registerListener(mGLView.magListener,
       	        		mGLView.mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
       	        		SensorManager.SENSOR_DELAY_NORMAL);
       	          mGLView.mSensorManager.registerListener(mGLView.accListener,
       	        		mGLView.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
       	        		SensorManager.SENSOR_DELAY_UI);
        	  }
          });
          ImageView sImageView = (ImageView) findViewById (R.id.map_search);
          sImageView.setOnClickListener(new ImageView.OnClickListener() {
        	  public void onClick (View v) {
        		  Intent sintent = new Intent (MapActivity.this, Search.class);      
        		  startActivity(sintent);
        	  }
          });
          ImageView rImageView = (ImageView) findViewById (R.id.map_recent);
          rImageView.setOnClickListener(new ImageView.OnClickListener() {
        	  public void onClick (View v) {
                  Intent intent = new Intent (MapActivity.this, Recent.class);      
                  startActivity(intent);
        	  }
          });
          mGLView.mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
          mGLView.lm = (LocationManager) getSystemService(LOCATION_SERVICE);
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
          if (!prefs.getString ("vehicle", "0").equals("0")) {
        	  mGLView.mRenderer.mTts = new TextToSpeech(this, this);
          }
        }     
        } // External storage is mounted
        //mGLView.setDebugFlags (GLSurfaceView.DEBUG_CHECK_GL_ERROR );
    }
    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inf = getMenuInflater();
    	inf.inflate(R.menu.map, (Menu)menu);
    	return true;
    } 
    @Override 
    public Dialog onCreateDialog(int id) {
    	if (id == 1) {
    		mGLView.mRenderer.rProgress = new ProgressDialog(this);
    		//mGLView.mRenderer.rProgress.setIcon(R.drawable.alert_dialog_icon);
    		mGLView.mRenderer.rProgress.setTitle(R.string.routing_progress);
    		mGLView.mRenderer.rProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    		//mGLView.mRenderer.rProgress.setMax(MAX_PROGRESS);
    		mGLView.mRenderer.rProgress.setButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				mGLView.mRenderer.route = false;
    			}
    		});
        	return mGLView.mRenderer.rProgress;
    	}
    	else if (id == 2) {
    		return new AlertDialog.Builder(this)
            //.setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.about_title)
            .setMessage(R.string.about_msg)
            .setPositiveButton(R.string.ok, null)
            .create ();
    	}
    	else if (id == 3) {
    		return new AlertDialog.Builder(this)
            //.setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.no_location_title)
            .setMessage(R.string.no_location_msg)
            .setPositiveButton(R.string.ok, null)
            .create (); 
    	}
    	else if (id == 4) {
    		return new AlertDialog.Builder(this)
            //.setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.first_map_title)
            .setMessage(R.string.first_map_msg)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	Intent intent = new Intent (MapActivity.this, Update.class);      
          		  	startActivity(intent);
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .create ();
    	}
    	else if (id == 5) {
    		return new AlertDialog.Builder(this)
            //.setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.no_sdcard_title)
            .setMessage(R.string.no_sdcard_msg)
            .setPositiveButton(R.string.ok, null)
            .create (); 
    	}
    	return null;
    }
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.start_route) {
        	mGLView.mRenderer.startR = true;
        	mGLView.requestRender ();
        } 
        else if (item.getItemId() == R.id.end_route) {
        	showDialog(1);
    		mGLView.mRenderer.route = true;
    		mGLView.requestRender();
        }
        else if (item.getItemId() == R.id.preferences) {
        	Intent sintent = new Intent (MapActivity.this, Preferences.class);      
  		  	startActivity(sintent);
        }
        else if (item.getItemId() == R.id.update) {
        	Intent sintent = new Intent (MapActivity.this, Update.class);      
  		  	startActivity(sintent);
        }
        else if (item.getItemId() == R.id.about) {
        	showDialog(2);
        }
        else return false;
        return true;
    }

    @Override
    public boolean onSearchRequested() {
		  Intent sintent = new Intent (MapActivity.this, Search.class);      
		  startActivity(sintent);
		  return false;
    }
    
    @Override 
    protected void onPause() {
        super.onPause();
        Gosmore g = (Gosmore)getApplicationContext();
   		try {
   			FileOutputStream fos = openFileOutput ("settings", Context.MODE_PRIVATE);
   			ObjectOutputStream out = new ObjectOutputStream (fos);
   			out.writeObject(g.s.recent); // Can't serialize a Setting
   			out.writeObject(g.s.search); // for some unknown reason.
   			out.writeObject(g.s.searchResult);
   			out.close();
   		} catch (IOException ex) { Log.w ("Gosmore", "Cannot write settings");}
   		if (mGLView != null && mGLView.mRenderer.follow) {
   			mGLView.mRenderer.follow = false;
   			mGLView.lm.removeUpdates(mGLView);
   			mGLView.mSensorManager.unregisterListener (mGLView.magListener);
   			mGLView.mSensorManager.unregisterListener (mGLView.accListener);
   		}
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // Don't resume tracking automatically.
    }
    @Override
    protected void onDestroy()
    {
    	if (mGLView != null && mGLView.mRenderer.mTts != null) {
    		mGLView.mRenderer.mTts.stop();
    		mGLView.mRenderer.mTts.shutdown();
        }
        super.onDestroy();
    }
    public void onInit(int status) {
    	if (status == TextToSpeech.SUCCESS) {
    		int result = mGLView.mRenderer.mTts.setLanguage(Locale.US);
    		mGLView.mRenderer.tts = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED;
    	}
    }
}

class MapView extends GLSurfaceView implements LocationListener {
	public LocationManager lm;
	public SensorManager mSensorManager;
	public MapActivity activity;
    // ---- Location Listener methods ----
    public void onLocationChanged(Location location) {
    	mRenderer.lonSum += location.getLongitude();
    	mRenderer.latSum += location.getLatitude();
    	mRenderer.latLonCnt++;
    	
    	mRenderer.p.lat = location.getLatitude();
    	mRenderer.declination =
    		 (int)(new GeomagneticField((float)location.getLatitude(),
    	  (float)location.getLongitude(),(float)location.getAltitude(),
    	  /*time:*/1300000000000L)).getDeclination();
    	mRenderer.bearing = location.getBearing();
    	mRenderer.speed = location.getSpeed();
    	mRenderer.doNavigate = true;
    	if (Math.abs (mRenderer.p.lon - mRenderer.lonSum / mRenderer.latLonCnt) * 2 +
    		Math.abs (mRenderer.p.lat - mRenderer.latSum / mRenderer.latLonCnt)
    		> .000164 / mRenderer.latLonCnt + 0.00004) {
    		mRenderer.p.lon = mRenderer.lonSum / mRenderer.latLonCnt;
    		mRenderer.p.lat = mRenderer.latSum / mRenderer.latLonCnt;
    		mRenderer.latLonCnt = 0;
    		mRenderer.lonSum = 0;
    		mRenderer.latSum = 0;
        	requestRender ();
    	}
    }
    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    // ---- Location Listener methods ----
    
    //public native void setLocation (double lon, double lat);

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRenderer = new MapRenderer(context);
    	mRenderer.mIcons = ((Gosmore)context.getApplicationContext()).icons;
        mRenderer.p = ((Gosmore)context.getApplicationContext()).s.searchResult;
        mRenderer.mapView = this;
        setRenderer(mRenderer);
        setRenderMode (RENDERMODE_WHEN_DIRTY);
    }
    private float mPreviousX, mPreviousY, prevX2, prevY2;

    @Override public boolean onTrackballEvent(MotionEvent e) {
        mRenderer.mPixelX += e.getX() * 5.0 / 320;
        mRenderer.mPixelY += e.getY() * 5.0 / 320;
        requestRender();
        if (mRenderer.follow) {
        	mRenderer.follow = false;
        	lm.removeUpdates(this);
            mSensorManager.unregisterListener (magListener);
            mSensorManager.unregisterListener (accListener);
        }
        return true;
    }
    private static Method getPointerCount, getPointerId, getX, getY;
    static {
    	try {
    		getPointerCount = MotionEvent.class.getMethod("getPointerCount",
    				new Class[] {});
    		getPointerId = MotionEvent.class.getMethod("getPointerId",
    				new Class[] { int.class });
    		getX = MotionEvent.class.getMethod("getX",
    				new Class[] { int.class });
    		getY = MotionEvent.class.getMethod("getY",
    				new Class[] { int.class });
    	} catch (NoSuchMethodException nsme) {}
    }
     
    @Override public boolean onTouchEvent(MotionEvent e) {
    	if (true) {//getPointerCount == null) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
        case MotionEvent.ACTION_MOVE:
            mRenderer.mPixelX += (int)(x - mPreviousX);
            mRenderer.mPixelY += (int)(y - mPreviousY);
            if (mRenderer.follow) {
            	mRenderer.follow = false;
            	lm.removeUpdates(this);
                mSensorManager.unregisterListener (magListener);
                mSensorManager.unregisterListener (accListener);
            }
            /*Log.d("Gosmore", "zoom = " + mRenderer.p.zoom);
            if (mRenderer.p.zoom < 100000000 && activity.pakType < 2) {
            	activity.showDialog(Math.abs(mRenderer.p.lat) < 1 &&
            		Math.abs(mRenderer.p.lon) < 1 ? 3 : 4);
            }*/
        }
        // pid = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        mPreviousX = x;
        mPreviousY = y;  
    	}
    	else {
    		try {
    			int pcnt = (Integer) getPointerCount.invoke (e);
    			float x = (Float) getPointerCount.invoke (e, 0);
    			//getAction  & MotionEvent.ACTION_MASK
    		} catch (InvocationTargetException ite) {}
    		  catch (IllegalAccessException ie) {}
    	}
        requestRender();
        return true;
    }
    private float[] lastAccels=new float[3];
    
    public SensorEventListener accListener=new SensorEventListener() {
    	public void onSensorChanged(SensorEvent e) {
    		System.arraycopy(e.values, 0, lastAccels, 0, 3);
    	}
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};

    public final SensorEventListener magListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent e) {
        	double g = Math.sqrt(lastAccels[0]*lastAccels[0]+lastAccels[1]*lastAccels[1]+
			  lastAccels[2]*lastAccels[2]);
        	if (g > 5) {// If we are not in (momentary) free fall
        		float east[] = { e.values[1]*lastAccels[2]-e.values[2]*lastAccels[1],
        				         e.values[2]*lastAccels[0]-e.values[0]*lastAccels[2],
        				         e.values[0]*lastAccels[1]-e.values[1]*lastAccels[0]};
        		mRenderer.threeD = lastAccels[2]*2 < g; 
        		int d = (int)(180/Math.PI*(lastAccels[2]*2 < g
         				? Math.atan2(east[2],(east[0]*lastAccels[1]-east[1]*lastAccels[0])/g)
          				: -Math.atan2((east[1]*lastAccels[2]-east[2]*lastAccels[1])/g, east[0])))
          				 - mRenderer.declination;
        		mRenderer.dirSum += mRenderer.dirSum < 90 * mRenderer.dirCnt ?
        				  (d + 540) % 360 - 180 : mRenderer.dirSum > 270 * mRenderer.dirCnt
        				  ? (d + 540) % 360 + 180 : (d + 720) % 360; 
        		mRenderer.dirCnt++;
        		if (Math.abs(mRenderer.dirSum / mRenderer.dirCnt - mRenderer.p.dir) >
        		    30 / mRenderer.dirCnt + 10) {
        			//Log.d("Gosmore", "Azimuth = " + mRenderer.p.dir);
        			mRenderer.p.dir = mRenderer.dirSum / mRenderer.dirCnt;
        			mRenderer.dirSum = 0;
        			mRenderer.dirCnt = 0;
        			requestRender();
        		}
        	} 
        	/*float gg = lastAccels[0]*lastAccels[0]+lastAccels[1]*lastAccels[1]+
				lastAccels[2]*lastAccels[2];
        	if (gg > 5*5) { // If we are not in (momentary) free fall
        		float p = (e.values[0]*lastAccels[0]+e.values[1]*lastAccels[1]+
        				e.values[2]*lastAccels[2])/gg;
        		mRenderer.p.dir = (int) ((lastAccels[2]*lastAccels[2]*3 > gg ? Math.atan2(e.values[0]-p*lastAccels[0], e.values[1] - p*lastAccels[1]) :
		            lastAccels[1]*lastAccels[1]*3 > gg ? Math.PI - Math.atan2(e.values[0]-p*lastAccels[0], e.values[2] - p*lastAccels[2]) :
		    	        Math.atan2(e.values[2]-p*lastAccels[2], e.values[1] - p*lastAccels[1]))
		    	        *(180/Math.PI)) - mRenderer.declination; 
        		mRenderer.threeD = lastAccels[1]*lastAccels[1]*3 > gg; 
        		requestRender();
        	}*/
        }      
        public void onAccuracyChanged(Sensor s, int accuracy) {}
    };
  
    public MapRenderer mRenderer;
}

class MapRenderer implements GLSurfaceView.Renderer {
	private int width, height;
	public int mPixelX = 0, mPixelY = 0; // These are only modified by the UI thread
	public int oldPixelX = 0, oldPixelY = 0;
    public int bitmapWidth, bitmapHeight;
    public int dirSum = 0, dirCnt = 0, latLonCnt = 0;
    public long zTime = 0;
    public float zExp, speed, bearing;
    public double latSum = 0, lonSum = 0;
    public Bitmap mBitmap, mIcons; 
    public Paint mClearPaint;
    public Place p; // = Gosmore.s.searchResult
    public MapView mapView;
    public int mTextureID, icons;
    public boolean route = false, startR = false, doNavigate = false;
    public ProgressDialog rProgress; 
	public TextToSpeech mTts;
	public boolean tts = false, follow = false;
    //public Context mContext;
    /*public class Btn { int x, y, w, h, draw; }
    public Btn btn[] = {
    		
    }*/

/*    public float mTexelWidth;  // Convert texel to U
    public float mTexelHeight; // Convert texel to V
    public int mU;
    public int mV;
    public int mLineHeight;
*/    //public ArrayList<Label> mLabels = new ArrayList<Label>();

    public Canvas mCanvas;
    public Paint mTextPaint;
    public boolean fast = false, threeD = false;
    public int declination = 0;
    public void drawTeks(String s, int x, int y, float sin, float cos) {
    	if (!fast) {
          Matrix m = mCanvas.getMatrix ();
          m.setSinCos (-sin, cos, x, y);  
          mCanvas.setMatrix (m);
          mCanvas.drawText(s, x, y, mTextPaint);
    	}
    }      
    public long last = 0, fps = 0, frame = 0;
    public native void render (double lon, double lat, int dir, int zoom,
    		boolean threeD,	boolean follow, int width, int height);
    public native String navigate (double lon, double lat, float speed,
    		float bearing);
    public native void startRoute (double lon, double lat);
    public native void endRoute (double lon, double lat, boolean fastest,
    		int vehicle);
    public native int doRoute ();
    public MapRenderer(Context context) {
    	//mContext = context;
    }  
    public SharedPreferences prefs; 
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	mTextPaint = new Paint();       
        mTextPaint.setAntiAlias(false);
        mTextPaint.setTextSize(16);
        mTextPaint.setColor(Color.BLACK);//0xFF000000);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        //mTextPaint.setPadding(3, 3, 3, 3);
        gl.glDisable(GL10.GL_DITHER);
        gl.glClearColor(1f,1f,1f, 1f);
  	  	prefs = PreferenceManager.getDefaultSharedPreferences(mapView.getContext());
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
    	width = w;
    	height = h;
        gl.glViewport(0, 0, w, h);
        //lm = new LabelMaker (false, 64,64);
        bitmapWidth = w - 1;
        bitmapWidth |= bitmapWidth >> 1;
        bitmapWidth |= bitmapWidth >> 2;
        bitmapWidth |= bitmapWidth >> 4;
        bitmapWidth |= bitmapWidth >> 8;
        bitmapWidth++;
        bitmapHeight = h - 1;
        bitmapHeight |= bitmapHeight >> 1;
        bitmapHeight |= bitmapHeight >> 2;
        bitmapHeight |= bitmapHeight >> 4;
        bitmapHeight |= bitmapHeight >> 8;
        bitmapHeight++;
/*        mTexelWidth = (float) (1.0 / bitmapWidth);
        mTexelHeight = (float) (1.0 / bitmapHeight);*/
        mClearPaint = new Paint();
        mClearPaint.setARGB(0, 0, 0, 0);
        mClearPaint.setStyle(Style.FILL);

        //initialize(gl);
        int[] textures = new int[2];
        gl.glGenTextures(2, textures, 0);
        mTextureID = textures[1];
        icons = textures[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        // Use Nearest for performance.
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_NEAREST);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_REPLACE); // GL_MODULATE also works here.

        gl.glBindTexture(GL10.GL_TEXTURE_2D, icons);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_NEAREST);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_REPLACE); // GL_MODULATE also works here.
        //int iw = mIcons.getWidth();
        //int ih = mIcons.getHeight();
        /*int[] pixels = new int[iw*ih];
        mIcons.getPixels(pixels, 0, iw, 0, 0, iw, ih);
        mBitmap = Bitmap.createBitmap(pixels, 0, iw, iw, ih,
                                       Bitmap.Config.ARGB_8888);*/
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mIcons, 0);  
        
        mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, //Bitmap.Config.ARGB_4444); 
            	Bitmap.Config.ALPHA_8);
        mCanvas = new Canvas(mBitmap);
    } 
           
    public void onDrawFrame(GL10 gl) {
    	if (startR)	{
    		startRoute (p.lon, p.lat);
    		startR = false;
    	}
    	if (route) {
    	  //Log.d ("Gosmore", "start route");
    	  endRoute (p.lon, p.lat, prefs.getBoolean ("fastest", true),
    			Integer.parseInt(prefs.getString ("vehicle", "12")));
    	  int p; 
    	  while ((p = doRoute()) < 999 && p >= 0 && route) rProgress.setProgress (p);
    	  route = false;
    	  //Log.d ("Gosmore", "p = " + p);
    	  rProgress.dismiss();
    	}
    	if (doNavigate) {
    		doNavigate = false;
        	String msg = navigate (p.lon, p.lat, speed, bearing);
        	if (msg != "" && tts) mTts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    	}

    	gl.glViewport(0,0,width,height);
    	gl.glMatrixMode(GL10.GL_PROJECTION);
      	gl.glLoadIdentity();

    	gl.glOrthof(0.0f,width,height,0.0f,-1.0f,1.0f);

    	gl.glMatrixMode(GL10.GL_MODELVIEW);	// Really necessary ?
    	gl.glLoadIdentity();				// Really necessary ?
    	gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
    	gl.glDisable(GL10.GL_DEPTH_TEST);

        if (!fast) mBitmap.eraseColor(0); // I don't think this can be done with drawRect,
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, icons); 
        gl.glShadeModel(GL10.GL_FLAT);
//        gl.glEnable(GL10.GL_BLEND);  
        //gl.glEnable(GL10.GL_LINE_SMOOTH); // If this is enabled on the LG OO,
        // all lines will have width 1.
        
        //gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        // Not sure what this does.
        
        //mouseEv (mPixelX, mPixelY, 0, 1, false);
        if (zTime != 0) {
        	long now = SystemClock.uptimeMillis();
        	p.zoom = (int)(p.zoom * Math.exp (zExp * (now - zTime)));
        	zTime = now;
        	mapView.requestRender();
        }
        if (p.zoom < 999) p.zoom = 999;
        else if (p.zoom > 500000000) p.zoom = 500000000;
        double z = 360.0 / 4294967296.0 * (threeD ? 10000 : p.zoom) / width;
        if (!threeD && prefs.getBoolean ("north2d", false)) p.dir = 0;
        int diffY = threeD ? 0 : mPixelY-oldPixelY;
        p.lat += z * (diffY * Math.cos(Math.PI/180*p.dir) +
        		      (oldPixelX-mPixelX)*Math.sin(Math.PI/180*p.dir));
        p.lon += z * ((oldPixelX-mPixelX)*Math.cos(Math.PI/180*p.dir) -
		  diffY*Math.sin(Math.PI/180*p.dir))
		  / Math.cos(Math.PI/180*p.lat);
        if (threeD) p.dir += mPixelY - oldPixelY;
        oldPixelX = mPixelX; 
        oldPixelY = mPixelY; 
    	render(p.lon, p.lat, p.dir, threeD ? 50000 : p.zoom, threeD, follow, width, height);
    
    	/*long now = System.currentTimeMillis();
        drawTeks (fps + "", 10, 250, 0, 1);
    	if (now - last > 1000) {
    	  fps = frame;
    	  frame = 0; 
    	  last = now;
    	}  
    	else frame++;*/
               
    	if (!fast) {
        gl.glEnable(GL10.GL_BLEND);   
        gl.glEnable(GL10.GL_TEXTURE_2D);
        //gl.glShadeModel(GL10.GL_FLAT);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        
/*        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, width, 0.0f, height, 0.0f, 1.0f); 
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        // Magic offsets to promote consistent rasterization.
        gl.glTranslatef(0.375f, 0.375f, 0.0f);
//        beginDrawing(gl, width, height);
        gl.glPushMatrix();*/
        /*float snappedX = 10;
        float snappedY = 10;
        gl.glTranslatef(snappedX, snappedY, 0.0f);*/
        //Label label = mLabels.get(id);
        //Log.d("Gosmore", "w" + mBitmap.getWidth() + " h" + mBitmap.getHeight() + " x"+mBitmap.getRowBytes());
        //Log.d("Gosmore", "w" + mIcons.getWidth() + " h" + mIcons.getHeight() + " x"+mIcons.getRowBytes());
        gl.glBindTexture(GL10.GL_TEXTURE_2D, icons);  

/*        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mIcons, 0);  
        //mBitmap2.recycle();

        int[] crop2 = { 0, height - 1, width - 1, -height + 1 };
        ((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
                GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop2, 0); //label.mCrop, 0);
        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
        ((GL11Ext)gl).glDrawTexiOES((int) 1, (int) 1, 0,
                (int) width - 1, (int) height - 1);    
*/
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
          
        int[] crop = { 0, height - 1, width - 1, -height + 1 };
        ((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
                GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0); //label.mCrop, 0);
        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
        ((GL11Ext)gl).glDrawTexiOES((int) 1, (int) 1, 0,
                (int) width - 1, (int) height - 1);    
        ((GL11Ext)gl).glDrawTexiOES((int) -1, (int) -1, 0, 
                (int) width - 1, (int) height - 1);    
        gl.glColor4x(0,0,0,0x10000);//0x10000, 0x10000, 0x10000, 0x10000); 
        ((GL11Ext)gl).glDrawTexiOES((int) 0, (int) 0, 0,
                (int) width - 1, (int) height - 1);    
        /*gl.glPopMatrix();  
 
        gl.glDisable(GL10.GL_BLEND);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPopMatrix();*/  
    	}
    	/*
        draw(gl, 10, 10, id);*/
//        endDrawing(gl);
    }
}
