package org.openstreetmap.josm.plugins.waypointSearch;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.tools.Shortcut;
import static org.openstreetmap.josm.tools.I18n.tr;


public class SelectWaypointDialog extends ToggleDialog implements KeyListener, MouseListener {

    private JTextField searchPattern = new JTextField(20);
    private DefaultListModel listModel = new DefaultListModel();
    private JList searchResult = new JList(listModel);
    private List<Marker> SearchResultObjectCache = new ArrayList<Marker>();
    private boolean first_time_search = true;
    private Engine engine = new Engine();
    
    
    public SelectWaypointDialog(String name, String iconName, String tooltip,
            Shortcut shortcut, int preferredHeight) {
        super(name, iconName, tooltip, shortcut, preferredHeight);
        build();
    }
    

    protected void build() {
        //add panel - all the gui of the plugin goes in here
        JPanel panel = new JPanel(new BorderLayout());
        
        //search field
        searchPattern.setText(tr("Enter search expression here.."));
        searchPattern.addKeyListener(this);
        searchPattern.addMouseListener(this);
        panel.add(searchPattern,BorderLayout.NORTH);
        
        //add result table
        searchResult.setLayoutOrientation(JList.VERTICAL);
        searchResult.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResult.addMouseListener(this);
        JScrollPane scrollPane = new JScrollPane(searchResult);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane,BorderLayout.CENTER);
        
        //add label
        JLabel label = new JLabel(tr("Select waypoint to move map"));
        panel.add(label,BorderLayout.SOUTH);
        
        //add panel to JOSM gui
        createLayout(panel, false, null);
    }

    
    
    public void updateSearchResults(){
        String searchfor = "";
        listModel.clear();
        SearchResultObjectCache.clear();
        if (!first_time_search) {
            searchfor = searchPattern.getText();
        }
        for (Iterator<Marker> i = engine.searchGpxWaypoints(searchfor).iterator(); i.hasNext();) {
            Marker marker = i.next();
            listModel.addElement(marker.getText());
            SearchResultObjectCache.add(marker);
        }
    }
    

    @Override
    public void keyPressed(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }


    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub
        updateSearchResults();
    }


    @Override
    public void keyTyped(KeyEvent arg0) {
        first_time_search = false;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource()==searchResult) {
            //click on the search result box
            Marker marker = SearchResultObjectCache.get(searchResult.getSelectedIndex());
            Main.map.mapView.zoomTo(marker.getCoor());
        } else {
            //click on the text field (input search expression)
        }
    }


    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void mousePressed(MouseEvent arg0) {
        if (searchPattern.getSelectedText()==null) {
            searchPattern.selectAll();
        }
        
    }


    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }
}
