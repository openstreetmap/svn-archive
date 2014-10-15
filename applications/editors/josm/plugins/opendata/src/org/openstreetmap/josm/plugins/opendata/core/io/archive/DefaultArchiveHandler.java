// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import java.io.File;

public class DefaultArchiveHandler implements ArchiveHandler {

    private boolean skipXsdValidation = false;
    
    @Override
    public final void setSkipXsdValidation(boolean skip) {
        skipXsdValidation = skip;
    }
    
    @Override
    public boolean skipXsdValidation() {
        return skipXsdValidation;
    }
    
    @Override
    public void notifyTempFileWritten(File file) {
        // Do nothing, let subclass override this method if they need it
    }
}
