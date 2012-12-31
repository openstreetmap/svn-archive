package smed2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.DataSetListenerAdapter;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.Main;

import s57.S57dat;

import panels.PanelMain;

public class Smed2Action extends JosmAction implements EditLayerChangeListener, SelectionChangedListener {

	private static final long serialVersionUID = 1L;
	private static String editor = tr("SeaMap Editor");
	public static JFrame frame = null;
	public static S57dat panelS57;
	private boolean isOpen = false;
	public static PanelMain panelMain = null;
	public ImageryLayer rendering;
	public Collection<OsmPrimitive> data = null;

	private final DataSetListener dataSetListener = new DataSetListener() {

		@Override
		public void dataChanged(DataChangedEvent e) {
			System.out.println(e);
		}

		@Override
		public void nodeMoved(NodeMovedEvent e) {
			System.out.println(e);
		}

		@Override
		public void otherDatasetChange(AbstractDatasetChangedEvent e) {
			System.out.println(e);
		}

		@Override
		public void primitivesAdded(PrimitivesAddedEvent e) {
			System.out.println(e);
		}

		@Override
		public void primitivesRemoved(PrimitivesRemovedEvent e) {
			System.out.println(e);
		}

		@Override
		public void relationMembersChanged(RelationMembersChangedEvent e) {
			System.out.println(e);
		}

		@Override
		public void tagsChanged(TagsChangedEvent e) {
			System.out.println(e);
		}

		@Override
		public void wayNodesChanged(WayNodesChangedEvent e) {
			System.out.println(e);
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
		System.out.println("hello");
		rendering = ImageryLayer.create(new ImageryInfo("OpenSeaMap"));
		Main.main.addLayer(rendering);
	}

	public void closeDialog() {
		if (isOpen) {
			Main.main.removeLayer(rendering);
			frame.setVisible(false);
			frame.dispose();
		}
		isOpen = false;
	}

	@Override
	public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
		System.out.println(newLayer);
		if (oldLayer != null) {
			oldLayer.data.removeDataSetListener(dataSetListener);
		}

		if (newLayer != null) {
			newLayer.data.addDataSetListener(dataSetListener);
			data = newLayer.data.allPrimitives();
		} else {
			data = null;
		}
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> arg0) {
		// TODO Auto-generated method stub

	}

}
