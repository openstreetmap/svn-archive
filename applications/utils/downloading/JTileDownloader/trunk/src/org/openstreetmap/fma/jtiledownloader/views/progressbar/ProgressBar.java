/*
 * Copyright 2009 - 2010, Sven Strickroth <email@cs-ware.de>
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
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private JButton pauseResumeButton = new JButton("Pause");
    private JButton abortButton = new JButton("Abort");
    private TilePreviewViewComponent tilePreviewViewComponent = new TilePreviewViewComponent();
    private TileListDownloader downloader = null;
    private Calendar start = Calendar.getInstance();

    /**
     * @param tilesCount
     * @param downloader
     */
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
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(pauseResumeButton);
        pauseResumeButton.addActionListener(this);
        buttonsPanel.add(abortButton);
        abortButton.addActionListener(this);
        add(buttonsPanel, constraints);
        constraints.insets = new Insets(5, 5, 5, 5);
        add(showPreview, constraints);
        showPreview.addActionListener(this);

        tilePreviewViewComponent.setPreferredSize(new Dimension(256, 256));
        setShowPreview(AppConfiguration.getInstance().isShowTilePreview());
        center();
        downloader.setListener(this);
        downloader.start();
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0)
    {
        if (arg0.getSource().equals(showPreview))
        {
            setShowPreview(showPreview.isSelected());
        }
        else if (arg0.getSource().equals(pauseResumeButton))
        {
            if (pauseResumeButton.getText().equals("Pause"))
            {
                pauseResumeButton.setEnabled(false);
                downloader.pause();
            }
            else
            {
                pauseResumeButton.setText("Pause");
                downloader.start();
            }
        }
        else
        {
            pauseResumeButton.setEnabled(false);
            abortButton.setEnabled(false);
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
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadComplete(int, java.util.ArrayList, int)
     */
    public void downloadComplete(int errorCount, ArrayList<TileDownloadError> errorTileList, int updatedTileCount)
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
                for (TileDownloadError tde : errorTileList)
                {
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
     */
    public void downloadStopped(int actCount, int maxCount)
    {
        setVisible(false);
        dispose();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadPaused(int, int)
     */
    public void downloadPaused(int actCount, int maxCount)
    {
        pauseResumeButton.setText("Resume");
        pauseResumeButton.setEnabled(true);
        progressBar.setString("Paused at " + actCount + "/" + maxCount);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadedTile(int, int, java.lang.String, int, boolean)
     */
    public void downloadedTile(int actCount, int maxCount, String path, int updatedCount, boolean updatedTile)
    {
        progressBar.setValue(actCount);
        progressBar.setMaximum(maxCount);
        progressBar.setString("Download tile " + actCount + "/" + maxCount);
        updateTimes();
        updatedTileCounter.setText("Updated Tile Counter: " + updatedCount);
        if (previewVisible)
        {
            tilePreviewViewComponent.setImage(path);
        }
        repaint();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#errorOccured(int, int, Tile)
     */
    public void errorOccured(int actCount, int maxCount, Tile tile)
    {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#setInfo(java.lang.String)
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
            timeRemaining.setText("Remaining: " + timeDiff((long) (ticks / (progressBar.getValue() / (double) progressBar.getMaximum())) - ticks));
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

    private String timeDiff(long millis)
    {
        StringBuilder sb = new StringBuilder();
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
