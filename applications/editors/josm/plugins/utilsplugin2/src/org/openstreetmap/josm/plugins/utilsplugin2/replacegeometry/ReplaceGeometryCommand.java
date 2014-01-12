package org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry;

import java.util.Collection;
import javax.swing.Icon;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Command to replace the geometry of one object with another.
 * 
 * @author joshdoe
 */
public class ReplaceGeometryCommand extends SequenceCommand {
    private final String description;
    
    public ReplaceGeometryCommand(String description, Collection<Command> sequence) {
        super(description, sequence);
        this.description = description;
    }

    @Override
    public String getDescriptionText() {
        return description;
    }
    
    @Override
    public Icon getDescriptionIcon() {
        return ImageProvider.get("dumbutils", "replacegeometry");
    }
    
}
