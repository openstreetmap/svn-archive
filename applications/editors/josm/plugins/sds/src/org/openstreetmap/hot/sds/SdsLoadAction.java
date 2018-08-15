// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.widgets.SwingFileChooser;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Logging;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class SdsLoadAction extends SdsDiskAccessAction {

    private SeparateDataStorePlugin plugin;

    public SdsLoadAction(SeparateDataStorePlugin p) {
        super(tr("Load..."), "sds_load", tr("Load separate data store data from a file."), null);
        plugin = p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingFileChooser fc = createAndOpenFileChooser(true, true, null);
        if (fc == null)
            return;
        File[] files = fc.getSelectedFiles();
        openFiles(Arrays.asList(files));
    }

    public void openFiles(List<File> fileList) {
        OpenFileTask task = new OpenFileTask(fileList, plugin);
        MainApplication.worker.submit(task);
    }

    public class OpenFileTask extends PleaseWaitRunnable {
        private List<File> files;
        private boolean canceled;
        private SeparateDataStorePlugin plugin;

        public OpenFileTask(List<File> files, SeparateDataStorePlugin p) {
            super(tr("Loading files"), false /* don't ignore exception */);
            this.files = new ArrayList<>(files);
            plugin = p;
        }

        @Override
        protected void cancel() {
            this.canceled = true;
        }

        @Override
        protected void finish() {
            // do nothing
        }

        @Override
        protected void realRun() throws SAXException, IOException, OsmTransferException {
            if (files == null || files.isEmpty()) return;

            getProgressMonitor().setTicksCount(files.size());

            for (File f : files) {
                if (canceled) return;
                getProgressMonitor().indeterminateSubTask(tr("Opening file ''{0}'' ...", f.getAbsolutePath()));

                InputStream fileStream;
                try {
                    fileStream = new FileInputStream(f);
                    SdsParser p = new SdsParser(getLayerManager().getEditDataSet(), plugin, false);
                    InputSource inputSource = new InputSource(fileStream);
                    SAXParserFactory.newInstance().newSAXParser().parse(inputSource, p);
                } catch (SAXException | IOException | ParserConfigurationException e) {
                    Logging.error(e);
                }
            }
        }
    }
}
