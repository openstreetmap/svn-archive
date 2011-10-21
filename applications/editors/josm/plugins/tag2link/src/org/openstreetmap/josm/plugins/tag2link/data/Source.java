package org.openstreetmap.josm.plugins.tag2link.data;

import java.util.ArrayList;
import java.util.Collection;

public class Source {
    public String name;
    public final Collection<Rule> rules = new ArrayList<Rule>();
}
