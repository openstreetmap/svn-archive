package org.openstreetmap.osmolt;

public class Grammar {
  final static String grammar = "	" +
      "<!ELEMENT MapFeatures (entry*)>\n" + "\n"
      + "	<!ELEMENT entry (filter,titel,description)>\n" + "\n"
      + " <!ELEMENT filter (logical)>\n"
      + " <!ELEMENT logical (restriction|logical)+>\n"
      + "	<!ELEMENT titel (#PCDATA|valueof|br)* >\n"
      + "	<!ELEMENT description (#PCDATA|valueof|br)* >\n" + "\n"
      + "	<!ELEMENT restriction EMPTY>\n" 
      + "	<!ELEMENT valueof EMPTY>\n"
      + "	<!ELEMENT br EMPTY>\n" + "\n" + "\n" 
      + " <!ATTLIST MapFeatures\n" 
      + "    output   CDATA    #IMPLIED\n"
      + "    bbox     CDATA    #IMPLIED\n"
      + "    version  CDATA    #IMPLIED\n"
      + " >\n" 
      + "	<!ATTLIST entry\n"
      + "	   filename		  CDATA    #REQUIRED\n"
      + "	   image		    CDATA    #REQUIRED\n" 
      + "	   name			    CDATA    #REQUIRED\n"
      + "	   imagesize	  CDATA    #REQUIRED\n"
      + "	   imageoffset	CDATA    #REQUIRED\n" + "	>\n" + "\n"
      + "	<!ATTLIST logical\n" 
      + "	   type       CDATA    #REQUIRED\n"
      + "	   negation		CDATA	   #IMPLIED\n"
      + "	>\n" 
      + "	<!ATTLIST restriction\n"
      + "	   osmKey	  	CDATA    #REQUIRED\n"
      + "    osmValue   CDATA    #REQUIRED\n"
      + "    type       CDATA    #IMPLIED\n"
      + "	   negation		CDATA	   #IMPLIED\n" + "	>\n" + "\n"
      + "	<!ATTLIST valueof\n" + "	   osmKey		CDATA    #REQUIRED\n" + "	>\n";

  public static String getGrammar() {
    return grammar;
  }
}