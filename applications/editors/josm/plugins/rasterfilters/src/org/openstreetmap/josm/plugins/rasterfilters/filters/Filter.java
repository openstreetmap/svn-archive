package org.openstreetmap.josm.plugins.rasterfilters.filters;

import java.awt.image.BufferedImage;
import java.rmi.server.UID;

import javax.json.JsonObject;

public interface Filter {

	public JsonObject changeFilterState(JsonObject filterState);

	public BufferedImage applyFilter(BufferedImage img);

	public void setId(UID id);

	public UID getId();
}
