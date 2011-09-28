package org.openstreetmap.osmosis.plugins.relationtags;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;

public class RelationTagsTaskFactory extends TaskManagerFactory {
    private static final String DEFAULT_TYPES = "route,destination_sign,enforcement";

    @Override
    protected TaskManager createTaskManagerImpl( TaskConfiguration taskConfig ) {
        String[] types = getStringArgument(taskConfig, "types", DEFAULT_TYPES).split(",\\s*");
        String separator = getStringArgument(taskConfig, "separator", "_");
        String multi = getStringArgument(taskConfig, "multi", ";");
        boolean sort = getBooleanArgument(taskConfig, "sort", false);

        return new SinkSourceManager(taskConfig.getId(),
                new RelationTagsTask(types, separator, multi, sort),
                taskConfig.getPipeArgs());
    }
}
