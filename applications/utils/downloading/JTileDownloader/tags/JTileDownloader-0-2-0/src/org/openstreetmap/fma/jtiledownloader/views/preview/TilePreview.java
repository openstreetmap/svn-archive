package org.openstreetmap.fma.jtiledownloader.views.preview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class TilePreview
    extends JFrame
{
    private static final long serialVersionUID = 1L;

    private ViewComponent _component = new ViewComponent();

    public TilePreview()
    {
        super("TilePreview");

        addWindowListener(new TilePreviewWindowListener());

        Dimension size = new Dimension(256, 256);
        _component.setPreferredSize(size);
        getContentPane().add(_component);
        pack();
        setVisible(true);
    }

    /**
     * 
     */
    public void showImage(String filePathName)
    {
        _component.setImage(filePathName);
    }

    /**
     * @see javax.swing.JFrame#update(java.awt.Graphics)
     * {@inheritDoc}
     */
    public void update(Graphics g)
    {
        paint(g);
    }

    class TilePreviewWindowListener
        extends WindowAdapter
    {
        /**
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         * {@inheritDoc}
         */
        public void windowClosing(WindowEvent e)
        {
            System.out.println("WindowEvent windowClosing (TilePreview)");
            Window window = e.getWindow();
            window.dispose();
        }
    }

}
