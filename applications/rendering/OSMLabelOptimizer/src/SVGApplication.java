import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.openstreetmap.labelling.annealing.AnnealingMaterial;
import org.openstreetmap.labelling.annealing.AnnealingOven;
import org.openstreetmap.labelling.osm.OSMMapFactory;
import org.openstreetmap.preprocessing.osm.OSMPreprocessors;

public class SVGApplication {

    public static void main(String[] args) {
        // Create a new JFrame.
        JFrame f = new JFrame("Batik");
        SVGApplication app = new SVGApplication(f);

        // Add components to the frame.
        f.getContentPane().add(app.createComponents());

        // Display the frame.
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setSize(1024, 768);
        f.setVisible(true);
    }
    
    // The frame.
    protected JFrame frame;

    // The "Load" button, which displays up a file chooser upon clicking.
    protected JButton button = new JButton("Load...");
    protected JButton button1 = new JButton("Preprocess!");
    protected JButton button2 = new JButton("Start!");
    protected JButton button3 = new JButton("Save...");

    // The status label.
    protected JLabel label = new JLabel();

    // The SVG canvas.
    protected JSVGCanvas svgCanvas = new JSVGCanvas();

    public SVGApplication(JFrame f) {
        frame = f;
    }

    public JComponent createComponents() {
        // Create a panel and add the button, status label and the SVG canvas.
        final JPanel panel = new JPanel(new BorderLayout());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(button);
        p.add(button1);
        p.add(button2);
        p.add(button3);
        p.add(label);

        panel.add("North", p);
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        //svgCanvas.setPreferredSize(new Dimension(1200, 1024));
        panel.add("Center", svgCanvas);
        frame.pack();

        // Set the button action.
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFileChooser fc = new JFileChooser(".");
                int choice = fc.showOpenDialog(panel);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    try {
                        svgCanvas.setURI(f.toURI().toURL().toString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new Thread() 
                {
                    public void run()
                    {
                        OSMPreprocessors p = new OSMPreprocessors(svgCanvas.getSVGDocument(), svgCanvas.getUpdateManager());
                        p.preprocessAll();
                        System.out.println("Preprocessing finished!");
                    }
                }.start();
            }
        });

        
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new Thread() 
                {
                    public void run()
                    {
                        AnnealingOven oven = new AnnealingOven();
                        OSMMapFactory fac = new OSMMapFactory(svgCanvas.getSVGDocument(), svgCanvas.getUpdateManager());
                        oven.setAnnealingMaterial(new AnnealingMaterial(fac.getMap(), fac.getEvaluation()));
                        oven.anneal();
                        fac.render();
                    }
                }.start();
                /*
                svgCanvas.getUpdateManager().getUpdateRunnableQueue().
                invokeLater(new Runnable()
                {
                    public void run()
                    {
                        SVGSVGElement svg = svgCanvas.getSVGDocument().getRootElement();
                        SVGTextPathElement elm = (SVGTextPathElement)svg.getElementById("test");
                        NodeList nl = svg.getElementsByTagName("textPath");
                        for (int i = 0; i < nl.getLength(); ++i)
                        {
                            //NodeList nl2 = ((SVGTextElement)nl.item(i)).getElementsByTagName("textPath");
                            //for (int y = 0; y < nl2.getLength(); ++y)
                            //{
                                System.out.println(((SVGTextPathElement)nl.item(i)).getTextContent());   
                            //}
                        }
                        //String tmp = elm.getAttributeNS(null, "startOffset");
                        //System.out.println(tmp);
                        //elm.removeAttributeNS(null, "startOffset");
                        elm.setAttributeNS(null, "startOffset", "10%");
                        //System.out.println(svgCanvas.isDynamic());
                        //System.out.println("hallo");
                        //svgCanvas.setSVGDocument(svgCanvas.getSVGDocument());
                    }
                });
                */
            }
        });
        
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFileChooser fc = new JFileChooser(".");
                int choice = fc.showSaveDialog(panel);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    try {
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        DOMSource        source = new DOMSource(svgCanvas.getSVGDocument());
                        FileOutputStream os     = new FileOutputStream(f);
                        StreamResult     result = new StreamResult(os);
                        transformer.transform( source, result );
                    } 
                    catch (TransformerConfigurationException ex) 
                    {
                        ex.printStackTrace();
                    } 
                    catch (FileNotFoundException ex)
                    {
                        ex.printStackTrace();
                    } 
                    catch (TransformerException ex)
                    {
                        ex.printStackTrace();
                    }
                }                        
            }
        });


        // Set the JSVGCanvas listeners.
        svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
            public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                label.setText("Document Loading...");
            }
            public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                label.setText("Document Loaded.");
            }
        });

        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            public void gvtBuildStarted(GVTTreeBuilderEvent e) {
                label.setText("Build Started...");
            }
            public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
                label.setText("Build Done.");
                //frame.pack();
                //Dimension d = svgCanvas.getPreferredSize();
                //d.setSize(d.getWidth()*5, d.getHeight()*5);
                //svgCanvas.setPreferredSize(d);
                //frame.pack();
            }
        });

        svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
                label.setText("Rendering Started...");
            }
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                label.setText("");
            }
        });

        return panel;
    }
}

