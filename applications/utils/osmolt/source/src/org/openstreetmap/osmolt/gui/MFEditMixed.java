package org.openstreetmap.osmolt.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

public class MFEditMixed extends JPanel implements ActionListener, ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3063877741915801946L;

	Element element;

	final String str_value = "Please enter the key of the value \nthat sould be at this position.";

	final String str_text = "Please enter the text that sould be at this position. ";

	final String str_br = "At this position will be a Line Break.";

	private JList listbox;

	private Vector<String> listData;

	private JButton addButton;

	private JButton removeButton;

	private JScrollPane scrollPane;

	private JRadioButton rb_Text;

	private JRadioButton rb_Valueof;

	private JRadioButton rb_br;

	private JLabel lb_Info = new JLabel("");

	private JTextField tf_text = new JTextField();

	int currentListElement = 0;

	String type;

	MFGuiAccess gui;

	public MFEditMixed(String type, MapFeatures mapFeatures, MFGuiAccess gui) {
		this.type = type;
		this.gui = gui;

		JPanel selectionPanel = new JPanel();

		selectionPanel.setLayout(new MigLayout("fill"));

		// Create the data model
		listData = new Vector<String>();

		listbox = new JList(listData);

		listbox.addListSelectionListener(this);

		scrollPane = new JScrollPane();

		scrollPane.getViewport().add(listbox);

		JButton btn_movup = new JButton("move up");
		btn_movup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				movUp();
			}
		});

		JButton btn_movdown = new JButton("move down");
		btn_movdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				movDown();
			}
		});
		addButton = new JButton("Add");
		addButton.addActionListener(this);

		removeButton = new JButton("Delete");
		removeButton.addActionListener(this);

		selectionPanel.add(scrollPane, "grow,wrap,span 4");

		selectionPanel.add(addButton, "growx");

		selectionPanel.add(removeButton, "");

		selectionPanel.add(btn_movup, "");

		selectionPanel.add(btn_movdown, "");

		JPanel editPanel = new JPanel();

		editPanel.setLayout(new MigLayout());

		ButtonGroup g;
		g = new ButtonGroup();
		rb_br = new JRadioButton("line break");
		g.add(rb_br);
		rb_br.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setbr();
			}
		});
		rb_Valueof = new JRadioButton("ValueOf");
		g.add(rb_Valueof);
		rb_Valueof.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setvalue();
			}
		});

		rb_Text = new JRadioButton("Text");
		g.add(rb_Text);
		rb_Text.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settext();
			}
		});

		JButton bt_apply = new JButton("Apply");

		bt_apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});
		lb_Info.setText(str_text);

		editPanel.add(rb_br, "split 3");
		editPanel.add(rb_Valueof, "");
		editPanel.add(rb_Text, "wrap");

		editPanel.add(lb_Info, "wrap,grow");
		editPanel.add(tf_text, "wrap,grow");

		editPanel.add(bt_apply, "wrap,grow");

		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(700, 200));
		add(selectionPanel, BorderLayout.CENTER);
		add(editPanel, BorderLayout.EAST);
		updateGui();
	}

	// Handler for list selection changes
	public void valueChanged(ListSelectionEvent event) {
		// See if this is a listbox selection and the
		// event stream has settled
		if (event.getSource() == listbox && !event.getValueIsAdjusting()) {
			// Get the current selection and place it in the
			// edit field
			currentListElement = listbox.getSelectedIndex();
			// System.out.println(currentListElement);
			filledit();
			// if (stringValue != null)
			// mapFeaturesGUI.setWorkElement(mapFeatures
			// .getElementByName(stringValue));
		}
	}

	// Handler for button presses
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == addButton) {
			String name = JOptionPane.showInputDialog(null, new String("Text"));
			element.addContent(new Text(name));
		}

		if (event.getSource() == removeButton) {
			if (currentListElement >= 0)
				element.removeContent(currentListElement);

		}
		updateGui();
	}

	private void setvalue() {
		tf_text.setEnabled(true);
		lb_Info.setText(str_value);

	}

	private void setbr() {

		tf_text.setEnabled(false);
		lb_Info.setText(str_br);
	}

	private void settext() {

		tf_text.setEnabled(true);
		lb_Info.setText(str_text);
	}

	public void filledit() {
		if ((currentListElement >= 0)&&(element.getContentSize()>0)) {
			//System.out.println(element);
			Content content = element.getContent(currentListElement);
			if (content.getClass().equals(new Element("test").getClass())) {
				Element element = (Element) content;
				if (element.getName().equals("valueof")) {
					rb_Valueof.setSelected(true);
					setvalue();
					tf_text.setText(element.getAttribute("osmKey").getValue());

				} else if (element.getName().equals("br")) {
					rb_br.setSelected(true);
					tf_text.setText("");
					setbr();
				}

			} else if (content.getClass().equals(new Text("").getClass())) {
				settext();
				rb_Text.setSelected(true);
				Text text = (Text) content;
				tf_text.setText(text.getText());
			}
		}
	}

	private void movUp() {
		if (currentListElement > 0) {
			List<Content> contentlist = new ArrayList<Content>();
			List<Content> newcontentlist = new ArrayList<Content>();
			int size = element.getContentSize();
			for (int i = 0; i < size; i++) {

				Content content = element.getContent(0).detach();
				contentlist.add(content);
			}

			for (int i = 0; i < size; i++) {
				if (i == currentListElement - 1)
					newcontentlist.add(contentlist.get(i + 1));
				else if (i == currentListElement)
					newcontentlist.add(contentlist.get(i - 1));
				else
					newcontentlist.add(contentlist.get(i));
			}

			element.addContent(newcontentlist);
			updateGui();
		}
	}

	private void movDown() {
		int size = element.getContentSize();
		if ((currentListElement >= 0) && (currentListElement < size)) {
			List<Content> contentlist = new ArrayList<Content>();
			List<Content> newcontentlist = new ArrayList<Content>();
			for (int i = 0; i < size; i++) {

				Content content = element.getContent(0).detach();
				contentlist.add(content);
			}

			for (int i = 0; i < size; i++) {
				if (i == currentListElement + 1)
					newcontentlist.add(contentlist.get(i - 1));
				else if (i == currentListElement)
					newcontentlist.add(contentlist.get(i + 1));
				else
					newcontentlist.add(contentlist.get(i));
			}

			element.addContent(newcontentlist);
			updateGui();
		}
	}

	public void apply() {
		if (rb_Valueof.isSelected()) {
			Element key = new Element("valueof");
			key.setAttribute("osmKey", tf_text.getText());
			element.setContent(currentListElement, key);
		} else if (rb_br.isSelected()) {
			Element key = new Element("br");
			element.setContent(currentListElement, key);
		} else if (rb_Text.isSelected()) {
			element.setContent(currentListElement, new Text(tf_text.getText()));
		}
		updateGui();
	}

	private void updateCurrentElement() {
		Element WorkFilter = gui.getWorkFilter();
		if (WorkFilter != null)
			element = WorkFilter.getChild(type);

	}

	public void updateGui() {
		updateCurrentElement();
		listData = new Vector<String>();
		// List elements = element.getContent(0);
		if (element != null) {
			for (int i = 0; i < element.getContentSize(); i++) {
				Content content = element.getContent(i);
				String stringValue = "fehler!!";
				if (content.getClass().equals(new Element("test").getClass())) {
					Element element = (Element) content;
					if (element.getName().equals("valueof")) {
						stringValue = "valueof:" + element.getAttributeValue("osmKey");
					}
					if (element.getName().equals("br")) {
						stringValue = "br:Line Break";
					}
				} else if (content.getClass().equals(new Text("test").getClass())) {
          
					Text text = (Text) content;
          if (text.getText().trim()!="")
					stringValue = "text:" + text.getText().trim();
				}
				// if(. )
				// String stringValue = elements.get(i);
				listData.addElement(stringValue);
				// System.out.println( elements.get(i));
			}
			listbox.setListData(listData);
			scrollPane.revalidate();
			scrollPane.repaint();
			currentListElement = 0;
			listbox.setSelectedIndex(currentListElement);
			filledit();
		}

	}

	void emptyForm() {
		listData = new Vector<String>();
		listbox.setListData(listData);
		scrollPane.revalidate();
		scrollPane.repaint();
		currentListElement = 0;
		tf_text.setText("");
	}

	public void updateElement(Element child) {
		// TODO Automatisch erstellter Methoden-Stub

	}
}
