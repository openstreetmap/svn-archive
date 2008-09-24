import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

public class EditMixed implements ActionListener, ListSelectionListener {

	Element element;

	final String str_value = "Please enter the key of the value \nthat sould be at this position.";

	final String str_text = "Please enter the text that sould be at this position. ";

	final String str_br = "At this position will be a Line Break.";

	private JList listbox;

	private Vector listData;

	private JButton addButton;

	private JButton removeButton;

	private JScrollPane scrollPane;

	private JRadioButton rb_Text;

	private JRadioButton rb_Valueof;

	private JRadioButton rb_br;

	private JLabel lb_Info = new JLabel("");

	private JTextField tf_text = new JTextField("", 40);

	int currentElement = 0;

	public void start(Element element) {
		this.element = element;
		JFrame frame = new JFrame("Edit Titel/Comment");
		JPanel selectionPanel = new JPanel();

		selectionPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.fill = GridBagConstraints.BOTH;
		
		

		// Create the data model
		listData = new Vector();

		listbox = new JList(listData);

		listbox.addListSelectionListener(this);

		scrollPane = new JScrollPane();

		scrollPane.getViewport().add(listbox);

		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 0;
		selectionPanel.add(scrollPane, constraints);
		
		
		
		JButton btn_movup = new JButton("move up");
		btn_movup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				movUp();
			}
		});

		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 1;
		selectionPanel.add(btn_movup, constraints);
		
		JButton btn_movdown = new JButton("move down");
		btn_movdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				movDown();
			}
		});

		constraints.gridx = 1;
		constraints.gridy = 1;
		selectionPanel.add(btn_movdown, constraints);



		addButton = new JButton("Add");
		addButton.addActionListener(this);

		constraints.gridx = 0;
		constraints.gridy = 2;
		selectionPanel.add(addButton, constraints);

		removeButton = new JButton("Delete");
		removeButton.addActionListener(this);

		constraints.gridx = 1;
		constraints.gridy = 2;
		selectionPanel.add(removeButton, constraints);

		
		
		JPanel editPanel = new JPanel();
		editPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;

		ButtonGroup g;
		g = new ButtonGroup();

		c.gridx = 0;
		c.gridy = 0;
		editPanel.add(rb_br = new JRadioButton("line break"), c);
		g.add(rb_br);

		rb_br.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setbr();
			}
		});

		c.gridx = 1;
		c.gridy = 0;
		editPanel.add(rb_Valueof = new JRadioButton("ValueOf"), c);
		g.add(rb_Valueof);

		rb_Valueof.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setvalue();
			}
		});

		c.gridx = 2;
		c.gridy = 0;
		editPanel.add(rb_Text = new JRadioButton("Text"), c);
		g.add(rb_Text);

		rb_Text.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settext();
			}
		});

		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		editPanel.add(lb_Info, c);
		lb_Info.setText(str_text);

		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 2;
		editPanel.add(tf_text, c);

		JButton bt_apply = new JButton("Apply");

		bt_apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});

		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 3;
		editPanel.add(bt_apply, c);

		frame.setLayout(new BorderLayout());
		frame.setMinimumSize(new Dimension(700, 240));
		frame.add(selectionPanel, BorderLayout.CENTER);
		frame.add(editPanel, BorderLayout.EAST);
		frame.setVisible(true);
		updateList();
	}

	// Handler for list selection changes
	public void valueChanged(ListSelectionEvent event) {
		// See if this is a listbox selection and the
		// event stream has settled
		if (event.getSource() == listbox && !event.getValueIsAdjusting()) {
			// Get the current selection and place it in the
			// edit field
			currentElement = listbox.getSelectedIndex();
			// System.out.println(currentElement);
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
			if (currentElement >= 0)
				element.removeContent(currentElement);

		}
		updateList();
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
		if (currentElement >= 0) {
			Content content = element.getContent(currentElement);
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
		if (currentElement > 0) {
			List<Content> contentlist =new ArrayList<Content>();
			List<Content> newcontentlist =new ArrayList<Content>();
			int size = element.getContentSize();
			for (int i = 0; i < size; i++) {

				Content content=element.getContent(0).detach();
				contentlist.add(content);
			}
			
			for (int i = 0; i < size; i++) {
				if (i == currentElement - 1)
					newcontentlist.add(contentlist.get(i + 1));
				else if (i == currentElement)
					newcontentlist.add(contentlist.get(i - 1));
				else
					newcontentlist.add(contentlist.get(i));
			}
			
			element.addContent(newcontentlist);
			updateList();
		}
	}
	
	private void movDown() {
		int size = element.getContentSize();
		if ((currentElement >= 0)&&(currentElement<size)) {
			List<Content> contentlist =new ArrayList<Content>();
			List<Content> newcontentlist =new ArrayList<Content>();
			for (int i = 0; i < size; i++) {

				Content content=element.getContent(0).detach();
				contentlist.add(content);
			}
			
			for (int i = 0; i < size; i++) {
				if (i == currentElement + 1)
					newcontentlist.add(contentlist.get(i - 1));
				else if (i == currentElement)
					newcontentlist.add(contentlist.get(i + 1));
				else
					newcontentlist.add(contentlist.get(i));
			}
			
			element.addContent(newcontentlist);
			updateList();
		}
	}

	public void apply() {
		if (rb_Valueof.isSelected()) {
			Element key = new Element("valueof");
			key.setAttribute("osmKey", tf_text.getText());
			element.setContent(currentElement, key);
		} else if (rb_br.isSelected()) {
			Element key = new Element("br");
			element.setContent(currentElement, key);
		} else if (rb_Text.isSelected()) {
			element.setContent(currentElement, new Text(tf_text.getText()));
		}
		updateList();
	}

	public void updateList() {

		listData = new Vector();
		// List elements = element.getContent(0);

		for (int i = 0; i < element.getContentSize(); i++) {
			Content content = element.getContent(i);
			String stringValue = "fehler!!";
			if (content.getClass().equals(new Element("test").getClass())) {
				Element element = (Element) content;
				if (element.getName().equals("valueof")) {
					stringValue = "valueof:"
							+ element.getAttributeValue("osmKey");
				}
				if (element.getName().equals("br")) {
					stringValue = "br:Line Break";
				}
			} else if (content.getClass().equals(new Text("test").getClass())) {

				Text text = (Text) content;
				stringValue = "text:" + text.getText();
			}
			// if(. )
			// String stringValue = elements.get(i);
			listData.addElement(stringValue);
			// System.out.println( elements.get(i));
		}
		listbox.setListData(listData);
		scrollPane.revalidate();
		scrollPane.repaint();
		currentElement = 0;
		listbox.setSelectedIndex(currentElement);
		filledit();

	}

}
