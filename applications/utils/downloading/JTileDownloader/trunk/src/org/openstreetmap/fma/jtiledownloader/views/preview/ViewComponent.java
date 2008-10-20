package org.openstreetmap.fma.jtiledownloader.views.preview;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JComponent;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class ViewComponent
    extends JComponent
{
    private static final long serialVersionUID = 1L;
    private Image image;

    protected void paintComponent(Graphics g)
    {
        if (image != null)
            g.drawImage(image, 0, 0, this);
    }

    public void setImage(String filePathName)
    {
        image = Toolkit.getDefaultToolkit().getImage(filePathName);
        if (image != null)
        {
            repaint();
        }
    }

    /**
     * @see javax.swing.JComponent#update(java.awt.Graphics)
     * {@inheritDoc}
     */
    public void update(Graphics g)
    {
        paint(g);
    }

}
