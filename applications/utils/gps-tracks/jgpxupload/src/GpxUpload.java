import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 */

/**
 * @author cdaller
 *
 */
public class GpxUpload {
    public static final String API_VERSION = "0.4";
    private static final int BUFFER_SIZE = 65535;
    
    public GpxUpload() {
        
    }
    
    public void upload(String username, String password, String description, String tags, File gpxFile) throws IOException {
        System.err.println("uploading " + gpxFile.getAbsolutePath() + " to openstreetmap.org");
        String gpxName = gpxFile.getName();
        try {
            String urlGpxName = URLEncoder.encode(gpxName.replaceAll("\\.","_"), "UTF-8");
            String urlDesc = URLEncoder.encode(description.replaceAll("\\.","_"), "UTF-8");
            String urlTags = URLEncoder.encode(tags.replaceAll("\\.","_"), "UTF-8");
            URL url = new URL("http://www.openstreetmap.org/api/" 
                + API_VERSION 
                + "/gpx/create"
                + "/" + urlGpxName
                + "/" + urlDesc
                + "/" + urlTags);
            System.err.println("url: " + url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(15000);
            con.setRequestMethod("PUT");
            con.setDoOutput(true);
            con.addRequestProperty("Authorization", "Basic "+Base64.encode(username+":"+password));
            con.connect();
            OutputStream out = con.getOutputStream();
            //  copy file to out:
            byte[] buffer = new byte[BUFFER_SIZE];
            int fileLen = (int)gpxFile.length();
            int read;
            int sumread = 0;
            InputStream in = new BufferedInputStream(new FileInputStream(gpxFile));
            System.err.println("Transferring data to server");
            while((read = in.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
                out.flush();
                sumread += read;
//                System.out.print("Transferred " + ((1.0 * sumread / fileLen) * 100) + "%                                \r");
            }
            in.close();
            out.flush();
            
            int retCode = con.getResponseCode();
            String retMsg = con.getResponseMessage();
            System.err.println("\nreturn code: "+retCode + " " + retMsg);
            if (retCode != 200) {
                // Look for a detailed error message from the server
                if (con.getHeaderField("Error") != null)
                    retMsg += "\n" + con.getHeaderField("Error");
                con.disconnect();
                throw new RuntimeException(retCode+" "+retMsg);
            }
            out.close();
            con.disconnect();
            System.out.println(gpxFile.getAbsolutePath());
        } catch(UnsupportedEncodingException ignore) {
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }     
    }
    
    /**
     * Return the location of the user defined preferences file
     */
    public static String getPreferencesDir() {
        if (System.getenv("APPDATA") != null)
            return System.getenv("APPDATA")+"/JOSM/";
        return System.getProperty("user.home")+"/.josm/";
    }

    
    public static void main(String[] args) {
        if(args.length < 3) {
            printHelp();
            return;
        }
        String description = args[0];
        String tags = args[1];
        List<File> files = new ArrayList<File>();
        File file;
        for(int index = 2; index < args.length; ++index) {
            file = new File(args[index]);
            if(file.exists()) {
                files.add(file);
            } else {
                System.err.println("File " + file.getAbsolutePath() + " does not exist - will be ignored!");
            }
        }
        GpxUpload gpxUpload = new GpxUpload();
        String username = System.getProperty("username");
        String password = System.getProperty("password");

        if(username != null && username.length() > 0 && password != null) {
            System.err.println("using username and password from system properties");
        } else {
            File josmPropFile = new File(getPreferencesDir(), "preferences");
            if(josmPropFile.exists()) {
                System.err.println("using username and password from josm preferences");
                Properties josmProps = new Properties();
                try {
                    josmProps.load(new FileReader(josmPropFile));
                    username = josmProps.getProperty("osm-server.username");
                    password = josmProps.getProperty("osm-server.password");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(username == null || username.length() == 0 || password == null) {
            System.err.println("No Username/password given, cannot upload gpx files!");
        } else {
            try {
                for (File gpxFile : files) {
                    gpxUpload.upload(username, password, description, tags, gpxFile);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     */
    private static void printHelp() {
        System.out.println("Usage: java " + GpxUpload.class.getName() 
            + " <description> <tags> <files*>");
        System.out.println("Osm username and password can be defined as system properties" +
        		" by -Dusername=<username> and -Dpassword=<password> or if not given, josm's" +
        		"preference file is read.");
        System.out.println("Any messages are printed to stderror, only the filename that " +
        		"was sent successfully is printed to stdout, so you may use the output of this " +
        		"program in a pipe for other calls (like \"| xargs -i mv '{}' target_dir\"");
        
    }

}
