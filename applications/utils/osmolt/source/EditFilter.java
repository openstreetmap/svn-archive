import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import net.miginfocom.swing.MigLayout;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

public class EditFilter implements TreeSelectionListener {

	Element element;

	final String str_restriction = "Please enter the key and the value";

	final String str_filter = "how do you want combine the children?";

	private JTree tree;

	private Vector listData;

	private JButton addButton;

	private JButton removeButton;

	private JScrollPane scrollPane;

	private JRadioButton rb_restriction;

	private JRadioButton rb_filter;

	private JRadioButton rb_and;

	private JRadioButton rb_or;

	private JCheckBox cb_negation;

	JDOMTreeModel treeModel;

	JPanel filterpanel;

	JPanel restrictionpanel;

	JPanel editElementPanel;

	private JLabel lb_Info;

	private JTextField tf_key;

	private JTextField tf_value;

	Element currentElement = null;

	JFrame frame;

	public EditFilter() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// Unschalten des Look & Feels für das Applikations-Frame
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		frame = new JFrame("Edit Titel/Comment");

		rb_and = new JRadioButton("AND");
		rb_or = new JRadioButton("OR");
		cb_negation = new JCheckBox("exclude");
		filterpanel = new JPanel();
		restrictionpanel = new JPanel(new GridLayout(2, 2));
		lb_Info = new JLabel("");
		tf_key = new JTextField("", 20);
		tf_value = new JTextField("", 20);
		editElementPanel = new JPanel();
	}

	public void start(Element element) {

		this.element = element;

		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());

		// DefaultMutableTreeNode root = new DefaultMutableTreeNode(element);
		// root.getUserObjectPath();

		treeModel = new JDOMTreeModel(element);

		tree = new JTree(treeModel);
		tree.addTreeSelectionListener(this);

		scrollPane = new JScrollPane(tree);

		selectionPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel selectioncontrollPanel = new JPanel();

		selectionPanel.add(selectioncontrollPanel, BorderLayout.SOUTH);

		selectioncontrollPanel.setLayout(new BorderLayout());

		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add();
			}
		});

		selectioncontrollPanel.add(addButton, BorderLayout.CENTER);

		removeButton = new JButton("Delete");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});

		selectioncontrollPanel.add(removeButton, BorderLayout.EAST);

		ButtonGroup g = new ButtonGroup();

		g.add(rb_filter = new JRadioButton("logical operation"));

		rb_filter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setfilter();
			}
		});
		g.add(rb_restriction = new JRadioButton("Filter-Element"));

		rb_restriction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setrestriction();
			}
		});

		lb_Info.setText(str_restriction);

		restrictionpanel.setLayout(new MigLayout());

		restrictionpanel.add(new JLabel("Key"));
		restrictionpanel.add(tf_key, "wrap");
		restrictionpanel.add(new JLabel("Value"));
		restrictionpanel.add(tf_value, "wrap");

		ButtonGroup bg_filter = new ButtonGroup();
		bg_filter.add(rb_and);
		bg_filter.add(rb_or);

		filterpanel.setLayout(new MigLayout());
		filterpanel.add(rb_and);
		filterpanel.add(rb_or);

		filterpanel.setVisible(false);

		JButton bt_apply = new JButton("Apply");

		bt_apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});

		// editElementPanel.setLayout(new MigLayout(( "debug, inset 20"),
		// "[para]0[][100lp, fill][60lp][95lp, fill]", ""));
		editElementPanel.setLayout(new MigLayout());
		editElementPanel.add(rb_filter, "split 2");
		editElementPanel.add(rb_restriction, "wrap");
		editElementPanel.add(lb_Info, "wrap");
		editElementPanel.add(restrictionpanel, "wrap, hidemode 1");
		editElementPanel.add(filterpanel, "wrap, hidemode 1");
		editElementPanel.add(cb_negation, "wrap");
		editElementPanel.add(bt_apply, "span");

		frame.setLayout(new BorderLayout());
		frame.setMinimumSize(new Dimension(700, 240));
		frame.add(selectionPanel, BorderLayout.CENTER);
		frame.add(editElementPanel, BorderLayout.EAST);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	protected void setrestriction() {
		rb_restriction.setSelected(true);
		restrictionpanel.setVisible(true);
		filterpanel.setVisible(false);
		tf_key.setText("");
		tf_value.setText("");
	}

	protected void setfilter() {
		rb_filter.setSelected(true);
		restrictionpanel.setVisible(false);
		filterpanel.setVisible(true);
	}

	protected void remove() {
		if (currentElement != element) {

			if (JOptionPane.showConfirmDialog(null,
					"do you realy whant to remove this node", "remove",
					JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
				currentElement.detach();
				updatetree();
			}

		} else
			JOptionPane.showMessageDialog(frame, "you cant remove this",
					"Error", JOptionPane.ERROR_MESSAGE);
	}

	protected void add() {
		if (!currentElement.getName().equals("restriction")) {
			Element elm = new Element("restriction");
			String osmKey = "";
			String osmValue = "";
			while ((osmKey == null) || (osmKey.isEmpty()))
				osmKey = JOptionPane.showInputDialog(frame, "Key");
			while ((osmValue == null) || (osmValue.isEmpty()))
				osmValue = JOptionPane.showInputDialog(frame, "Value");
			elm.setAttribute("osmKey", osmKey);
			elm.setAttribute("osmValue", osmValue);
			currentElement.addContent(elm);
			updatetree();
		} else
			JOptionPane.showMessageDialog(frame, "you cant add a filter to a filter.\n" +
					"Change this to a logical operation",
					"Error", JOptionPane.ERROR_MESSAGE);
	}

	private void updatetree() {
		treeModel = new JDOMTreeModel(element);
		tree.setModel(treeModel);

	}

	public void filledit() {
		if (currentElement != null) {
			Element selElement = currentElement;
			if (selElement.getName().equals("filter")) {
				setfilter();
				String logical = currentElement.getAttribute("logical")
						.getValue();
				if (logical.toLowerCase().equals("and"))
					rb_and.setSelected(true);
				else
					rb_or.setSelected(true);

				if (currentElement.getAttribute("negation") != null)
					cb_negation.setSelected(true);
				else
					cb_negation.setSelected(false);
			} else if (selElement.getName().equals("restriction")) {

				setrestriction();
				tf_key
						.setText(currentElement.getAttribute("osmKey")
								.getValue());
				tf_value.setText(currentElement.getAttribute("osmValue")
						.getValue());
			}
			if (currentElement.getAttribute("negation") != null)
				cb_negation.setSelected(true);
			else
				cb_negation.setSelected(false);

		}
	}

	public void apply() {
		if (rb_filter.isSelected()) {
			currentElement.setName("filter");
			if (rb_and.isSelected())
				currentElement.setAttribute("logical", "and");
			else if (rb_or.isSelected())
				currentElement.setAttribute("logical", "or");
			else
				JOptionPane.showMessageDialog(frame,
						"please select an Operation", "Error",
						JOptionPane.ERROR_MESSAGE);
			if (cb_negation.isSelected())
				currentElement.setAttribute("negation", "true");
			else
				currentElement.removeAttribute("negation");
			updatetree();

		} else if (rb_restriction.isSelected()) {
			if (tf_key.getText().isEmpty())
				JOptionPane.showMessageDialog(frame, "please insert a Key",
						"Error", JOptionPane.ERROR_MESSAGE);
			else if (tf_value.getText().isEmpty())
				JOptionPane.showMessageDialog(frame, "please insert a Value",
						"Error", JOptionPane.ERROR_MESSAGE);
			else {
				currentElement.setName("restriction");
				currentElement.removeContent();
				currentElement.setAttribute("osmKey", tf_key.getText());
				currentElement.setAttribute("osmValue", tf_value.getText());

				if (cb_negation.isSelected())
					currentElement.setAttribute("negation", "true");
				else
					currentElement.removeAttribute("negation");
				updatetree();
			}
		}

	}

	public void valueChanged(TreeSelectionEvent arg0) {
		Object obj = arg0.getPath().getLastPathComponent();
		if ((obj != null)
				&& (obj.getClass().equals(new Element("test").getClass()))) {
			Element selElement = (Element) obj;
			currentElement = selElement;
			filledit();
		}
	}

}
