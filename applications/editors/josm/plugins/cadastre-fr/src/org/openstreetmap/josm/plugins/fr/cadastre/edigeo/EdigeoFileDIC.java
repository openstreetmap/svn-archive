// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Edigeo DIC file.
 */
public class EdigeoFileDIC extends EdigeoFile {

    /**
     * Constructs a new {@code EdigeoFileDIC}.
     * @param path path to DIC file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileDIC(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
