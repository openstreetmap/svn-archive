package net.simon04.comfort0;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.simon04.comfort0.level0l.parsergen.Level0LParser;
import net.simon04.comfort0.level0l.parsergen.ParseException;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveData;
import org.openstreetmap.josm.data.osm.RelationData;
import org.openstreetmap.josm.data.osm.WayData;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.Logging;

/**
 * Edit the selected objects in an external editor in the Level0L format.
 */
public class EditLevel0LAction extends JosmAction {

    private static final Charset CHARSET = Charset.defaultCharset();

    /**
     * Constructs a new {@link EditLevel0LAction}.
     */
    public EditLevel0LAction() {
        super(tr("Edit as Level0L"),
                "theta",
                tr("Edit the selected objects in an external editor in the Level0L format"),
                null, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            editLevel0();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void editLevel0() throws IOException {
        final DataSet dataSet = MainApplication.getLayerManager().getEditDataSet();
        final Path path = writeLevel0(dataSet);
        final Process editor = new EditorLauncher(path).launch();
        Logging.info("Comfort0: Launching editor on file {0}", path);
        new Thread(() -> awaitEditing(dataSet, path, editor), path.getFileName().toString()).start();
    }

    private Path writeLevel0(DataSet dataSet) throws IOException {
        final byte[] level0 = new OsmToLevel0L().visit(dataSet.getSelected())
                .toString()
                .replace("\u200E", "")
                .replace("\u200F", "")
                .getBytes(CHARSET);
        final Path path = Files.createTempFile("josm_level0_", ".txt");
        Files.write(path, level0);
        return path;
    }

    private void awaitEditing(DataSet dataSet, Path path, Process editor) {
        try {
            editor.waitFor();
            Logging.info("Comfort0: Editing of file {0} done", path);
            readLevel0(path, dataSet);
            Files.delete(path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void readLevel0(Path path, final DataSet dataSet) throws IOException, ParseException {
        final List<PrimitiveData> primitives;
        try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
            primitives = new Level0LParser(reader).primitives();
        }
        Logging.info("Comfort0: Reading file {0} yielded {1} primitives", path, primitives.size());

        buildChangeCommands(dataSet, primitives);
    }

    private void buildChangeCommands(DataSet dataSet, List<PrimitiveData> primitives) {
        final List<Command> commands = new ArrayList<>();
        for (PrimitiveData fromLevel0L : primitives) {
            final OsmPrimitive fromDataSet = dataSet.getPrimitiveById(fromLevel0L);

            // TODO handle way nodes, relation members
            if (fromLevel0L instanceof WayData) {
                ((WayData) fromLevel0L).setNodeIds(Collections.emptyList());
            } else if (fromLevel0L instanceof RelationData) {
                ((RelationData) fromLevel0L).setMembers(Collections.emptyList());
            }

            final OsmPrimitive newInstance = fromLevel0L.getType().newVersionedInstance(fromDataSet.getUniqueId(), fromDataSet.getVersion());
            newInstance.load(fromLevel0L);

            final boolean equalKeys = Objects.equals(newInstance.getKeys(), fromDataSet.getKeys());
            if (!equalKeys) {
                final ChangePropertyCommand command = new ChangePropertyCommand(Collections.singleton(fromDataSet), newInstance.getKeys());
                commands.add(command);
            }
        }

        Logging.info("Comfort0: Changing {0} primitives", commands.size());
        if (commands.isEmpty()) {
            return;
        }
        final SequenceCommand command = new SequenceCommand("Comfort0", commands);
        GuiHelper.runInEDT(() -> UndoRedoHandler.getInstance().add(command));
    }
}
