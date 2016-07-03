// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import java.io.File;

public interface ArchiveHandler {

    void notifyTempFileWritten(File file);

    boolean skipXsdValidation();

    void setSkipXsdValidation(boolean skip);
}
