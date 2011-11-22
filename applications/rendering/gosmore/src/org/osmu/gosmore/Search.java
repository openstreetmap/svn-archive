package org.osmu.gosmore;

import java.util.ArrayList;

import android.app.ListActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Search extends ListActivity implements TextWatcher {
    public Search ()
    {
    	super();
    	adap = new EfficientAdapter ();
    }
    private EfficientAdapter adap;
    private EditText et;
    
    //---- It's also a TextWatcher for the EditText
  	public void afterTextChanged (Editable s){
          adap.results.clear();
          this.search (et.getText().toString());
          setListAdapter(adap);
   	}
   	public void beforeTextChanged (CharSequence s, int start, int count, int after){}
   	public void onTextChanged (CharSequence s, int start, int before, int count){}
   	//---- End TextWatcher

   	protected void onListItemClick(ListView l, View v, int position, long id)
   	{
		Gosmore g = (Gosmore)getApplicationContext(); 
		g.s.searchResult.lat = adap.results.get(position).lat;
   		g.s.searchResult.lon = adap.results.get(position).lon;
   		g.s.searchResult.zoom = adap.results.get(position).zoom;
   		g.s.searchResult.dir = 0;
   		g.s.searchResult.name = adap.results.get(position).s;
   		Place p = new Place();
   		p.lat = g.s.searchResult.lat;
   		p.lon = g.s.searchResult.lon;
   		p.zoom = g.s.searchResult.zoom;
   		p.dir = g.s.searchResult.dir;
   		p.name = g.s.searchResult.name;
   		g.s.recent.add(p);
   		if (g.s.recent.size() > 40) g.s.recent.remove(0);
   		g.s.search = et.getText().toString();
   		finish();
   	}  
    @Override  
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
   	    adap.icons = ((Gosmore)getApplicationContext()).icons;
        adap.mInflater = getLayoutInflater();

        et = (EditText) findViewById (R.id.search_query);
        //et.setInputType(InputType.TYPE_CLASS_TEXT|0x00080000);
        et.addTextChangedListener(this);
        et.setText(((Gosmore)getApplicationContext()).s.search);
        afterTextChanged (null);
        et.requestFocus();//|0x00080000
    }  
    //public int zoom;
    //public native void setLocation (double lon, double lat);
    public native void search (String s);
    public void searchResult(int x, int y, int w, int h,
    		double dist, int u, int v, String s,
    		double lon, double lat, int _zoom) {
        adap.results.add(new ResultData());
        adap.results.get(adap.results.size()-1).x = x;
        adap.results.get(adap.results.size()-1).y = y;
        adap.results.get(adap.results.size()-1).w = w;
        adap.results.get(adap.results.size()-1).h = h;
        adap.results.get(adap.results.size()-1).dist = dist;
        adap.results.get(adap.results.size()-1).s = s;
        adap.results.get(adap.results.size()-1).v = v;
        adap.results.get(adap.results.size()-1).u = u;
        adap.results.get(adap.results.size()-1).lon = lon;
        adap.results.get(adap.results.size()-1).lat = lat;
        adap.results.get(adap.results.size()-1).zoom = _zoom;
    } 
}

class ResultData {
	public ResultData () {}
	public double dist, lon, lat;
	public String s;
	public int u, v, x, y, w, h, zoom;
}
 
class EfficientAdapter extends BaseAdapter {
    public LayoutInflater mInflater;
    public ArrayList<ResultData> results;
    public Bitmap icons;
    public EfficientAdapter() {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        results = new ArrayList<ResultData>();
    }
    public int getCount() {
        return results.size();
    }
    public Object getItem(int position) {
        return position;
    }
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
    	//if (convertView == null) {
    	convertView = mInflater.inflate(R.layout.search_result, null);
    	TextView t = (TextView) convertView.findViewById(R.id.result_text);
    	t.setText(results.get(position).s);
    	TextView d = (TextView) convertView.findViewById(R.id.distance);
    	double dist = results.get(position).dist;
    	d.setText(dist >= 998 ? "Far " : dist > 1 ? (int) dist + " km " :
    				(int)(dist*1000) + " m ");   
    	ImageView i = (ImageView) convertView.findViewById(R.id.result_icon);
    	if (results.get(position).w > 0) i.setImageBitmap(Bitmap.createBitmap(icons,results.get(position).x, 
    				results.get(position).y, results.get(position).w, results.get(position).h));
    	return convertView;
    }
}