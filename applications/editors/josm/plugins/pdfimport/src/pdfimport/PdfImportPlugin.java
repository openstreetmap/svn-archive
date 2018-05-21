// License: GPL. For details, see LICENSE file.
package pdfimport;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * A plugin to import a PDF file.
 */

public class PdfImportPlugin extends Plugin {

	public enum GuiMode {
		Auto, Expert, Simple
	};

	public static class Preferences {

		public static String LoadDir;
		public static GuiMode guiMode;
		public static boolean MergeNodes;
		public static double MergeNodesValue;
		public static boolean RemoveSmall;
		public static double RemoveSmallValue;
		public static boolean RemoveLarge;
		public static double RemoveLargeValue;
		public static boolean RemoveParallel;
		public static double RemoveParallelValue;
		public static boolean LimitPath;
		public static int LimitPathValue;
		public static boolean LimitColor;
		public static String LimitColorValue;
		public static boolean DebugTags;
		public static boolean LayerClosed;
		public static boolean LayerSegment;
		public static boolean LayerAttribChange;
		public static boolean LayerOrtho;

		protected static int GuiCode;

		private static String PreFix;

		private Preferences() {
			return;
		}

		public static void reload(String Name) {
			Preferences.PreFix = Name + ".";
			Preferences.LoadDir = Config.getPref().get(Preferences.PreFix + "loadDir");
			Preferences.GuiCode = Config.getPref().getInt(Preferences.PreFix + "GuiCode",0 );
			switch (Preferences.GuiCode) {
			case 1:
				Preferences.guiMode = GuiMode.Expert;
			case 2:
				Preferences.guiMode = GuiMode.Simple;
			default:
				Preferences.guiMode = GuiMode.Expert;
			}
			Preferences.MergeNodes = Config.getPref().getBoolean(Preferences.PreFix + "MergeNodes");
			Preferences.MergeNodesValue = Config.getPref().getDouble(Preferences.PreFix + "MergeNodes.Value", 1e-3);
			Preferences.RemoveSmall = Config.getPref().getBoolean(Preferences.PreFix + "RemoveSmall");
			Preferences.RemoveSmallValue = Config.getPref().getDouble(Preferences.PreFix + "RemoveSmall.Value", 1);
			Preferences.RemoveLarge = Config.getPref().getBoolean(Preferences.PreFix + "RemoveLarge");
			Preferences.RemoveLargeValue = Config.getPref().getDouble(Preferences.PreFix + "RemoveLarge.Value", 10);
			Preferences.RemoveParallel = Config.getPref().getBoolean(Preferences.PreFix + "RemoveParallel");
			Preferences.RemoveParallelValue = Config.getPref().getDouble(Preferences.PreFix + "RemoveParalle.Value", 3);
			Preferences.LimitPath = Config.getPref().getBoolean(Preferences.PreFix + "LimitPath");
			Preferences.LimitPathValue = Config.getPref().getInt(Preferences.PreFix + "LimitPath.Value", 10000);
			Preferences.LimitColor = Config.getPref().getBoolean(Preferences.PreFix + "LimitColor");
			Preferences.LimitColorValue = Config.getPref().get(Preferences.PreFix + "LimitColor.Value","#000000");
			Preferences.DebugTags = Config.getPref().getBoolean(Preferences.PreFix + "DebugTags");
			Preferences.LayerClosed = Config.getPref().getBoolean(Preferences.PreFix + "LayerClosed");
			Preferences.LayerSegment = Config.getPref().getBoolean(Preferences.PreFix + "LayerSegment");
			Preferences.LayerAttribChange = Config.getPref().getBoolean(Preferences.PreFix + "LayerAttribChanges");
			Preferences.LayerOrtho = Config.getPref().getBoolean(Preferences.PreFix + "LayerOrtho");
		}
	}

	public PdfImportPlugin(PluginInformation info) {
		super(info);
		MainMenu.add(MainApplication.getMenu().imagerySubMenu, new PdfImportAction());
		Preferences.reload(this.getPluginInformation().name);
	}

}
