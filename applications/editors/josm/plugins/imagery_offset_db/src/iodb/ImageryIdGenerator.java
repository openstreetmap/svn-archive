package iodb;

import java.util.*;
import org.openstreetmap.josm.data.imagery.ImageryInfo.ImageryType;

/**
 * Generate unique imagery identifier based on its type and URL.
 *
 * @author Zverik
 * @license WTFPL
 */
public class ImageryIdGenerator {

    public static String getImageryID( String url, ImageryType type ) {
        if( url == null )
            return null;

        // predefined layers
        if( ImageryType.BING.equals(type) || url.contains("tiles.virtualearth.net") )
            return "bing";

        if( ImageryType.SCANEX.equals(type) && url.toLowerCase().equals("irs") )
            return "scanex_irs";

        boolean isWMS = ImageryType.WMS.equals(type);

//        System.out.println(url);

        // Remove protocol
        int i = url.indexOf("://");
        url = url.substring(i + 3);

        // Split URL into address and query string
        i = url.indexOf('?');
        String query = "";
        if( i > 0 ) {
            query = url.substring(i);
            url = url.substring(0, i);
        }

        // Parse query parameters into a sorted map
        final Set<String> removeWMSParams = new TreeSet<String>(Arrays.asList(new String[] {
                    "srs", "width", "height", "bbox", "service", "request", "version", "format", "styles", "transparent"
                }));
        Map<String, String> qparams = new TreeMap<String, String>();
        String[] qparamsStr = query.length() > 1 ? query.substring(1).split("&") : new String[0];
        for( String param : qparamsStr ) {
            String[] kv = param.split("=");
            kv[0] = kv[0].toLowerCase();
            // WMS: if this is WMS, remove all parameters except map and layers
            if( isWMS && removeWMSParams.contains(kv[0]) )
                continue;
            // TMS: skip parameters with variable values
            if( kv.length > 1 && kv[1].indexOf('{') >= 0 && kv[1].indexOf('}') > 0 )
                continue;
            qparams.put(kv[0].toLowerCase(), kv.length > 1 ? kv[1] : null);
        }

        // Reconstruct query parameters
        StringBuilder sb = new StringBuilder();
        for( String qk : qparams.keySet() ) {
            if( sb.length() > 0 )
                sb.append('&');
            else if( query.length() > 0 )
                sb.append('?');
            sb.append(qk).append('=').append(qparams.get(qk));
        }
        query = sb.toString();

        // TMS: remove /{zoom} and /{y}.png parts
        url = url.replaceAll("\\/\\{[^}]+\\}(?:\\.\\w+)?", "");
        // TMS: remove variable parts
        url = url.replaceAll("\\{[^}]+\\}", "");
        while( url.contains("..") )
            url = url.replace("..", ".");
        if( url.startsWith(".") )
            url = url.substring(1);

//        System.out.println("-> " + url + query);
        return url + query;
    }
}
