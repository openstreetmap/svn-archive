package oseam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import static org.openstreetmap.josm.tools.I18n.tr;

import panels.PanelMain;

public class OSeaMAction extends JosmAction {

	private static final long serialVersionUID = 1L;
	private static String editor = tr("SeaMap Editor");
	public static JFrame frame = null;
	private boolean isOpen = false;
	public JTextField messageBar = null;

	private OSeaMAction dlg = this;
	public PanelMain panelMain = null;

	public Node node = null;
	private Collection<? extends OsmPrimitive> Selection = null;

	public SelectionChangedListener SmpListener = new SelectionChangedListener() {
		public void selectionChanged(
				Collection<? extends OsmPrimitive> newSelection) {
			Node nextNode = null;
			Selection = newSelection;

			for (OsmPrimitive osm : Selection) {
				if (osm instanceof Node) {
					nextNode = (Node) osm;
					if (Selection.size() == 1) {
						if (nextNode.compareTo(node) != 0) {
							node = nextNode;
							panelMain.mark.parseMark(node);
						}
					} else {
						node = null;
						panelMain.mark.clrMark();
						messageBar.setText(Messages.getString("OneNode"));
					}
				}
			}
			if (nextNode == null) {
				node = null;
				panelMain.mark.clrMark();
				messageBar.setText(Messages.getString("SelectNode"));
			}
		}
	};

	public OSeaMAction() {
		super(editor, "Smed", editor, null, true);
		DataSet.addSelectionListener(SmpListener);
		String str = Main.pref.get("mappaint.style.sources");
		if (!str.contains("dev.openseamap.org")) {
			if (!str.isEmpty())
				str += new String(new char[] { 0x1e });
			Main.pref.put("mappaint.style.sources", str
					+ "http://dev.openseamap.org/josm/seamark_styles.xml");
		}
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
		frame.setSize(new Dimension(400, 400));
		frame.setLocation(100, 200);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		messageBar = new JTextField();
		messageBar.setBounds(10, 355, 380, 20);
		messageBar.setEditable(false);
		messageBar.setBackground(Color.WHITE);
		frame.add(messageBar);
		panelMain = new PanelMain(dlg);
		frame.add(panelMain);
		panelMain.syncPanel();
		// System.out.println("hello");
	}

	public void closeDialog() {
		if (isOpen) {
			frame.setVisible(false);
			frame.dispose();
		}
		isOpen = false;
	}

}
