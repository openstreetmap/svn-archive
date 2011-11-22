package org.osmu.gosmore;

import java.io.Serializable;

public class Place implements Serializable {
	static final long serialVersionUID = 6618718668L;
      double lat, lon;
      int zoom, dir; // Currently dir is always 0
      String name;
}
