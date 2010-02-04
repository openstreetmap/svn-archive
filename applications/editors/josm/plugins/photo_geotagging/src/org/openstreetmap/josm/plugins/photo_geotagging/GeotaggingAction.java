//License: GPL (v2 or above)
package org.openstreetmap.josm.plugins.photo_geotagging;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.text.DecimalFormat;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.tools.GBC;

/**
 * The action to aks the user for confirmation and then do the tagging.
 */
class GeotaggingAction implements ActionListener {
    final static boolean debug = false;
    final static String KEEP_BACKUP = "plugins.photo_geotagging.keep_backup";
    final static int MTIME_MODE_GPS = 1;
    final static int MTIME_MODE_PREVIOUS_VALUE = 2;

    final private GeoImageLayer layer;
    public GeotaggingAction(GeoImageLayer layer) {
        this.layer = layer;
    }

    public void actionPerformed(ActionEvent arg0) {
        final List<ImageEntry> images = new ArrayList<ImageEntry>();
        for (ImageEntry e : layer.getImages()) {
             /* Only write lat/lon to the file, if the position is known and
                we have a time from the correlation to the gpx track. */
            if (e.getPos() != null && e.getGpsTime() != null) {
                images.add(e);
            }
        }

        final JPanel cont = new JPanel(new GridBagLayout());
        cont.add(new JLabel(tr("Write position information into the exif header of the following files:")), GBC.eol());

        DefaultListModel listModel = new DefaultListModel();
        DecimalFormat dFormatter = new DecimalFormat("###0.000000");
        for (ImageEntry e : images) {
            listModel.addElement(e.getFile().getAbsolutePath()+
                " ("+dFormatter.format(e.getPos().lat())+","+dFormatter.format(e.getPos().lon())+")");
        }

        JList entryList = new JList(listModel);

        JScrollPane scroll = new JScrollPane(entryList);
        scroll.setPreferredSize(new Dimension(900, 250));
        cont.add(scroll, GBC.eol().fill(GBC.BOTH));

        final JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder(tr("settings")));
        cont.add(settingsPanel, GBC.eol().insets(3,10,3,0));

        final JCheckBox backups = new JCheckBox(tr("keep backup files"), Main.pref.getBoolean(KEEP_BACKUP, true));
        settingsPanel.add(backups, GBC.eol().insets(3,3,0,0));

        final JCheckBox setMTime = new JCheckBox(tr("change file modification time:"), false);
        settingsPanel.add(setMTime, GBC.std().insets(3,3,5,3));

        final String[] mTimeModeArray = {"----", tr("to gps time"), tr("to previous value (unchanged mtime)")};
        final JComboBox mTimeMode = new JComboBox(mTimeModeArray);
        settingsPanel.add(mTimeMode, GBC.eol().insets(3,3,3,3));

        setMTime.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (setMTime.isSelected()) {
                    mTimeMode.setEnabled(true);
                } else {
                    mTimeMode.setSelectedIndex(0);
                    mTimeMode.setEnabled(false);
                }
            }
        });

        // Toggle the checkbox to fire actionListener
        setMTime.setSelected(!setMTime.isSelected());
        setMTime.doClick();

        int result = new ExtendedDialog(
                Main.parent,
                tr("Photo Geotagging Plugin"),
                new String[] {tr("OK"), tr("Cancel")})
            .setButtonIcons(new String[] {"ok.png", "cancel.png"})
            .setContent(cont)
            .setCancelButton(2)
            .setDefaultButton(1)
            .showDialog()
            .getValue();

        if (result != 1)
            return;

        final boolean keep_backup = backups.isSelected();
        Main.pref.put(KEEP_BACKUP, keep_backup);

        Main.worker.execute(new GeoTaggingRunnable(images, keep_backup, mTimeMode.getSelectedIndex()));
    }

    class GeoTaggingRunnable extends PleaseWaitRunnable {
        final private List<ImageEntry> images;
        final private boolean keep_backup;
        final private int mTimeMode;

        private boolean cancelled = false;
        private Boolean override_backup = null;

        private File fileFrom;
        private File fileTo;
        private File fileDelete;

        public GeoTaggingRunnable(List<ImageEntry> images, boolean keep_backup, int mTimeMode) {
            super(tr("Photo Geotagging Plugin"));
            this.images = images;
            this.keep_backup = keep_backup;
            this.mTimeMode = mTimeMode;
        }
        @Override
        protected void realRun() {
            progressMonitor.subTask(tr("Writing position information to image files..."));
            progressMonitor.setTicksCount(images.size());

            for (int i=0; i<images.size(); ++i) {
                if (cancelled) return;

                ImageEntry e = images.get(i);
                if (debug) {
                    System.err.print("i:"+i+" "+e.getFile().getName()+" ");
                }

                fileFrom = null;
                fileTo = null;
                fileDelete = null;

                try {
                    if (mTimeMode != 0) {
                        testMTimeReadAndWrite(e.getFile());
                    }
                    
                    Long mTime = null;
                    if (mTimeMode == MTIME_MODE_PREVIOUS_VALUE) {
                        mTime = e.getFile().lastModified();
                        if (mTime.equals(0L))
                            throw new IOException(tr("Could not read mtime."));
                    }

                    chooseFiles(e.getFile());
                    if (cancelled) return;
                    ExifGPSTagger.setExifGPSTag(fileFrom, fileTo, e.getPos().lat(), e.getPos().lon(), e.getGpsTime().getTime());

                    if (mTimeMode == MTIME_MODE_GPS) {
                        mTime = e.getGpsTime().getTime();
                    }

                    if (mTime != null) {
                        if (!fileTo.setLastModified(mTime))
                            throw new IOException(tr("Could not write mtime."));
                    }

                    cleanupFiles();

                } catch (final IOException ioe) {
                    ioe.printStackTrace();
                    // need this so the dialogs don't block
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JOptionPane.showMessageDialog(Main.parent, ioe.getMessage(), tr("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    return;
                }
                progressMonitor.worked(1);
                if (debug) {
                    System.err.println("");
                }
            }
        }

        private void chooseFiles(File file) throws IOException {
            if (debug) {
                System.err.println("f: "+file.getAbsolutePath());
            }

            if (!keep_backup) {
                chooseFilesNoBackup(file);
                return;
            }

            File fileBackup = new File(file.getParentFile(),file.getName()+"_");
            if (fileBackup.exists()) {
                confirm_override();
                if (cancelled)
                    return;

                if (override_backup) {
                    if (!fileBackup.delete())
                        throw new IOException(tr("File could not be deleted!"));
                } else {
                    chooseFilesNoBackup(file);
                    return;
                }
            }
            if (!file.renameTo(fileBackup))
                throw new IOException(tr("Could not rename file!"));

            fileFrom = fileBackup;
            fileTo = file;
            fileDelete = null;
        }

        private void chooseFilesNoBackup(File file) throws IOException {
            File fileTmp;
            fileTmp = File.createTempFile("img", ".jpg", file.getParentFile());
            if (debug) {
                System.err.println("TMP: "+fileTmp.getAbsolutePath());
            }
            if (! file.renameTo(fileTmp))
                throw new IOException(tr("Could not rename file!"));

            fileFrom = fileTmp;
            fileTo = file;
            fileDelete = fileTmp;
        }

        private void confirm_override() {
            if (override_backup == null) {
                JLabel l = new JLabel(tr("<html><h3>There are old backup files in the image directory!</h3>"));
                l.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
                int override = new ExtendedDialog(
                        Main.parent,
                        tr("Override old backup files?"),
                        new String[] {tr("Cancel"), tr("Keep old backups and continue"), tr("Override")})
                    .setButtonIcons(new String[] {"cancel.png", "ok.png", "dialogs/delete.png"})
                    .setContent(l)
                    .setCancelButton(1)
                    .setDefaultButton(2)
                    .showDialog()
                    .getValue();
                if (override == 2) {
                    override_backup = false;
                } else if (override == 3) {
                    override_backup = true;
                } else {
                    cancelled = true;
                    return;
                }
            }
        }

        private void cleanupFiles() throws IOException {
            if (fileDelete != null) {
                if (!fileDelete.delete())
                    throw new IOException(tr("Could not delete temporary file!"));
            }
        }
        
        boolean testMTimeReadAndWriteDone = false;
        
        private void testMTimeReadAndWrite(File file) throws IOException {
            if (testMTimeReadAndWriteDone)  // do this only once
                return;
            File fileTest = File.createTempFile("geo", ".txt", file.getParentFile());
            long mTimeTest = fileTest.lastModified();
            if (mTimeTest == 0L)
                throw new IOException(tr("Test failed: Could not read mtime."));
            if (!fileTest.setLastModified(mTimeTest))
                throw new IOException(tr("Test failed: Could not write mtime."));
            if (!fileTest.delete())
                throw new IOException(tr("Could not delete temporary file!"));

            testMTimeReadAndWriteDone = true;
        }

        @Override
        protected void finish() {
        }

        @Override
        protected void cancel() {
            cancelled = true;
        }
    }
}
