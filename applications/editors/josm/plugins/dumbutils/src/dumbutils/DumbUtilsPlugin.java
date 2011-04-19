package dumbutils;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DumbUtilsPlugin extends Plugin {
    JMenuItem replaceGeometry;
    JMenuItem tagBuffer;
    JMenuItem sourceTag;
    JMenuItem pasteRelations;
    JMenuItem alignWayNodes;

    public DumbUtilsPlugin(PluginInformation info) {
        super(info);
        Main.main.menu.toolsMenu.addSeparator();
        replaceGeometry = MainMenu.add(Main.main.menu.toolsMenu, new ReplaceGeometryAction());
        tagBuffer = MainMenu.add(Main.main.menu.toolsMenu, new TagBufferAction());
        sourceTag = MainMenu.add(Main.main.menu.toolsMenu, new TagSourceAction());
        pasteRelations = MainMenu.add(Main.main.menu.toolsMenu, new PasteRelationsAction());
//        alignWayNodes = MainMenu.add(Main.main.menu.toolsMenu, new AlignWayNodesAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        boolean enabled = newFrame != null;
        replaceGeometry.setEnabled(enabled);
        tagBuffer.setEnabled(enabled);
        sourceTag.setEnabled(enabled);
        pasteRelations.setEnabled(enabled);
//        alignWayNodes.setEnabled(enabled);
    }
}
