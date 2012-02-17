package public_transport;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.mapmode.DeleteAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.UrlLabel;

public class RoutePatternAction extends JosmAction {

  public static int STOPLIST_ROLE_COLUMN = 2;

  private class RoutesLSL implements ListSelectionListener {
    RoutePatternAction root = null;

    public RoutesLSL(RoutePatternAction rpa) {
      root = rpa;
    }

    public void valueChanged(ListSelectionEvent e) {
      root.routesSelectionChanged();
    }
  };

  private class RouteReference implements Comparable< RouteReference > {
    Relation route;

    public RouteReference(Relation route) {
      this.route = route;
    }

    public int compareTo(RouteReference rr) {
      if (route.get("route") != null)
      {
        if (rr.route.get("route") == null)
          return -1;
        int result = route.get("route").compareTo(rr.route.get("route"));
        if (result != 0)
          return result;
      }
      else if (rr.route.get("route") != null)
        return 1;
      if (route.get("ref") != null)
      {
        if (rr.route.get("ref") == null)
          return -1;
        int result = route.get("ref").compareTo(rr.route.get("ref"));
        if (result != 0)
      return result;
      }
      else if (rr.route.get("ref") != null)
        return 1;
      if (route.get("to") != null)
      {
        if (rr.route.get("to") == null)
          return -1;
        int result = route.get("to").compareTo(rr.route.get("to"));
        if (result != 0)
          return result;
      }
      else if (rr.route.get("to") != null)
        return 1;
      if (route.get("direction") != null)
      {
        if (rr.route.get("direction") == null)
          return -1;
        int result = route.get("direction").compareTo(rr.route.get("direction"));
        if (result != 0)
          return result;
      }
      else if (rr.route.get("direction") != null)
        return 1;
      if (route.getId() < rr.route.getId())
        return -1;
      else if (route.getId() > rr.route.getId())
        return 1;
      return 0;
    }

    public String toString() {
      String buf = route.get("route");
      if ((route.get("ref") != null) && (route.get("ref") != ""))
      {
        buf += " " + route.get("ref");
      }
      if ((route.get("loc_ref") != null) && (route.get("loc_ref") != ""))
      {
        buf += " [" + route.get("loc_ref") + "]";
      }

      if ((route.get("to") != null) && (route.get("to") != ""))
      {
        buf += ": " + route.get("to");
      }
      else if ((route.get("direction") != null) && (route.get("direction") != ""))
      {
        buf += " " + route.get("ref") + ": " + route.get("direction");
      }
      else
      {
        buf += " " + route.get("ref");
      }
      buf += tr(" [ID] {0}", Long.toString(route.getId()));

      return buf;
    }
  };

  private class TagTableModel extends DefaultTableModel implements TableModelListener {
    Relation relation = null;
    TreeSet< String > blacklist = null;
    boolean hasFixedKeys = true;

    public TagTableModel(boolean hasFixedKeys) {
      this.hasFixedKeys = hasFixedKeys;
    }

    public boolean isCellEditable(int row, int column) {
      if ((column == 0) && (hasFixedKeys))
        return false;
      return true;
    }

    public void readRelation(Relation rel) {
      relation = rel;

      for (int i = 0; i < getRowCount(); ++i)
      {
        String value = rel.get((String)getValueAt(i, 0));
        if (value == null)
          value = "";
        setValueAt(value, i, 1);
      }
    }

    public void readRelation(Relation rel, TreeSet< String > blacklist) {
      relation = rel;
      this.blacklist = blacklist;

      setRowCount(0);
      Iterator< Map.Entry< String, String > > iter = rel.getKeys().entrySet().iterator();
      while (iter.hasNext())
      {
        Map.Entry< String, String > entry = iter.next();
        if (!blacklist.contains(entry.getKey()))
        {
          Vector< String > newRow = new Vector< String >();
          newRow.add(entry.getKey());
          newRow.add(entry.getValue());
          addRow(newRow);
        }
      }

      for (int i = 0; i < getRowCount(); ++i)
      {
        String value = rel.get((String)getValueAt(i, 0));
        if (value == null)
          value = "";
        setValueAt(value, i, 1);
      }
    }

    public void tableChanged(TableModelEvent e)
    {
      if (e.getType() == TableModelEvent.UPDATE)
      {
        relation.setModified(true);

        String key = (String)getValueAt(e.getFirstRow(), 0);
        if (key == null)
          return;
        if ((blacklist == null) || (!blacklist.contains(key)))
        {
          relation.setModified(true);
          if ("".equals(getValueAt(e.getFirstRow(), 1)))
            relation.remove(key);
          else
            relation.put(key, (String)getValueAt(e.getFirstRow(), 1));
        }
        else
        {
          if (e.getColumn() == 0)
            setValueAt("", e.getFirstRow(), 0);
        }
      }
    }
  };

  private class CustomCellEditorTable extends JTable {
    TreeMap< Integer, TableCellEditor > col1 = null;
    TreeMap< Integer, TableCellEditor > col2 = null;

    public CustomCellEditorTable() {
      col1 = new TreeMap< Integer, TableCellEditor >();
      col2 = new TreeMap< Integer, TableCellEditor >();
    }

    public TableCellEditor getCellEditor(int row, int column) {
      TableCellEditor editor = null;
      if (column == 0)
        editor = col1.get(new Integer(row));
      else
        editor = col2.get(new Integer(row));
      if (editor == null)
        return new DefaultCellEditor(new JTextField());
      else
        return editor;
    }

    public void setCellEditor(int row, int column, TableCellEditor editor) {
      if (column == 0)
        col1.put(new Integer(row), editor);
      else
        col2.put(new Integer(row), editor);
    }
  };

  private class StoplistTableModel extends DefaultTableModel {

    public Vector<Node> nodes = new Vector<Node>();

    public boolean isCellEditable(int row, int column) {
      if (column != STOPLIST_ROLE_COLUMN)
        return false;
      return true;
    }

    public void addRow(Object[] obj) {
      throw new UnsupportedOperationException();
    }

    public void insertRow(int insPos, Object[] obj) {
      throw new UnsupportedOperationException();
    }

    public void addRow(Node node, String role, double distance) {
      insertRow(-1, node, role, distance);
    }

    public void insertRow(int insPos, Node node, String role, double distance) {
      String[] buf = { "", "", "", "" };
      String curName = node.get("name");
      if (curName != null)
      {
        buf[0] = curName;
      }
      else
      {
        buf[0] = tr("[ID] {0}", (new Long(node.getId())).toString());
      }
      String curRef = node.get("ref");
      if (curRef != null)
      {
        buf[1] = curRef;
      }
      buf[STOPLIST_ROLE_COLUMN] = role;
      buf[3] = Double.toString(((double)(int)(distance*40000/360.0*100))/100);

      if (insPos == -1)
      {
        nodes.addElement(node);
        super.addRow(buf);
      }
      else
      {
        nodes.insertElementAt(node, insPos);
        super.insertRow(insPos, buf);
      }
    }

    public void clear()
    {
      nodes.clear();
      super.setRowCount(0);
    }
  };

  private class StoplistTableModelListener implements TableModelListener {
    public void tableChanged(TableModelEvent e)
    {
      if (e.getType() == TableModelEvent.UPDATE)
      {
        rebuildNodes();
      }
    }
  };

  private class SegmentMetric {
    public double aLat, aLon;
    public double length;
    public double d1, d2, o1, o2;
    public double distance;

    public SegmentMetric(double fromLat, double fromLon, double toLat, double toLon,
                         double distance) {
      this.distance = distance;

      aLat = fromLat;
      aLon = fromLon;

      //Compute length and direction
      //length is in units of latitude degrees
      d1 = toLat - fromLat;
      d2 = (toLon - fromLon) * Math.cos(fromLat * Math.PI/180.0);
      length = Math.sqrt(d1*d1 + d2*d2);

      //Normalise direction
      d1 = d1 / length;
      d2 = d2 / length;

      //Compute orthogonal direction (right hand size is positive)
      o1 = - d2;
      o2 = d1;

      //Prepare lon direction to reduce the number of necessary multiplications
      d2 = d2 * Math.cos(fromLat * Math.PI/180.0);
      o2 = o2 * Math.cos(fromLat * Math.PI/180.0);
    }
  };

  private class StopReference implements Comparable< StopReference > {
    public int index = 0;
    public double pos = 0;
    public double distance = 0;
    public String name = "";
    public String role = "";
    public Node node;

    public StopReference(int inIndex, double inPos, double inDistance,
             String inName, String inRole, Node inNode) {
      index = inIndex;
      pos = inPos;
      distance = inDistance;
      name = inName;
      role = inRole;
      node = inNode;
    }

    public int compareTo(StopReference sr) {
      if (this.index < sr.index)
    return -1;
      if (this.index > sr.index)
    return 1;
      if (this.pos < sr.pos)
    return -1;
      if (this.pos > sr.pos)
    return 1;
      return 0;
    }
  };

  private static JDialog jDialog = null;
  private static JTabbedPane tabbedPane = null;
  private static DefaultListModel relsListModel = null;
  private static TagTableModel requiredTagsData = null;
  private static CustomCellEditorTable requiredTagsTable = null;
  private static TagTableModel commonTagsData = null;
  private static CustomCellEditorTable commonTagsTable = null;
  private static TagTableModel otherTagsData = null;
  private static TreeSet< String > tagBlacklist = null;
  private static CustomCellEditorTable otherTagsTable = null;
  private static ItineraryTableModel itineraryData = null;
  private static JTable itineraryTable = null;
  private static StoplistTableModel stoplistData = null;
  private static JTable stoplistTable = null;
  private static JList relsList = null;
  private static JCheckBox cbRight = null;
  private static JCheckBox cbLeft = null;
  private static JTextField tfSuggestStopsLimit = null;
  private static Relation currentRoute = null;
  private static Vector< SegmentMetric > segmentMetrics = null;
  private static Vector< RelationMember > markedWays = new Vector< RelationMember >();
  private static Vector< RelationMember > markedNodes = new Vector< RelationMember >();

  private static Relation copy = null;

  public RoutePatternAction() {
    super(tr("Route patterns ..."), null,
      tr("Edit Route patterns for public transport"), null, false);
    putValue("toolbar", "publictransport/routepattern");
    Main.toolbar.register(this);
  }

  public void actionPerformed(ActionEvent event) {
    Frame frame = JOptionPane.getFrameForComponent(Main.parent);
    DataSet mainDataSet = Main.main.getCurrentDataSet();

    if (jDialog == null)
    {
      jDialog = new JDialog(frame, tr("Route Patterns"), false);
      tabbedPane = new JTabbedPane();
      JPanel tabOverview = new JPanel();
      tabbedPane.addTab(tr("Overview"), tabOverview);
      JPanel tabTags = new JPanel();
      tabbedPane.addTab(tr("Tags"), tabTags);
      JPanel tabItinerary = new JPanel();
      tabbedPane.addTab(tr("Itinerary"), tabItinerary);
      JPanel tabStoplist = new JPanel();
      tabbedPane.addTab(tr("Stops"), tabStoplist);
      JPanel tabMeta = new JPanel();
      tabbedPane.addTab(tr("Meta"), tabMeta);
      tabbedPane.setEnabledAt(0, true);
      tabbedPane.setEnabledAt(1, false);
      tabbedPane.setEnabledAt(2, false);
      tabbedPane.setEnabledAt(3, false);
      tabbedPane.setEnabledAt(4, false);
      jDialog.add(tabbedPane);

      //Overview Tab
      Container contentPane = tabOverview;
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layoutCons = new GridBagConstraints();
      contentPane.setLayout(gridbag);

      JLabel headline = new JLabel(tr("Existing route patterns:"));

      layoutCons.gridx = 0;
      layoutCons.gridy = 0;
      layoutCons.gridwidth = 3;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(headline, layoutCons);
      contentPane.add(headline);

      relsListModel = new DefaultListModel();
      relsList = new JList(relsListModel);
      JScrollPane rpListSP = new JScrollPane(relsList);
      String[] data = {"1", "2", "3", "4", "5", "6"};
      relsListModel.copyInto(data);
      relsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      relsList.addListSelectionListener(new RoutesLSL(this));

      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 3;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 1.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(rpListSP, layoutCons);
      contentPane.add(rpListSP);

      JButton bRefresh = new JButton(tr("Refresh"));
      bRefresh.setActionCommand("routePattern.refresh");
      bRefresh.addActionListener(this);

      layoutCons.gridx = 0;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.gridheight = 2;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bRefresh, layoutCons);
      contentPane.add(bRefresh);

      JButton bNew = new JButton(tr("New"));
      bNew.setActionCommand("routePattern.overviewNew");
      bNew.addActionListener(this);

      layoutCons.gridx = 1;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.gridheight = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bNew, layoutCons);
      contentPane.add(bNew);

      JButton bDelete = new JButton(tr("Delete"));
      bDelete.setActionCommand("routePattern.overviewDelete");
      bDelete.addActionListener(this);

      layoutCons.gridx = 1;
      layoutCons.gridy = 3;
      layoutCons.gridwidth = 1;
      layoutCons.gridheight = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bDelete, layoutCons);
      contentPane.add(bDelete);

      JButton bDuplicate = new JButton(tr("Duplicate"));
      bDuplicate.setActionCommand("routePattern.overviewDuplicate");
      bDuplicate.addActionListener(this);

      layoutCons.gridx = 2;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.gridheight = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bDuplicate, layoutCons);
      contentPane.add(bDuplicate);

      JButton bReflect = new JButton(tr("Reflect"));
      bReflect.setActionCommand("routePattern.overviewReflect");
      bReflect.addActionListener(this);

      layoutCons.gridx = 2;
      layoutCons.gridy = 3;
      layoutCons.gridwidth = 1;
      layoutCons.gridheight = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bReflect, layoutCons);
      contentPane.add(bReflect);

      //Tags Tab
      /*Container*/ contentPane = tabTags;
      /*GridBagLayout*/ gridbag = new GridBagLayout();
      /*GridBagConstraints*/ layoutCons = new GridBagConstraints();
      contentPane.setLayout(gridbag);

      /*JLabel*/ headline = new JLabel(tr("Required tags:"));

      layoutCons.gridx = 0;
      layoutCons.gridy = 0;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(headline, layoutCons);
      contentPane.add(headline);

      requiredTagsTable = new CustomCellEditorTable();
      requiredTagsData = new TagTableModel(true);
      requiredTagsData.addColumn(tr("Key"));
      requiredTagsData.addColumn(tr("Value"));
      tagBlacklist = new TreeSet< String >();
      Vector< String > rowContent = new Vector< String >();
      /* TODO: keys and values should also be translated using TransText class */
      rowContent.add("type");
      tagBlacklist.add("type");
      rowContent.add("route");
      requiredTagsData.addRow(rowContent);
      JComboBox comboBox = new JComboBox();
      comboBox.addItem("route");
      requiredTagsTable.setCellEditor(0, 1, new DefaultCellEditor(comboBox));
      rowContent = new Vector< String >();
      rowContent.add(0, "route");
      tagBlacklist.add("route");
      rowContent.add(1, "bus");
      requiredTagsData.addRow(rowContent);
      /*JComboBox*/ comboBox = new JComboBox();
      comboBox.addItem("bus");
      comboBox.addItem("trolleybus");
      comboBox.addItem("tram");
      comboBox.addItem("light_rail");
      comboBox.addItem("subway");
      comboBox.addItem("rail");
      requiredTagsTable.setCellEditor(1, 1, new DefaultCellEditor(comboBox));
      rowContent = new Vector< String >();
      rowContent.add(0, "ref");
      tagBlacklist.add("ref");
      rowContent.add(1, "");
      requiredTagsData.addRow(rowContent);
      rowContent = new Vector< String >();
      rowContent.add(0, "to");
      tagBlacklist.add("to");
      rowContent.add(1, "");
      requiredTagsData.addRow(rowContent);
      rowContent = new Vector< String >();
      rowContent.add(0, "network");
      tagBlacklist.add("network");
      rowContent.add(1, "");
      requiredTagsData.addRow(rowContent);
      requiredTagsTable.setModel(requiredTagsData);
      JScrollPane tableSP = new JScrollPane(requiredTagsTable);
      requiredTagsData.addTableModelListener(requiredTagsData);

      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.25;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tableSP, layoutCons);
      Dimension preferredSize = tableSP.getPreferredSize();
      preferredSize.setSize(tableSP.getPreferredSize().getWidth(), tableSP.getPreferredSize().getHeight()/4.0);
      tableSP.setPreferredSize(preferredSize);
      contentPane.add(tableSP);

      headline = new JLabel(tr("Common tags:"));

      layoutCons.gridx = 0;
      layoutCons.gridy = 2;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(headline, layoutCons);
      contentPane.add(headline);

      commonTagsTable = new CustomCellEditorTable();
      commonTagsData = new TagTableModel(true);
      commonTagsData.addColumn(tr("Key"));
      commonTagsData.addColumn(tr("Value"));
      rowContent = new Vector< String >();
      rowContent.add(0, "loc_ref");
      tagBlacklist.add("loc_ref");
      rowContent.add(1, "");
      commonTagsData.addRow(rowContent);
      rowContent = new Vector< String >();
      rowContent.add(0, "direction");
      tagBlacklist.add("direction");
      rowContent.add(1, "");
      commonTagsData.addRow(rowContent);
      rowContent = new Vector< String >();
      rowContent.add(0, "from");
      tagBlacklist.add("from");
      rowContent.add(1, "");
      commonTagsData.addRow(rowContent);
      rowContent = new Vector< String >();
      rowContent.add(0, "operator");
      tagBlacklist.add("operator");
      rowContent.add(1, "");
      commonTagsData.addRow(rowContent);
      rowContent = new Vector< String >();
      rowContent.add(0, "color");
      tagBlacklist.add("color");
      rowContent.add(1, "");
      commonTagsData.addRow(rowContent);
      rowContent = new Vector< String >();
      rowContent.add(0, "name");
      tagBlacklist.add("name");
      rowContent.add(1, "");
      commonTagsData.addRow(rowContent);
      commonTagsTable.setModel(commonTagsData);
      /*JScrollPane*/ tableSP = new JScrollPane(commonTagsTable);
      commonTagsData.addTableModelListener(commonTagsData);

      layoutCons.gridx = 0;
      layoutCons.gridy = 3;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.25;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tableSP, layoutCons);
      /*Dimension*/ preferredSize = tableSP.getPreferredSize();
      preferredSize.setSize(tableSP.getPreferredSize().getWidth(), tableSP.getPreferredSize().getHeight()/4.0);
      tableSP.setPreferredSize(preferredSize);
      contentPane.add(tableSP);

      headline = new JLabel(tr("Additional tags:"));

      layoutCons.gridx = 0;
      layoutCons.gridy = 4;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(headline, layoutCons);
      contentPane.add(headline);

      otherTagsTable = new CustomCellEditorTable();
      otherTagsData = new TagTableModel(false);
      otherTagsData.addColumn(tr("Key"));
      otherTagsData.addColumn(tr("Value"));
      otherTagsTable.setModel(otherTagsData);
      /*JScrollPane*/ tableSP = new JScrollPane(otherTagsTable);
      otherTagsData.addTableModelListener(otherTagsData);

      layoutCons.gridx = 0;
      layoutCons.gridy = 5;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 1.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tableSP, layoutCons);
      /*Dimension*/ preferredSize = tableSP.getPreferredSize();
      preferredSize.setSize(tableSP.getPreferredSize().getWidth(), tableSP.getPreferredSize().getHeight()/2.0);
      tableSP.setPreferredSize(preferredSize);
      contentPane.add(tableSP);

      JButton bAddTag = new JButton(tr("Add a new Tag"));
      bAddTag.setActionCommand("routePattern.tagAddTag");
      bAddTag.addActionListener(this);

      layoutCons.gridx = 0;
      layoutCons.gridy = 6;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bAddTag, layoutCons);
      contentPane.add(bAddTag);

      //Itinerary Tab
      contentPane = tabItinerary;
      gridbag = new GridBagLayout();
      layoutCons = new GridBagConstraints();
      contentPane.setLayout(gridbag);

      itineraryTable = new JTable();
      itineraryData = new ItineraryTableModel();
      itineraryData.addColumn(tr("Name/Id"));
      itineraryData.addColumn(tr("Role"));
      itineraryTable.setModel(itineraryData);
      /*JScrollPane*/ tableSP = new JScrollPane(itineraryTable);
      /*JComboBox*/ comboBox = new JComboBox();
      comboBox.addItem("");
      comboBox.addItem("forward");
      comboBox.addItem("backward");
      itineraryTable.getColumnModel().getColumn(1)
      .setCellEditor(new DefaultCellEditor(comboBox));
      itineraryData.addTableModelListener(itineraryData);

      layoutCons.gridx = 0;
      layoutCons.gridy = 0;
      layoutCons.gridwidth = 4;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 1.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tableSP, layoutCons);
      contentPane.add(tableSP);

      JButton bFind = new JButton(tr("Find"));
      bFind.setActionCommand("routePattern.itineraryFind");
      bFind.addActionListener(this);

      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bFind, layoutCons);
      contentPane.add(bFind);

      JButton bShow = new JButton(tr("Show"));
      bShow.setActionCommand("routePattern.itineraryShow");
      bShow.addActionListener(this);

      layoutCons.gridx = 0;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bShow, layoutCons);
      contentPane.add(bShow);

      JButton bMark = new JButton(tr("Mark"));
      bMark.setActionCommand("routePattern.itineraryMark");
      bMark.addActionListener(this);

      layoutCons.gridx = 1;
      layoutCons.gridy = 1;
      layoutCons.gridheight = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bMark, layoutCons);
      contentPane.add(bMark);

      JButton bAdd = new JButton(tr("Add"));
      bAdd.setActionCommand("routePattern.itineraryAdd");
      bAdd.addActionListener(this);

      layoutCons.gridx = 2;
      layoutCons.gridy = 1;
      layoutCons.gridheight = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bAdd, layoutCons);
      contentPane.add(bAdd);

      /*JButton*/ bDelete = new JButton(tr("Delete"));
      bDelete.setActionCommand("routePattern.itineraryDelete");
      bDelete.addActionListener(this);

      layoutCons.gridx = 2;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bDelete, layoutCons);
      contentPane.add(bDelete);

      JButton bSort = new JButton(tr("Sort"));
      bSort.setActionCommand("routePattern.itinerarySort");
      bSort.addActionListener(this);

      layoutCons.gridx = 3;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bSort, layoutCons);
      contentPane.add(bSort);

      /*JButton*/ bReflect = new JButton(tr("Reflect"));
      bReflect.setActionCommand("routePattern.itineraryReflect");
      bReflect.addActionListener(this);

      layoutCons.gridx = 3;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bReflect, layoutCons);
      contentPane.add(bReflect);

      //Stoplist Tab
      contentPane = tabStoplist;
      gridbag = new GridBagLayout();
      layoutCons = new GridBagConstraints();
      contentPane.setLayout(gridbag);

      stoplistTable = new JTable();
      stoplistData = new StoplistTableModel();
      stoplistData.addColumn(tr("Name/Id"));
      stoplistData.addColumn(tr("Ref"));
      stoplistData.addColumn(tr("Role"));
      stoplistData.addColumn(tr("km"));
      stoplistTable.setModel(stoplistData);
      /*JScrollPane*/ tableSP = new JScrollPane(stoplistTable);
      /*JComboBox*/ comboBox = new JComboBox();
      comboBox.addItem("");
      comboBox.addItem("forward_stop");
      comboBox.addItem("backward_stop");
      stoplistTable.getColumnModel().getColumn(STOPLIST_ROLE_COLUMN)
                   .setCellEditor(new DefaultCellEditor(comboBox));
      stoplistData.addTableModelListener(new StoplistTableModelListener());

      layoutCons.gridx = 0;
      layoutCons.gridy = 0;
      layoutCons.gridwidth = 4;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 1.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tableSP, layoutCons);
      contentPane.add(tableSP);

      /*JButton*/ bFind = new JButton(tr("Find"));
      bFind.setActionCommand("routePattern.stoplistFind");
      bFind.addActionListener(this);

      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bFind, layoutCons);
      contentPane.add(bFind);

      /*JButton*/ bShow = new JButton(tr("Show"));
      bShow.setActionCommand("routePattern.stoplistShow");
      bShow.addActionListener(this);

      layoutCons.gridx = 0;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bShow, layoutCons);
      contentPane.add(bShow);

      /*JButton*/ bMark = new JButton(tr("Mark"));
      bMark.setActionCommand("routePattern.stoplistMark");
      bMark.addActionListener(this);

      layoutCons.gridx = 1;
      layoutCons.gridy = 1;
      layoutCons.gridheight = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bMark, layoutCons);
      contentPane.add(bMark);

      /*JButton*/ bAdd = new JButton(tr("Add"));
      bAdd.setActionCommand("routePattern.stoplistAdd");
      bAdd.addActionListener(this);

      layoutCons.gridx = 2;
      layoutCons.gridy = 1;
      layoutCons.gridheight = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bAdd, layoutCons);
      contentPane.add(bAdd);

      /*JButton*/ bDelete = new JButton(tr("Delete"));
      bDelete.setActionCommand("routePattern.stoplistDelete");
      bDelete.addActionListener(this);

      layoutCons.gridx = 2;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bDelete, layoutCons);
      contentPane.add(bDelete);

      /*JButton*/ bSort = new JButton(tr("Sort"));
      bSort.setActionCommand("routePattern.stoplistSort");
      bSort.addActionListener(this);

      layoutCons.gridx = 3;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bSort, layoutCons);
      contentPane.add(bSort);

      /*JButton*/ bReflect = new JButton(tr("Reflect"));
      bReflect.setActionCommand("routePattern.stoplistReflect");
      bReflect.addActionListener(this);

      layoutCons.gridx = 3;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bReflect, layoutCons);
      contentPane.add(bReflect);

      //Meta Tab
      contentPane = tabMeta;
      gridbag = new GridBagLayout();
      layoutCons = new GridBagConstraints();
      contentPane.setLayout(gridbag);

      JLabel rightleft = new JLabel(tr("Stops are possible on the"));

      layoutCons.gridx = 0;
      layoutCons.gridy = 1;
      layoutCons.gridwidth = 2;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(rightleft, layoutCons);
      contentPane.add(rightleft);

      cbRight = new JCheckBox(tr("right hand side"), true);

      layoutCons.gridx = 0;
      layoutCons.gridy = 2;
      layoutCons.gridwidth = 2;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(cbRight, layoutCons);
      contentPane.add(cbRight);

      cbLeft = new JCheckBox(tr("left hand side"), false);

      layoutCons.gridx = 0;
      layoutCons.gridy = 3;
      layoutCons.gridwidth = 2;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(cbLeft, layoutCons);
      contentPane.add(cbLeft);

      JLabel maxdist = new JLabel(tr("Maximum distance from route"));

      layoutCons.gridx = 0;
      layoutCons.gridy = 4;
      layoutCons.gridwidth = 2;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(maxdist, layoutCons);
      contentPane.add(maxdist);

      tfSuggestStopsLimit = new JTextField("20", 4);

      layoutCons.gridx = 0;
      layoutCons.gridy = 5;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(tfSuggestStopsLimit, layoutCons);
      contentPane.add(tfSuggestStopsLimit);

      JLabel meters = new JLabel(tr("meters"));

      layoutCons.gridx = 1;
      layoutCons.gridy = 5;
      layoutCons.gridwidth = 1;
      layoutCons.weightx = 0.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(meters, layoutCons);
      contentPane.add(meters);

      JButton bSuggestStops = new JButton(tr("Suggest Stops"));
      bSuggestStops.setActionCommand("routePattern.metaSuggestStops");
      bSuggestStops.addActionListener(this);

      layoutCons.gridx = 0;
      layoutCons.gridy = 6;
      layoutCons.gridwidth = 3;
      layoutCons.weightx = 1.0;
      layoutCons.weighty = 0.0;
      layoutCons.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(bSuggestStops, layoutCons);
      contentPane.add(bSuggestStops);

      jDialog.pack();
    }

    if ("routePattern.refresh".equals(event.getActionCommand()))
    {
      refreshData();
    }
    else if ("routePattern.overviewNew".equals(event.getActionCommand()))
    {
      currentRoute = new Relation();
      currentRoute.put("type", "route");
      currentRoute.put("route", "bus");
      mainDataSet.addPrimitive(currentRoute);

      refreshData();

      for (int i = 0; i < relsListModel.size(); ++i)
      {
    if (currentRoute == ((RouteReference)relsListModel.elementAt(i)).route)
      relsList.setSelectedIndex(i);
      }
    }
    else if ("routePattern.overviewDuplicate".equals(event.getActionCommand()))
    {
      currentRoute = new Relation(currentRoute, true);
      currentRoute.put("type", "route");
      currentRoute.put("route", "bus");
      mainDataSet.addPrimitive(currentRoute);

      refreshData();

      for (int i = 0; i < relsListModel.size(); ++i)
      {
    if (currentRoute == ((RouteReference)relsListModel.elementAt(i)).route)
      relsList.setSelectedIndex(i);
      }
    }
    else if ("routePattern.overviewReflect".equals(event.getActionCommand()))
    {
      currentRoute.setModified(true);
      String tag_from = currentRoute.get("from");
      String tag_to = currentRoute.get("to");
      currentRoute.put("from", tag_to);
      currentRoute.put("to", tag_from);

      Vector< RelationMember > itemsToReflect = new Vector< RelationMember >();
      Vector< RelationMember > otherItems = new Vector< RelationMember >();
      int insPos = itineraryTable.getSelectedRow();

      for (int i = 0; i < currentRoute.getMembersCount(); ++i)
      {
        RelationMember item = currentRoute.getMember(i);

        if (item.isWay())
        {
          String role = item.getRole();
          if ("backward".equals(role))
            role = "forward";
          else if ("forward".equals(role))
            role = "backward";
          else
            role = "backward";

          itemsToReflect.add(new RelationMember(role, item.getWay()));
        }
        else if (item.isNode())
          itemsToReflect.add(item);
        else
          otherItems.add(item);
      }

      currentRoute.setMembers(null);
      for (int i = itemsToReflect.size()-1; i >= 0; --i)
        currentRoute.addMember(itemsToReflect.elementAt(i));
      for (int i = 0; i < otherItems.size(); ++i)
        currentRoute.addMember(otherItems.elementAt(i));

      refreshData();

      for (int i = 0; i < relsListModel.size(); ++i)
      {
        if (currentRoute == ((RouteReference)relsListModel.elementAt(i)).route)
          relsList.setSelectedIndex(i);
      }
    }
    else if ("routePattern.overviewDelete".equals(event.getActionCommand()))
    {
      DeleteAction.deleteRelation(Main.main.getEditLayer(), currentRoute);

      currentRoute = null;
      tabbedPane.setEnabledAt(1, false);
      tabbedPane.setEnabledAt(2, false);
      tabbedPane.setEnabledAt(3, false);
      tabbedPane.setEnabledAt(4, false);

      refreshData();
    }
    else if ("routePattern.tagAddTag".equals(event.getActionCommand()))
    {
      Vector< String > rowContent = new Vector< String >();
      rowContent.add("");
      rowContent.add("");
      otherTagsData.addRow(rowContent);
    }
    else if ("routePattern.itineraryFind".equals(event.getActionCommand()))
    {
      if (mainDataSet == null)
        return;

      itineraryTable.clearSelection();

      for (int i = 0; i < itineraryData.getRowCount(); ++i)
      {
        if ((itineraryData.ways.elementAt(i) != null) &&
            (mainDataSet.isSelected(itineraryData.ways.elementAt(i))))
            itineraryTable.addRowSelectionInterval(i, i);
      }
    }
    else if ("routePattern.itineraryShow".equals(event.getActionCommand()))
    {
      BoundingXYVisitor box = new BoundingXYVisitor();
      if (itineraryTable.getSelectedRowCount() > 0)
      {
        for (int i = 0; i < itineraryData.getRowCount(); ++i)
        {
          if ((itineraryTable.isRowSelected(i)) && (itineraryData.ways.elementAt(i) != null))
          {
            itineraryData.ways.elementAt(i).visit(box);
          }
        }
      }
      else
      {
        for (int i = 0; i < itineraryData.getRowCount(); ++i)
        {
          if (itineraryData.ways.elementAt(i) != null)
          {
            itineraryData.ways.elementAt(i).visit(box);
          }
        }
      }
      if (box.getBounds() == null)
        return;
      box.enlargeBoundingBox();
      Main.map.mapView.recalculateCenterScale(box);
    }
    else if ("routePattern.itineraryMark".equals(event.getActionCommand()))
    {
      OsmPrimitive[] osmp = { null };
      Main.main.getCurrentDataSet().setSelected(osmp);
      markedWays.clear();
      if (itineraryTable.getSelectedRowCount() > 0)
      {
        for (int i = 0; i < itineraryData.getRowCount(); ++i)
        {
          if ((itineraryTable.isRowSelected(i)) && (itineraryData.ways.elementAt(i) != null))
          {
            mainDataSet.addSelected(itineraryData.ways.elementAt(i));

            RelationMember markedWay = new RelationMember
            ((String)(itineraryData.getValueAt(i, 1)), itineraryData.ways.elementAt(i));
            markedWays.addElement(markedWay);
          }
        }
      }
      else
      {
        for (int i = 0; i < itineraryData.getRowCount(); ++i)
        {
          if (itineraryData.ways.elementAt(i) != null)
          {
            mainDataSet.addSelected(itineraryData.ways.elementAt(i));

            RelationMember markedWay = new RelationMember
            ((String)(itineraryData.getValueAt(i, 1)), itineraryData.ways.elementAt(i));
            markedWays.addElement(markedWay);
          }
        }
      }
    }
    else if ("routePattern.itineraryAdd".equals(event.getActionCommand()))
    {
      int insPos = itineraryTable.getSelectedRow();
      Iterator<RelationMember> relIter = markedWays.iterator();
      TreeSet<Way> addedWays = new TreeSet<Way>();
      if (mainDataSet == null)
        return;

      while (relIter.hasNext())
      {
        RelationMember curMember = relIter.next();
        if ((curMember.isWay()) && (mainDataSet.isSelected(curMember.getWay())))
        {
          itineraryData.insertRow(insPos, curMember.getWay(), curMember.getRole());
          if (insPos >= 0)
            ++insPos;

          addedWays.add(curMember.getWay());
        }
      }

      Collection<Way> selectedWays = mainDataSet.getSelectedWays();
      Iterator<Way> wayIter = selectedWays.iterator();

      while (wayIter.hasNext())
      {
        Way curMember = wayIter.next();
        if (!(addedWays.contains(curMember)))
        {
          itineraryData.insertRow(insPos, curMember, "");
          if (insPos >= 0)
            ++insPos;
        }
      }

      if ((insPos > 0) && (insPos < itineraryData.getRowCount()))
      {
        while ((insPos < itineraryData.getRowCount())
                  && (itineraryData.ways.elementAt(insPos) == null))
          ++insPos;
        itineraryTable.removeRowSelectionInterval(0, itineraryData.getRowCount()-1);
        if (insPos < itineraryData.getRowCount())
          itineraryTable.addRowSelectionInterval(insPos, insPos);
      }

      itineraryData.cleanupGaps();
      segmentMetrics = fillSegmentMetrics();
      rebuildWays();
    }
    else if ("routePattern.itineraryDelete".equals(event.getActionCommand()))
    {
      for (int i = itineraryData.getRowCount()-1; i >=0; --i)
      {
        if ((itineraryTable.isRowSelected(i)) && (itineraryData.ways.elementAt(i) != null))
        {
          itineraryData.ways.removeElementAt(i);
          itineraryData.removeRow(i);
        }
      }

      itineraryData.cleanupGaps();
      segmentMetrics = fillSegmentMetrics();
      rebuildWays();
    }
    else if ("routePattern.itinerarySort".equals(event.getActionCommand()))
    {
      TreeSet<Way> usedWays = new TreeSet<Way>();
      TreeMap<Node, LinkedList<RelationMember> > frontNodes =
      new TreeMap<Node, LinkedList<RelationMember> >();
      TreeMap<Node, LinkedList<RelationMember> > backNodes =
      new TreeMap<Node, LinkedList<RelationMember> >();
      Vector< LinkedList<RelationMember> > loops =
      new Vector< LinkedList<RelationMember> >();
      int insPos = itineraryTable.getSelectedRow();

      if (itineraryTable.getSelectedRowCount() > 0)
      {
        for (int i = itineraryData.getRowCount()-1; i >=0; --i)
        {
          if ((itineraryTable.isRowSelected(i)) && (itineraryData.ways.elementAt(i) != null))
          {
            if (!(usedWays.contains(itineraryData.ways.elementAt(i))))
            {
              addWayToSortingData
              (itineraryData.ways.elementAt(i), frontNodes, backNodes, loops);
              usedWays.add(itineraryData.ways.elementAt(i));
            }

            itineraryData.ways.removeElementAt(i);
            itineraryData.removeRow(i);
          }
        }
      }
      else
      {
        for (int i = itineraryData.getRowCount()-1; i >=0; --i)
        {
          if (itineraryData.ways.elementAt(i) != null)
          {
            if (!(usedWays.contains(itineraryData.ways.elementAt(i))))
            {
              addWayToSortingData
              (itineraryData.ways.elementAt(i), frontNodes, backNodes, loops);
              usedWays.add(itineraryData.ways.elementAt(i));
            }
          }
        }

        itineraryData.clear();
      }

      Iterator< Map.Entry< Node, LinkedList<RelationMember> > > entryIter
      = frontNodes.entrySet().iterator();
      while (entryIter.hasNext())
      {
        Iterator<RelationMember> relIter = entryIter.next().getValue().iterator();
        while (relIter.hasNext())
        {
          RelationMember curMember = relIter.next();
          itineraryData.insertRow(insPos, curMember.getWay(), curMember.getRole());
          if (insPos >= 0)
            ++insPos;
        }
      }

      Iterator< LinkedList<RelationMember> > listIter = loops.iterator();
      while (listIter.hasNext())
      {
        Iterator<RelationMember> relIter = listIter.next().iterator();
        while (relIter.hasNext())
        {
          RelationMember curMember = relIter.next();
          itineraryData.insertRow(insPos, curMember.getWay(), curMember.getRole());
          if (insPos >= 0)
            ++insPos;
        }
      }

      itineraryData.cleanupGaps();
      segmentMetrics = fillSegmentMetrics();
      rebuildWays();
    }
    else if ("routePattern.itineraryReflect".equals(event.getActionCommand()))
    {
      Vector<RelationMember> itemsToReflect = new Vector<RelationMember>();
      int insPos = itineraryTable.getSelectedRow();

      if (itineraryTable.getSelectedRowCount() > 0)
      {
        for (int i = itineraryData.getRowCount()-1; i >=0; --i)
        {
          if ((itineraryTable.isRowSelected(i)) && (itineraryData.ways.elementAt(i) != null))
          {
            String role = (String)(itineraryData.getValueAt(i, 1));
            if ("backward".equals(role))
              role = "forward";
            else if ("forward".equals(role))
              role = "backward";
            else
              role = "backward";
            RelationMember markedWay = new RelationMember
            (role, itineraryData.ways.elementAt(i));
            itemsToReflect.addElement(markedWay);

            itineraryData.ways.removeElementAt(i);
            itineraryData.removeRow(i);
          }
        }
      }
      else
      {
        for (int i = itineraryData.getRowCount()-1; i >=0; --i)
        {
          if (itineraryData.ways.elementAt(i) != null)
          {
            String role = (String)(itineraryData.getValueAt(i, 1));
            if ("backward".equals(role))
              role = "forward";
            else if ("forward".equals(role))
              role = "backward";
            else
              role = "backward";
            RelationMember markedWay = new RelationMember
            (role, itineraryData.ways.elementAt(i));
            itemsToReflect.addElement(markedWay);
          }
        }

        itineraryData.clear();
      }

      int startPos = insPos;
      Iterator<RelationMember> relIter = itemsToReflect.iterator();
      while (relIter.hasNext())
      {
        RelationMember curMember = relIter.next();
        if (curMember.isWay())
        {
          itineraryData.insertRow(insPos, curMember.getWay(), curMember.getRole());
          if (insPos >= 0)
            ++insPos;
        }
      }
      if (insPos >= 0)
        itineraryTable.addRowSelectionInterval(startPos, insPos-1);

      itineraryData.cleanupGaps();
      segmentMetrics = fillSegmentMetrics();
      rebuildWays();
    }
    else if ("routePattern.stoplistFind".equals(event.getActionCommand()))
    {
      if (mainDataSet == null)
        return;

      stoplistTable.clearSelection();

      for (int i = 0; i < stoplistData.getRowCount(); ++i)
      {
        if ((stoplistData.nodes.elementAt(i) != null) &&
              (mainDataSet.isSelected(stoplistData.nodes.elementAt(i))))
          stoplistTable.addRowSelectionInterval(i, i);
      }
    }
    else if ("routePattern.stoplistShow".equals(event.getActionCommand()))
    {
      BoundingXYVisitor box = new BoundingXYVisitor();
      if (stoplistTable.getSelectedRowCount() > 0)
      {
        for (int i = 0; i < stoplistData.getRowCount(); ++i)
        {
          if (stoplistTable.isRowSelected(i))
          {
            stoplistData.nodes.elementAt(i).visit(box);
          }
        }
      }
      else
      {
        for (int i = 0; i < stoplistData.getRowCount(); ++i)
        {
          stoplistData.nodes.elementAt(i).visit(box);
        }
      }
      if (box.getBounds() == null)
        return;
      box.enlargeBoundingBox();
      Main.map.mapView.recalculateCenterScale(box);
    }
    else if ("routePattern.stoplistMark".equals(event.getActionCommand()))
    {
      OsmPrimitive[] osmp = { null };
      Main.main.getCurrentDataSet().setSelected(osmp);
      markedNodes.clear();
      if (stoplistTable.getSelectedRowCount() > 0)
      {
        for (int i = 0; i < stoplistData.getRowCount(); ++i)
        {
          if (stoplistTable.isRowSelected(i))
          {
            mainDataSet.addSelected(stoplistData.nodes.elementAt(i));

            RelationMember markedNode = new RelationMember
            ((String)(stoplistData.getValueAt(i, 1)), stoplistData.nodes.elementAt(i));
            markedNodes.addElement(markedNode);
          }
        }
      }
      else
      {
        for (int i = 0; i < stoplistData.getRowCount(); ++i)
        {
          mainDataSet.addSelected(stoplistData.nodes.elementAt(i));

          RelationMember markedNode = new RelationMember
              ((String)(stoplistData.getValueAt(i, 1)), stoplistData.nodes.elementAt(i));
          markedNodes.addElement(markedNode);
        }
      }
    }
    else if ("routePattern.stoplistAdd".equals(event.getActionCommand()))
    {
      int insPos = stoplistTable.getSelectedRow();
      Iterator<RelationMember> relIter = markedNodes.iterator();
      TreeSet<Node> addedNodes = new TreeSet<Node>();
      if (mainDataSet == null)
        return;

      while (relIter.hasNext())
      {
        RelationMember curMember = relIter.next();
        if ((curMember.isNode()) && (mainDataSet.isSelected(curMember.getNode())))
        {
          StopReference sr = detectMinDistance
              (curMember.getNode(), segmentMetrics, cbRight.isSelected(), cbLeft.isSelected());
          double offset = segmentMetrics.elementAt((sr.index+1) / 2).distance;
          if (sr.index % 2 == 0)
            offset += sr.pos;
          stoplistData.insertRow(insPos, curMember.getNode(), curMember.getRole(), offset);
          if (insPos >= 0)
            ++insPos;

          addedNodes.add(curMember.getNode());
        }
      }

      Collection<Node> selectedNodes = mainDataSet.getSelectedNodes();
      Iterator<Node> nodeIter = selectedNodes.iterator();

      while (nodeIter.hasNext())
      {
        Node curMember = nodeIter.next();
        if (!(addedNodes.contains(curMember)))
        {
          StopReference sr = detectMinDistance
              (curMember, segmentMetrics, cbRight.isSelected(), cbLeft.isSelected());
          double offset = segmentMetrics.elementAt((sr.index+1) / 2).distance;
          if (sr.index % 2 == 0)
            offset += sr.pos;
          stoplistData.insertRow(insPos, curMember, "", offset);
          if (insPos >= 0)
            ++insPos;
        }
      }

      if ((insPos > 0) && (insPos < stoplistData.getRowCount()))
      {
        while ((insPos < stoplistData.getRowCount())
                  && (stoplistData.nodes.elementAt(insPos) == null))
          ++insPos;
        stoplistTable.removeRowSelectionInterval(0, stoplistData.getRowCount()-1);
        if (insPos < stoplistData.getRowCount())
          stoplistTable.addRowSelectionInterval(insPos, insPos);
      }

      rebuildNodes();
    }
    else if ("routePattern.stoplistDelete".equals(event.getActionCommand()))
    {
      for (int i = stoplistData.getRowCount()-1; i >=0; --i)
      {
        if (stoplistTable.isRowSelected(i))
        {
          stoplistData.nodes.removeElementAt(i);
          stoplistData.removeRow(i);
        }
      }

      rebuildNodes();
    }
    else if ("routePattern.stoplistSort".equals(event.getActionCommand()))
    {
      // Prepare Segments: The segments of all usable ways are arranged in a linear
      // list such that a coor can directly be checked concerning position and offset
      Vector< StopReference > srm = new Vector< StopReference >();
      int insPos = stoplistTable.getSelectedRow();
      if (stoplistTable.getSelectedRowCount() > 0)
      {
        // Determine for each member its position on the itinerary: position means here the
        // point on the itinerary that has minimal distance to the coor
        for (int i = stoplistData.getRowCount()-1; i >= 0; --i)
        {
          if (stoplistTable.isRowSelected(i))
          {
            StopReference sr = detectMinDistance
            (stoplistData.nodes.elementAt(i), segmentMetrics,
            cbRight.isSelected(), cbLeft.isSelected());
            if (sr != null)
            {
              if (sr.distance <
                Double.parseDouble(tfSuggestStopsLimit.getText()) * 9.0 / 1000000.0 )
              {
            sr.role = (String)stoplistData.getValueAt(i, STOPLIST_ROLE_COLUMN);
            srm.addElement(sr);
              }
              else
              {
            sr.role = (String)stoplistData.getValueAt(i, STOPLIST_ROLE_COLUMN);
            sr.index = segmentMetrics.size()*2;
            sr.pos = 0;
            srm.addElement(sr);
              }

              stoplistData.nodes.removeElementAt(i);
              stoplistData.removeRow(i);
            }

          }
        }
      }
      else
      {
        // Determine for each member its position on the itinerary: position means here the
            // point on the itinerary that has minimal distance to the coor
        for (int i = stoplistData.getRowCount()-1; i >= 0; --i)
        {
          StopReference sr = detectMinDistance
              (stoplistData.nodes.elementAt(i), segmentMetrics,
              cbRight.isSelected(), cbLeft.isSelected());
          if (sr != null)
          {
            if (sr.distance <
                  Double.parseDouble(tfSuggestStopsLimit.getText()) * 9.0 / 1000000.0 )
            {
              sr.role = (String)stoplistData.getValueAt(i, STOPLIST_ROLE_COLUMN);
              srm.addElement(sr);
            }
            else
            {
              sr.role = (String)stoplistData.getValueAt(i, STOPLIST_ROLE_COLUMN);
              sr.index = segmentMetrics.size()*2;
              sr.pos = 0;
              srm.addElement(sr);
            }
          }
        }

        stoplistData.clear();
      }

      Collections.sort(srm);

      for (int i = 0; i < srm.size(); ++i)
      {
        StopReference sr = detectMinDistance
            (srm.elementAt(i).node, segmentMetrics, cbRight.isSelected(), cbLeft.isSelected());
        double offset = segmentMetrics.elementAt((sr.index+1) / 2).distance;
        if (sr.index % 2 == 0)
          offset += sr.pos;
        stoplistData.insertRow(insPos, srm.elementAt(i).node, srm.elementAt(i).role, offset);
        if (insPos >= 0)
          ++insPos;
      }

      rebuildNodes();
    }
    else if ("routePattern.stoplistReflect".equals(event.getActionCommand()))
    {
      Vector< RelationMember > itemsToReflect = new Vector< RelationMember >();
      Vector< Double > distancesToReflect = new Vector< Double >();
      int insPos = stoplistTable.getSelectedRow();

      if (stoplistTable.getSelectedRowCount() > 0)
      {
        for (int i = stoplistData.getRowCount()-1; i >=0; --i)
        {
          if (stoplistTable.isRowSelected(i))
          {
            String role = (String)(stoplistData.getValueAt(i, STOPLIST_ROLE_COLUMN));
            RelationMember markedNode = new RelationMember
                (role, stoplistData.nodes.elementAt(i));
            itemsToReflect.addElement(markedNode);

            stoplistData.nodes.removeElementAt(i);
            stoplistData.removeRow(i);
          }
        }
      }
      else
      {
        for (int i = stoplistData.getRowCount()-1; i >=0; --i)
        {
          String role = (String)(stoplistData.getValueAt(i, STOPLIST_ROLE_COLUMN));
          RelationMember markedNode = new RelationMember
              (role, stoplistData.nodes.elementAt(i));
          itemsToReflect.addElement(markedNode);
        }

        stoplistData.clear();
      }

      int startPos = insPos;
      Iterator<RelationMember> relIter = itemsToReflect.iterator();
      while (relIter.hasNext())
      {
        RelationMember curMember = relIter.next();
        if (curMember.isNode())
        {
          StopReference sr = detectMinDistance
              (curMember.getNode(), segmentMetrics, cbRight.isSelected(), cbLeft.isSelected());
          double offset = segmentMetrics.elementAt((sr.index+1) / 2).distance;
          if (sr.index % 2 == 0)
            offset += sr.pos;
          stoplistData.insertRow(insPos, curMember.getNode(), curMember.getRole(), offset);
          if (insPos >= 0)
            ++insPos;
        }
      }
      if (insPos >= 0)
        stoplistTable.addRowSelectionInterval(startPos, insPos-1);

      rebuildNodes();
    }
    else if ("routePattern.metaSuggestStops".equals(event.getActionCommand()))
    {
      // Prepare Segments: The segments of all usable ways are arranged in a linear
      // list such that a coor can directly be checked concerning position and offset
      Vector< StopReference > srm = new Vector< StopReference >();
      // Determine for each member its position on the itinerary: position means here the
      // point on the itinerary that has minimal distance to the coor
      mainDataSet = Main.main.getCurrentDataSet();
      if (mainDataSet != null)
      {
        String stopKey = "";
        String stopValue = "";
        if ("bus".equals(currentRoute.get("route")))
        {
          stopKey = "highway";
          stopValue = "bus_stop";
        }
        else if ("trolleybus".equals(currentRoute.get("route")))
        {
          stopKey = "highway";
          stopValue = "bus_stop";
        }
        else if ("tram".equals(currentRoute.get("route")))
        {
          stopKey = "railway";
          stopValue = "tram_stop";
        }
        else if ("light_rail".equals(currentRoute.get("route")))
        {
          stopKey = "railway";
          stopValue = "station";
        }
        else if ("subway".equals(currentRoute.get("route")))
        {
          stopKey = "railway";
          stopValue = "station";
        }
        else if ("rail".equals(currentRoute.get("route")))
        {
          stopKey = "railway";
          stopValue = "station";
        }

        Collection< Node > nodeCollection = mainDataSet.getNodes();
        Iterator< Node > nodeIter = nodeCollection.iterator();
        while (nodeIter.hasNext())
        {
          Node currentNode = nodeIter.next();
          if (!currentNode.isUsable())
            continue;
          if (stopValue.equals(currentNode.get(stopKey)))
          {
            StopReference sr = detectMinDistance
            (currentNode, segmentMetrics,
            cbRight.isSelected(), cbLeft.isSelected());
            if ((sr != null) && (sr.distance <
                Double.parseDouble(tfSuggestStopsLimit.getText()) * 9.0 / 1000000.0 ))
              srm.addElement(sr);
          }
        }
      }
      else
      {
        JOptionPane.showMessageDialog(null, tr("There exists no dataset."
            + " Try to download data from the server or open an OSM file."),
        tr("No data found"), JOptionPane.ERROR_MESSAGE);
      }

      Collections.sort(srm);

      stoplistData.clear();
      for (int i = 0; i < srm.size(); ++i)
      {
        StopReference sr = detectMinDistance
            (srm.elementAt(i).node, segmentMetrics, cbRight.isSelected(), cbLeft.isSelected());
        double offset = segmentMetrics.elementAt((sr.index+1) / 2).distance;
        if (sr.index % 2 == 0)
          offset += sr.pos;
        stoplistData.addRow(srm.elementAt(i).node, srm.elementAt(i).role, offset);
      }

      rebuildNodes();
    }
    else
    {
      refreshData();

      jDialog.setLocationRelativeTo(frame);
      jDialog.setVisible(true);
    }
  }

  private void refreshData() {
    Relation copy = currentRoute;
    relsListModel.clear();
    currentRoute = copy;

    DataSet mainDataSet = Main.main.getCurrentDataSet();
    if (mainDataSet != null)
    {
      Vector< RouteReference > relRefs = new Vector< RouteReference >();
      Collection< Relation > relCollection = mainDataSet.getRelations();
      Iterator< Relation > relIter = relCollection.iterator();

      while (relIter.hasNext())
      {
        Relation currentRel = relIter.next();
        if (!currentRel.isDeleted())
        {
          String routeVal = currentRel.get("route");
          if ("bus".equals(routeVal))
            relRefs.add(new RouteReference(currentRel));
          else if ("trolleybus".equals(routeVal))
            relRefs.add(new RouteReference(currentRel));
          else if ("tram".equals(routeVal))
            relRefs.add(new RouteReference(currentRel));
          else if ("light_rail".equals(routeVal))
            relRefs.add(new RouteReference(currentRel));
          else if ("subway".equals(routeVal))
            relRefs.add(new RouteReference(currentRel));
          else if ("rail".equals(routeVal))
            relRefs.add(new RouteReference(currentRel));
        }
      }

      Collections.sort(relRefs);

      Iterator< RouteReference > iter = relRefs.iterator();
      while (iter.hasNext())
        relsListModel.addElement(iter.next());
    }
    else
    {
      JOptionPane.showMessageDialog(null, tr("There exists no dataset."
      + " Try to download data from the server or open an OSM file."),
      tr("No data found"), JOptionPane.ERROR_MESSAGE);
    }
  }

  //Rebuild ways in the relation currentRoute
  public static void rebuildWays() {
    currentRoute.setModified(true);
    List< RelationMember > members = currentRoute.getMembers();
    ListIterator< RelationMember > iter = members.listIterator();
    while (iter.hasNext())
    {
      if (iter.next().isWay())
    iter.remove();
    }
    for (int i = 0; i < itineraryData.getRowCount(); ++i)
    {
      if (itineraryData.ways.elementAt(i) != null)
      {
        RelationMember member = new RelationMember
        ((String)(itineraryData.getValueAt(i, 1)),
         itineraryData.ways.elementAt(i));
        members.add(member);
      }
    }
    currentRoute.setMembers(members);
  }

  //Rebuild nodes in the relation currentRoute
  private void rebuildNodes() {
    currentRoute.setModified(true);
    for (int i = currentRoute.getMembersCount()-1; i >=0; --i)
    {
      if (currentRoute.getMember(i).isNode())
      {
        currentRoute.removeMember(i);
      }
    }
    for (int i = 0; i < stoplistData.getRowCount(); ++i)
    {
      RelationMember member = new RelationMember
          ((String)(stoplistData.getValueAt(i, STOPLIST_ROLE_COLUMN)),
           stoplistData.nodes.elementAt(i));
      currentRoute.addMember(member);
    }
  }

  private void addWayToSortingData
      (Way way, TreeMap<Node, LinkedList<RelationMember> > frontNodes,
       TreeMap<Node, LinkedList<RelationMember> > backNodes,
       Vector< LinkedList<RelationMember> > loops)
  {
    if (way.getNodesCount() < 1)
      return;

    Node firstNode = way.getNode(0);
    Node lastNode = way.getNode(way.getNodesCount() - 1);

    if (frontNodes.get(firstNode) != null)
    {
      LinkedList<RelationMember> list = frontNodes.get(firstNode);
      list.addFirst(new RelationMember("backward", way));
      frontNodes.remove(firstNode);

      Node lastListNode = null;
      if ("backward".equals(list.getLast().getRole()))
        lastListNode = list.getLast().getWay().getNode(0);
      else
        lastListNode = list.getLast().getWay().getNode
        (list.getLast().getWay().getNodesCount() - 1);
      if (lastNode.equals(lastListNode))
      {
        backNodes.remove(lastListNode);
        loops.add(list);
      }
      else if (frontNodes.get(lastNode) != null)
      {
        backNodes.remove(lastListNode);
        LinkedList<RelationMember> listToAppend = frontNodes.get(lastNode);
        Iterator<RelationMember> memberIter = list.iterator();
        while (memberIter.hasNext())
        {
          RelationMember member = memberIter.next();
          if ("backward".equals(member.getRole()))
            listToAppend.addFirst(new RelationMember("forward", member.getWay()));
          else
            listToAppend.addFirst(new RelationMember("backward", member.getWay()));
        }
        frontNodes.remove(lastNode);
        frontNodes.put(lastListNode, listToAppend);
      }
      else if (backNodes.get(lastNode) != null)
      {
        backNodes.remove(lastListNode);
        LinkedList<RelationMember> listToAppend = backNodes.get(lastNode);
        Iterator<RelationMember> memberIter = list.iterator();
        while (memberIter.hasNext())
        {
          RelationMember member = memberIter.next();
          listToAppend.addLast(member);
        }
        backNodes.remove(lastNode);
        backNodes.put(lastListNode, listToAppend);
      }
      else
        frontNodes.put(lastNode, list);
    }
    else if (backNodes.get(firstNode) != null)
    {
      LinkedList<RelationMember> list = backNodes.get(firstNode);
      list.addLast(new RelationMember("forward", way));
      backNodes.remove(firstNode);

      Node firstListNode = null;
      if ("backward".equals(list.getFirst().getRole()))
        firstListNode = list.getFirst().getWay().getNode
        (list.getFirst().getWay().getNodesCount() - 1);
      else
        firstListNode = list.getFirst().getWay().getNode(0);
      if (lastNode.equals(firstListNode))
      {
        frontNodes.remove(firstListNode);
        loops.add(list);
      }
      else if (frontNodes.get(lastNode) != null)
      {
        frontNodes.remove(firstListNode);
        LinkedList<RelationMember> listToAppend = frontNodes.get(lastNode);
        ListIterator<RelationMember> memberIter = list.listIterator(list.size());
        while (memberIter.hasPrevious())
        {
          RelationMember member = memberIter.previous();
          listToAppend.addFirst(member);
        }
        frontNodes.remove(lastNode);
        frontNodes.put(firstListNode, listToAppend);
      }
      else if (backNodes.get(lastNode) != null)
      {
        frontNodes.remove(firstListNode);
        LinkedList<RelationMember> listToAppend = backNodes.get(lastNode);
        ListIterator<RelationMember> memberIter = list.listIterator(list.size());
        while (memberIter.hasPrevious())
        {
          RelationMember member = memberIter.previous();
          if ("backward".equals(member.getRole()))
            listToAppend.addLast(new RelationMember("forward", member.getWay()));
          else
            listToAppend.addLast(new RelationMember("backward", member.getWay()));
        }
        backNodes.remove(lastNode);
        backNodes.put(firstListNode, listToAppend);
      }
      else
        backNodes.put(lastNode, list);
    }
    else if (frontNodes.get(lastNode) != null)
    {
      LinkedList<RelationMember> list = frontNodes.get(lastNode);
      list.addFirst(new RelationMember("forward", way));
      frontNodes.remove(lastNode);
      frontNodes.put(firstNode, list);
    }
    else if (backNodes.get(lastNode) != null)
    {
      LinkedList<RelationMember> list = backNodes.get(lastNode);
      list.addLast(new RelationMember("backward", way));
      backNodes.remove(lastNode);
      backNodes.put(firstNode, list);
    }
    else
    {
      LinkedList<RelationMember> newList = new LinkedList<RelationMember>();
      newList.add(new RelationMember("forward", way));
      frontNodes.put(firstNode, newList);
      backNodes.put(lastNode, newList);
    }
  }

  private void routesSelectionChanged() {
    int selectedPos = relsList.getAnchorSelectionIndex();
    if (relsList.isSelectedIndex(selectedPos))
    {
      currentRoute = ((RouteReference)relsListModel.elementAt(selectedPos)).route;
      tabbedPane.setEnabledAt(1, true);
      tabbedPane.setEnabledAt(2, true);
      tabbedPane.setEnabledAt(3, true);
      tabbedPane.setEnabledAt(4, true);

      //Prepare Tags
      requiredTagsData.readRelation(currentRoute);
      commonTagsData.readRelation(currentRoute);
      otherTagsData.readRelation(currentRoute, tagBlacklist);

      //Prepare Itinerary
      itineraryData.clear();
      List<RelationMember> relMembers = currentRoute.getMembers();
      Iterator<RelationMember> relIter = relMembers.iterator();
      fillItineraryTable(relIter, 0, -1);

      //Prepare Stoplist
      stoplistData.clear();
      /*List<RelationMember>*/ relMembers = currentRoute.getMembers();
      /*Iterator<RelationMember>*/ relIter = relMembers.iterator();
      fillStoplistTable(relIter, -1);
    }
    else
    {
      currentRoute = null;
      tabbedPane.setEnabledAt(1, false);
      tabbedPane.setEnabledAt(2, false);
      tabbedPane.setEnabledAt(3, false);
      tabbedPane.setEnabledAt(4, false);
    }
  }

  private void fillItineraryTable
      (Iterator<RelationMember> relIter, long lastNodeId, int insPos) {
    while (relIter.hasNext())
    {
      RelationMember curMember = relIter.next();
      if (curMember.isWay())
      {
        itineraryData.insertRow(insPos, curMember.getWay(), curMember.getRole());
        if (insPos >= 0)
          ++insPos;
      }
    }
    itineraryData.cleanupGaps();
    segmentMetrics = fillSegmentMetrics();
  }

  private void fillStoplistTable
      (Iterator<RelationMember> relIter, int insPos) {
    while (relIter.hasNext())
    {
      RelationMember curMember = relIter.next();
      if (curMember.isNode())
      {
        StopReference sr = detectMinDistance
            (curMember.getNode(), segmentMetrics, cbRight.isSelected(), cbLeft.isSelected());
        double offset = segmentMetrics.elementAt((sr.index+1) / 2).distance;
        System.out.print(sr.index);
        System.out.print(" ");
        System.out.print(offset);
        System.out.print(" ");
        System.out.println(sr.pos);
        if (sr.index % 2 == 0)
          offset += sr.pos;
        stoplistData.insertRow(insPos, curMember.getNode(), curMember.getRole(), offset);
        if (insPos >= 0)
          ++insPos;
      }
    }
  }

  private Vector< SegmentMetric > fillSegmentMetrics()
  {
    Vector< SegmentMetric > segmentMetrics = new Vector< SegmentMetric >();
    double distance = 0;
    for (int i = 0; i < itineraryData.getRowCount(); ++i)
    {
      if (itineraryData.ways.elementAt(i) != null)
      {
        Way way = itineraryData.ways.elementAt(i);
        if (!(way.isIncomplete()))
        {
          if ("backward".equals((String)(itineraryData.getValueAt(i, 1))))
          {
            for (int j = way.getNodesCount()-2; j >= 0; --j)
            {
              SegmentMetric sm = new SegmentMetric
                  (way.getNode(j+1).getCoor().lat(), way.getNode(j+1).getCoor().lon(),
                  way.getNode(j).getCoor().lat(), way.getNode(j).getCoor().lon(), distance);
              segmentMetrics.add(sm);
              distance += sm.length;
            }
          }
          else
          {
            for (int j = 0; j < way.getNodesCount()-1; ++j)
            {
              SegmentMetric sm = new SegmentMetric
                  (way.getNode(j).getCoor().lat(), way.getNode(j).getCoor().lon(),
                  way.getNode(j+1).getCoor().lat(), way.getNode(j+1).getCoor().lon(), distance);
              segmentMetrics.add(sm);
              distance += sm.length;
            }
          }
        }
      }
      else
        segmentMetrics.add(null);
    }
    return segmentMetrics;
  }

  private StopReference detectMinDistance
      (Node node, Vector< SegmentMetric > segmentMetrics,
       boolean rhsPossible, boolean lhsPossible) {
    if (node == null)
      return null;

    int minIndex = -1;
    double position = -1.0;
    double distance = 180.0;
    double lat = node.getCoor().lat();
    double lon = node.getCoor().lon();

    int curIndex = -2;
    double angleLat = 100.0;
    double angleLon = 200.0;
    Iterator<SegmentMetric> iter = segmentMetrics.iterator();
    while (iter.hasNext())
    {
      curIndex += 2;
      SegmentMetric sm = iter.next();

      if (sm == null)
      {
        angleLat = 100.0;
        angleLon = 200.0;

        continue;
      }

      double curPosition = (lat - sm.aLat)*sm.d1 + (lon - sm.aLon)*sm.d2;

      if (curPosition < 0)
      {
        if (angleLat <= 90.0)
        {
          double lastSegAngle = Math.atan2(angleLat - sm.aLat, angleLon - sm.aLon);
          double segAngle = Math.atan2(sm.d1, -sm.o1);
          double vertexAngle = Math.atan2(lat - sm.aLat, lon - sm.aLon);

          boolean vertexOnSeg = (vertexAngle == segAngle) ||
              (vertexAngle == lastSegAngle);
          boolean vertexOnTheLeft = (!vertexOnSeg) &&
              (((lastSegAngle > vertexAngle) && (vertexAngle > segAngle))
              || ((vertexAngle > segAngle) && (segAngle > lastSegAngle))
              || ((segAngle > lastSegAngle) && (lastSegAngle > vertexAngle)));

          double currentDistance = Math.sqrt((lat - sm.aLat)*(lat - sm.aLat)
            + (lon - sm.aLon)*(lon - sm.aLon)
            *Math.cos(sm.aLat * Math.PI/180.0)*Math.cos(sm.aLat * Math.PI/180.0));
          curPosition = vertexAngle - segAngle;
          if (vertexOnTheLeft)
            curPosition = -curPosition;
          if (curPosition < 0)
            curPosition += 2*Math.PI;
          if ((Math.abs(currentDistance) < distance)
            && (((!vertexOnTheLeft) && (rhsPossible))
            || ((vertexOnTheLeft) && (lhsPossible))
              || (vertexOnSeg)))
          {
            distance = Math.abs(currentDistance);
            minIndex = curIndex-1;
            position = curPosition;
          }
        }
        angleLat = 100.0;
        angleLon = 200.0;
      }
      else if (curPosition > sm.length)
      {
        angleLat = sm.aLat;
        angleLon = sm.aLon;
      }
      else
      {
        double currentDistance = (lat - sm.aLat)*sm.o1 + (lon - sm.aLon)*sm.o2;
        if ((Math.abs(currentDistance) < distance)
                && (((currentDistance >= 0) && (rhsPossible))
                || ((currentDistance <= 0) && (lhsPossible))))
        {
          distance = Math.abs(currentDistance);
          minIndex = curIndex;
          position = curPosition;
        }

        angleLat = 100.0;
        angleLon = 200.0;
      }
    }

    if (minIndex == -1)
      return new StopReference(segmentMetrics.size()*2, 0, 180.0, node.get("name"),
                   "", node);

    return new StopReference(minIndex, position, distance, node.get("name"),
                 "", node);
  }
}
