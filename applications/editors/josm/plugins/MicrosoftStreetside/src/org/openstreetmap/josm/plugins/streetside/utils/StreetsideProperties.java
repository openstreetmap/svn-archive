// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import java.awt.Color;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.DoubleProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.ImageInfoPanel;
import org.openstreetmap.josm.plugins.streetside.gui.imageinfo.StreetsideViewerPanel;
import org.openstreetmap.josm.plugins.streetside.io.download.StreetsideDownloader;

public final class StreetsideProperties {
  public static final BooleanProperty DELETE_AFTER_UPLOAD = new BooleanProperty("streetside.delete-after-upload", true);
  public static final BooleanProperty DEVELOPER = new BooleanProperty("streetside.developer", false);
  public static final BooleanProperty DISPLAY_HOUR = new BooleanProperty("streetside.display-hour", true);
  public static final BooleanProperty HOVER_ENABLED = new BooleanProperty("streetside.hover-enabled", true);
  public static final BooleanProperty MOVE_TO_IMG = new BooleanProperty("streetside.move-to-picture", true);
  public static final BooleanProperty TIME_FORMAT_24 = new BooleanProperty("streetside.format-24", false);
  public static final BooleanProperty IMAGE_LINK_TO_BLUR_EDITOR = new BooleanProperty("streetside.image-link-to-blur-editor", true);
  public static final BooleanProperty CUBEMAP_LINK_TO_BLUR_EDITOR = new BooleanProperty("streetside.cubemap-link-to-blur-editor", true);
  public static final IntegerProperty TILE_DOWNLOAD_THREAD_PAUSE_LEN_SEC = new IntegerProperty("streetside.tile-download-thread-pause-len-sec", 60);
  public static final BooleanProperty PREDOWNLOAD_CUBEMAPS = new BooleanProperty("streetside.predownload-cubemaps", false);
  public static final BooleanProperty DEBUGING_ENABLED = new BooleanProperty("streetside.debugging-enabled", true);
  /**
   * If false, all sequences that cross the download bounds are put completely into the StreetsideData object.
   * Otherwise only all images (!) inside the download bounds are added, the others are discarded.
   */
  public static final BooleanProperty CUT_OFF_SEQUENCES_AT_BOUNDS =
    new BooleanProperty("streetside.cut-off-sequences-at-bounds", false);
  public static final IntegerProperty MAPOBJECT_ICON_SIZE = new IntegerProperty("streetside.mapobjects.iconsize", 32);
  public static final IntegerProperty MAX_MAPOBJECTS = new IntegerProperty("streetside.mapobjects.maximum-number", 200);
  public static final BooleanProperty SHOW_DETECTED_SIGNS = new BooleanProperty("streetside.show-detected-signs", true);
  public static final BooleanProperty SHOW_HIGH_RES_STREETSIDE_IMAGERY = new BooleanProperty("streetside.show-high-res-streetside-imagery", true);

  /**
   * See {@code OsmDataLayer#PROPERTY_BACKGROUND_COLOR}
   */
  public static final NamedColorProperty BACKGROUND = new NamedColorProperty("background", Color.BLACK);
  /**
   * See {@code OsmDataLayer#PROPERTY_OUTSIDE_COLOR}
   */
  public static final NamedColorProperty OUTSIDE_DOWNLOADED_AREA = new NamedColorProperty("outside downloaded area", Color.YELLOW);

  public static final DoubleProperty MAX_DOWNLOAD_AREA = new DoubleProperty("streetside.max-download-area", 0.015);

  public static final IntegerProperty PICTURE_DRAG_BUTTON = new IntegerProperty("streetside.picture-drag-button", 3);
  public static final IntegerProperty PICTURE_OPTION_BUTTON = new IntegerProperty("streetside.picture-option-button", 2);
  public static final IntegerProperty PICTURE_ZOOM_BUTTON = new IntegerProperty("streetside.picture-zoom-button", 1);
  public static final IntegerProperty SEQUENCE_MAX_JUMP_DISTANCE =
    new IntegerProperty("streetside.sequence-max-jump-distance", 100);

  public static final StringProperty ACCESS_TOKEN = new StringProperty("streetside.access-token", null);
  public static final StringProperty DOWNLOAD_MODE =
    new StringProperty("streetside.download-mode", StreetsideDownloader.DOWNLOAD_MODE.DEFAULT.getPrefId());
  public static final StringProperty START_DIR =
    new StringProperty("streetside.start-directory", System.getProperty("user.home"));

  /**
   * The number of times the help popup for the {@link ImageInfoPanel} will be displayed.
   * But regardless of this number, the popup will only show up at most once between two (re)starts of JOSM.
   * Or opening the {@link ImageInfoPanel} immediately brings this number down to zero.
   */
  public static final IntegerProperty IMAGEINFO_HELP_COUNTDOWN =
    new IntegerProperty("streetside.imageInfo.helpDisplayedCountdown", 4);

  /**
   * The number of times the help popup for the {@link StreetsideViewerPanel} will be displayed.
   * But regardless of this number, the popup will only show up at most once between two (re)starts of JOSM.
   * Or opening the {@link StreetsideViewerPanel} immediately brings this number down to zero.
   */
  public static final IntegerProperty STREETSIDE_VIEWER_HELP_COUNTDOWN =
    new IntegerProperty("streetside.streetsideViewer.helpDisplayedCountdown", 4);

  /**
   * The number of images to be prefetched when a streetside image is selected
   */
  public static final IntegerProperty PRE_FETCH_IMAGE_COUNT = new IntegerProperty("streetside.prefetch-image-count", 2);

  /**
   * The number of images to be prefetched when a streetside image is selected
   */
  public static final IntegerProperty PRE_FETCH_CUBEMAP_COUNT = new IntegerProperty("streetside.prefetch-image-count", 2);


  private StreetsideProperties() {
    // Private constructor to avoid instantiation
  }
}
