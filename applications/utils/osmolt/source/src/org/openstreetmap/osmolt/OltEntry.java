package org.openstreetmap.osmolt;

public class OltEntry {
  public String name = "";

  public String url = "";

  public String imgUrl = "";

  public OltEntry(String name, String url, String imgUrl) {
    this.name = name;
    this.url = url;
    this.imgUrl = imgUrl;
  }
}
