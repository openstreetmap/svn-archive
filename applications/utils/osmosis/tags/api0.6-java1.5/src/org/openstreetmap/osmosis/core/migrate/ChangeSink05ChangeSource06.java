// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.migrate;

import org.openstreetmap.osmosis.core.task.v0_5.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSource;


/**
 * A ChangeSinkChangeSource interface for converting from 0.5 to 0.6 format change data.
 * 
 * @author Brett Henderson
 */
public interface ChangeSink05ChangeSource06 extends ChangeSink, ChangeSource {
	// Interface only combines functionality of its extended interfaces.
}
