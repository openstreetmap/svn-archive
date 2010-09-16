package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.remotecontrol.PermissionPrefWithDefault;
import org.openstreetmap.josm.plugins.remotecontrol.RequestHandler;
import org.openstreetmap.josm.plugins.remotecontrol.RequestHandlerErrorException;

public class WMSRemoteHandler extends RequestHandler {

    public static final String command = "wms";

    @Override
    public String getPermissionMessage() {
        return tr("Remote Control has been asked to load a WMS layer from the following URL:") +
        "<br>" + args.get("url");
    }

    @Override
    public PermissionPrefWithDefault getPermissionPref()
    {
        return new PermissionPrefWithDefault(
                "wmsplugin.remotecontrol",
                true,
        "RemoteControl: WMS forbidden by preferences");
    }

    @Override
    protected String[] getMandatoryParams()
    {
        return new String[] { "url" };
    }

    @Override
    protected void handleRequest() throws RequestHandlerErrorException {
        String url = args.get("url");
        String title = args.get("title");
        if((title == null) || (title.length() == 0))
        {
            title = tr("Remote WMS");
        }
        String cookies = args.get("cookies");
        WMSLayer wmsLayer = new WMSLayer(new WMSInfo(title, url, cookies));
        Main.main.addLayer(wmsLayer);

    }

    @Override
    public void parseArgs() {
        StringTokenizer st = new StringTokenizer(request, "&?");
        HashMap<String, String> args = new HashMap<String, String>();
        // skip first element which is the command
        if(st.hasMoreTokens()) st.nextToken();
        while (st.hasMoreTokens()) {
            String param = st.nextToken();
            int eq = param.indexOf("=");
            if (eq > -1)
            {
                String key = param.substring(0, eq);
                /* "url=" terminates normal parameters
                 * and will be handled separately
                 */
                if("url".equals(key)) break;

                String value = param.substring(eq + 1);
                // urldecode all normal values
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                args.put(key,
                        value);
            }
        }
        // url as second or later parameter
        int urlpos = request.indexOf("&url=");
        // url as first (and only) parameter
        if(urlpos < 0) urlpos = request.indexOf("?url=");
        // url found?
        if(urlpos >= 0) {
            // URL value
            String value = request.substring(urlpos + 5);
            // allow skipping URL decoding with urldecode=false
            String urldecode = args.get("urldecode");
            if((urldecode == null) || (Boolean.valueOf(urldecode) == true))
            {
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            args.put("url", value);
        }
        this.args = args;
    }
}
