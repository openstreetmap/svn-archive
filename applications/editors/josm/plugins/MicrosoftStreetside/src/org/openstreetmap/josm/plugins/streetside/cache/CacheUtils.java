// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cache;


import java.io.IOException;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.cache.CacheEntry;
import org.openstreetmap.josm.data.cache.CacheEntryAttributes;
import org.openstreetmap.josm.data.cache.ICachedLoaderListener;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.cubemap.CubemapBuilder;

/**
* Utility methods for working with cache.
*
* @author nokutu
*
*/
public final class CacheUtils {

final static Logger logger = Logger.getLogger(CacheUtils.class);

private static IgnoreDownload ignoreDownload = new IgnoreDownload();

/** Picture quality */
public enum PICTURE {
 /** Thumbnail quality picture (320 p) */
 THUMBNAIL,
 /** Full quality picture (2048 p) */
 FULL_IMAGE,
 /** Both of them */
 BOTH,
 /** Streetside cubemap */
 CUBEMAP
}

private CacheUtils() {
 // Private constructor to avoid instantiation
}

/**
* Downloads the the thumbnail and the full resolution picture of the given
* image. Does nothing if it is already in cache.
*
* @param img
*          The image whose picture is going to be downloaded.
*/
public static void downloadPicture(StreetsideImage img) {
 downloadPicture(img, PICTURE.BOTH);
}

/**
* Downloads the the thumbnail and the full resolution picture of the given
* image. Does nothing if it is already in cache.
*
* @param cm
*          The image whose picture is going to be downloaded.
*/
public static void downloadCubemap(StreetsideImage cm) {
	downloadPicture(cm, PICTURE.CUBEMAP);
}

/**
* Downloads the picture of the given image. Does nothing when it is already
* in cache.
*
* @param img
*          The image to be downloaded.
* @param pic
*          The picture type to be downloaded (full quality, thumbnail or
*          both.)
*/
public static void downloadPicture(StreetsideImage img, PICTURE pic) {
 switch (pic) {
   case BOTH:
     if (new StreetsideCache(img.getId(), StreetsideCache.Type.THUMBNAIL).get() == null)
       submit(img.getId(), StreetsideCache.Type.THUMBNAIL, ignoreDownload);
     if (new StreetsideCache(img.getId(), StreetsideCache.Type.FULL_IMAGE).get() == null)
       submit(img.getId(), StreetsideCache.Type.FULL_IMAGE, ignoreDownload);
     break;
   case THUMBNAIL:
     submit(img.getId(), StreetsideCache.Type.THUMBNAIL, ignoreDownload);
     break;
   case FULL_IMAGE:
     // not used (relic from Mapillary)
     break;
   case CUBEMAP:
	   if(img.getId()==null) {
		   logger.error("Download cancelled. Image id is null.");
	   } else {
		   CubemapBuilder.getInstance().downloadCubemapImages(img.getId());
	   }
	   break;
   default:
     submit(img.getId(), StreetsideCache.Type.FULL_IMAGE, ignoreDownload);
     break;
 }
}

/**
* Requests the picture with the given key and quality and uses the given
* listener.
*
* @param key
*          The key of the picture to be requested.
* @param type
*          The quality of the picture to be requested.
* @param lis
*          The listener that is going to receive the picture.
*/
public static void submit(String key, StreetsideCache.Type type,
   ICachedLoaderListener lis) {
 try {
   new StreetsideCache(key, type).submit(lis, false);
 } catch (IOException e) {
   logger.error(e);
 }
}

  private static class IgnoreDownload implements ICachedLoaderListener {

    @Override
    public void loadingFinished(CacheEntry arg0, CacheEntryAttributes arg1, LoadResult arg2) {
      // Ignore download
    }
  }
}