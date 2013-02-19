package smed2;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.layer.*;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.event.*;
import org.openstreetmap.josm.Main;

import s57.S57dat;
import seamap.SeaMap;

import panels.PanelMain;
import panels.ShowFrame;

public class Smed2Action extends JosmAction implements EditLayerChangeListener, SelectionChangedListener {

	private static String editor = tr("SeaMap Editor");
	public static JFrame editFrame = null;
	public static ShowFrame showFrame = null;
	public static S57dat panelS57;
	private boolean isOpen = false;
	public static PanelMain panelMain = null;
	public MapImage rendering;
	public SeaMap map = null;
	public Collection<OsmPrimitive> data = null;

	private final DataSetListener dataSetListener = new DataSetListener() {

		@Override
		public void dataChanged(DataChangedEvent e) {
			System.out.println("DataChangedEvent");
		}

		@Override
		public void nodeMoved(NodeMovedEvent e) {
			if (map != null) {
				makeMap();
			}
		}

		@Override
		public void otherDatasetChange(AbstractDatasetChangedEvent e) {
			System.out.println("AbstractDatasetChangedEvent");
		}

		@Override
		public void primitivesAdded(PrimitivesAddedEvent e) {
			System.out.println("PrimitivesAddedEvent");
		}

		@Override
		public void primitivesRemoved(PrimitivesRemovedEvent e) {
			System.out.println("PrimitivesRemovedEvent");
		}

		@Override
		public void relationMembersChanged(RelationMembersChangedEvent e) {
			System.out.println("RelationMembersChangedEvent");
		}

		@Override
		public void tagsChanged(TagsChangedEvent e) {
			System.out.println("TagsChangedEvent");
		}

		@Override
		public void wayNodesChanged(WayNodesChangedEvent e) {
			System.out.println("WayNodesChangedEvent");
		}
	};

	public Smed2Action() {
		super(editor, "Smed2", editor, null, true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!isOpen)
					createFrame();
				else
					editFrame.toFront();
				isOpen = true;
			}
		});
	}

	protected void createFrame() {
		editFrame = new JFrame(editor);
		editFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeDialog();
			}
		});
		editFrame.setSize(new Dimension(480, 480));
		editFrame.setLocation(100, 200);
		editFrame.setResizable(true);
		editFrame.setAlwaysOnTop(true);
		editFrame.setVisible(true);
		panelMain = new PanelMain();
		editFrame.add(panelMain);
		panelS57 = new S57dat();
		panelS57.setVisible(false);
		editFrame.add(panelS57);

		showFrame = new ShowFrame("Seamark Inspector");
		showFrame.setSize(new Dimension(300, 300));
		Rectangle rect = Main.map.mapView.getBounds();
		showFrame.setLocation(50, (rect.y + rect.height - 200));
		showFrame.setResizable(false);
		showFrame.setAlwaysOnTop(true);
		showFrame.setVisible(false);

		// System.out.println("hello");
		rendering = new MapImage(new ImageryInfo("OpenSeaMap"), this);
		rendering.setBackgroundLayer(true);
		Main.main.addLayer(rendering);
		MapView.addEditLayerChangeListener(this);
		DataSet.addSelectionListener(this);
		editLayerChanged(Main.main.getEditLayer(), Main.main.getEditLayer());
	}

	public void closeDialog() {
		if (isOpen) {
			Main.main.removeLayer(rendering);
			MapView.removeEditLayerChangeListener(this);
			editFrame.setVisible(false);
			editFrame.dispose();
			data = null;
			map = null;
		}
		isOpen = false;
	}

	@Override
	public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
		if (oldLayer != null) {
			oldLayer.data.removeDataSetListener(dataSetListener);
		}
		if (newLayer != null) {
			newLayer.data.addDataSetListener(dataSetListener);
			data = newLayer.data.allPrimitives();
			makeMap();
			if (rendering != null) rendering.zoomChanged();
		} else {
			data = null;
			map = null;
		}
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> selection) {
		Node nextNode = null;
		Node node = null;
		Way nextWay = null;
		Way way = null;

		if (selection.size() == 0) showFrame.setVisible(false);
		for (OsmPrimitive osm : selection) {
			if (osm instanceof Node) {
				nextNode = (Node) osm;
				if (selection.size() == 1) {
					if (nextNode.compareTo(node) != 0) {
						node = nextNode;
						showFrame.setVisible(true);
						showFrame.showFeature(node, map);
					}
				} else {
					showFrame.setVisible(false);
				}
			}
			else if (osm instanceof Way) {
				nextWay = (Way) osm;
				if (selection.size() == 1) {
					if (nextWay.compareTo(way) != 0) {
						way = nextWay;
						showFrame.setVisible(true);
						showFrame.showFeature(way, map);
					}
				} else {
					showFrame.setVisible(false);
				}
			}
		}
		if (nextNode == null) {
			node = null;
		}
	}

	void makeMap() {
		map = new SeaMap();
		if (data != null) {
			for (OsmPrimitive osm : data) {
				if (osm instanceof Node) {
					map.addNode(((Node) osm).getUniqueId(), ((Node) osm).getCoor().lat(), ((Node) osm).getCoor().lon());
				} else if (osm instanceof Way) {
					map.addEdge(((Way) osm).getUniqueId());
					for (Node node : ((Way) osm).getNodes()) {
						map.addToEdge((node.getUniqueId()));
					}
				} else if ((osm instanceof Relation) && ((Relation) osm).isMultipolygon()) {
					map.addArea(((Relation) osm).getUniqueId());
					for (RelationMember mem : ((Relation) osm).getMembers()) {
						if (mem.getType() == OsmPrimitiveType.WAY)
							map.addToArea(mem.getUniqueId(), (mem.getRole().equals("outer")));
					}
				}
				for (Entry<String, String> entry : osm.getKeys().entrySet()) {
					map.addTag(entry.getKey(), entry.getValue());
				}
				map.tagsDone(osm.getUniqueId());
			}
		}
	}
}
