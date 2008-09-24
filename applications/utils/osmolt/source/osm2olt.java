import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class osm2olt extends Thread implements ProcessbarAccess {

	private static final JFileChooser fileChooser = new JFileChooser();

	private static JFrame frame;

	private static JTextField tf_MFfile;

	private static JButton btn_MfFile;

	private static JTextField tf_osmfile;

	private static JButton btn_OsmFile;

	private static JButton btn_Close;

	private static JButton btn_StartCalc;

	private static JProgressBar progressBar;

	private static MapFeatures mapFeatures = new MapFeatures(false);

	private static MapFeaturesGUI mapFeaturesGUI = new MapFeaturesGUI(
			mapFeatures);

	// private static MapFeaturesGUI mapFeaturesGUI= new
	// MapFeaturesGUI(mapFeatures);

	public static void main(String[] args) {

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);


		frame = new JFrame("Client");

		tf_osmfile = new JTextField("/home/josias/test2.osm", 30);
		tf_osmfile.setEditable(false);

		btn_OsmFile = new JButton("open OSM-XML - File");
		btn_OsmFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fileChooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".osm")
								|| f.isDirectory();
					}

					public String getDescription() {
						return "OSM Files (*.osm)";
					}
				});
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.exists())
						tf_osmfile.setText(file.getAbsolutePath());
					else
						JOptionPane.showMessageDialog(frame,
								"This isn't a correct File.", "Error",
								JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		JButton btn_Mfedit = new JButton("edit");
		btn_Mfedit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mapFeaturesGUI = new MapFeaturesGUI(mapFeatures);
				mapFeaturesGUI.start();
			}
		});

		tf_MFfile = new JTextField(
				"/home/josias/osm/java/osm2olt/mapFeatures.xml", 30);
		tf_MFfile.setEditable(false);

		btn_MfFile = new JButton("open MapFeature-File");
		btn_MfFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fileChooser.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".xml")
								|| f.isDirectory();
					}

					public String getDescription() {
						return "XML Files (*.xml)";
					}
				});
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					if (file.exists()) {

						tf_MFfile.setText(file.getAbsolutePath());
						mapFeatures.openFile(tf_MFfile.getText());
					} else
						JOptionPane.showMessageDialog(frame,
								"This isn't a correct File.", "Error",
								JOptionPane.ERROR_MESSAGE);

				}

			}
		});

		btn_StartCalc = new JButton("Start calculation");

		btn_StartCalc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tf_MFfile.getText().equals("")
						|| tf_osmfile.getText().equals(""))
					JOptionPane.showMessageDialog(frame,
							"Please choose correct files", "Error",
							JOptionPane.ERROR_MESSAGE);
				else {

					startCalc();
				}
			}
		});

		btn_Close = new JButton("Schliessen");

		btn_Close.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});

		frame.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		frame.add(tf_MFfile, c);

		c.gridx = 1;
		c.gridy = 0;
		frame.add(btn_MfFile, c);
		c.gridx = 2;
		c.gridy = 0;
		frame.add(btn_Mfedit, c);

		c.gridx = 0;
		c.gridy = 1;
		frame.add(tf_osmfile, c);

		c.gridx = 1;
		c.gridy = 1;
		frame.add(btn_OsmFile, c);

		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		
		c.gridwidth = 2;
		frame.add(progressBar, c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		frame.add(btn_StartCalc, c);

		c.gridx = 1;
		c.gridy = 3;
		c.fill = GridBagConstraints.BOTH;
		frame.add(btn_Close, c);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		progressBar.setVisible(false);
	}

	private static void startCalc() {
		
		
		processOSM  proggi = new processOSM(tf_osmfile.getText(),mapFeatures,new osm2olt());
		//osmparser proggi = new osmparser(mapFeatures, tf_osmfile.getText(),new osm2olt());
		try {
			proggi.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void setStatusbar(int value) {
		progressBar.setValue(value);

	}

	public void processAdd() {
		progressBar.setValue(progressBar.getValue() + 1);
		frame.repaint();

	}

	public void processStart() {
		progressBar.setVisible(true);
		progressBar.setValue(0);
		frame.repaint();
	}

	public void processStop() {
		progressBar.setVisible(false);
		frame.repaint();
		progressBar.setValue(0);
	}

	public JFileChooser getFileChooser() {
		return fileChooser;
	}

}
