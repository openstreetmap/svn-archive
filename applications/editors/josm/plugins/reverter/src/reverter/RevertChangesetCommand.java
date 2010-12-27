package reverter;

import java.util.Collection;

import javax.swing.JLabel;

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

    @Override public JLabel getDescription() {
        return new JLabel(name, ImageProvider.get("revert-changeset"), JLabel.HORIZONTAL);
    }

}
