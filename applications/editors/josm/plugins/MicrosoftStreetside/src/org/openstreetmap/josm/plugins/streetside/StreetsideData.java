// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.commons.jcs.access.CacheAccess;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.streetside.cache.CacheUtils;
import org.openstreetmap.josm.plugins.streetside.cache.Caches;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideMainDialog;
import org.openstreetmap.josm.plugins.streetside.gui.StreetsideViewerDialog;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.ImageInfoPanel;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;

/**
 * Database class for all the {@link StreetsideAbstractImage} objects.
 *
 * @author nokutu
 * @author renerr18 (extended for Streetside)
 * @see StreetsideAbstractImage
 * @see StreetsideSequence
 */
public class StreetsideData {
  private final Set<StreetsideAbstractImage> images = ConcurrentHashMap.newKeySet();
  /**
   * The image currently selected, this is the one being shown.
   */
  private StreetsideAbstractImage selectedImage;
  /**
   * The image under the cursor.
   */
  private StreetsideAbstractImage highlightedImage;
  /**
   * All the images selected, can be more than one.
   */
  private final Set<StreetsideAbstractImage> multiSelectedImages = ConcurrentHashMap.newKeySet();
  /**
   * Listeners of the class.
   */
  private final List<StreetsideDataListener> listeners = new CopyOnWriteArrayList<>();
  /**
   * The bounds of the areas for which the pictures have been downloaded.
   */
  private final List<Bounds> bounds;

  /**
   * Creates a new object and adds the initial set of listeners.
   */
  protected StreetsideData() {
    selectedImage = null;
    bounds = new CopyOnWriteArrayList<>();

  // Adds the basic set of listeners.
  Arrays.stream(StreetsidePlugin.getStreetsideDataListeners()).forEach(this::addListener);
      addListener(StreetsideViewerDialog.getInstance().getStreetsideViewerPanel());
      addListener(StreetsideMainDialog.getInstance());
      addListener(ImageInfoPanel.getInstance());
  }

  /**
   * Adds an StreetsideImage to the object, and then repaints mapView.
   *
   * @param image The image to be added.
   */
  public void add(StreetsideAbstractImage image) {
    add(image, true);
  }

  /**
   * Adds a StreetsideImage to the object, but doesn't repaint mapView. This is
   * needed for concurrency.
   *
   * @param image  The image to be added.
   * @param update Whether the map must be updated or not
   *        (updates are currently unsupported by Streetside).
   */
  public void add(StreetsideAbstractImage image, boolean update) {
    	images.add(image);
    	if (update) {
    		StreetsideLayer.invalidateInstance();
    	}
    	fireImagesAdded();
  }

  /**
   * Adds a set of StreetsideImages to the object, and then repaints mapView.
   *
   * @param images The set of images to be added.
   */
  public void addAll(Collection<? extends StreetsideAbstractImage> images) {
    addAll(images, true);
  }

  /**
   * Adds a set of {link StreetsideAbstractImage} objects to this object.
   *
   * @param newImages The set of images to be added.
   * @param update Whether the map must be updated or not.
   */
  public void addAll(Collection<? extends StreetsideAbstractImage> newImages, boolean update) {
    images.addAll(newImages);
    if (update) {
      StreetsideLayer.invalidateInstance();
    }
    fireImagesAdded();
  }

 /**
   * Adds a new listener.
   *
   * @param lis Listener to be added.
   */
  public final void addListener(final StreetsideDataListener lis) {
    listeners.add(lis);
  }

  /**
   * Adds a {@link StreetsideImage} object to the list of selected images, (when
   * ctrl + click)
   *
   * @param image The {@link StreetsideImage} object to be added.
   */
  public void addMultiSelectedImage(final StreetsideAbstractImage image) {
    if (!multiSelectedImages.contains(image)) {
      if (getSelectedImage() == null) {
        this.setSelectedImage(image);
      } else {
        multiSelectedImages.add(image);
      }
    }
    StreetsideLayer.invalidateInstance();
  }

  /**
   * Adds a set of {@code StreetsideAbstractImage} objects to the list of
   * selected images.
   *
   * @param images A {@link Collection} object containing the set of images to be added.
   */
  public void addMultiSelectedImage(Collection<StreetsideAbstractImage> images) {
    images.stream().filter(image -> !multiSelectedImages.contains(image)).forEach(image -> {
      if (getSelectedImage() == null) {
        this.setSelectedImage(image);
      } else {
        multiSelectedImages.add(image);
      }
    });
    StreetsideLayer.invalidateInstance();
  }

  public List<Bounds> getBounds() {
    return bounds;
  }

  /**
   * Removes a listener.
   *
   * @param lis Listener to be removed.
   */
  public void removeListener(StreetsideDataListener lis) {
    listeners.remove(lis);
  }

  /**
   * Highlights the image under the cursor.
   *
   * @param image The image under the cursor.
   */
  public void setHighlightedImage(StreetsideAbstractImage image) {
    highlightedImage = image;
  }

  /**
   * Returns the image under the mouse cursor.
   *
   * @return The image under the mouse cursor.
   */
  public StreetsideAbstractImage getHighlightedImage() {
    return highlightedImage;
  }

  /**
   * Returns a Set containing all images.
   *
   * @return A Set object containing all images.
   */
  public Set<StreetsideAbstractImage> getImages() {
    return images;
  }

  /**
   * Returns a Set of all sequences, that the images are part of.
   * @return all sequences that are contained in the Streetside data
   */
  public Set<StreetsideSequence> getSequences() {
    return images.stream().map(StreetsideAbstractImage::getSequence).collect(Collectors.toSet());
  }

  /**
   * Returns the StreetsideImage object that is currently selected.
   *
   * @return The selected StreetsideImage object.
   */
  public StreetsideAbstractImage getSelectedImage() {
    return selectedImage;
  }

  private void fireImagesAdded() {
    listeners.stream().filter(Objects::nonNull).forEach(StreetsideDataListener::imagesAdded);
  }

  /**
   * If the selected StreetsideImage is part of a StreetsideSequence then the
   * following visible StreetsideImage is selected. In case there is none, does
   * nothing.
   *
   * @throws IllegalStateException if the selected image is null or the selected image doesn't
   *                               belong to a sequence.
   */
  public void selectNext() {
    selectNext(StreetsideProperties.MOVE_TO_IMG.get());
  }

  /**
   * If the selected StreetsideImage is part of a StreetsideSequence then the
   * following visible StreetsideImage is selected. In case there is none, does
   * nothing.
   *
   * @param moveToPicture True if the view must me moved to the next picture.
   * @throws IllegalStateException if the selected image is null or the selected image doesn't
   *                               belong to a sequence.
   */
  public void selectNext(boolean moveToPicture) {
    StreetsideAbstractImage tempImage = selectedImage;
    if (selectedImage != null && selectedImage.getSequence() != null) {
      while (tempImage.next() != null) {
        tempImage = tempImage.next();
        if (tempImage.isVisible()) {
          setSelectedImage(tempImage, moveToPicture);
          break;
        }
      }
    }
  }

  /**
   * If the selected StreetsideImage is part of a StreetsideSequence then the
   * previous visible StreetsideImage is selected. In case there is none, does
   * nothing.
   *
   * @throws IllegalStateException if the selected image is null or the selected image doesn't
   *                               belong to a sequence.
   */
  public void selectPrevious() {
    selectPrevious(StreetsideProperties.MOVE_TO_IMG.get());
  }

  /**
   * If the selected StreetsideImage is part of a StreetsideSequence then the
   * previous visible StreetsideImage is selected. In case there is none, does
   * nothing. * @throws IllegalStateException if the selected image is null or
   * the selected image doesn't belong to a sequence.
   *
   * @param moveToPicture True if the view must me moved to the previous picture.
   * @throws IllegalStateException if the selected image is null or the selected image doesn't
   *                               belong to a sequence.
   */
  public void selectPrevious(boolean moveToPicture) {
    if (selectedImage != null && selectedImage.getSequence() != null) {
      StreetsideAbstractImage tempImage = selectedImage;
      while (tempImage.previous() != null) {
        tempImage = tempImage.previous();
        if (tempImage.isVisible()) {
          setSelectedImage(tempImage, moveToPicture);
          break;
        }
      }
    }
  }

  /**
   * Selects a new image.If the user does ctrl + click, this isn't triggered.
   *
   * @param image The StreetsideImage which is going to be selected
   */
  public void setSelectedImage(StreetsideAbstractImage image) {
    setSelectedImage(image, false);
  }

  /**
   * Selects a new image.If the user does ctrl+click, this isn't triggered. You
   * can choose whether to center the view on the new image or not.
   *
   * @param image The {@link StreetsideImage} which is going to be selected.
   * @param zoom  True if the view must be centered on the image; false otherwise.
   */
  public void setSelectedImage(StreetsideAbstractImage image, boolean zoom) {
    StreetsideAbstractImage oldImage = selectedImage;
    selectedImage = image;
    multiSelectedImages.clear();
    final MapView mv = StreetsidePlugin.getMapView();
    if (image != null) {
      multiSelectedImages.add(image);
      if (mv != null && image instanceof StreetsideImage) {
        StreetsideImage streetsideImage = (StreetsideImage) image;

        // Downloading thumbnails of surrounding pictures.
        downloadSurroundingImages(streetsideImage);
      }
    }
    if (mv != null && zoom && selectedImage != null) {
      mv.zoomTo(selectedImage.getMovingLatLon());
    }
    fireSelectedImageChanged(oldImage, selectedImage);
    StreetsideLayer.invalidateInstance();
  }

  /**
   * Downloads surrounding images of this mapillary image in background threads
   * @param streetsideImage the image for which the surrounding images should be downloaded
   */
  private static void downloadSurroundingImages (StreetsideImage streetsideImage) {
    MainApplication.worker.execute(() -> {
      final int prefetchCount = StreetsideProperties.PRE_FETCH_IMAGE_COUNT.get();
      CacheAccess <String, BufferedImageCacheEntry> imageCache = Caches.ImageCache.getInstance().getCache();

      StreetsideAbstractImage nextImage = streetsideImage.next();
      StreetsideAbstractImage prevImage = streetsideImage.previous();

      for (int i = 0; i < prefetchCount; i++) {
        if (nextImage != null) {
          if (nextImage instanceof StreetsideImage &&
            imageCache.get(((StreetsideImage) nextImage).getId()) == null) {
            CacheUtils.downloadPicture((StreetsideImage) nextImage);
          }
          nextImage = nextImage.next();
        }
        if (prevImage != null) {
          if (prevImage instanceof StreetsideImage &&
            imageCache.get(((StreetsideImage) prevImage).getId()) == null) {
            CacheUtils.downloadPicture((StreetsideImage) prevImage);
          }
          prevImage = prevImage.previous();
        }
      }
    });
  }

  /**
   * Downloads surrounding images of this mapillary image in background threads
   * @param streetsideImage the image for which the surrounding images should be downloaded
   */
  public static void downloadSurroundingCubemaps(StreetsideImage streetsideImage) {
      MainApplication.worker.execute(() -> {
        final int prefetchCount = StreetsideProperties.PRE_FETCH_IMAGE_COUNT.get();
        CacheAccess<String, BufferedImageCacheEntry> imageCache = Caches.ImageCache.getInstance().getCache();

        StreetsideAbstractImage nextImage = streetsideImage.next();
        StreetsideAbstractImage prevImage = streetsideImage.previous();

        for (int i = 0; i < prefetchCount; i++) {
          if (nextImage != null) {
            if (nextImage instanceof StreetsideImage && imageCache.get(((StreetsideImage) nextImage).getId()) == null) {
              CacheUtils.downloadCubemap((StreetsideImage) nextImage);
            }
            nextImage = nextImage.next();
          }
          if (prevImage != null) {
            if (prevImage instanceof StreetsideImage && imageCache.get(((StreetsideImage) prevImage).getId()) == null) {
              CacheUtils.downloadCubemap((StreetsideImage) prevImage);
            }
            prevImage = prevImage.previous();
          }
        }
      });
  }

  private void fireSelectedImageChanged(StreetsideAbstractImage oldImage, StreetsideAbstractImage newImage) {
	listeners.stream().filter(Objects::nonNull).forEach(lis -> lis.selectedImageChanged(oldImage, newImage));
  }

  /**
   * Returns a List containing all {@code StreetsideAbstractImage} objects
   * selected with ctrl + click.
   *
   * @return A List object containing all the images selected.
   */
  public Set<StreetsideAbstractImage> getMultiSelectedImages() {
    return multiSelectedImages;
  }

  /**
   * Sets a new {@link Collection} object as the used set of images.
   * Any images that are already present, are removed.
   *
   * @param newImages the new image list (previously set images are completely replaced)
   */
  public void setImages(Collection<StreetsideAbstractImage> newImages) {
    synchronized (this) {
      images.clear();
      images.addAll(newImages);
    }
  }
}