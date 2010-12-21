package harbour.panels;


import harbour.widgets.CraneTable;
import harbour.widgets.TristateCheckBox;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

public class PanelServices extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel raPicLabel = null;
	private TristateCheckBox telCheckBox = null;
	private TristateCheckBox faxCheckBox = null;
	private TristateCheckBox radioCheckBox = null;
	private TristateCheckBox raTelCheckBox = null;
	private JLabel infraLabel = null;
	private TristateCheckBox opnvCheckBox = null;
	private TristateCheckBox railCheckBox = null;
	private JLabel airLabel = null;
	private TristateCheckBox airCheckBox = null;
	private JLabel loadLabel = null;
	private TristateCheckBox kaiCheckBox = null;
	private TristateCheckBox ancCheckBox = null;
	private JLabel moorLabel = null;
	private TristateCheckBox slipCheckBox = null;
	private TristateCheckBox dockCheckBox = null;
	private JLabel DLLabel = null;
	private TristateCheckBox anbCheckBox = null;
	private TristateCheckBox repCheckBox = null;
	private JLabel servLabel = null;
	private TristateCheckBox medCheckBox = null;
	private TristateCheckBox degCheckBox = null;
	private JLabel recLabel = null;
	private TristateCheckBox recmCheckBox = null;
	private TristateCheckBox recwCheckBox = null;
	private JLabel dLabel = null;
	private TristateCheckBox workCheckBox = null;
	private TristateCheckBox elCheckBox = null;
	private TristateCheckBox stCheckBox = null;
	private TristateCheckBox navCheckBox = null;
	private JLabel provLabel = null;
	private TristateCheckBox oilCheckBox = null;
	private TristateCheckBox dieselCheckBox = null;
	private TristateCheckBox waterCheckBox = null;
	private TristateCheckBox provCheckBox = null;
	private TristateCheckBox deckCheckBox = null;
	private TristateCheckBox mCheckBox = null;
	private TristateCheckBox mmCheckBox = null;
	private TristateCheckBox imCheckBox = null;
	private TristateCheckBox bmCheckBox = null;
	private CraneTable craneScrollPane = null;
	/**
	 * This is the default constructor
	 */
	public PanelServices() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		
		provLabel = new JLabel();
		provLabel.setBounds(new Rectangle(1, 225, 45, 20));
		provLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		provLabel.setText("Gueter:");
		dLabel = new JLabel();
		dLabel.setBounds(new Rectangle(1, 205, 45, 20));
		dLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		dLabel.setText(" Dienst:");
		recLabel = new JLabel();
		recLabel.setBounds(new Rectangle(187, 165, 90, 20));
		recLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		recLabel.setText("Entsorgung:");
		servLabel = new JLabel();
		servLabel.setBounds(new Rectangle(1, 185, 45, 20));
		servLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		servLabel.setText("Service:");
		DLLabel = new JLabel();
		DLLabel.setBounds(new Rectangle(3, 165, 147, 20));
		DLLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		DLLabel.setText("weitere Dienstleistungen");
		moorLabel = new JLabel();
		moorLabel.setBounds(new Rectangle(145, 50, 70, 16));
		moorLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		moorLabel.setText("Ankerplatz:");
		loadLabel = new JLabel();
		loadLabel.setBounds(new Rectangle(3, 50, 97, 16));
		loadLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		loadLabel.setText("Laden / Entladen");
		airLabel = new JLabel();
		airLabel.setBounds(new Rectangle(215, 26, 20, 20));
		airLabel.setIcon(new ImageIcon(getClass().getResource("/images/Airplane_20x19.png")));
		infraLabel = new JLabel();
		infraLabel.setBounds(new Rectangle(5, 26, 20, 20));
		infraLabel.setIcon(new ImageIcon(getClass().getResource("/images/Bahn_20x16.png")));
		raPicLabel = new JLabel();
		raPicLabel.setBounds(new Rectangle(5, 2, 20, 20));
		raPicLabel.setIcon(new ImageIcon(getClass().getResource("/images/Telefon_14x20.png")));
		this.setSize(330, 270);
		this.setLayout(null);
		this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.add(raPicLabel, null);
		this.add(getTelCheckBox(), null);
		this.add(getFaxCheckBox(), null);
		this.add(getRadioCheckBox(), null);
		this.add(getRaTelCheckBox(), null);
		this.add(infraLabel, null);
		this.add(getOpnvCheckBox(), null);
		this.add(getRailCheckBox(), null);
		this.add(airLabel, null);
		this.add(getAirCheckBox(), null);
		this.add(loadLabel, null);
		this.add(getKaiCheckBox(), null);
		this.add(getAncCheckBox(), null);
		this.add(moorLabel, null);
		this.add(getSlipCheckBox(), null);
		this.add(getDockCheckBox(), null);
		this.add(DLLabel, null);
		this.add(getAnbCheckBox(), null);
		this.add(getRepCheckBox(), null);
		this.add(servLabel, null);
		this.add(getMedCheckBox(), null);
		this.add(getDegCheckBox(), null);
		this.add(recLabel, null);
		this.add(getRecmCheckBox(), null);
		this.add(getRecwCheckBox(), null);
		this.add(dLabel, null);
		this.add(getWorkCheckBox(), null);
		this.add(getElCheckBox(), null);
		this.add(getStCheckBox(), null);
		this.add(getNavCheckBox(), null);
		this.add(provLabel, null);
		this.add(getOilCheckBox(), null);
		this.add(getDieselCheckBox(), null);
		this.add(getWaterCheckBox(), null);
		this.add(getProvCheckBox(), null);
		this.add(getDeckCheckBox(), null);
		this.add(getMCheckBox(), null);
		this.add(getMmCheckBox(), null);
		this.add(getImCheckBox(), null);
		this.add(getBmCheckBox(), null);
		this.add(getCraneScrollPane(), null);
	}

	/**
	 * This method initializes telCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getTelCheckBox() {
		if (telCheckBox == null) {
			telCheckBox = new TristateCheckBox();
			telCheckBox.setBounds(new Rectangle(30, 2, 70, 20));
			telCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			telCheckBox.setText("Telefon");
		}
		return telCheckBox;
	}

	/**
	 * This method initializes faxCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getFaxCheckBox() {
		if (faxCheckBox == null) {
			faxCheckBox = new TristateCheckBox();
			faxCheckBox.setBounds(new Rectangle(98, 2, 70, 20));
			faxCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			faxCheckBox.setText("Fax");
		}
		return faxCheckBox;
	}

	/**
	 * This method initializes radioCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getRadioCheckBox() {
		if (radioCheckBox == null) {
			radioCheckBox = new TristateCheckBox();
			radioCheckBox.setBounds(new Rectangle(170,2, 70, 20));
			radioCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			radioCheckBox.setText("Funk");
		}
		return radioCheckBox;
	}

	/**
	 * This method initializes raTelCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getRaTelCheckBox() {
		if (raTelCheckBox == null) {
			raTelCheckBox = new TristateCheckBox();
			raTelCheckBox.setBounds(new Rectangle(240, 2, 89, 21));
			raTelCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			raTelCheckBox.setText("Funktelefon");
		}
		return raTelCheckBox;
	}

	/**
	 * This method initializes opnvCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getOpnvCheckBox() {
		if (opnvCheckBox == null) {
			opnvCheckBox = new TristateCheckBox();
			opnvCheckBox.setBounds(new Rectangle(30, 26, 70, 20));
			opnvCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			opnvCheckBox.setText("OEPNV");
		}
		return opnvCheckBox;
	}

	/**
	 * This method initializes railCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getRailCheckBox() {
		if (railCheckBox == null) {
			railCheckBox = new TristateCheckBox();
			railCheckBox.setBounds(new Rectangle(98, 26, 80, 20));
			railCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			railCheckBox.setText("Bahnhof");
		}
		return railCheckBox;
	}

	/**
	 * This method initializes airCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getAirCheckBox() {
		if (airCheckBox == null) {
			airCheckBox = new TristateCheckBox();
			airCheckBox.setBounds(new Rectangle(240, 26, 89, 20));
			airCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			airCheckBox.setText("Flughafen");
		}
		return airCheckBox;
	}

	/**
	 * This method initializes kaiCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getKaiCheckBox() {
		if (kaiCheckBox == null) {
			kaiCheckBox = new TristateCheckBox();
			kaiCheckBox.setBounds(new Rectangle(5, 65, 70, 20));
			kaiCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			kaiCheckBox.setText("Kai");
		}
		return kaiCheckBox;
	}

	/**
	 * This method initializes ancCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getAncCheckBox() {
		if (ancCheckBox == null) {
			ancCheckBox = new TristateCheckBox();
			ancCheckBox.setBounds(new Rectangle(75, 65, 70, 20));
			ancCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			ancCheckBox.setText("Anker");
		}
		return ancCheckBox;
	}

	/**
	 * This method initializes slipCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getSlipCheckBox() {
		if (slipCheckBox == null) {
			slipCheckBox = new TristateCheckBox();
			slipCheckBox.setBounds(new Rectangle(247, 90, 78, 20));
			slipCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			slipCheckBox.setText("Schleppe");
		}
		return slipCheckBox;
	}

	/**
	 * This method initializes dockCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getDockCheckBox() {
		if (dockCheckBox == null) {
			dockCheckBox = new TristateCheckBox();
			dockCheckBox.setBounds(new Rectangle(247, 108, 78, 20));
			dockCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			dockCheckBox.setText("Dock");
		}
		return dockCheckBox;
	}

	/**
	 * This method initializes anbCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getAnbCheckBox() {
		if (anbCheckBox == null) {
			anbCheckBox = new TristateCheckBox();
			anbCheckBox.setBounds(new Rectangle(247, 124, 78, 20));
			anbCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			anbCheckBox.setText("Zug");
		}
		return anbCheckBox;
	}

	/**
	 * This method initializes repCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getRepCheckBox() {
		if (repCheckBox == null) {
			repCheckBox = new TristateCheckBox();
			repCheckBox.setBounds(new Rectangle(247, 140, 82, 20));
			repCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			repCheckBox.setText("Reperatur");
		}
		return repCheckBox;
	}

	/**
	 * This method initializes medCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getMedCheckBox() {
		if (medCheckBox == null) {
			medCheckBox = new TristateCheckBox();
			medCheckBox.setBounds(new Rectangle(42, 185, 66, 20));
			medCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			medCheckBox.setText("Medizin");
		}
		return medCheckBox;
	}

	/**
	 * This method initializes degCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getDegCheckBox() {
		if (degCheckBox == null) {
			degCheckBox = new TristateCheckBox();
			degCheckBox.setBounds(new Rectangle(104, 185, 80, 20));
			degCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			degCheckBox.setText("Degauss");
		}
		return degCheckBox;
	}

	/**
	 * This method initializes recmCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getRecmCheckBox() {
		if (recmCheckBox == null) {
			recmCheckBox = new TristateCheckBox();
			recmCheckBox.setBounds(new Rectangle(180, 185, 55, 20));
			recmCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			recmCheckBox.setText("Muell"); // waste
		}
		return recmCheckBox;
	}

	/**
	 * This method initializes recwCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getRecwCheckBox() {
		if (recwCheckBox == null) {
			recwCheckBox = new TristateCheckBox();
			recwCheckBox.setBounds(new Rectangle(244, 185, 65, 20));
			recwCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			recwCheckBox.setText("Ballast");
		}
		return recwCheckBox;
	}

	/**
	 * This method initializes workCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getWorkCheckBox() {
		if (workCheckBox == null) {
			workCheckBox = new TristateCheckBox();
			workCheckBox.setBounds(new Rectangle(42, 205, 66, 20));
			workCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			workCheckBox.setText("Arbeit");
		}
		return workCheckBox;
	}

	/**
	 * This method initializes elCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getElCheckBox() {
		if (elCheckBox == null) {
			elCheckBox = new TristateCheckBox();
			elCheckBox.setBounds(new Rectangle(104, 205, 80, 20));
			elCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			elCheckBox.setText("Elektrik");
		}
		return elCheckBox;
	}

	/**
	 * This method initializes stCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getStCheckBox() {
		if (stCheckBox == null) {
			stCheckBox = new TristateCheckBox();
			stCheckBox.setBounds(new Rectangle(180, 205, 62, 20));
			stCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			stCheckBox.setText("Dampf");
		}
		return stCheckBox;
	}

	/**
	 * This method initializes navCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getNavCheckBox() {
		if (navCheckBox == null) {
			navCheckBox = new TristateCheckBox();
			navCheckBox.setBounds(new Rectangle(244, 205, 83, 20));
			navCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			navCheckBox.setText("Navigation");
		}
		return navCheckBox;
	}

	/**
	 * This method initializes oilCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getOilCheckBox() {
		if (oilCheckBox == null) {
			oilCheckBox = new TristateCheckBox();
			oilCheckBox.setBounds(new Rectangle(42, 225, 60, 20));
			oilCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			oilCheckBox.setText("Oel");
		}
		return oilCheckBox;
	}

	/**
	 * This method initializes dieselCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getDieselCheckBox() {
		if (dieselCheckBox == null) {
			dieselCheckBox = new TristateCheckBox();
			dieselCheckBox.setBounds(new Rectangle(104, 225, 80, 20));
			dieselCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			dieselCheckBox.setText("Diesel");
		}
		return dieselCheckBox;
	}

	/**
	 * This method initializes waterCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getWaterCheckBox() {
		if (waterCheckBox == null) {
			waterCheckBox = new TristateCheckBox();
			waterCheckBox.setBounds(new Rectangle(180, 225, 68
					, 20));
			waterCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			waterCheckBox.setText("Wasser");
		}
		return waterCheckBox;
	}

	/**
	 * This method initializes provCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getProvCheckBox() {
		if (provCheckBox == null) {
			provCheckBox = new TristateCheckBox();
			provCheckBox.setBounds(new Rectangle(244, 225, 83, 20));
			provCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			provCheckBox.setText("Proviant");
		}
		return provCheckBox;
	}

	/**
	 * This method initializes deckCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getDeckCheckBox() {
		if (deckCheckBox == null) {
			deckCheckBox = new TristateCheckBox();
			deckCheckBox.setBounds(new Rectangle(42, 245, 60, 20));
			deckCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			deckCheckBox.setText("Deck");
		}
		return deckCheckBox;
	}

	/**
	 * This method initializes mCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getMCheckBox() {
		if (mCheckBox == null) {
			mCheckBox = new TristateCheckBox();
			mCheckBox.setBounds(new Rectangle(104, 245, 80, 20));
			mCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			mCheckBox.setText("Maschine");
		}
		return mCheckBox;
	}

	/**
	 * This method initializes mmCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getMmCheckBox() {
		if (mmCheckBox == null) {
			mmCheckBox = new TristateCheckBox();
			mmCheckBox.setBounds(new Rectangle(145, 65, 55, 20));
			mmCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			mmCheckBox.setText("Med.");
		}
		return mmCheckBox;
	}

	/**
	 * This method initializes imCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getImCheckBox() {
		if (imCheckBox == null) {
			imCheckBox = new TristateCheckBox();
			imCheckBox.setBounds(new Rectangle(200, 65, 55, 20));
			imCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			imCheckBox.setText("Ice");
		}
		return imCheckBox;
	}

	/**
	 * This method initializes bmCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private TristateCheckBox getBmCheckBox() {
		if (bmCheckBox == null) {
			bmCheckBox = new TristateCheckBox();
			bmCheckBox.setBounds(new Rectangle(255, 65, 60, 20));
			bmCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			bmCheckBox.setText("Beach");
		}
		return bmCheckBox;
	}

	/**
	 * This method initializes craneScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private CraneTable getCraneScrollPane() {
		if (craneScrollPane == null) {
			craneScrollPane = new CraneTable();
			craneScrollPane.setBounds(new Rectangle(3, 90, 207, 71));
		}
		return craneScrollPane;
	}

}
