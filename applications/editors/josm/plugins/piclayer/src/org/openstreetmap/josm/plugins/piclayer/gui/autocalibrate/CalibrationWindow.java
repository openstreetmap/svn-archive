package org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openstreetmap.josm.plugins.piclayer.PicLayerPlugin;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.autocalibrate.AutoCalibratePictureAction;
import org.openstreetmap.josm.tools.I18n;

/**
 * Class providing main window for {@link AutoCalibratePictureAction} in {@link PicLayerPlugin}.
 * @author rebsc
 *
 */
public class CalibrationWindow extends JFrame {

	 private static final long serialVersionUID = 1L;
	 private static final int FILES_ONLY = 0;

	 private JFileChooser fileChooser;
	 private String referenceFileName;
	 private List<Point2D> originPoints;
	 private List<Point2D> referencePoints;
	 private String dist1Value;
	 private String dist2Value;

	 private JPanel dialogPane;
	 private JPanel contentPanel;
	 private JPanel infoBar;
	 private JPanel buttonBar;

	 private JButton addRefPointsButton;
	 private JButton addEdgePointsButton;
	 private JButton helpButton;
	 private JButton openButton;
	 private JButton selectLayerButton;
	 private JButton runButton;
	 private JButton cancelButton;

	 private JLabel infoHeader;
	 private JLabel edgePointHeader;
	 private JLabel edgePointNames;
	 private JLabel edgePointValues;
	 private JLabel distanceHeader;
	 private JLabel distance1;
	 private JLabel distance2;
	 private JTextField distance1Field;
	 private JTextField distance2Field;
	 private JLabel distance1Value;
	 private JLabel distance2Value;
	 private JLabel refFileHeader;
	 private JLabel refFileName;
	 private JLabel refFileNameValue;
	 private JLabel refPointHeader;
	 private JLabel refPointNames;
	 private JLabel refPointValues;

	 private JLabel edgePointsChecked;
	 private JLabel distance1Checked;
	 private JLabel distance2Checked;
	 private JLabel fileChecked;
	 private JLabel refPointsChecked;


	 private String separator;
	 private String ws = " ";
	 private Locale language;


	 public CalibrationWindow() {
		 setLanguageFormat();
	     fileChooser = new JFileChooser();
	     referenceFileName = null;
		 setFileChooser();

		 originPoints = new ArrayList<>();
		 referencePoints = new ArrayList<>();
		 dist1Value = null;
		 dist2Value = null;

		 initComponents();
		 updateState();
	 }

	 /**
	  * initialize components
	  */
	  private void initComponents() {
		  dialogPane = new JPanel();
	      contentPanel = new JPanel();
	      infoBar = new JPanel();
	      buttonBar = new JPanel();

	      addRefPointsButton = new JButton();
	      addEdgePointsButton = new JButton();
	      helpButton = new JButton();
	      openButton = new JButton();
	      selectLayerButton = new JButton();
	      runButton = new JButton();
	      cancelButton = new JButton();

	      infoHeader = new JLabel();
	      edgePointHeader = new JLabel();
	      edgePointNames = new JLabel();
	      edgePointValues = new JLabel();
	      distanceHeader = new JLabel();
	      distance1 = new JLabel();
	      distance2 = new JLabel();
	      distance1Field = new JTextField();
	      distance2Field = new JTextField();
	      distance1Value = new JLabel();
	      distance2Value = new JLabel();
	      refFileHeader = new JLabel();
	      refFileName = new JLabel();
	      refFileNameValue = new JLabel();
	      refPointHeader = new JLabel();
	      refPointNames = new JLabel();
	      refPointValues = new JLabel();

	      edgePointsChecked = new JLabel();
	      distance1Checked = new JLabel();
	      distance2Checked = new JLabel();
	      fileChecked = new JLabel();
	      refPointsChecked = new JLabel();

	      // this
	      setTitle(tr("AutoCalibration"));
	      java.awt.Container contentPane = getContentPane();
	      contentPane.setLayout(new BorderLayout());
	      this.setMinimumSize(new Dimension(50,100));

	      // dialog pane
	      dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
	      dialogPane.setLayout(new BorderLayout());

	      // info bar
	      setInfoBar();
	      setInfoHeader();
	      dialogPane.add(infoBar, BorderLayout.NORTH);

	      // content panel
	      setContentPanel();
	      setPointHeader();
		  setEdgePointNamesValues();
		  setDistanceHeader();
	      setDistance1();
	      setDistance1Field();
	      setDistance2();
	      setDistance2Field();
	      setRefFileHeader();
    	  setRefFileName();
		  setOpenButton();
		  setSelectLayerButton();
	      setRefPointHeader();
	      setRefPointNamesValues();
	      dialogPane.add(contentPanel, BorderLayout.CENTER);

	      // button bar
	      setButtonBar();
	      setOKButton();
	      setCancelButton();
	      dialogPane.add(buttonBar, BorderLayout.SOUTH);

	      // content Pane
	      contentPane.add(dialogPane, BorderLayout.CENTER);
	      pack();
	      setLocationRelativeTo(getOwner());
	}

	 private void setLanguageFormat(){
		 // TODO get application language instead of system language
		 String c = I18n.getOriginalLocale().getCountry();
		 switch(c) {
		 	case "DE":
		 		language = Locale.GERMAN;
		 		separator = ";";
		 		break;
		 	case "FR":
		 		language = Locale.FRANCE;
		 		separator = ";";
		 		break;
		 	case "IT":
		 		language = Locale.ITALIAN;
		 		separator = ";";
		 		break;
		 	default:
		 		language = Locale.US;
		 		separator = ",";
		 }
	 }


	// COMPONENTS

	private void setInfoBar() {
		infoBar.setBorder(new EmptyBorder(0, 0, 12, 0));
		infoBar.setLayout(new GridBagLayout());
		((GridBagLayout) infoBar.getLayout()).columnWidths = new int[] {0, 85, 80};
		((GridBagLayout) infoBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};
	}

	private void setInfoHeader() {
		 infoHeader.setText(tr("<html>Please enter the required information.</html>"));
	     infoBar.add(infoHeader, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
	            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(5, 5, 0, 0), 0, 0));

	     String space = "     ";
	     helpButton = new JButton(tr(space + "help" + space));
	     infoBar.add(helpButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
	            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(0, 0, 0, 0), 0, 0));
	}

	private void setContentPanel() {
		contentPanel.setLayout(new GridBagLayout());
	    contentPanel.setBackground(new Color(200, 200, 200));
	    ((GridBagLayout) contentPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
	    ((GridBagLayout) contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        ((GridBagLayout) contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
	    ((GridBagLayout) contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
	}

	private void setPointHeader() {
		edgePointHeader.setText(tr("<html><b><u>Local Edge Points</u></b></html>"));
	    contentPanel.add(edgePointHeader, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
	            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(5, 5, 5, 30), 0, 0));
	}

	private void setEdgePointNamesValues() {
		edgePointNames.setText(tr("<html>"
				+ String.format("Point 1 (Lat%sLon):<br>", separator)
				+ String.format("Point 2 (Lat%sLon):<br>", separator)
				+ String.format("Point 3 (Lat%sLon):<br>", separator)
						+ "</html>"));
		contentPanel.add(edgePointNames, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0,
	                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	                new Insets(5, 5, 5, 30), 0, 0));

		if(!this.originPoints.isEmpty()) {
			edgePointValuesEntered();
		}
		else {
		    addEdgePointsButton = new JButton(tr("Add Points..."));
			contentPanel.add(addEdgePointsButton, new GridBagConstraints(3, 1, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
			          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			          new Insets(5, 50, 5, 5), 0, 0));
		}
	}

	private void setDistanceHeader() {
		 distanceHeader.setText(tr("<html><b><u>True Distances</u></b></html>"));
	     contentPanel.add(distanceHeader, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
	              GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	              new Insets(5, 5, 5, 30), 0, 0));
	}

	private void setDistance1Field() {
		distance1Field.setText("Click here...");
	    contentPanel.add(distance1Field, new GridBagConstraints(3, 3, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
	            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(5, 50, 5, 5), 0, 0));
	}

	private void setDistance2() {
	    distance2.setText(tr("Point 2 to Point 3 (meter):"));
	    contentPanel.add(distance2, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(5, 5, 5, 30), 0, 0));
	}

	private void setDistance2Field() {
	    distance2Field.setText("Click here...");
	    contentPanel.add(distance2Field, new GridBagConstraints(3, 4, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(5, 50, 5, 5), 0, 0));
	}

	private void setRefFileHeader() {
		refFileHeader.setText(tr("<html><b><u>Reference File</u></b></html>"));
	    contentPanel.add(refFileHeader, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(5, 5, 5, 30), 0, 0));
	}

	private void setRefFileName() {
		refFileName.setText("<html>"+tr("Reference Name:")
		      + "<br>"
		      + "<br>"
		      + "<br>"
		      + "</html>");

		contentPanel.add(refFileName, new GridBagConstraints(0, 6, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 30), 0, 0));
	}

	private void setSelectLayerButton() {
		String imageName = "layerlist.png";
		Image image = null;
		try {
			image = ImageIO.read(getClass().getResource("/images/" + imageName));
		} catch (Exception ex) {
			System.out.println("Error: Could not load image " + imageName + "," + ex);
	 	}

		selectLayerButton.setToolTipText(tr("Select a layer as reference..."));
	    selectLayerButton.setIcon(new ImageIcon(image));
	    contentPanel.add(selectLayerButton, new GridBagConstraints(3, 6, 2, 1, 1.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(5, 50, 5, 5), 0, 0));
	}

	private void setOpenButton() {
		String imageName = "open.png";
		Image image = null;
		try {
			image = ImageIO.read(getClass().getResource("/images/" + imageName));
		} catch (Exception ex) {
			System.out.println("Error: Could not load image " + imageName + "," + ex);
	 	}

		openButton.setToolTipText(tr("Open a file as reference..."));
	    openButton.setIcon(new ImageIcon(image));
	    contentPanel.add(openButton, new GridBagConstraints(6, 6, GridBagConstraints.REMAINDER, 1, 1.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(5, 5, 5, 5), 0, 0));
	}

	private void setRefPointHeader() {
		refPointHeader.setText("<html><b><u>Reference Points</u></b></html>\"");
	    contentPanel.add(refPointHeader, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		      new Insets(5, 5, 5, 30), 0, 0));
	}

	private void setRefPointNamesValues() {
		 refPointNames.setText(tr("<html>"
				+ String.format("Point 1 (Lat%sLon):<br>", separator)
				+ String.format("Point 2 (Lat%sLon):<br>", separator)
				+ String.format("Point 3 (Lat%sLon):<br>", separator)
						+ "</html>"));
		 contentPanel.add(refPointNames, new GridBagConstraints(0, 8, 3, 1, 0.0, 0.0,
	                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	                new Insets(5, 5, 5, 30), 0, 0));

		 if(!this.referencePoints.isEmpty()) {
			 refPointValuesEntered();
		 }
		 else {
		      addRefPointsButton = new JButton(tr("Add Points..."));
			  contentPanel.add(addRefPointsButton, new GridBagConstraints(3, 8, GridBagConstraints.REMAINDER, 1, 0.0, 0.0,
			            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			            new Insets(5, 50, 5, 5), 0, 0));
		 }
	}

	private void setButtonBar() {
		buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
	    buttonBar.setLayout(new GridBagLayout());
	    ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
	    ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};
	}

	private void setOKButton() {
		runButton.setText(tr("Run"));
	    buttonBar.add(runButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(0, 0, 0, 5), 0, 0));
	}

	private void setCancelButton() {
		cancelButton.setText(tr("Cancel"));
	    buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(0, 0, 0, 0), 0, 0));
	}


	// DYNAMIC FIELD CHANGES

	private void edgePointValuesEntered() {
		Point2D p1 = null;
		Point2D p2 = null;
		Point2D p3 = null;

		if(this.originPoints.size() == 3) {
			p1 = originPoints.get(0);
			p2 = originPoints.get(1);
			p3 = originPoints.get(2);
		}
		else return;

		edgePointValues.setText(tr("<html>"
			+ formatValue(p1.getY()) + ws + separator + ws + formatValue(p1.getX()) + "<br>"
			+ formatValue(p2.getY()) + ws + separator + ws + formatValue(p2.getX()) + "<br>"
			+ formatValue(p3.getY()) + ws + separator + ws + formatValue(p3.getX()) + "<br>"
					+ "</html>"));

		contentPanel.remove(addEdgePointsButton);
		contentPanel.add(edgePointValues, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0,
				  GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			      new Insets(5, 5, 5, 30), 0, 0));

		edgePointsChecked.setIcon(getCheckedIcon());
		contentPanel.add(edgePointsChecked, new GridBagConstraints(6, 1, 3, 1, 0.0, 0.0,
				  GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			      new Insets(5, 5, 5, 5), 0, 0));
	}

	private void distance1Entered() {
		contentPanel.remove(distance1Field);
		distance1Value.setText(dist1Value);
	    contentPanel.add(distance1Value, new GridBagConstraints(3, 3, 2, 1, 0.0, 0.0,
	            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(5, 5, 5, 30), 0, 0));

		distance1Checked.setIcon(getCheckedIcon());
		contentPanel.add(distance1Checked, new GridBagConstraints(6, 3, 3, 1, 0.0, 0.0,
				  GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			      new Insets(5, 5, 5, 5), 0, 0));
	}

	private void distance2Entered() {
		contentPanel.remove(distance2Field);
		distance2Value.setText(dist2Value);
	    contentPanel.add(distance2Value, new GridBagConstraints(3, 4, 2, 1, 0.0, 0.0,
	            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(5, 5, 5, 30), 0, 0));

		distance2Checked.setIcon(getCheckedIcon());
		contentPanel.add(distance2Checked, new GridBagConstraints(6, 4, 3, 1, 0.0, 0.0,
				  GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			      new Insets(5, 5, 5, 5), 0, 0));
	}

	private void refFileEntered() {
		contentPanel.remove(selectLayerButton);
		contentPanel.remove(openButton);
		refFileName.setText("<html>"+tr("Reference Name:")+"</html>");
		refFileNameValue.setText(referenceFileName);
		contentPanel.add(refFileNameValue, new GridBagConstraints(3, 6, 2, 1, 0.0, 0.0,
		           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		           new Insets(5, 5, 5, 30), 0, 0));

		fileChecked.setIcon(getCheckedIcon());
		contentPanel.add(fileChecked, new GridBagConstraints(6, 6, 3, 1, 0.0, 0.0,
				  GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			      new Insets(5, 5, 5, 5), 0, 0));
	}

	private void refPointValuesEntered() {
		Point2D p1 = null;
		Point2D p2 = null;
		Point2D p3 = null;

		if(this.referencePoints.size() == 3) {
			p1 = referencePoints.get(0);
			p2 = referencePoints.get(1);
			p3 = referencePoints.get(2);
		}
		else return;

		refPointValues.setText(tr("<html>"
				+ formatValue(p1.getY()) + ws + separator + ws + formatValue(p1.getX()) + "<br>"
				+ formatValue(p2.getY()) + ws + separator + ws + formatValue(p2.getX()) + "<br>"
				+ formatValue(p3.getY()) + ws + separator + ws + formatValue(p3.getX()) + "<br>"
						+ "</html>"));

		contentPanel.remove(addRefPointsButton);
		contentPanel.add(refPointValues, new GridBagConstraints(3, 8, 3, 1, 0.0, 0.0,
	            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(5, 5, 5, 30), 0, 0));

		refPointsChecked.setIcon(getCheckedIcon());
		contentPanel.add(refPointsChecked, new GridBagConstraints(6, 8, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			    new Insets(5, 5, 5, 5), 0, 0));
	}

	private void setDistance1() {
		 distance1.setText(tr("Point 1 to Point 2 (meter):"));
	     contentPanel.add(distance1, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0,
	            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(5, 5, 5, 30), 0, 0));
	}


	// GETTER / SETTER

	public JButton getOpenButton() {
		return this.openButton;
	}

	public JTextField getDistance1Field() {
		return this.distance1Field;
	}

	public JTextField getDistance2Field() {
		return this.distance2Field;
	}

	public String getDistance1FieldText() {
	    return this.distance1Field.getText();
	}

	public String getDistance2FieldText() {
	    return this.distance2Field.getText();
	}

	public void setOriginPoints(List<Point2D> points) {
		this.originPoints = points;
		edgePointValuesEntered();
		updateState();
	}

	public void setReferencePoints(List<Point2D> points) {
		this.referencePoints = points;
		refPointValuesEntered();
		updateState();
	}

	public void setDistance1Field(String s) {
	    this.distance1Field.setText(s);
	    updateState();
	}

	public void setDistance2Field(String s) {
	    this.distance2Field.setText(s);
	    updateState();
	}

	public void setDistance1Value(String valueAsString) {
		this.dist1Value = valueAsString;
		if(!valueAsString.equals(""))	distance1Entered();
	    updateState();
	}

	public void setDistance2Value(String valueAsString) {
		this.dist2Value = valueAsString;
		if(!valueAsString.equals(""))	distance2Entered();
	    updateState();
	}

	public void setReferenceFileName(String name) {
		  this.referenceFileName = name;
	}

	private void setFileChooser() {
		fileChooser.setFileSelectionMode(FILES_ONLY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".osm, .gpx","osm", "gpx");
		fileChooser.setFileFilter(filter);
	}

	public void setReferenceFileNameValue(String value) {
		this.referenceFileName = value;
		this.refFileNameValue.setText(value);
		refFileEntered();
		updateState();
	}

	public JFileChooser getFileChooser() {
		return this.fileChooser;
	}

	public String getFileName() {
		return this.referenceFileName;
	}

	// LISTENER

	public void setOkButtonListener(ActionListener l) {
	    this.runButton.addActionListener(l);
	}

	public void setCancelButtonListener(ActionListener l) {
	    this.cancelButton.addActionListener(l);
	}

	public void setWindowListener(WindowListener l) {
	    this.addWindowListener(l);
	}

	public void addOpenFileButtonListener(ActionListener l) {
		this.openButton.addActionListener(l);
	}

	public void addSelectLayerButtonListener(ActionListener l) {
		this.selectLayerButton.addActionListener(l);
	}

	public void addCancelButtonListener(ActionListener l) {
		this.cancelButton.addActionListener(l);
	}

	public void addRunButtonListener(ActionListener l) {
		this.runButton.addActionListener(l);
	}

	public void addEdgePointButtonListener(ActionListener l) {
		this.addEdgePointsButton.addActionListener(l);
	}

	public void addReferencePointButtonListener(ActionListener l) {
		this.addRefPointsButton.addActionListener(l);
	}

	public void addFrameWindowListener(WindowAdapter wAdapter) {
		this.addWindowListener(wAdapter);
	}

	public void addDistance1FieldListener(FocusListener l) {
		this.distance1Field.addFocusListener(l);
	}

	public void addDistance2FieldListener(FocusListener l) {
		this.distance2Field.addFocusListener(l);
	}

	public void addHelpButtonListener(ActionListener l) {
		this.helpButton.addActionListener(l);
	}


	// HELPER

	private String formatValue(double value) {
		return String.format(language, "%.3f", value);
	}

	private ImageIcon getCheckedIcon() {
		String imageName = "checked.png";
		Image image = null;
		try {
			image = ImageIO.read(getClass().getResource("/images/" + imageName));
		} catch (Exception ex) {
			System.out.println("Error: Could not load image " + imageName + "," + ex);
	 	}
		return new ImageIcon(image);
	}

	public void updateState() {
		if(originPoints.isEmpty()) {
			// button blink
			distance1Field.setEnabled(false);
			distance2Field.setEnabled(false);
			openButton.setEnabled(false);
			selectLayerButton.setEnabled(false);
			addRefPointsButton.setEnabled(false);
			runButton.setEnabled(false);
		}
		else {
			if(dist1Value == null && dist2Value == null) {
				distance1Field.setEnabled(true);
				distance2Field.setEnabled(true);
			}
			if(dist1Value != null) {
				openButton.setEnabled(true);
				selectLayerButton.setEnabled(true);
			}
			if(referenceFileName != null)	addRefPointsButton.setEnabled(true);
			if(!referencePoints.isEmpty())	runButton.setEnabled(true);
		}
	}

	public void refresh() {
		this.setVisible(true);
	}

}
