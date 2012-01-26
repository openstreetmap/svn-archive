/*
 *      CommandLine.java
 * 
 *      Copyright 2011 Hind <foxhind@gmail.com>
 * 
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 * 
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 * 
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 */

package CommandLine;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.GpxWriter;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.io.OsmWriterFactory;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

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

	static final String pluginDir = Main.pref.getPluginsDirectory().getAbsolutePath() + "/CommandLine/";

	public CommandLine(PluginInformation info) {
		super(info);
		commandSymbol = ": ";
		history = new History(100);
		historyField = new JTextField();
		textField = new JTextField() {
			@Override
			protected void processKeyEvent(KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_PRESSED) {
					String text = textField.getText();
					int code = e.getKeyCode();
					if (code == KeyEvent.VK_ENTER) {
						String commandText = textField.getText().substring(prefix.length());
						switch (mode) {
						case IDLE:
							if (commandText.isEmpty()) {
								commandText = history.getLastItem();
							}
							else {
								history.addItem(commandText);
							}
							Command command = findCommand(commandText, true);
							if (command != null) {
								startCommand(command);
							}
							else
								setMode(Mode.IDLE);
							break;
						case SELECTION:
							if (currentMapFrame.mapMode instanceof WayAction || currentMapFrame.mapMode instanceof NodeAction || currentMapFrame.mapMode instanceof RelationAction || currentMapFrame.mapMode instanceof AnyAction) {
								Collection<OsmPrimitive> selected = Main.main.getCurrentDataSet().getSelected();
								if (selected.size() > 0)
									loadParameter(selected, true);
							}
							else {
								loadParameter(commandText, currentCommand.parameters.get(currentCommand.currentParameterNum).maxInstances == 1);
							}
							break;
						case ADJUSTMENT:
							break;
						}
						e.consume();
					}
					else if (code == KeyEvent.VK_UP) {
						textField.setText(prefix + history.getPrevItem());
						e.consume();
					}
					else if (code == KeyEvent.VK_DOWN) {
						textField.setText(prefix + history.getNextItem());
						e.consume();
					}
					else if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_LEFT) {
						if (textField.getCaretPosition() <= prefix.length())
							e.consume();
					}
					else if (code == KeyEvent.VK_HOME) {
						setCaretPosition(prefix.length());
						e.consume();
					}
					else if (code == KeyEvent.VK_ESCAPE) {
						if (textField.getText().length() == prefix.length() && mode == Mode.IDLE)
							deactivate();
						else
							endInput();
						e.consume();
					}
					else if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_END) {
					}
					else {
						e.consume();
					}
					if (textField.getCaretPosition() < prefix.length() || (textField.getSelectionStart() < prefix.length() && textField.getSelectionStart() > 0) )
						e.consume();
				}
				if (e.getID() == KeyEvent.KEY_TYPED)
					if (textField.getCaretPosition() < prefix.length() || (textField.getSelectionStart() < prefix.length() && textField.getSelectionStart() > 0) )
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
		};

		if ( Main.main.menu != null ) {
			commandMenu = Main.main.menu.addMenu(marktr("Commands") , KeyEvent.VK_C, Main.main.menu.defaultMenuPos, ht("/Plugin/CommandLine"));
			MainMenu.add(Main.main.menu.toolsMenu, new CommandLineAction(this));
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
		if (Main.map == null)
			return;
		DataSet ds = Main.main.getCurrentDataSet();
		if (ds == null)
			return;
		currentCommand = command;
		currentCommand.resetLoading();
		parseSelection(ds.getSelected());
		if (!(Main.map.mapMode instanceof AnyAction || Main.map.mapMode instanceof DummyAction || Main.map.mapMode instanceof LengthAction || Main.map.mapMode instanceof NodeAction || Main.map.mapMode instanceof PointAction || Main.map.mapMode instanceof RelationAction || Main.map.mapMode instanceof WayAction)) {
			previousMode = Main.map.mapMode;
		}
		if (currentCommand.currentParameterNum < currentCommand.parameters.size())
			setMode(Mode.SELECTION);
		else
			runTool();
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame)
	{
		if (oldFrame == null && newFrame != null) {
			currentMapFrame = newFrame;
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

	protected void printHistory(String text) {
		historyField.setText(text);
	}

	private void loadCommands() {
		commands = (new Loader(getPluginDir())).load();
		for (Command command : commands) {
			commandMenu.add(new CommandAction(command, this));
		}
	}

	private Command findCommand(String text, boolean strict) {
		for (int i = 0; i < commands.size(); i++) {
			if (strict) {
				if ( commands.get(i).name.equalsIgnoreCase(text) ) {
					return commands.get(i);
				}
			}
			else {
				if ( commands.get(i).name.toLowerCase().startsWith( text.toLowerCase() ) && text.length() > 1 ) {
					return commands.get(i);
				}
			}
		}
		return null;
	}

	protected void setMode(Mode targetMode) {
		DataSet currentDataSet = Main.main.getCurrentDataSet();
		if (currentDataSet != null) {
			currentDataSet.clearSelection();
			Main.map.mapView.repaint();
		}
		if (targetMode == Mode.IDLE) {
			mode = Mode.IDLE;
			currentCommand = null;
			prefix = tr("Command") + commandSymbol;
			textField.setText(prefix);
		}
		else if (targetMode == Mode.SELECTION) {
			mode = Mode.SELECTION;
			Parameter currentParameter = currentCommand.parameters.get(currentCommand.currentParameterNum);
			prefix = tr(currentParameter.description == null ? currentParameter.name : currentParameter.description);
			if (currentParameter.getRawValue() instanceof Relay)
				prefix = prefix + " (" + ((Relay)(currentParameter.getRawValue())).getOptionsString() + ")";
			prefix += commandSymbol;
			String value = currentParameter.getValue();
			textField.setText(prefix + value);
			Type currentType = currentParameter.type;
			MapMode action = null;
			switch (currentType) {
			case POINT:
				action = new PointAction(currentMapFrame, this);
				break;
			case WAY:
				action = new WayAction(currentMapFrame, this);
				break;
			case NODE:
				action = new NodeAction(currentMapFrame, this);
				break;
			case RELATION:
				action = new RelationAction(currentMapFrame, this);
				break;
			case ANY:
				action = new AnyAction(currentMapFrame, this);
				break;
			case LENGTH:
				action = new LengthAction(currentMapFrame, this);
				break;
			case USERNAME:
				loadParameter(Main.pref.get("osm-server.username", null), true);
				action = new DummyAction(currentMapFrame, this);
				break;
			case IMAGERYURL:
				Layer layer = Main.map.mapView.getActiveLayer();
				if (layer != null) {
					if (layer instanceof ImageryLayer) {
					}
					else {
						List<ImageryLayer> imageryLayers = Main.map.mapView.getLayersOfType(ImageryLayer.class);
						if (imageryLayers.size() == 1) {
							layer = imageryLayers.get(0);
						}
						else {
							endInput();
							return;
						}
					}
				}
				ImageryInfo info = ((ImageryLayer)layer).getInfo();
				String url = info.getUrl();
				String itype = info.getImageryType().getUrlString();
				loadParameter((url.equals("") ? itype : url), true);
				action = new DummyAction(currentMapFrame, this);
				break;
			case IMAGERYOFFSET:
				Layer olayer = Main.map.mapView.getActiveLayer();
				if (olayer != null) {
					if (olayer instanceof ImageryLayer) {
					}
					else {
						List<ImageryLayer> imageryLayers = Main.map.mapView.getLayersOfType(ImageryLayer.class);
						if (imageryLayers.size() == 1) {
							olayer = imageryLayers.get(0);
						}
						else {
							endInput();
							return;
						}
					}
				}
				loadParameter((String.valueOf(((ImageryLayer)olayer).getDx()) + "," + String.valueOf(((ImageryLayer)olayer).getDy())), true);
				action = new DummyAction(currentMapFrame, this);
				break;
			default:
				action = new DummyAction(currentMapFrame, this);
				break;
			}
			currentMapFrame.selectMapMode(action);
			activate();
			textField.select(prefix.length(), textField.getText().length());
		}
		else if (targetMode == Mode.PROCESSING) {
			mode = Mode.PROCESSING;
			prefix = tr("Processing...");
			textField.setText(prefix);
			Main.map.mapView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}

	public void activate() {
		textField.requestFocus();
		textField.setCaretPosition(textField.getText().length());
	}

	public void deactivate() {
		Main.map.mapView.requestFocus();
	}

	public void abortInput() {
		printHistory(tr("Aborted") + ".");
		endInput();
	}

	public void endInput() {
		setMode(Mode.IDLE);
		Main.map.selectMapMode(previousMode);
		Main.map.mapView.repaint();
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
			}
			else {
				runTool();
			}
		}
		else {
			System.out.println("Invalid argument");
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
		}
		else {
			currentCommand.resetLoading();
		}
		//System.out.println("Selected before " + String.valueOf(currentCommand.currentParameterNum) + "\n");
	}

	private class ToolProcess {
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
		builder.directory(new File(getPluginDir()));

		final StringBuilder debugstr = new StringBuilder();

		// debug: print resulting cmdline
		for (String s : builder.command())
			debugstr.append(s + " ");
		debugstr.append("\n");
		System.out.print(debugstr.toString());

		final ToolProcess tp = new ToolProcess();
		try {
			tp.process = builder.start();
		} catch (final IOException e) {
			e.printStackTrace();
			synchronized (debugstr) {
				System.out.print(
						tr("Error executing the script: ") +
						debugstr.toString() + e.getMessage() + "\n" + e.getStackTrace());
			}
			return;
		}
		tp.running = true;

		// redirect child process's stderr to JOSM stderr
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] buffer = new byte[1024];
					InputStream errStream = tp.process.getErrorStream();
					int len;
					while ((len = errStream.read(buffer)) > 0) {
						synchronized (debugstr) {
							debugstr.append(new String(buffer, 0, len));
						}
						System.err.write(buffer, 0, len);
					}
				} catch (IOException e) {
				}
			}
		}).start();

		// Write stdin stream
		Thread osmWriteThread = new Thread(new Runnable() {
			@Override
			public void run() {
				BBox bbox = null;
				final OutputStream outputStream = tp.process.getOutputStream();
				PrintWriter printWriter = null;
				try { printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "utf-8")); }
				catch (Exception e) {e.printStackTrace();}
				final OsmWriter osmWriter = OsmWriterFactory.createOsmWriter(printWriter, true, null);
				Collection<OsmPrimitive> refObjects = currentCommand.getDepsObjects();
				Collection<OsmPrimitive> pObjects;
				osmWriter.header();
				for (OsmPrimitive primitive : refObjects) {
					if (primitive instanceof Node)
						osmWriter.visit((Node)primitive);
					else if (primitive instanceof Way)
						osmWriter.visit((Way)primitive);
					else if (primitive instanceof Relation)
						osmWriter.visit((Relation)primitive);
					if (bbox == null)
						bbox = new BBox(primitive.getBBox());
					else
						bbox.addPrimitive(primitive, 0.0);
				}
				osmWriter.footer();
				osmWriter.flush();
				for (Parameter parameter : parameters) {
					if (!parameter.isOsm())
						continue;
					osmWriter.header();
					pObjects = parameter.getParameterObjects();
					for (OsmPrimitive primitive : pObjects) {
						if (primitive instanceof Node)
							osmWriter.visit((Node)primitive);
						else if (primitive instanceof Way)
							osmWriter.visit((Way)primitive);
						else if (primitive instanceof Relation)
							osmWriter.visit((Relation)primitive);
						if (bbox == null)
							bbox = new BBox(primitive.getBBox());
						else
							bbox.addPrimitive(primitive, 0.0);
					}
					osmWriter.footer();
					osmWriter.flush();
				}
				if (tracks) {
					final GpxWriter gpxWriter = new GpxWriter(printWriter);
					GpxFilter gpxFilter = new GpxFilter();
					gpxFilter.initBboxFilter(bbox);
					List<GpxLayer> gpxLayers = Main.map.mapView.getLayersOfType(GpxLayer.class);
					for (GpxLayer gpxLayer : gpxLayers) {
						gpxFilter.addGpxData(gpxLayer.data);
					}
					gpxWriter.write(gpxFilter.getGpxData());
				}
				osmWriter.close();
				synchronized (syncObj) {
					tp.running = false;
					syncObj.notifyAll();
				}
			}

		});

		// Read stdout stream
		final OsmToCmd osmToCmd = new OsmToCmd(this, Main.main.getCurrentDataSet());
		Thread osmParseThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String commandName = currentCommand.name;
					HashMap<Long, Long> inexiDMap = new HashMap<Long, Long>();
					final InputStream inputStream = tp.process.getInputStream();
					osmToCmd.parseStream(inputStream);
					final List<org.openstreetmap.josm.command.Command> cmdlist = osmToCmd.getCommandList();
					if (!cmdlist.isEmpty()) {
						SequenceCommand cmd = new SequenceCommand(commandName, cmdlist);
						Main.main.undoRedo.add(cmd);
					}
				}
				catch (Exception e) {}
				finally {
					synchronized (syncObj) {
						tp.running = false;
						syncObj.notifyAll();
					}
				}
			}

		});

		osmParseThread.start();
		osmWriteThread.start();

		synchronized (syncObj) {
			try {
				syncObj.wait(10000);
			} catch (InterruptedException e) {
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
