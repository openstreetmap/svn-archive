package org.openstreetmap.josm.plugins.rasterfilters.filters;

import java.awt.image.BufferedImage;
import java.rmi.server.UID;

import javax.json.JsonObject;
/**
 * The Filter interface is inherited by all filters which are implemented.
 *
 * This interface has four methods that should be overrided in
 *
 * implementation.
 *
 * @author Nipel-Crumple
 *
 */
public interface Filter {

	/**
	 * This method should take external fields values of filter's parameters
	 * which should be described in the meta-information. In other words, if you have 3
	 * controls of type 'linear_slider' and if state at least one of these
	 * controls has changed, you will get new filter state in the form of
	 * json object 'filterState', then parse it and
	 * store given parameters' values in class.
	 *
	 * @param filterState json that has information about current filter state
	 *
	 * @return json object 'filterState'
	 */
	public JsonObject changeFilterState(JsonObject filterState);

	/**
	 * This method processes given image and returns
	 * updated version of the image. Algorithm and implementation of
	 * this method depends on your needs and wishes.
	 *
	 * @param img image to process
	 *
	 * @return processed image
	 */
	public BufferedImage applyFilter(BufferedImage img);

	/**
	 * Every filter must have his own unique ID number.
	 * In case of rasterfilters plugin it ID is the type of UID.
	 *
	 * @param id sets value of ID field
	 */
	public void setId(UID id);

	/**
	 * Getter for filter's ID field.
	 *
	 * @return id of filter
	 */
	public UID getId();
}
