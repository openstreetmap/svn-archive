package net.simon04.comfort0;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

/**
 * Launches an external editor.
 */
public class EditorLauncher {

    private Path fileToEdit;

    /**
     * Constructs a new {@link EditorLauncher}.
     * @param fileToEdit the file to edit
     */
    public EditorLauncher(Path fileToEdit) {
        this.fileToEdit = fileToEdit;
    }

    /**
     * Attempts to launch an external editor.
     * @return the process of the running editor
     * @throws IOException if any I/O error occurs, in particular if no editor could be launched
     */
    public Process launch() throws IOException {
        final String key = "comfort0.editor";
        final List<String> editors = Config.getPref().getList(key, Arrays.asList(
                "code --new-window --reuse-window --wait",
                "atom --wait",
                "gedit --standalone --wait",
                "kate --new --block",
                "konsole -e nvim",
                "urxvt -e nvim",
                "konsole -e vim",
                "urxvt -e vim",
                "konsole -e nano",
                "urxvt -e nano",
                "notepad++",
                "notepad"
        ));
        for (String editor : editors) {
            final List<String> cmd = Stream.concat(Pattern.compile("\\s+").splitAsStream(editor),
                    Stream.of(fileToEdit.toString()))
                    .collect(Collectors.toList());
            try {
                return new ProcessBuilder(cmd).start();
            } catch (IOException ex) {
                Logging.trace(ex);
                // try next editor
            }
        }
        throw new IOException(tr("No supported editor found. Please ''{0}'' set in advanced preferences!", key));
    }
}
