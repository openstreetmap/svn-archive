import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class OsmParserGUI extends Thread implements ProcessbarAccess{

	private static JFrame frame;


	private static JTextField tf_MFfile;
	private static JButton btn_MfFile;

	private static JTextField tf_osmfile;
	private static JButton btn_OsmFile;
	
	private static JTextField tf_outputfile;
	private static JButton btn_outputFile;
	
	private static JButton btn_Close;

	private static JRadioButton rb_SplitFiles;
	private static JLabel lb_SplitFiles; 
	private static JButton btn_StartCalc;
	
	private static JProgressBar progressBar;
	
	

	public static void main(String[] args) {

		
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
	    progressBar.setStringPainted(true);

		progressBar.setVisible(false);
		
		final JFileChooser fileChooser = new JFileChooser();
		frame = new JFrame("Client");

		tf_osmfile = new JTextField("/home/josias/map3.osm",30);
		tf_osmfile.setEditable(false);

		btn_OsmFile = new JButton("open OSM-XML - File");
		btn_OsmFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fileChooser.setFileFilter(new FileFilter() {
		             public boolean accept(File f) {
		                 return f.getName().toLowerCase().endsWith(".osm") || f.isDirectory();
		             }
		             public String getDescription() {
		                 return "OSM Files (*.osm)";
		             }
		         });
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
					if (file.exists())
						tf_osmfile.setText(file.getAbsolutePath());
					else JOptionPane.showMessageDialog(frame,
		            	    "This isn't a correct File.", 
		            "Error", JOptionPane.ERROR_MESSAGE);
		        }

			}			
		});
		

		tf_outputfile = new JTextField("/home/josias/test.txt",30);
		tf_outputfile.setEditable(false);

		btn_outputFile = new JButton("open output File");
		btn_outputFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fileChooser.setFileFilter(new FileFilter() {
		             public boolean accept(File f) {
		                 return f.getName().toLowerCase().endsWith(".txt") || f.isDirectory();
		             }
		             public String getDescription() {
		                 return "output File (*.txt)";
		             }
		         });
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
					if (file.exists())
						{
							if (JOptionPane.showConfirmDialog(null,
	                            "File overwrite?",
	                            "overwrite",
	                            JOptionPane.YES_NO_CANCEL_OPTION)==0);
							tf_outputfile.setText(file.getAbsolutePath()); 
						}

					else 
						tf_outputfile.setText(file.getAbsolutePath()); 
		        }

			}			
		});
		

		tf_MFfile = new JTextField("/home/josias/kinder.xml",30);
		tf_MFfile.setEditable(false);

		btn_MfFile = new JButton("open MapFeature-File");
		btn_MfFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fileChooser.setFileFilter(new FileFilter() {
		             public boolean accept(File f) {
		                 return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
		             }
		             public String getDescription() {
		                 return "XML Files (*.xml)";
		             }
		         });
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
		            if (file.exists())
		            tf_MFfile.setText(file.getAbsolutePath());
		            else JOptionPane.showMessageDialog(frame,
		            	    "This isn't a correct File.", 
		            "Error", JOptionPane.ERROR_MESSAGE);

		        }
			
			}			
		});
		 

		
		
		JPanel jp_SplitFiles =new JPanel();
		rb_SplitFiles = new JRadioButton();
		lb_SplitFiles = new JLabel("make different files for each tag");

		jp_SplitFiles.add(rb_SplitFiles);
		jp_SplitFiles.add(lb_SplitFiles);
		
		btn_StartCalc =new JButton("Start calculation");
		

		btn_StartCalc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tf_MFfile.getText().equals("")||tf_osmfile.getText().equals(""))
				JOptionPane.showMessageDialog(frame,
		            	    "Please choose correct files", 
		            "Error", JOptionPane.ERROR_MESSAGE);
				else {

					startCalc();
				}
			}			
		});
		
		
		JPanel panelImport = new JPanel();
		panelImport.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;	
		panelImport.add(tf_MFfile,c);
		
		
		c.gridx = 1;
		c.gridy = 0;	
		panelImport.add(btn_MfFile,c);
		
		c.gridx = 0;
		c.gridy = 1;	
		panelImport.add(tf_osmfile,c);

		c.gridx = 1;
		c.gridy = 1;	
		panelImport.add(btn_OsmFile,c);
		
		c.gridx = 0;
		c.gridy = 2;	
		panelImport.add(tf_outputfile,c);
		
		
		c.gridx = 1;
		c.gridy = 2;	
		panelImport.add(btn_outputFile,c);


		c.gridx = 0;
		c.gridy = 3;	
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		panelImport.add(jp_SplitFiles,c);
		
		
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth=2;
		c.fill = GridBagConstraints.BOTH;
		panelImport.add(progressBar,c);
		
		
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth=2;
		c.fill = GridBagConstraints.BOTH;
		panelImport.add(btn_StartCalc,c);

		
		
		


		btn_Close = new JButton("Schliessen");
		
		btn_Close.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Verbindung trennen

				System.exit(0);
			}

		});

		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		panelButtons.add(btn_Close);
		if (args.length != 0) {
		} else {
			//btnAbsenden.setEnabled(false);
			//btnTrennen.setEnabled(false);
			//btnVerbinden.setEnabled(false);			aktionen
		}

		
		
		
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panelImport, BorderLayout.NORTH);
		frame.add(panelButtons, BorderLayout.SOUTH);
		frame.pack();
		frame.setSize(800, 650);
		frame.setVisible(true);
	}
	
	private static void startCalc() {
		osmparser proggi = new osmparser(tf_MFfile.getText(),tf_osmfile.getText(),tf_outputfile.getText(),rb_SplitFiles.isSelected(), new OsmParserGUI());
		try {
			proggi.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	}
	
	public void setStatusbar(int value) {
		progressBar.setValue(value);

		
	}

	public void processAdd() {
		progressBar.setValue(progressBar.getValue()+1);
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
	

}
