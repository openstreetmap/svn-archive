import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import net.miginfocom.swing.MigLayout;

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

	private JTextField tf_text = new JTextField();

	int currentElement = 0;

	public void start(Element element) {
		this.element = element;
		JFrame frame = new JFrame("Edit Titel/Comment");
		JPanel selectionPanel = new JPanel();

		selectionPanel.setLayout(new MigLayout("fill"));
		
		

		// Create the data model
		listData = new Vector();

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
		
		selectionPanel.add(scrollPane,"grow,wrap,span 4");		

		selectionPanel.add(addButton, "growx");

		selectionPanel.add(removeButton, "");

		selectionPanel.add(btn_movup, "");
		
		selectionPanel.add(btn_movdown,"");

		
		
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
		
		editPanel.add(lb_Info,"wrap,grow");	
		editPanel.add(tf_text, "wrap,grow");
		
		editPanel.add(bt_apply, "wrap,grow");

		frame.setLayout(new BorderLayout());
		frame.setMinimumSize(new Dimension(700, 200));
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
