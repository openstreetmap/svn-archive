// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;


public class ChantiersLineairesHandler extends ChantiersHandler {

    public ChantiersLineairesHandler() {
        super(14063, "Chantiers en cours (linéaire)");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Chantiers_Lineaires");
    }
}
