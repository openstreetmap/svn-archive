package org.openstreetmap.osmolt;

import java.util.Iterator;
import java.util.List;

import javax.swing.JRadioButton;

import org.jdom.Element;

public class Filter {
  
  public enum Logical {
    l_no, l_and, l_or
  };
  
  public enum RestrictionType {
    equal, amongothers, regex, anything
  };
  
  static Boolean matches(final Element element, final Element filter) {
    
    if (filter.getName() == "restriction") {
      return profRestriction(element, filter);
    }
    if (filter.getName() == "logical") {
      final Logical logical = makeLogicalfromString(filter.getAttributeValue("type"));
      switch (logical) {
        case l_and: {
          Boolean match = true;
          final List children = filter.getChildren();
          for (final Iterator iter = children.iterator(); iter.hasNext();) {
            final Element child = (Element) iter.next();
            match &= matches(element, child);
          }
          if ("true".equals(filter.getAttributeValue("negation")))
            match = !match;
          return match;
        }
        case l_or: {
          Boolean match = false;
          final List children = filter.getChildren();
          for (final Iterator iter = children.iterator(); iter.hasNext();) {
            final Element child = (Element) iter.next();
            match |= matches(element, child);
          }
          if ("true".equals(filter.getAttributeValue("negation")))
            match = !match;
          return match;
        }
      }
    }
    return false;
  }
  
  static Boolean profRestriction(final Element element, final Element filter) {
    final List tags = element.getChildren("tag");
    final String filterKey = filter.getAttributeValue("osmKey");
    final String filterValue = filter.getAttributeValue("osmValue");
    final RestrictionType filterType = makeRestrictionTypefromString(filter.getAttributeValue("type"));
    
    Boolean match = false;
    for (final Iterator iter = tags.iterator(); iter.hasNext();) {
      final Element tag = (Element) iter.next();
      // regexpr erlaubt
      String key = tag.getAttributeValue("k");
      String value = tag.getAttributeValue("v");
      
      // if ((Osmolt.debug) && false)
      // System.out.println(filterKey + " " + key + " " + filterValue + " " +
      // value);
      switch (filterType) {
        case equal:
          if (filterKey.equals(key) && filterValue.equals(value))
            match = true;
          break;
        case amongothers:
          if (filterKey.equals(key)) {
            String[] elem = value.split(",|;");
            for (int i = 0; i < elem.length; i++) {
              //System.out.println(elem[i]);
              // trim: remove whitspaces at begin and end
              if (elem[i].trim().equals(filterValue))
                match = true;
            }            
          }
          break;
        
        case regex:
          if (key.matches(filterKey) && value.matches(filterValue))
            match = true;
          break;
        
        case anything:
          if (filterKey.equals(key))
            match = true;
          break;
        default:
          break;
      }
    }
    if ("true".equals(filter.getAttributeValue("negation")))
      match = !match;
    return match;
  }
  
  static public Logical makeLogicalfromString(final String s) {
    
    if (s.equals("and"))
      return Logical.l_and;
    if (s.equals("or"))
      return Logical.l_or;
    return Logical.l_no;
  }
  
  static public RestrictionType makeRestrictionTypefromString(final String s) {
    
    if (s.equals("equal"))
      return RestrictionType.equal;
    if (s.equals("amongothers"))
      return RestrictionType.amongothers;
    if (s.equals("regex"))
      return RestrictionType.regex;
    if (s.equals("anything"))
      return RestrictionType.anything;
    return RestrictionType.equal;
  }
  
}
