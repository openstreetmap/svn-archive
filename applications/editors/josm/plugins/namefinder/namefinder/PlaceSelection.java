package namefinder;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.download.DownloadSelection;
import org.openstreetmap.josm.tools.GBC;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.co.wilson.xml.MinML2;

public class PlaceSelection implements DownloadSelection {

	private JTextField searchTerm = new JTextField();
	private JButton submitSearch = new JButton(tr("Search..."));
	private DefaultListModel searchResults = new DefaultListModel();
	private JList searchResultDisplay = new JList(searchResults);
	private boolean updatingSelf;
	
	/**
	 * Data storage for search results.
	 */
	class SearchResult 
	{
		public String name;
		public String description;
		public double lat;
		public double lon;
		public int zoom;
	}
	
	/**
	 * A very primitive parser for the name finder's output. 
	 * Structure of xml described here:  http://wiki.openstreetmap.org/index.php/Name_finder
	 *
	 */
	private class Parser extends MinML2
	{
		private SearchResult currentResult = null;
		private StringBuffer description = null;
		private int depth = 0;
		/**
		 * Detect starting elements.
		 * 
		 */
		@Override public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException 
		{
			depth++;
			try 
			{
				if (qName.equals("searchresults")) 
				{
					searchResults.clear();
				}
				else if (qName.equals("named") && (depth == 2))
				{	
					currentResult = new PlaceSelection.SearchResult();
					currentResult.name = atts.getValue("name");
					currentResult.lat = Double.parseDouble(atts.getValue("lat"));
					currentResult.lon = Double.parseDouble(atts.getValue("lon"));
					currentResult.zoom = Integer.parseInt(atts.getValue("zoom"));
					searchResults.addElement(currentResult);
				}
				else if (qName.equals("description") && (depth == 3))
				{
					description = new StringBuffer();
				} 
			}
			catch (NumberFormatException x) 
			{
				x.printStackTrace(); // SAXException does not chain correctly
				throw new SAXException(x.getMessage(), x);
			} 
			catch (NullPointerException x) 
			{
				x.printStackTrace(); // SAXException does not chain correctly
				throw new SAXException(tr("NullPointerException. Possible some missing tags."), x);
			}
		}
		/** 
		 * Detect ending elements.
		 */
		@Override public void endElement(String namespaceURI, String localName, String qName) throws SAXException
		{

			if (qName.equals("searchresults")) 
			{
			}
			else if (qName.equals("description") && description != null)
			{
				currentResult.description = description.toString();
				description = null;
			}
			depth--;

		}
		/** 
		 * Read characters for description.
		 */
		@Override public void characters(char[] data, int start, int length) throws org.xml.sax.SAXException
		{
			if (description != null) 
			{
				description.append(data, start, length);
			}
		}
	}
	
	/**
	 * This queries David Earl's server. Needless to say, stuff should be configurable, and 
	 * error handling improved.
	 */
	public void queryServer()
	{
		try
		{
			URL url = new URL("http://www.frankieandshadow.com/osm/search.xml?find="+java.net.URLEncoder.encode(searchTerm.getText(), "UTF-8"));
			HttpURLConnection activeConnection = (HttpURLConnection)url.openConnection();
			System.out.println("got return: "+activeConnection.getResponseCode());
			activeConnection.setConnectTimeout(15000);
			InputStream inputStream = activeConnection.getInputStream();
			new Parser().parse(new InputStreamReader(inputStream, "UTF-8"));
		}
		catch (Exception x) 
		{
			JOptionPane.showMessageDialog(Main.parent,tr("Cannot read place search results from server"));
			x.printStackTrace();
		}
	}
	
	// add a new tab to the download dialog
	public void addGui(final DownloadDialog gui) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.add(new JLabel(tr("Enter a place name to search for:")), GBC.eol());
		panel.add(searchTerm, GBC.std().fill(GBC.HORIZONTAL));
		panel.add(submitSearch, GBC.eol());
		
		GBC c = GBC.std().fill();
		JScrollPane scrollPane = new JScrollPane(searchResultDisplay);
		panel.add(scrollPane, c);
		gui.tabpane.add(panel, tr("Places"));
		
		scrollPane.setPreferredSize(scrollPane.getPreferredSize());
		
		// when the button is clicked
		submitSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				queryServer();
			}
		});
		
		// display search results in list just by name, and add tooltip 
		// for description. would also be possible to use a table model
		// instead of list, and display lat/lon etc.
		searchResultDisplay.setCellRenderer(new DefaultListCellRenderer() {
			@Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value != null) {
					setText(((SearchResult)value).name);
					setToolTipText("<html>"+((SearchResult)value).description+"</html>");
				}
				return this;
			}
		});
		
		// if item is selected in list, notify dialog
		//searchResultDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchResultDisplay.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent lse) {
				if (lse.getValueIsAdjusting()) return;
				SearchResult r = null;
				try 
				{
					r = (SearchResult) searchResults.getElementAt(lse.getFirstIndex());
				}
				catch (Exception x)
				{
					// Ignore
				}
				if (r != null)
				{
					double size = 180.0 / Math.pow(2, r.zoom);
					gui.minlat = r.lat - size / 2;
					gui.maxlat = r.lat + size / 2;
					gui.minlon = r.lon - size;
					gui.maxlon = r.lon + size;
					updatingSelf = true;
					gui.boundingBoxChanged(null);
					updatingSelf = false;
				}
			}
		});
	}

	// if bounding box selected on other tab, de-select item
	public void boundingBoxChanged(DownloadDialog gui) {
		if (!updatingSelf) searchResultDisplay.clearSelection();
	}
}
