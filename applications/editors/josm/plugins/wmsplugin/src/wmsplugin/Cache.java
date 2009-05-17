package wmsplugin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.*;

import org.openstreetmap.josm.Main;

public class Cache {
    final File dir;
    // Last 5 days
    private final long expire = Main.pref.getInteger("wmsplugin.cache.expire", 1000*60*60*24*5);
    // 40 MBytes
    private final long maxsize = Main.pref.getInteger("wmsplugin.cache.size", 1000*1000*40);
    private boolean disabled = false;
    // If the cache is full, we don't want to delete just one file
    private final int cleanUpThreshold = 20;
    // After how many file-writes do we want to check if the cache needs emptying?
    private final int cleanUpInterval = 5;

    Cache() {
        this(Main.pref.get("wmsplugin.cache.path", WMSPlugin.getPrefsPath() + "cache"));
    }

    Cache(String working_dir) {
        // Override default working directory
        this.dir = new File(working_dir);
        try {
            this.dir.mkdirs();
        } catch(Exception e) {
            // We probably won't be able to do anything anyway
            disabled = true;
        }

        if(expire <= 0 || maxsize <= 0)
            disabled = true;
    }

    public BufferedImage getImg(String ident) {
        if(disabled) return null;
        try {
            File img = getPath(ident);
            if(!img.exists()) {
                //System.out.println("Miss");
                return null;
            }
            if(img.lastModified() < (new Date().getTime() - expire)) {
                img.delete();
                //System.out.println("Miss");
                return null;
            }
            //System.out.println("Hit");
            return ImageIO.read(img);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        //System.out.println("Miss");
        return null;
    }

    public void saveImg(String ident, BufferedImage image) {
        if(disabled) return;
        try {
            ImageIO.write(image, "png", getPath(ident));
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        checkCleanUp();
    }

    public BufferedImage saveImg(String ident, BufferedImage image, boolean passThrough) {
        saveImg(ident, image);
        return image;
    }

    public void checkCleanUp() {
        // The Cache class isn't persistent in its current implementation,
        // therefore clean up on random intervals, but not every write
        if(new Random().nextInt(cleanUpInterval) == 0)
            cleanUp();
    }

    public void cleanUp() {
        if(disabled) return;

        TreeMap<Long, File> modtime = new TreeMap<Long, File>();
        long time = new Date().getTime() - expire;
        long dirsize = 0;

        for(File f : getFiles()) {
            if(f.lastModified() < time)
                f.delete();
            else {
                dirsize += f.length();
                modtime.put(f.lastModified(), f);
            }
        }

        if(dirsize < maxsize) return;

        Set keySet = modtime.keySet();
        Iterator it = keySet.iterator();
        int i=0;
        while (it.hasNext()) {
            i++;
            ((File)modtime.get(it.next())).delete();

            // Delete a couple of files, then check again
            if(i % cleanUpThreshold == 0 && getDirSize() < maxsize)
                return;
        }
    }

    public void deleteSmallFiles(int size) {
        if(disabled) return;
        for(File f : getFiles()) {
            if(f.length() < size)
                f.delete();
        }
    }

    private long getDirSize() {
        if(disabled) return -1;
        long dirsize = 0;

        for(File f : getFiles())
            dirsize += f.length();
        return dirsize;
    }

    private File[] getFiles() {
        if(disabled) return null;
        return dir.listFiles(
            new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().endsWith(".png");
                }
            }
        );
    }

    private static String getUniqueFilename(String ident) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            BigInteger number = new BigInteger(1, md.digest(ident.getBytes()));
            return number.toString(16);
        } catch(Exception e) {
            // Fall back. Remove unsuitable characters and some random ones to shrink down path length.
            // Limit it to 70 characters, that leaves about 190 for the path on Windows/NTFS
            ident = ident.replaceAll("[^a-zA-Z0-9]", "");
            ident = ident.replaceAll("[acegikmoqsuwy]", "");
            return ident.substring(ident.length() - 70);
        }
    }

    private File getPath(String ident) {
        return new File(dir, getUniqueFilename(ident) + ".png");
    }
}
