import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JPanel;


public class Logo extends JPanel{
	private static final long serialVersionUID = 1L;

	
	  String Path;
	        String Ver;
	        Image LogoImage;
	        boolean enableDrawing=false;
	 
	        public Logo(String Path, String Ver)
	        {
	            this.Path=Path;
	            this.Ver=Ver;
	       
	            // Getting the Data out of the an jar-file or the File-System using the classloader
				LogoImage = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource(Path));
				        // LogoImage= ImageIO.read(new File(Path));
				        enableDrawing=true;
	                       
	            this.repaint();
	        }
	        public void paintComponent(Graphics g)
	        {
	            if(enableDrawing)
	           {
	               super.paintComponent(g);
	               g.drawImage(LogoImage,0,0,this);
	               g.setColor(Color.WHITE);
	               FontMetrics fm = g.getFontMetrics();
	               int x=(getWidth()-fm.stringWidth(Ver))/2;
	               int y=(getHeight()-5-fm.getLeading());
	               g.drawString(Ver,x,y);
	            }
	        }
	
	
	
	
}
