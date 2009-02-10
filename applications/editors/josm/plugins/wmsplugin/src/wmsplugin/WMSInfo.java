package wmsplugin;

import org.openstreetmap.josm.Main;

/**
 * Class that stores info about a WMS server.
 *
 * @author Frederik Ramm <frederik@remote.org>
 */
public class WMSInfo implements Comparable<WMSInfo> {

    String name;
    String url;
    int prefid;

    public WMSInfo(String name, String url, int prefid) {
        this.name=name; this.url=url; this.prefid=prefid;
    }


    public void save() {
        Main.pref.put("wmsplugin.url." + prefid + ".name", name);
        Main.pref.put("wmsplugin.url." + prefid + ".url", url);
    }
    public int compareTo(WMSInfo c)
    {
        WMSInfo in = (WMSInfo)c;
        Integer i = name.compareTo(in.name);
        if(i == 0)
            i = url.compareTo(in.url);
        if(i == 0)
            i = prefid-in.prefid;
        return i;
    }
}
