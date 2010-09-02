package wmsplugin;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.io.CacheFiles;
import org.openstreetmap.josm.io.MirroredInputStream;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginHandler;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.PluginProxy;

import wmsplugin.io.WMSLayerExporter;
import wmsplugin.io.WMSLayerImporter;

public class WMSPlugin extends Plugin {
	static CacheFiles cache = new CacheFiles("wmsplugin");

	public static final IntegerProperty PROP_SIMULTANEOUS_CONNECTIONS = new IntegerProperty("wmsplugin.simultaneousConnections", 3);
	public static final BooleanProperty PROP_OVERLAP = new BooleanProperty("wmsplugin.url.overlap", false);
	public static final IntegerProperty PROP_OVERLAP_EAST = new IntegerProperty("wmsplugin.url.overlapEast", 14);
	public static final IntegerProperty PROP_OVERLAP_NORTH = new IntegerProperty("wmsplugin.url.overlapNorth", 4);

	WMSLayer wmsLayer;
	static JMenu wmsJMenu;

	static ArrayList<WMSInfo> wmsList = new ArrayList<WMSInfo>();
	static TreeMap<String,String> wmsListDefault = new TreeMap<String,String>();

	// remember state of menu item to restore on changed preferences
	static private boolean menuEnabled = false;

	/***************************************************************
	 * Remote control initialization:
	 * If you need remote control in some other plug-in
	 * copy this stuff and the call to initRemoteControl below
	 * and replace the RequestHandler subclass in initRemoteControl
	 ***************************************************************/

	/** name of remote control plugin */
	private static final String REMOTECONTROL_NAME = "remotecontrol";

	/* if necessary change these version numbers to ensure compatibility */

	/** RemoteControlPlugin older than this SVN revision is not compatible */
	static final int REMOTECONTROL_MIN_REVISION = 22734;
	/** WMSPlugin needs this specific API major version of RemoteControlPlugin */
	static final int REMOTECONTROL_NEED_API_MAJOR = 1;
	/** All API minor versions starting from this should be compatible */
	static final int REMOTECONTROL_MIN_API_MINOR = 0;

	/* these fields will contain state and version of remote control plug-in */
	boolean remoteControlAvailable = false;
	boolean remoteControlCompatible = true;
	boolean remoteControlInitialized = false;
	int remoteControlRevision = 0;
	int remoteControlApiMajor = 0;
	int remoteControlApiMinor = 0;
	int remoteControlProtocolMajor = 0;
	int remoteControlProtocolMinor = 0;

	/**
	 * Check if remote control plug-in is available and if its version is
	 * high enough and register remote control command for this plug-in.
	 */
	private void initRemoteControl() {
		for(PluginProxy pp: PluginHandler.pluginList)
		{
			PluginInformation info = pp.getPluginInformation();
			if(REMOTECONTROL_NAME.equals(info.name))
			{
				remoteControlAvailable = true;
				remoteControlRevision = Integer.parseInt(info.version);
				if(REMOTECONTROL_MIN_REVISION > remoteControlRevision)
				{
					remoteControlCompatible = false;
				}
			}
		}

		if(remoteControlAvailable && remoteControlCompatible)
		{
			Plugin plugin =
				(Plugin) PluginHandler.getPlugin(REMOTECONTROL_NAME);
			try {
				Method method;
				method = plugin.getClass().getMethod("getVersion");
				Object obj = method.invoke(plugin);
				if((obj != null ) && (obj instanceof int[]))
				{
					int[] versions = (int[]) obj;
					if(versions.length >= 4)
					{
						remoteControlApiMajor = versions[0];
						remoteControlApiMinor = versions[1];
						remoteControlProtocolMajor = versions[2];
						remoteControlProtocolMinor = versions[3];
					}
				}

				if((remoteControlApiMajor != REMOTECONTROL_NEED_API_MAJOR) ||
						(remoteControlApiMinor < REMOTECONTROL_MIN_API_MINOR))
				{
					remoteControlCompatible = false;
				}
				if(remoteControlCompatible)
				{
					System.out.println(this.getClass().getSimpleName() + ": initializing remote control");
					method = plugin.getClass().getMethod("addRequestHandler", String.class, Class.class);
					// replace command and class when you copy this to some other plug-in
					// for compatibility with old remotecontrol add leading "/"
					method.invoke(plugin, "/" + WMSRemoteHandler.command, WMSRemoteHandler.class);
					remoteControlInitialized = true;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if(remoteControlAvailable)
		{
			String msg = null;

			if(remoteControlCompatible)
			{
				if(!remoteControlInitialized)
				{
					msg  = tr("Could not initialize remote control.");
				}
			}
			else
			{
				msg  = tr("Remote control plugin is not compatible with {0}.",
						this.getClass().getSimpleName());
			}

			if(msg != null)
			{
				String additionalMessage = tr("{0} will work but remote control for this plugin is disabled.\n"
						+ "You should update the plugins.",
						this.getClass().getSimpleName());
				String versionMessage = tr("Current version of \"{1}\": {2}, internal version {3}. "
						+ "Need version {4}, internal version {5}.\n"
						+ "If updating the plugins does not help report a bug for \"{0}\".",
						this.getClass().getSimpleName(),
						REMOTECONTROL_NAME,
						""+remoteControlRevision,
						(remoteControlApiMajor != 0) ?
								""+remoteControlApiMajor+"."+remoteControlApiMinor :
									tr("unknown"),
									""+REMOTECONTROL_MIN_REVISION,
									""+REMOTECONTROL_NEED_API_MAJOR+"."+REMOTECONTROL_MIN_API_MINOR );

				String title = tr("{0}: Problem with remote control",
						this.getClass().getSimpleName());

				System.out.println(this.getClass().getSimpleName() + ": " +
						msg + "\n" + versionMessage);

				JOptionPane.showMessageDialog(
						Main.parent,
						msg + "\n" + additionalMessage,
						title,
						JOptionPane.WARNING_MESSAGE
				);
			}
		}

		if(!remoteControlAvailable) {
			System.out.println(this.getClass().getSimpleName() + ": remote control not available");
		}
	}

	/***************************************
	 * end of remote control initialization
	 ***************************************/

	protected void initExporterAndImporter() {
		ExtensionFileFilter.exporters.add(new WMSLayerExporter());
		ExtensionFileFilter.importers.add(new WMSLayerImporter());
	}

	public WMSPlugin(PluginInformation info) {
		super(info);
		/*
		System.out.println("constructor " + this.getClass().getName() + " (" + info.name +
				" v " + info.version + " stage " + info.stage + ")");
		 */
		refreshMenu();
		cache.setExpire(CacheFiles.EXPIRE_MONTHLY, false);
		cache.setMaxSize(70, false);
		initExporterAndImporter();
		initRemoteControl();
	}

	// this parses the preferences settings. preferences for the wms plugin have to
	// look like this:
	// wmsplugin.1.name=Landsat
	// wmsplugin.1.url=http://and.so.on/

	@Override
	public void copy(String from, String to) throws FileNotFoundException, IOException
	{
		File pluginDir = new File(getPrefsPath());
		if (!pluginDir.exists())
			pluginDir.mkdirs();
		FileOutputStream out = new FileOutputStream(getPrefsPath() + to);
		InputStream in = WMSPlugin.class.getResourceAsStream(from);
		byte[] buffer = new byte[8192];
		for(int len = in.read(buffer); len > 0; len = in.read(buffer))
			out.write(buffer, 0, len);
		in.close();
		out.close();
	}


	public static void refreshMenu() {
		wmsList.clear();
		Map<String,String> prefs = Main.pref.getAllPrefix("wmsplugin.url.");

		TreeSet<String> keys = new TreeSet<String>(prefs.keySet());

		// And then the names+urls of WMS servers
		int prefid = 0;
		String name = null;
		String url = null;
		String cookies = "";
		int lastid = -1;
		for (String key : keys) {
			String[] elements = key.split("\\.");
			if (elements.length != 4) continue;
			try {
				prefid = Integer.parseInt(elements[2]);
			} catch(NumberFormatException e) {
				continue;
			}
			if (prefid != lastid) {
				name = url = null; lastid = prefid;
			}
			if (elements[3].equals("name"))
				name = prefs.get(key);
			else if (elements[3].equals("url"))
			{
				/* FIXME: Remove the if clause after some time */
				if(!prefs.get(key).startsWith("yahoo:")) /* legacy stuff */
					url = prefs.get(key);
			}
			else if (elements[3].equals("cookies"))
				cookies = prefs.get(key);
			if (name != null && url != null)
				wmsList.add(new WMSInfo(name, url, cookies, prefid));
		}
		String source = "http://svn.openstreetmap.org/applications/editors/josm/plugins/wmsplugin/sources.cfg";
		try
		{
			MirroredInputStream s = new MirroredInputStream(source,
					Main.pref.getPreferencesDir() + "plugins/wmsplugin/", -1);
			InputStreamReader r;
			try
			{
				r = new InputStreamReader(s, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				r = new InputStreamReader(s);
			}
			BufferedReader reader = new BufferedReader(r);
			String line;
			while((line = reader.readLine()) != null)
			{
				String val[] = line.split(";");
				if(!line.startsWith("#") && val.length == 3)
					setDefault("true".equals(val[0]), tr(val[1]), val[2]);
			}
		}
		catch (IOException e)
		{
		}

		Collections.sort(wmsList);
		MainMenu menu = Main.main.menu;

		if (wmsJMenu == null)
			wmsJMenu = menu.addMenu(marktr("WMS"), KeyEvent.VK_W, menu.defaultMenuPos, ht("/Plugin/WMS"));
		else
			wmsJMenu.removeAll();

		// for each configured WMSInfo, add a menu entry.
		for (final WMSInfo u : wmsList) {
			wmsJMenu.add(new JMenuItem(new WMSDownloadAction(u)));
		}
		wmsJMenu.addSeparator();
		wmsJMenu.add(new JMenuItem(new Map_Rectifier_WMSmenuAction()));

		wmsJMenu.addSeparator();
		wmsJMenu.add(new JMenuItem(new
				JosmAction(tr("Blank Layer"), "blankmenu", tr("Open a blank WMS layer to load data from a file"), null, false) {
			public void actionPerformed(ActionEvent ev) {
				Main.main.addLayer(new WMSLayer());
			}
		}));
		setEnabledAll(menuEnabled);
	}

	/* add a default entry in case the URL does not yet exist */
	private static void setDefault(Boolean force, String name, String url)
	{
		String testurl = url.replaceAll("=", "_");
		wmsListDefault.put(name, url);

		if(force && !Main.pref.getBoolean("wmsplugin.default."+testurl))
		{
			Main.pref.put("wmsplugin.default."+testurl, true);
			int id = -1;
			for(WMSInfo i : wmsList)
			{
				if(url.equals(i.url))
					return;
				if(i.prefid > id)
					id = i.prefid;
			}
			WMSInfo newinfo = new WMSInfo(name, url, id+1);
			newinfo.save();
			wmsList.add(newinfo);
		}
	}

	public static Grabber getGrabber(MapView mv, WMSLayer layer){
		if(layer.baseURL.startsWith("html:"))
			return new HTMLGrabber(mv, layer, cache);
		else
			return new WMSGrabber(mv, layer, cache);
	}

	private static void setEnabledAll(boolean isEnabled) {
		for(int i=0; i < wmsJMenu.getItemCount(); i++) {
			JMenuItem item = wmsJMenu.getItem(i);

			if(item != null) item.setEnabled(isEnabled);
		}
		menuEnabled = isEnabled;
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (oldFrame==null && newFrame!=null) {
			setEnabledAll(true);
			Main.map.addMapMode(new IconToggleButton
					(new WMSAdjustAction(Main.map)));
		} else if (oldFrame!=null && newFrame==null ) {
			setEnabledAll(false);
		}
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		return new WMSPreferenceEditor();
	}

	static public String getPrefsPath()
	{
		return Main.pref.getPluginsDirectory().getPath() + "/wmsplugin/";
	}
}
