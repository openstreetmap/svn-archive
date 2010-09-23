package wmsplugin;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class that stores info about a WMS server.
 *
 * @author Frederik Ramm <frederik@remote.org>
 */
public class WMSInfo implements Comparable<WMSInfo> {

    String name;
    String url=null;
    String cookies = null;
    String eulaAcceptanceRequired = null;
    boolean html = false;
    double pixelPerDegree = 0.0;

    public WMSInfo(String name) {
        this.name=name;
    }

    public WMSInfo(String name, String url) {
        this.name=name;
        setURL(url);
    }

    public WMSInfo(String name, String url, String eulaAcceptanceRequired) {
        this.name=name;
        setURL(url);
        this.eulaAcceptanceRequired = eulaAcceptanceRequired;
    }

    public WMSInfo(String name, String url, String eulaAcceptanceRequired, String cookies) {
        this.name=name;
        setURL(url);
        this.cookies=cookies;
    }

    public WMSInfo(String name, String url, String cookies, double pixelPerDegree) {
        this.name=name;
        setURL(url);
        this.cookies=cookies;
        this.pixelPerDegree=pixelPerDegree;
    }

    public ArrayList<String> getInfoArray() {
        String e2 = null;
        String e3 = null;
        String e4 = null;
        if(url != null && !url.isEmpty()) e2 = getFullURL();
        if(cookies != null && !cookies.isEmpty()) e3 = cookies;
        if(pixelPerDegree != 0.0) e4 = String.valueOf(pixelPerDegree);
        if(e4 != null && e3 == null) e3 = "";
        if(e3 != null && e2 == null) e2 = "";

        ArrayList<String> res = new ArrayList<String>();
        res.add(name);
        if(e2 != null) res.add(e2);
        if(e3 != null) res.add(e3);
        if(e4 != null) res.add(e4);
        return res;
    }

    public WMSInfo(Collection<String> list) {
        ArrayList<String> array = new ArrayList<String>(list);
        this.name=array.get(0);
        if(array.size() >= 2) setURL(array.get(1));
        if(array.size() >= 3) this.cookies=array.get(2);
        if(array.size() >= 4) this.pixelPerDegree=Double.valueOf(array.get(3));
    }

    public WMSInfo(WMSInfo i) {
        this.name=i.name;
        this.url=i.url;
        this.cookies=i.cookies;
        this.html=i.html;
        this.pixelPerDegree=i.pixelPerDegree;
    }

    public int compareTo(WMSInfo in)
    {
        int i = name.compareTo(in.name);
        if(i == 0)
            i = url.compareTo(in.url);
        if(i == 0)
            i = Double.compare(pixelPerDegree, in.pixelPerDegree);
        return i;
    }

    public boolean equalsBaseValues(WMSInfo in)
    {
        return url.equals(in.url);
    }

    public void setPixelPerDegree(double ppd) {
        this.pixelPerDegree = ppd;
    }

    public void setURL(String url) {
        if(url.startsWith("html:")) {
            this.url = url.substring(5);
            html = true;
        } else {
            this.url = url;
        }
    }

    public String getFullURL() {
        return html ? "html:" + url : url;
    }

    public String getToolbarName()
    {
        String res = name;
        if(pixelPerDegree != 0.0)
            res += "#PPD="+pixelPerDegree;
        return res;
    }

    public String getMenuName()
    {
        String res = name;
        if(pixelPerDegree != 0.0)
            res += " ("+pixelPerDegree+")";
        return res;
    }
}
