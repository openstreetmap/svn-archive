package org.openstreetmap.osmolt.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.openstreetmap.osmolt.OutputInterface;

public class ImagePreview extends JPanel {
  /**
   * 
   */
  BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);;
  
  OutputInterface gui;
  
  int[] offset = { 0, 0 };
  
  int[] size = { 0, 0 };
  
  private static final long serialVersionUID = 1L;
  
  public ImagePreview(OutputInterface gui) {
    this.gui = gui;
    setSize(40, 40);
    setPreferredSize(new Dimension(40, 40));
    setMinimumSize(new Dimension(40, 40));
  }
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.lightGray);
    g.drawLine(0, 20, 40, 20);
    g.drawLine(20, 0, 20, 40);
    g.drawLine(0, 0, 40, 40);
    g.drawLine(40, 0, 0, 40);
    g.setColor(Color.black);
    g.drawImage(image, 20 + offset[0], 20 + offset[1], size[0], size[1], this);
    
  }
  
  public void updateImage(String path) {
    if ((path != null) && (!path.equals("")))
      try {
        image = ImageIO.read(new File(path));
      } catch (IOException ex) {
        gui.printError("cant read file: " + path);
      }
    paint(this.getGraphics());
    
  }
  
  public void updatePosition(int[] size, int[] offset) {
    this.offset = offset;
    this.size = size;
    paint(this.getGraphics());
    
  }
  
}
