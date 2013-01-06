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
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.layer.*;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.event.*;
import org.openstreetmap.josm.Main;

import s57.S57dat;
import seamap.SeaMap;

import panels.PanelMain;

public class Smed2Action extends JosmAction implements EditLayerChangeListener, SelectionChangedListener {

	private static final long serialVersionUID = 1L;
	private static String editor = tr("SeaMap Editor");
	public static JFrame frame = null;
	public static S57dat panelS57;
	private boolean isOpen = false;
	public static PanelMain panelMain = null;
	public MapImage rendering;
	public SeaMap map = null;
	public Collection<OsmPrimitive> data = null;

	private final DataSetListener dataSetListener = new DataSetListener() {

		@Override
		public void dataChanged(DataChangedEvent e) {
			// reMap();
		}

		@Override
		public void nodeMoved(NodeMovedEvent e) {
			// reMap();
		}

		@Override
		public void otherDatasetChange(AbstractDatasetChangedEvent e) {
			// reMap();
		}

		@Override
		public void primitivesAdded(PrimitivesAddedEvent e) {
			// reMap();
		}

		@Override
		public void primitivesRemoved(PrimitivesRemovedEvent e) {
			// reMap();
		}

		@Override
		public void relationMembersChanged(RelationMembersChangedEvent e) {
			// reMap();
		}

		@Override
		public void tagsChanged(TagsChangedEvent e) {
			// reMap();
		}

		@Override
		public void wayNodesChanged(WayNodesChangedEvent e) {
			// reMap();
		}
	};

	public Smed2Action() {
		super(editor, "Smed2", editor, null, true);
		MapView.addEditLayerChangeListener(this);
		DataSet.addSelectionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!isOpen)
					createFrame();
				else
					frame.toFront();
				isOpen = true;
			}
		});
	}

	protected void createFrame() {
		frame = new JFrame(editor);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setResizable(true);
		frame.setAlwaysOnTop(false);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeDialog();
			}
		});
		frame.setSize(new Dimension(480, 480));
		frame.setLocation(100, 200);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		panelMain = new PanelMain();
		frame.add(panelMain);
		panelS57 = new S57dat();
		panelS57.setVisible(false);
		frame.add(panelS57);
		// System.out.println("hello");
		rendering = new MapImage(new ImageryInfo("OpenSeaMap"), map);
		rendering.setBackgroundLayer(true);
		Main.main.addLayer(rendering);
		reMap();
	}

	public void closeDialog() {
		if (isOpen) {
			Main.main.removeLayer(rendering);
			MapView.removeEditLayerChangeListener(this);
			frame.setVisible(false);
			frame.dispose();
//			data = null;
//			map = null;
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
			reMap();
		} else {
			data = null;
			map = null;
		}
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> arg0) {
		// TODO Auto-generated method stub

	}

	void reMap() {
		map = new SeaMap();
		if (data != null) {
			for (OsmPrimitive osm : data) {
				if ((osm instanceof Node) || (osm instanceof Way)) {
					if (osm instanceof Node) {
						map.addNode(((Node) osm).getUniqueId(), ((Node) osm).getCoor().lat(), ((Node) osm).getCoor().lon());
					} else {
						map.addWay(((Way) osm).getUniqueId());
						for (Node node : ((Way) osm).getNodes()) {
							map.addToWay((node.getUniqueId()));
						}
					}
					for (Entry<String, String> entry : osm.getKeys().entrySet()) {
						map.addTag(entry.getKey(), entry.getValue());
					}
					map.tagsDone();
				} else if ((osm instanceof Relation) && ((Relation) osm).isMultipolygon()) {
					map.addMpoly(((Relation) osm).getUniqueId());
					for (RelationMember mem : ((Relation) osm).getMembers()) {
						if (mem.getType() == OsmPrimitiveType.WAY)
							map.addToMpoly(mem.getUniqueId(), (mem.getRole().equals("outer")));
					}
				}
			}
		}
	}
}
