package org.openstreetmap.josm.plugins.directdownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.io.OsmApi;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DownloadDataGui extends ExtendedDialog {

    private NamedResultTableModel model;
    private NamedResultTableColumnModel columnmodel;
    private JTable tblSearchResults;

    public DownloadDataGui() {
        // Initalizes ExtendedDialog
        super(Main.parent,
          tr("Download Track"),
          new String[] {tr("Download Track"), tr("Cancel")},
          true
          );

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
    
        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
        model = new NamedResultTableModel(selectionModel);
        columnmodel = new NamedResultTableColumnModel();
        tblSearchResults = new JTable(model, columnmodel);
        tblSearchResults.setSelectionModel(selectionModel);
        tblSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tblSearchResults);
        scrollPane.setPreferredSize(new Dimension(800,300));
        panel.add(scrollPane, BorderLayout.CENTER);

    model.setData(getTrackList());

    setContent(panel);
    setupDialog();
    }

    private static class TrackListHandler extends DefaultHandler {
        private LinkedList<UserTrack> data = new LinkedList<UserTrack>();
        
        private String cdata = new String();
    
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (qName.equals("gpx_file")) {
            UserTrack track = new UserTrack();
    
            track.id       = atts.getValue("id");
            track.filename = atts.getValue("name");
            track.datetime = atts.getValue("timestamp").replaceAll("[TZ]", " "); // TODO: do real parsing and time zone conversion
            
            data.addFirst(track);
            } 
            cdata = new String();
        }
    
        public void characters(char ch[], int start, int length)
            throws SAXException {
            cdata += new String(ch, start, length);
        }
    
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("description")) {
            data.getFirst().description = cdata;
            }
            /*
            else if (qName.equals("tag")) {
            data.getFirst().tags = cdata;
            cdata = new String();
            }    
            */
        }

        public List<UserTrack> getResult() {
            return data;
        }
    }

    private List<UserTrack> getTrackList() {
        String urlString = OsmApi.getOsmApi().getBaseUrl() + "user/gpx_files";

        try {
            URL userTracksUrl = new URL(urlString);
    
            SAXParserFactory spf = SAXParserFactory.newInstance();
            TrackListHandler handler = new TrackListHandler();
    
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            
            //parse the file and also register this class for call backs
            sp.parse(userTracksUrl.openStream(), handler);
            
            return handler.getResult();
        } catch (java.net.MalformedURLException e) {
            JOptionPane.showMessageDialog(null, tr("Invalid URL {0}", urlString));
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(null, tr("Error fetching URL {0}", urlString));
        } catch (SAXException e) {
            JOptionPane.showMessageDialog(null, tr("Error parsing data from URL {0}", urlString));
        } catch(ParserConfigurationException pce) {
            JOptionPane.showMessageDialog(null, tr("Error parsing data from URL {0}", urlString));
        }

        return new LinkedList<UserTrack>();
    }

    static class NamedResultTableModel extends DefaultTableModel {
        private ArrayList<UserTrack> data;
        private ListSelectionModel selectionModel;

        public NamedResultTableModel(ListSelectionModel selectionModel) {
            data = new ArrayList<UserTrack>();
            this.selectionModel = selectionModel;
        }
        
        @Override
        public int getRowCount() {
            if (data == null) return 0;
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (data == null) return null;
            return data.get(row);
        }

        public void setData(List<UserTrack> data) {
            if (data == null) {
                this.data.clear();
            } else {
                this.data  =new ArrayList<UserTrack>(data);
            }
            fireTableDataChanged();
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public UserTrack getSelectedUserTrack() {
            if (selectionModel.getMinSelectionIndex() < 0)
                return null;
            return data.get(selectionModel.getMinSelectionIndex());
        }
    }

    public UserTrack getSelectedUserTrack() {
        return model.getSelectedUserTrack();
    }

    static class NamedResultTableColumnModel extends DefaultTableColumnModel {
        protected void createColumns() {
            TableColumn col = null;
            NamedResultCellRenderer renderer = new NamedResultCellRenderer();

            // column 0 - DateTime
            col = new TableColumn(0);
            col.setHeaderValue(tr("Date"));
            col.setResizable(true);
            col.setPreferredWidth(150);
            col.setCellRenderer(renderer);
            addColumn(col);

            // column 1 - Filename
            col = new TableColumn(1);
            col.setHeaderValue(tr("Filename"));
            col.setResizable(true);
            col.setPreferredWidth(200);
            col.setCellRenderer(renderer);
            addColumn(col);

            // column 2 - Description
            col = new TableColumn(2);
            col.setHeaderValue(tr("Description"));
            col.setResizable(true);
            col.setPreferredWidth(450);
            col.setCellRenderer(renderer);
            addColumn(col);

            // column 3 - tags
        /*
            col = new TableColumn(3);
            col.setHeaderValue(tr("Tags"));
            col.setResizable(true);
            col.setPreferredWidth(100);
            col.setCellRenderer(renderer);
            addColumn(col);
        */
        }

        public NamedResultTableColumnModel() {
            createColumns();
        }
    }

    class ListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent lse) {
        }
    }

    static class NamedResultCellRenderer extends JLabel implements TableCellRenderer {

        public NamedResultCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        }

        protected void reset() {
            setText("");
            setIcon(null);
        }

        protected void renderColor(boolean selected) {
            if (selected) {
                setForeground(UIManager.getColor("Table.selectionForeground"));
                setBackground(UIManager.getColor("Table.selectionBackground"));
            } else {
                setForeground(UIManager.getColor("Table.foreground"));
                setBackground(UIManager.getColor("Table.background"));
            }
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            reset();
            renderColor(isSelected);

            if (value == null) return this;
            UserTrack sr = (UserTrack) value;
            switch(column) {
            case 0:
                setText(sr.datetime);
                break;
            case 1:
                setText(sr.filename);
                break;        
            case 2:
                setText(sr.description);
                break;
        /*
            case 3:
                setText(sr.tags);
                break;
        */
            }
            return this;
        }
    }
}
