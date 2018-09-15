// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.AbstractTileSourceLayer;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.widgets.DisableShortcutsOnFocusGainedTextField;
import org.openstreetmap.josm.io.GpxWriter;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.io.OsmWriterFactory;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.SubclassFilteredCollection;
import org.openstreetmap.josm.tools.Utils;

public class CommandLine extends Plugin {
    protected JTextField textField;
    protected JTextField historyField;
    private String prefix;
    private Mode mode;
    private ArrayList<Command> commands;
    private JMenu commandMenu;
    protected Command currentCommand;
    protected String commandSymbol;
    protected History history;
    protected MapFrame currentMapFrame;
    protected MapMode previousMode;

    static final String pluginDir = Preferences.main().getPluginsDirectory().getAbsolutePath() + "/CommandLine/";

    public CommandLine(PluginInformation info) {
        super(info);
        commandSymbol = ": ";
        history = new History(100);
        historyField = new DisableShortcutsOnFocusGainedTextField();
        textField = new CommandTextField();

        MainMenu mainMenu = MainApplication.getMenu();
        if (mainMenu != null) {
            commandMenu = mainMenu.addMenu("Commands", tr("Commands"), KeyEvent.VK_O,
                    mainMenu.getDefaultMenuPos(), ht("/Plugin/CommandLine"));
            MainMenu.add(commandMenu, new CommandLineAction(this));
        }
        loadCommands();
        setMode(Mode.IDLE);
    }

    public void startCommand(String commandName) {
        Command command = findCommand(commandName, true);
        if (command != null) {
            startCommand(command);
        }
    }

    protected void startCommand(Command command) {
        MapFrame map = MainApplication.getMap();
        if (map == null)
            return;
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        if (ds == null)
            return;
        currentCommand = command;
        currentCommand.resetLoading();
        parseSelection(ds.getSelected());
        if (!(map.mapMode instanceof AnyAction
           || map.mapMode instanceof DummyAction
           || map.mapMode instanceof LengthAction
           || map.mapMode instanceof NodeAction
           || map.mapMode instanceof PointAction
           || map.mapMode instanceof RelationAction
           || map.mapMode instanceof WayAction)) {
            previousMode = map.mapMode;
        }
        if (currentCommand.currentParameterNum < currentCommand.parameters.size())
            setMode(Mode.SELECTION);
        else
            runTool();
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        currentMapFrame = newFrame;
        if (oldFrame == null && newFrame != null) {
            JToolBar tb = new JToolBar();
            tb.setLayout(new BorderLayout());
            tb.setFloatable(false);
            tb.setOrientation(JToolBar.HORIZONTAL);
            tb.add(historyField, BorderLayout.NORTH);
            tb.add(textField, BorderLayout.SOUTH);
            currentMapFrame.add(tb, BorderLayout.NORTH);
            printHistory("Loaded CommandLine, version " + getPluginInformation().version);
        }
    }

    protected void printHistory(final String text) {
        SwingUtilities.invokeLater(() -> historyField.setText(text));
    }

    private void loadCommands() {
        commands = new Loader(getPluginDirs().getUserDataDirectory(false)).load();
        if (commands.isEmpty()) {
            if (!GraphicsEnvironment.isHeadless() && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(MainApplication.getMainFrame(),
                    tr("No command has been found. Would you like to download and install default commands now?"),
                    tr("No command found"), JOptionPane.YES_NO_CANCEL_OPTION)) {
                try {
                    downloadAndInstallDefaultCommands();
                    commands = new Loader(getPluginDirs().getUserDataDirectory(false)).load();
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Default commands have been successfully installed"),
                            tr("Success"), JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    Logging.warn(e);
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                            tr("Failed to download and install default commands.\n\nError: {0}", e.getMessage()),
                            tr("Warning"), JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        for (Command command : commands) {
            commandMenu.add(new CommandAction(command, this));
        }
    }

    private void downloadAndInstallDefaultCommands() throws IOException {
        String url = Config.getPref().get("commandline.default.commands.url",
                "https://github.com/Foxhind/JOSM-CommandLine-commands/archive/master.zip");
        try (ZipInputStream zis = new ZipInputStream(HttpClient.create(new URL(url)).connect().getContent(), StandardCharsets.UTF_8)) {
            File dir = getPluginDirs().getUserDataDirectory(false);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    if (name.contains("/")) {
                        name = name.substring(name.lastIndexOf("/"));
                    }
                    File file = new File(dir + File.separator + name);
                    Logging.info("Installing command file: "+file);
                    if (!file.createNewFile()) {
                        throw new IOException("Could not create file: " + file.getAbsolutePath());
                    }
                    // Write file
                    Files.copy(zis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    // Set last modification date
                    long time = entry.getTime();
                    if (time > -1) {
                        file.setLastModified(time);
                    }
                }
            }
        }
    }

    private Command findCommand(String text, boolean strict) {
        for (int i = 0; i < commands.size(); i++) {
            if (strict) {
                if (commands.get(i).name.equalsIgnoreCase(text)) {
                    return commands.get(i);
                }
            } else if (commands.get(i).name.toLowerCase().startsWith(text.toLowerCase()) && text.length() > 1) {
                return commands.get(i);
            }
        }
        return null;
    }

    protected void setMode(Mode targetMode) {
        DataSet currentDataSet = MainApplication.getLayerManager().getEditDataSet();
        if (currentDataSet != null) {
            currentDataSet.clearSelection();
            MainApplication.getMap().mapView.repaint();
        }
        if (targetMode == Mode.IDLE) {
            mode = Mode.IDLE;
            currentCommand = null;
            prefix = tr("Command") + commandSymbol;
            textField.setText(prefix);
        } else if (targetMode == Mode.SELECTION) {
            mode = Mode.SELECTION;
            Parameter currentParameter = currentCommand.parameters.get(currentCommand.currentParameterNum);
            prefix = tr(currentParameter.description == null ? currentParameter.name : currentParameter.description);
            if (currentParameter.getRawValue() instanceof Relay)
                prefix = prefix + " (" + ((Relay) currentParameter.getRawValue()).getOptionsString() + ")";
            prefix += commandSymbol;
            String value = currentParameter.getValue();
            textField.setText(prefix + value);
            Type currentType = currentParameter.type;
            MapMode action = null;
            switch (currentType) {
            case POINT:
                action = new PointAction(this);
                break;
            case WAY:
                action = new WayAction(this);
                break;
            case NODE:
                action = new NodeAction(this);
                break;
            case RELATION:
                action = new RelationAction(this);
                break;
            case ANY:
                action = new AnyAction(this);
                break;
            case LENGTH:
                action = new LengthAction(this);
                break;
            case USERNAME:
                loadParameter(Config.getPref().get("osm-server.username", null), true);
                action = new DummyAction(this);
                break;
            case IMAGERYURL:
                Layer layer = MainApplication.getLayerManager().getActiveLayer();
                if (layer != null) {
                    if (!(layer instanceof ImageryLayer)) {
                        List<ImageryLayer> imageryLayers = MainApplication.getLayerManager().getLayersOfType(ImageryLayer.class);
                        if (imageryLayers.size() == 1) {
                            layer = imageryLayers.get(0);
                        } else {
                            endInput();
                            return;
                        }
                    }
                }
                if (layer != null) {
                    ImageryInfo info = ((ImageryLayer) layer).getInfo();
                    String url = info.getUrl();
                    loadParameter(url.isEmpty() ? info.getImageryType().getTypeString() : url, true);
                }
                action = new DummyAction(this);
                break;
            case IMAGERYOFFSET:
                Layer olayer = MainApplication.getLayerManager().getActiveLayer();
                if (olayer != null) {
                    if (!(olayer instanceof AbstractTileSourceLayer)) {
                        @SuppressWarnings("rawtypes")
                        List<AbstractTileSourceLayer> imageryLayers = MainApplication.getLayerManager().getLayersOfType(
                                AbstractTileSourceLayer.class);
                        if (imageryLayers.size() == 1) {
                            olayer = imageryLayers.get(0);
                        } else {
                            endInput();
                            return;
                        }
                    }
                }
                loadParameter((String.valueOf(((AbstractTileSourceLayer<?>) olayer).getDisplaySettings().getDx()) + "," +
                               String.valueOf(((AbstractTileSourceLayer<?>) olayer).getDisplaySettings().getDy())), true);
                action = new DummyAction(this);
                break;
            default:
                action = new DummyAction(this);
                break;
            }
            currentMapFrame.selectMapMode(action);
            activate();
            textField.select(prefix.length(), textField.getText().length());
        } else if (targetMode == Mode.PROCESSING) {
            mode = Mode.PROCESSING;
            prefix = tr("Processing...");
            textField.setText(prefix);
            MainApplication.getMap().mapView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
    }

    public void activate() {
        textField.requestFocus();
        textField.setCaretPosition(textField.getText().length());
    }

    public void deactivate() {
        MainApplication.getMap().mapView.requestFocus();
    }

    public void abortInput() {
        printHistory(tr("Aborted") + ".");
        endInput();
    }

    public void endInput() {
        setMode(Mode.IDLE);
        MainApplication.getMap().selectMapMode(previousMode);
        MainApplication.getMap().mapView.repaint();
    }

    public void loadParameter(Object obj, boolean next) {
        if (currentCommand.loadObject(obj)) {
            if (currentCommand.hasNextParameter()) {
                if (next) {
                    Parameter currentParameter = currentCommand.parameters.get(currentCommand.currentParameterNum);
                    String prefix = tr(currentParameter.description == null ? currentParameter.name : currentParameter.description);
                    prefix += commandSymbol;
                    String value = currentParameter.getValue();
                    printHistory(prefix + value);
                    currentCommand.nextParameter();
                    setMode(Mode.SELECTION);
                }
            } else {
                runTool();
            }
        } else {
            Logging.info("Invalid argument");
            endInput();
        }
    }

    private void parseSelection(Collection<OsmPrimitive> selection) {
        boolean ok = false;
        for (OsmPrimitive obj : selection) {
            ok = currentCommand.loadObject(obj);
            if (!ok)
                break;
        }
        if (ok) {
            currentCommand.nextParameter();
        } else {
            currentCommand.resetLoading();
        }
    }

    private final class CommandTextField extends DisableShortcutsOnFocusGainedTextField {
        @Override
        protected void processKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    String commandText = textField.getText().substring(prefix.length());
                    switch (mode) {
                    case IDLE:
                        if (commandText.isEmpty()) {
                            commandText = history.getLastItem();
                        } else {
                            history.addItem(commandText);
                        }
                        Command command = findCommand(commandText, true);
                        if (command != null) {
                            startCommand(command);
                        } else {
                            setMode(Mode.IDLE);
                        }
                        break;
                    case SELECTION:
                        if (currentMapFrame.mapMode instanceof WayAction
                         || currentMapFrame.mapMode instanceof NodeAction
                         || currentMapFrame.mapMode instanceof RelationAction
                         || currentMapFrame.mapMode instanceof AnyAction) {
                            Collection<OsmPrimitive> selected = MainApplication.getLayerManager().getEditDataSet().getSelected();
                            if (!selected.isEmpty())
                                loadParameter(selected, true);
                        } else {
                            loadParameter(commandText, currentCommand.parameters.get(currentCommand.currentParameterNum).maxInstances == 1);
                        }
                        break;
                    case ADJUSTMENT:
                    default:
                        break;
                    }
                    e.consume();
                } else if (code == KeyEvent.VK_UP) {
                    textField.setText(prefix + history.getPrevItem());
                    e.consume();
                } else if (code == KeyEvent.VK_DOWN) {
                    textField.setText(prefix + history.getNextItem());
                    e.consume();
                } else if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_LEFT) {
                    if (textField.getCaretPosition() <= prefix.length())
                        e.consume();
                } else if (code == KeyEvent.VK_HOME) {
                    setCaretPosition(prefix.length());
                    e.consume();
                } else if (code == KeyEvent.VK_ESCAPE) {
                    if (textField.getText().length() == prefix.length() && mode == Mode.IDLE)
                        deactivate();
                    else
                        endInput();
                    e.consume();
                } else if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_END) {
                } else {
                    e.consume();
                }
                if (textField.getCaretPosition() < prefix.length() ||
                        (textField.getSelectionStart() < prefix.length() && textField.getSelectionStart() > 0))
                    e.consume();
            }
            if (e.getID() == KeyEvent.KEY_TYPED)
                if (textField.getCaretPosition() < prefix.length() ||
                        (textField.getSelectionStart() < prefix.length() && textField.getSelectionStart() > 0))
                    e.consume();
            super.processKeyEvent(e);
            if (textField.getText().length() < prefix.length()) { // Safe
                setMode(mode);
            }
            if (e.getID() == KeyEvent.KEY_TYPED) {
                if (e.getKeyChar() > 'A' && e.getKeyChar() < 'z') {
                    Command command = findCommand(textField.getText().substring(prefix.length()), false);
                    if (command != null) {
                        int currentPos = textField.getSelectionStart() == 0 ? textField.getCaretPosition() : textField.getSelectionStart();
                        textField.setText(prefix + command.name);
                        textField.setCaretPosition(currentPos);
                        textField.select(currentPos, prefix.length() + command.name.length());
                    }
                }
            }
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
            if (e.getButton() == MouseEvent.BUTTON1 && e.getID() == MouseEvent.MOUSE_RELEASED) {
                if (textField.getSelectionStart() > 0 && textField.getSelectionStart() < prefix.length())
                    textField.setSelectionStart(prefix.length());
                else if (textField.getCaretPosition() < prefix.length())
                    textField.setCaretPosition(prefix.length());
            }
        }
    }

    private static class ToolProcess {
        public Process process;
        public volatile boolean running;
    }

    // Thanks to Upliner
    public void runTool() {
        setMode(Mode.PROCESSING);
        String commandToRun = currentCommand.run;
        final boolean tracks = currentCommand.tracks;
        final ArrayList<Parameter> parameters = currentCommand.parameters;

        for (Parameter parameter : currentCommand.parameters) {
            commandToRun = commandToRun.replace("{" + parameter.name + "}", parameter.getValue());
        }
        for (Parameter parameter : currentCommand.optParameters) {
            commandToRun = commandToRun.replace("{" + parameter.name + "}", parameter.getValue());
        }
        String[] listToRun = commandToRun.split(" ");

        // create the process
        final Object syncObj = new Object();

        ProcessBuilder builder;
        builder = new ProcessBuilder(listToRun);
        builder.directory(getPluginDirs().getUserDataDirectory(false));

        final StringBuilder debugstr = new StringBuilder();

        // debug: print resulting cmdline
        for (String s : builder.command()) {
            debugstr.append(s + " ");
        }
        debugstr.append("\n");
        Logging.info(debugstr.toString());

        final ToolProcess tp = new ToolProcess();
        try {
            tp.process = builder.start();
        } catch (final IOException e) {
            synchronized (debugstr) {
                Logging.error(
                        tr("Error executing the script:") + ' ' +
                        debugstr.toString() + e.getMessage() + '\n' + Arrays.toString(e.getStackTrace()));
            }
            return;
        }
        tp.running = true;

        // redirect child process's stderr to JOSM stderr
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                InputStream errStream = tp.process.getErrorStream();
                int len;
                while ((len = errStream.read(buffer)) > 0) {
                    synchronized (debugstr) {
                        debugstr.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
                    }
                    System.err.write(buffer, 0, len);
                }
            } catch (IOException e) {
                Logging.warn(e);
            }
        }).start();

        // Write stdin stream
        Thread osmWriteThread = new Thread(() -> {
            BBox bbox = null;
            final OutputStream outputStream = tp.process.getOutputStream();
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            } catch (Exception e1) {
                Logging.error(e1);
            }
            final OsmWriter osmWriter = OsmWriterFactory.createOsmWriter(printWriter, true, null);
            Collection<OsmPrimitive> refObjects = currentCommand.getDepsObjects();
            Collection<OsmPrimitive> pObjects;
            osmWriter.header();
            Collection<OsmPrimitive> contents = new ArrayList<>();
            for (OsmPrimitive primitive1 : refObjects) {
                contents.add(primitive1);
                if (bbox == null)
                    bbox = new BBox(primitive1.getBBox());
                else
                    bbox.addPrimitive(primitive1, 0.0);
            }
            osmWriter.writeNodes(new SubclassFilteredCollection<OsmPrimitive, Node>(contents, Node.class::isInstance));
            osmWriter.writeWays(new SubclassFilteredCollection<OsmPrimitive, Way>(contents, Way.class::isInstance));
            osmWriter.writeRelations(new SubclassFilteredCollection<OsmPrimitive, Relation>(contents, Relation.class::isInstance));
            osmWriter.footer();
            osmWriter.flush();

            for (Parameter parameter : parameters) {
                if (!parameter.isOsm())
                    continue;
                contents = new ArrayList<>();
                osmWriter.header();
                pObjects = parameter.getParameterObjects();
                for (OsmPrimitive primitive2 : pObjects) {
                    contents.add(primitive2);
                    if (bbox == null)
                        bbox = new BBox(primitive2.getBBox());
                    else
                        bbox.addPrimitive(primitive2, 0.0);
                }
                osmWriter.writeNodes(new SubclassFilteredCollection<OsmPrimitive, Node>(contents, Node.class::isInstance));
                osmWriter.writeWays(new SubclassFilteredCollection<OsmPrimitive, Way>(contents, Way.class::isInstance));
                osmWriter.writeRelations(new SubclassFilteredCollection<OsmPrimitive, Relation>(contents, Relation.class::isInstance));
                osmWriter.footer();
                osmWriter.flush();
            }

            if (tracks) {
                try (GpxWriter gpxWriter = new GpxWriter(printWriter)) {
                    GpxFilter gpxFilter = new GpxFilter();
                    gpxFilter.initBboxFilter(bbox);
                    List<GpxLayer> gpxLayers = MainApplication.getLayerManager().getLayersOfType(GpxLayer.class);
                    for (GpxLayer gpxLayer : gpxLayers) {
                        gpxFilter.addGpxData(gpxLayer.data);
                    }
                    gpxWriter.write(gpxFilter.getGpxData());
                } catch (IOException e2) {
                    Logging.warn(e2);
                }
            }
            Utils.close(osmWriter);
            synchronized (syncObj) {
                if (currentCommand.asynchronous) {
                    tp.running = false;
                    syncObj.notifyAll();
                }
            }
        });

        // Read stdout stream
        final DataSet currentDataSet = MainApplication.getLayerManager().getEditDataSet();
        final CommandLine that = this;
        Thread osmParseThread = new Thread(() -> {
            try {
                final OsmToCmd osmToCmd = new OsmToCmd(that, currentDataSet);
                String commandName = currentCommand.name;
                final InputStream inputStream = tp.process.getInputStream();
                osmToCmd.parseStream(inputStream);
                final List<org.openstreetmap.josm.command.Command> cmdlist = osmToCmd.getCommandList();
                if (!cmdlist.isEmpty()) {
                    final SequenceCommand cmd = new SequenceCommand(commandName, cmdlist);
                    SwingUtilities.invokeLater(() -> UndoRedoHandler.getInstance().add(cmd));
                }
            } catch (Exception e) {
                Logging.warn(e);
            } finally {
                synchronized (syncObj) {
                    tp.running = false;
                    syncObj.notifyAll();
                }
            }
        });

        osmParseThread.start();
        osmWriteThread.start();

        synchronized (syncObj) {
            try {
                syncObj.wait(Config.getPref().getInt("commandline.timeout", 20000));
            } catch (InterruptedException e) {
                Logging.warn(e);
            }
        }
        if (tp.running) {
            new Thread(new PleaseWaitRunnable(currentCommand.name) {
                @Override
                protected void realRun() {
                    try {
                        progressMonitor.indeterminateSubTask(null);
                        synchronized (syncObj) {
                            if (tp.running)
                                syncObj.wait();
                        }
                    } catch (InterruptedException e) {
                        Logging.warn(e);
                    }
                }

                @Override
                protected void cancel() {
                    synchronized (syncObj) {
                        tp.running = false;
                        tp.process.destroy();
                        syncObj.notifyAll();
                        endInput();
                    }
                }

                @Override
                protected void finish() {
                }
            }).start();
        }
        endInput();
    }
}
