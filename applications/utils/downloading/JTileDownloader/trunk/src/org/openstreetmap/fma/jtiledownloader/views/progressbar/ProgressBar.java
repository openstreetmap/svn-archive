/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of JTileDownloader.
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.fma.jtiledownloader.views.progressbar;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSimple;
import org.openstreetmap.fma.jtiledownloader.views.errortilelist.ErrorTileListView;

/**
 * Progressbar for Downloader
 */
public class ProgressBar
    extends JDialog
    implements ActionListener, TileDownloaderListener
{
    private static final long serialVersionUID = 1L;
    private JProgressBar progressBar = new JProgressBar(0, 0);
    private JLabel timeElapsed = new JLabel("Elapsed: n/a");
    private JLabel timeRemaining = new JLabel("Remaining: n/a");
    private JLabel updatedTileCounter = new JLabel("Updated Tile Counter: n/a");
    private JCheckBox showPreview = new JCheckBox("Show Preview");
    private Boolean previewVisible = false;
    private JButton abortButton = new JButton("Abort");
    private TilePreviewViewComponent tilePreviewViewComponent = new TilePreviewViewComponent();
    private TileListDownloader downloader = null;
    private Calendar start = Calendar.getInstance();

    private int _updatedTileCounter = 0;

    public ProgressBar(int tilesCount, TileListDownloader downloader)
    {
        super();
        setTitle("Download progress...");
        setModal(true);

        this.downloader = downloader;

        addWindowListener(new ProgressBarWindowListener());
        setResizable(false);

        setLayout(new GridBagLayout());

        progressBar.setMaximum(tilesCount);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 20));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        //constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.CENTER;
        constraints.insets = new Insets(5, 5, 0, 5);
        add(progressBar, constraints);
        add(timeElapsed, constraints);
        add(timeRemaining, constraints);
        add(updatedTileCounter, constraints);
        add(abortButton, constraints);
        abortButton.addActionListener(this);
        constraints.insets = new Insets(5, 5, 5, 5);
        add(showPreview, constraints);
        showPreview.addActionListener(this);

        tilePreviewViewComponent.setPreferredSize(new Dimension(256, 256));
        setShowPreview(AppConfiguration.getInstance().isShowTilePreview());
        downloader.setListener(this);
        downloader.start();
        setVisible(true);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent arg0)
    {
        if (arg0.getSource().equals(showPreview))
        {
            setShowPreview(showPreview.isSelected());
        }
        else
        {
            downloader.abort();
        }
    }

    /**
     * @param selected
     */
    private void setShowPreview(boolean selected)
    {
        previewVisible = selected;
        showPreview.setSelected(selected);
        AppConfiguration.getInstance().setShowTilePreview(selected);
        if (selected == true)
        {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.fill = GridBagConstraints.CENTER;
            constraints.insets = new Insets(0, 5, 5, 5);
            add(tilePreviewViewComponent, constraints);
        }
        else
        {
            remove(tilePreviewViewComponent);
        }
        pack();
        center();
    }

    private class ProgressBarWindowListener
        extends WindowAdapter
    {
        @Override
        public void windowClosing(WindowEvent e)
        {
            // ask and perform abort
            downloader.abort();
        }
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadComplete(int, java.util.Vector)
     * {@inheritDoc}
     */
    public void downloadComplete(int errorCount, Vector<TileDownloadError> errorTileList, int updatedTileCount)
    {
        try
        {
            // just wait a short time if no tiles were updated
            Thread.sleep(1500);
        }
        catch (InterruptedException e)
        {
        }

        if (errorTileList != null && errorTileList.size() > 0)
        {
            ErrorTileListView view = new ErrorTileListView(errorTileList);
            view.setVisible(true);
            int exitCode = view.getExitCode();
            view = null;

            if (exitCode == ErrorTileListView.CODE_RETRY)
            {
                TileListSimple tiles = new TileListSimple();
                for (Enumeration<TileDownloadError> enumeration = errorTileList.elements(); enumeration.hasMoreElements();)
                {
                    TileDownloadError tde = enumeration.nextElement();
                    tiles.addTile(tde.getTile());
                }

                downloader.setTilesToDownload(tiles.getTileListToDownload());
                downloader.start();
            }
            else
            {
                setVisible(false);
            }
        }
        else
        {
            setVisible(false);
        }
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadStopped(int, int)
     * {@inheritDoc}
     */
    public void downloadStopped(int actCount, int maxCount)
    {
        setVisible(false);
        dispose();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadedTile(int, int, java.lang.String)
     * {@inheritDoc}
     */
    public void downloadedTile(int actCount, int maxCount, String path, boolean updatedTile)
    {
        progressBar.setValue(actCount);
        progressBar.setMaximum(maxCount);
        progressBar.setString("Download tile " + actCount + "/" + maxCount);
        updateTimes();
        if (updatedTile)
        {
            _updatedTileCounter++;
        }
        updatedTileCounter.setText("Updated Tile Counter: " + _updatedTileCounter);
        if (previewVisible)
        {
            tilePreviewViewComponent.setImage(path);
        }
        repaint();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#errorOccured(int, int, java.lang.String)
     * {@inheritDoc}
     */
    public void errorOccured(int actCount, int maxCount, Tile tile)
    {
    // TODO Auto-generated method stub
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#setInfo(java.lang.String)
     * {@inheritDoc}
     */
    public void setInfo(String message)
    {
        progressBar.setString(message);
    }

    /**
     * Centers the window on the screen
     */
    protected void center()
    {
        setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);
    }

    private void updateTimes()
    {
        long ticks = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
        timeElapsed.setText("Elapsed: " + timeDiff(ticks));
        if (ticks > 0 && progressBar.getValue() > 0)
        {
            timeRemaining.setText("Remaining: " + timeDiff((long) (ticks / (double) (progressBar.getValue() / (double) progressBar.getMaximum())) - ticks));
        }
    }

    /** Constant for seconds unit and conversion */
    public static final int SECONDS = 1000;
    /** Constant for minutes unit and conversion */
    public static final int MINUTES = SECONDS * 60;
    /** Constant for hours unit and conversion */
    public static final int HOURS = MINUTES * 60;
    /** Constant for days unit and conversion */
    public static final int DAYS = HOURS * 24;

    /**
     * @param millisToString
     * @return
     */
    private String timeDiff(long millis)
    {
        StringBuffer sb = new StringBuffer();
        if (millis < 0)
        {
            sb.append("-");
            millis = -millis;
        }

        long day = millis / DAYS;

        if (day != 0)
        {
            sb.append(day);
            sb.append(".");
            millis = millis % DAYS;
        }

        sb.append(MessageFormat.format("{0,number,00}:", new Object[] { millis / HOURS }));
        millis = millis % HOURS;
        sb.append(MessageFormat.format("{0,number,00}:", new Object[] { millis / MINUTES }));
        millis = millis % MINUTES;
        sb.append(MessageFormat.format("{0,number,00}", new Object[] { millis / SECONDS }));
        return sb.toString();
    }
}
