// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import java.io.File;

public interface ArchiveHandler {

	public void notifyTempFileWritten(File file);

	public boolean skipXsdValidation();

	public void setSkipXsdValidation(boolean skip);
}
