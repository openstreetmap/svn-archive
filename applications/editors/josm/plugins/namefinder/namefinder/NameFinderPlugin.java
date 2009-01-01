package namefinder;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.List;

import org.openstreetmap.josm.gui.download.DownloadSelection;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * Main class for the name finder plugin.
 *
 *
 * @author Frederik Ramm <frederik@remote.org>
 *
 */
public class NameFinderPlugin extends Plugin
{
    public NameFinderPlugin()
    {
    }

    @Override public void addDownloadSelection(List<DownloadSelection> list)
    {
        list.add(new PlaceSelection());
    }

}
