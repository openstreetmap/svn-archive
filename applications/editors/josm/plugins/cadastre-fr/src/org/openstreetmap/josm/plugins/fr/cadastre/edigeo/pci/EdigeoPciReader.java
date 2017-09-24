// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo.pci;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.DataSet.UploadPolicy;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileTHF;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.EdigeoFileVEC;

/**
 * Reader for French Cadastre - Edigéo files.
 */
public class EdigeoPciReader extends AbstractReader {

    static {
        EdigeoFileVEC.addIgnoredObject("SYM_id", "31");
    }

    /**
     * Constructs a new {@code EdigeoReader}.
     */
    public EdigeoPciReader() {
    }

    static DataSet parseDataSet(InputStream in, File file, ProgressMonitor instance) throws IOException {
        if (in != null) {
            in.close();
        }
        try {
            return new EdigeoPciReader().parse(file.toPath(), instance);
        } catch (IOException e) {
            throw e;
        } catch (Exception | AssertionError e) {
            throw new IOException(e);
        }
    }

    DataSet parse(Path path, ProgressMonitor instance) throws IOException, ReflectiveOperationException {
        DataSet data = new DataSet();
        data.setUploadPolicy(UploadPolicy.DISCOURAGED);
        EdigeoFileTHF thf = new EdigeoFileTHF(path).read().fill(data);
        data.setName(thf.getSupport().getBlockIdentifier());
        return data;
    }

    @Override
    protected DataSet doParseDataSet(InputStream source, ProgressMonitor progressMonitor) throws IllegalDataException {
        return null;
    }
}
