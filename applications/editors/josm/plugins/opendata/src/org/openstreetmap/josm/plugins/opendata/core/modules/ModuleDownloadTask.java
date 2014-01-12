// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.openstreetmap.josm.data.Version;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Utils;
import org.xml.sax.SAXException;


/**
 * Asynchronous task for downloading a collection of modules.
 *
 * When the task is finished {@see #getDownloadedModules()} replies the list of downloaded modules
 * and {@see #getFailedModules()} replies the list of failed modules.
 *
 */
public class ModuleDownloadTask extends PleaseWaitRunnable{
    private final Collection<ModuleInformation> toUpdate = new LinkedList<ModuleInformation>();
    private final Collection<ModuleInformation> failed = new LinkedList<ModuleInformation>();
    private final Collection<ModuleInformation> downloaded = new LinkedList<ModuleInformation>();
    //private Exception lastException;
    private boolean canceled;
    private HttpURLConnection downloadConnection;

    /**
     * Creates the download task
     *
     * @param parent the parent component relative to which the {@see PleaseWaitDialog} is displayed
     * @param toUpdate a collection of module descriptions for modules to update/download. Must not be null.
     * @param title the title to display in the {@see PleaseWaitDialog}
     * @throws IllegalArgumentException thrown if toUpdate is null
     */
    public ModuleDownloadTask(Component parent, Collection<ModuleInformation> toUpdate, String title) throws IllegalArgumentException{
        super(parent, title == null ? "" : title, false /* don't ignore exceptions */);
        CheckParameterUtil.ensureParameterNotNull(toUpdate, "toUpdate");
        this.toUpdate.addAll(toUpdate);
    }

    /**
     * Creates the task
     *
     * @param monitor a progress monitor. Defaults to {@see NullProgressMonitor#INSTANCE} if null
     * @param toUpdate a collection of module descriptions for modules to update/download. Must not be null.
     * @param title the title to display in the {@see PleaseWaitDialog}
     * @throws IllegalArgumentException thrown if toUpdate is null
     */
    public ModuleDownloadTask(ProgressMonitor monitor, Collection<ModuleInformation> toUpdate, String title) {
        super(title, monitor == null? NullProgressMonitor.INSTANCE: monitor, false /* don't ignore exceptions */);
        CheckParameterUtil.ensureParameterNotNull(toUpdate, "toUpdate");
        this.toUpdate.addAll(toUpdate);
    }

    /**
     * Sets the collection of modules to update.
     *
     * @param toUpdate the collection of modules to update. Must not be null.
     * @throws IllegalArgumentException thrown if toUpdate is null
     */
    public void setModulesToDownload(Collection<ModuleInformation> toUpdate) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(toUpdate, "toUpdate");
        this.toUpdate.clear();
        this.toUpdate.addAll(toUpdate);
    }

    @Override protected void cancel() {
        this.canceled = true;
        synchronized(this) {
            if (downloadConnection != null) {
                downloadConnection.disconnect();
            }
        }
    }

    @Override protected void finish() {}

    protected void download(ModuleInformation pi, File file) throws ModuleDownloadException{
        OutputStream out = null;
        InputStream in = null;
        try {
            if (pi.downloadlink == null) {
                String msg = tr("Warning: Cannot download module ''{0}''. Its download link is not known. Skipping download.", pi.name);
                System.err.println(msg);
                throw new ModuleDownloadException(msg);
            }
            URL url = new URL(pi.downloadlink);
            synchronized(this) {
                downloadConnection = (HttpURLConnection)url.openConnection();
                downloadConnection.setRequestProperty("Cache-Control", "no-cache");
                downloadConnection.setRequestProperty("User-Agent",Version.getInstance().getAgentString());
                downloadConnection.setRequestProperty("Host", url.getHost());
                downloadConnection.connect();
            }
            in = downloadConnection.getInputStream();
            out = new FileOutputStream(file);
            byte[] buffer = new byte[8192];
            for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();
        } catch(MalformedURLException e) {
            String msg = tr("Warning: Cannot download module ''{0}''. Its download link ''{1}'' is not a valid URL. Skipping download.", pi.name, pi.downloadlink);
            System.err.println(msg);
            throw new ModuleDownloadException(msg);
        } catch (IOException e) {
            if (canceled)
                return;
            throw new ModuleDownloadException(e);
        } finally {
            Utils.close(in);
            synchronized(this) {
                downloadConnection = null;
            }
            Utils.close(out);
        }
    }

    @Override protected void realRun() throws SAXException, IOException {
        File moduleDir = OdPlugin.getInstance().getModulesDirectory();
        if (!moduleDir.exists()) {
            if (!moduleDir.mkdirs()) {
                //lastException = new ModuleDownloadException(tr("Failed to create module directory ''{0}''", moduleDir.toString()));
                failed.addAll(toUpdate);
                return;
            }
        }
        getProgressMonitor().setTicksCount(toUpdate.size());
        for (ModuleInformation d : toUpdate) {
            if (canceled) return;
            progressMonitor.subTask(tr("Downloading Module {0}...", d.name));
            progressMonitor.worked(1);
            File moduleFile = new File(moduleDir, d.name + ".jar.new");
            try {
                download(d, moduleFile);
            } catch(ModuleDownloadException e) {
                e.printStackTrace();
                failed.add(d);
                continue;
            }
            downloaded.add(d);
        }
        ModuleHandler.installDownloadedModules(false);
    }

    /**
     * Replies true if the task was canceled by the user
     *
     * @return
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Replies the list of successfully downloaded modules
     *
     * @return the list of successfully downloaded modules
     */
    public Collection<ModuleInformation> getFailedModules() {
        return failed;
    }

    /**
     * Replies the list of modules whose download has failed
     *
     * @return the list of modules whose download has failed
     */
    public Collection<ModuleInformation> getDownloadedModules() {
        return downloaded;
    }
}
