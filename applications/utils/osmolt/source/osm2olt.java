import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

public class osm2olt extends Thread implements ProcessbarAccess {
	public static boolean useOption = false;

	private static JFileChooser fileChooser = new JFileChooser();

	private static JFrame frame;

	private static String osmFile = "";

	private static JButton btn_OsmFile;

	private static JButton btn_Close;

	private static JButton btn_StartCalc;

	private static JProgressBar progressBar;

	private static JCheckBox cb_includeways;

	private static JCheckBox cb_isSorted;

	private static MapFeatures mapFeatures = new MapFeatures(false);

	private static MapFeaturesGUI mapFeaturesGUI = new MapFeaturesGUI(
			mapFeatures);

	public static final ResourceBundle bundle = ResourceBundle
			.getBundle("osmolt");

	public static void main(String[] args) {

		// LogoImage =
		// Toolkit.getDefaultToolkit().createImage(this.getClass().getResource(Path));
		// System.out.println(new
		// osm2olt().getClass().getClassLoader().getResource("osmolt_en.properties").getPath());

		// try {
		//			
		// RandomAccessFile datei = new RandomAccessFile(new
		// osm2olt().getClass().getClassLoader().getResource("osmolt_en.properties").getPath(),"r");
		// File f = new File(new
		// osm2olt().getClass().getClassLoader().getResource("osmolt_en.properties").getPath());
		// System.out.println(f.getParent());
		// System.out.println(new File(f.getParent()).getParent());
		// // RandomAccessFile neudatei = new RandomAccessFile(new
		// osm2olt().getClass().getClassLoader().getResource("osmolt_en.properties"),
		// "rw");
		// // while (neudatei.length() < datei.length()) {
		// // neudatei.write(datei.read());
		// // }
		// // datei.close();
		// // neudatei.close();
		// } catch (IOException e) {
		// System.err.println(e);
		// }

		if (args.length == 0)
			gui();
		else
			comandline(args);
	}

	private static void comandline(String[] args) {

	}

	public static void gui() {
		try {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
			}

			Options.openOptions();
			if (System.getProperty("os.name").equals("Linux"))
				useOption = true;
			if (useOption)
				Options.openOptions();
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);

			// JButton btn_Mfedit = new JButton("edit Filter");
			JButton btn_Mfedit = new JButton(bundle
					.getString("btn_editFilters"));
			btn_Mfedit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mapFeaturesGUI = new MapFeaturesGUI(mapFeatures);
					mapFeaturesGUI.start();
				}
			});

			// btn_OsmFile = new JButton("set OSM-File");
			btn_OsmFile = new JButton(bundle.getString("btn_setOSMFile"));
			btn_OsmFile.setToolTipText(bundle.getString("tip_setOSMFile"));
			btn_OsmFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					fileChooser.setCurrentDirectory(new File(
							Options.std_OSM_file));
					fileChooser.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".osm")
									|| f.isDirectory();
						}

						public String getDescription() {
							return "OSM Files (*.osm)";
						}
					});
					if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						if (file.exists()) {
							osmFile = file.getAbsolutePath();
							if (useOption)
								Options.std_OSM_file = osmFile;
							if (useOption)
								Options.saveOptions();
						}

						else
							JOptionPane.showMessageDialog(frame, bundle
									.getString("err_correct_osm_file")
									+ " ", "Error", JOptionPane.ERROR_MESSAGE);
					}

				}
			});

			cb_includeways = new JCheckBox(bundle.getString("cb_includeways"));
			cb_includeways.setToolTipText(bundle
					.getString("tool_cb_includeways"));
			cb_includeways.setSelected(false);
			 cb_includeways.setEnabled(false);

			cb_isSorted = new JCheckBox(bundle.getString("cb_isSorted"));
			cb_isSorted.setToolTipText(bundle.getString("tool_cb_isSorted"));
			cb_isSorted.setSelected(true);
			// cb_isSorted.setEnabled(false);

			// btn_StartCalc = new JButton("Start calculation");
			btn_StartCalc = new JButton(bundle
					.getString("btn_startCalculation"));
			btn_StartCalc.setToolTipText(bundle
					.getString("tip_startCalculation"));

			btn_StartCalc.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (osmFile.equals("")) {
						try {
							JOptionPane.showMessageDialog(frame, bundle
									.getString("err_correct_osm_file"),
									"Error", JOptionPane.ERROR_MESSAGE);

						} catch (MissingResourceException e2) {
							JOptionPane.showMessageDialog(frame,
									"can't find a correct local-file.",
									"Error", JOptionPane.ERROR_MESSAGE);
							System.exit(1);
						}
					} else {
						// System.out.println(osmFile);
						startCalc();
					}
				}
			});

			btn_Close = new JButton("Close");
			btn_Close = new JButton(bundle.getString("btn_close"));

			btn_Close.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}

			});

			JPanel Logoanzeige = new Logo("logo.png", "");
			Logoanzeige.setBounds(0, 0, 140, 235);
			Logoanzeige.setSize(140, 235);

			frame = new JFrame(bundle.getString("GuiTitle"));

			frame.setLayout(new MigLayout("fillx"));
			frame.add(btn_Mfedit, "wrap, grow");
			frame.add(btn_OsmFile, "wrap, grow");
			frame.add(cb_includeways, "wrap, grow");
			frame.add(cb_isSorted, "wrap, grow");
			frame.add(progressBar, "span 2, wrap, grow");

			frame.add(btn_StartCalc, "growx,split 2");
			frame.add(btn_Close, "growx,wrap");
			frame.add(Logoanzeige, "grow,west,w 212!,h 205!");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			frame.pack();

			progressBar.setVisible(false);
		} catch (MissingResourceException e) {
			JOptionPane.showMessageDialog(frame,
					"can't find a correct local-file.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	private static void startCalc() {

		processOSM proggi = new processOSM(osmFile, mapFeatures, new osm2olt(),
				cb_includeways.isSelected(), cb_isSorted.isSelected());
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
