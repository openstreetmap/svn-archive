package reverter;

import java.util.Collection;
import javax.swing.Icon;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.tools.ImageProvider;

public class RevertChangesetCommand extends SequenceCommand {
    protected String name;

    public RevertChangesetCommand(String name, Collection<Command> sequenz) {
        super(name, sequenz);
        this.name = name;
        ReverterPlugin.reverterUsed = true;
    }

    @Override
    public String getDescriptionText() {
        return name;
    }

    @Override
    public Icon getDescriptionIcon() {
        return ImageProvider.get("revert-changeset");
    }

}
