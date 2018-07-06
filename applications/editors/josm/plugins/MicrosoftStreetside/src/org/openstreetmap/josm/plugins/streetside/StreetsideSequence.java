// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openstreetmap.josm.plugins.streetside.model.UserProfile;

/**
 * Class that stores a sequence of {@link StreetsideAbstractImage} objects.
 *
 * @author nokutu
 * @see StreetsideAbstractImage
 */

public class StreetsideSequence {


  /**
   * Unique identifier. Used only for {@link StreetsideImage} sequences.
   */
  private String id;
  private UserProfile user;

  private double la;
  private double lo;

  /**
   * Epoch time when the sequence was created
   */
  private long cd;

  /**
   * The images in the sequence.
   */
  private List<StreetsideAbstractImage> images;

  public StreetsideSequence(String id, Long ca) {
	this.id = id;
    cd = ca;
    images = new CopyOnWriteArrayList<>();
  }

  public StreetsideSequence(String id, double la, double lo) {
    this.id = id;
    this.la = la;
    this.lo = lo;
    images = new CopyOnWriteArrayList<>();
  }

  /**
   * No argument constructor for StreetsideSequence - necessary for JSON serialization
   */
  public StreetsideSequence() {
	  images = new CopyOnWriteArrayList<>();
  }

  public StreetsideSequence(String id, double la, double lo, long ca) {
	this.id = id;
	this.la = la;
	this.lo = lo;
	cd = ca;
	images = new CopyOnWriteArrayList<>();
}

public StreetsideSequence(String id) {
	this.id = id;
	images = new CopyOnWriteArrayList<>();
}

  /**
   * Adds a new {@link StreetsideAbstractImage} object to the database.
   *
   * @param image The {@link StreetsideAbstractImage} object to be added
   */
  public synchronized void add(StreetsideAbstractImage image) {
    images.add(image);
    image.setSequence(this);
  }

  /**
   * Adds a set of {@link StreetsideAbstractImage} objects to the database.
   *
   * @param images The set of {@link StreetsideAbstractImage} objects to be added.
   */
  public synchronized void add(final Collection<? extends StreetsideAbstractImage> images) {
    this.images.addAll(images);
    images.forEach(img -> img.setSequence(this));
  }

  /**
   * Returns the next {@link StreetsideAbstractImage} in the sequence of a given
   * {@link StreetsideAbstractImage} object.
   *
   * @param image The {@link StreetsideAbstractImage} object whose next image is
   * going to be returned.
   *
   * @return The next {@link StreetsideAbstractImage} object in the sequence.
   *
   * @throws IllegalArgumentException if the given {@link StreetsideAbstractImage} object doesn't belong
   * the this sequence.
   */
  public StreetsideAbstractImage next(StreetsideAbstractImage image) {
    int i = images.indexOf(image);
    if (i == -1) {
      throw new IllegalArgumentException();
    }
    if (i == images.size() - 1) {
      return null;
    }
    return images.get(i + 1);
  }

  /**
   * Returns the previous {@link StreetsideAbstractImage} in the sequence of a
   * given {@link StreetsideAbstractImage} object.
   *
   * @param image The {@link StreetsideAbstractImage} object whose previous image is
   * going to be returned.
   *
   * @return The previous {@link StreetsideAbstractImage} object in the sequence.
   *
   * @throws IllegalArgumentException if the given {@link StreetsideAbstractImage} object doesn't belong
   * the this sequence.
   */
  public StreetsideAbstractImage previous(StreetsideAbstractImage image) {
    int i = images.indexOf(image);
    if (i < 0) {
      throw new IllegalArgumentException();
    }
    if (i == 0) {
      return null;
    }
    return images.get(i - 1);
  }

  /**
   * Removes a {@link StreetsideAbstractImage} object from the database.
   *
   * @param image The {@link StreetsideAbstractImage} object to be removed.
   */
  public void remove(StreetsideAbstractImage image) {
    images.remove(image);
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the la
   */
  public double getLa() {
    return la;
  }

  /**
   * @param la the la to set
   */
  public void setLa(double la) {
    this.la = la;
  }

  /**
   * @return the lo
   */
  public double getLo() {
    return lo;
  }

  /**
   * @param lo the lo to set
   */
  public void setLo(double lo) {
    this.lo = lo;
  }

  /**
   * Returns the Epoch time when the sequence was captured.
   *
   * Negative values mean, no value is set.
   *
   * @return A long containing the Epoch time when the sequence was captured.
   */
  public long getCd() {
    return cd;
  }

  /**
   * Returns all {@link StreetsideAbstractImage} objects contained by this
   * object.
   *
   * @return A {@link List} object containing all the
   * {@link StreetsideAbstractImage} objects that are part of the
   * sequence.
   */
  public List<StreetsideAbstractImage> getImages() {
    return images;
  }

  /**
   * Returns the unique identifier of the sequence.
   *
   * @return A {@code String} containing the unique identifier of the sequence.
   * null means that the sequence has been created locally for imported
   * images.
   */
  public String getId() {
    return id;
  }

  public UserProfile getUser() {
	return user;
  }
}