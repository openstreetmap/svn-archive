package org.osmu.gosmore;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Recent extends ListActivity {
   	protected void onListItemClick(ListView l, View v, int position, long id)
   	{
   		Gosmore g = (Gosmore)getApplicationContext();
   		g.s.searchResult.lat = g.s.recent.get(g.s.recent.size()-1-position).lat;
   		g.s.searchResult.lon = g.s.recent.get(g.s.recent.size()-1-position).lon;
   		g.s.searchResult.dir = g.s.recent.get(g.s.recent.size()-1-position).dir;
   		g.s.searchResult.zoom = g.s.recent.get(g.s.recent.size()-1-position).zoom;
   		finish(); // Easier way to do a deep copy ?
   	}
    @Override  
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.search);
        Gosmore g = ((Gosmore)getApplicationContext());
        String names[] = new String[g.s.recent.size()];
        for (int i = 0; i < g.s.recent.size(); i++) {
        	names[g.s.recent.size()-1-i] = g.s.recent.get(i).name;
        }
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, names));
    }
}
