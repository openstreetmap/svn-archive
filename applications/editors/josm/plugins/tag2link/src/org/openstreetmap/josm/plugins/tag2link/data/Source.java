package org.openstreetmap.josm.plugins.tag2link.data;

import java.util.ArrayList;
import java.util.Collection;

public class Source {
    public final String name;
    public final Collection<Rule> rules = new ArrayList<Rule>();

    public Source(String name) {
		this.name = name;
	}
}
