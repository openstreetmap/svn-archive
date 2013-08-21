package public_transport;

import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class PublicTransportPlugin extends Plugin {

  static JMenu jMenu;

  public PublicTransportPlugin(PluginInformation info)
  {
    super(info);
    refreshMenu();
  }

  public static void refreshMenu()
  {
    MainMenu menu = Main.main.menu;

    if (jMenu == null)
      jMenu = menu.addMenu(marktr("Public Transport"), KeyEvent.VK_COMMA, menu.getDefaultMenuPos(), "help");
    else
      jMenu.removeAll();

    jMenu.addSeparator();
    jMenu.add(new JMenuItem(new StopImporterAction()));
    jMenu.add(new JMenuItem(new RoutePatternAction()));
    jMenu.add(new JMenuItem(new GTFSImporterAction()));
    setEnabledAll(true);
  }

  private static void setEnabledAll(boolean isEnabled)
  {
    for(int i=0; i < jMenu.getItemCount(); i++) {
      JMenuItem item = jMenu.getItem(i);

      if(item != null) item.setEnabled(isEnabled);
    }
  }
}
