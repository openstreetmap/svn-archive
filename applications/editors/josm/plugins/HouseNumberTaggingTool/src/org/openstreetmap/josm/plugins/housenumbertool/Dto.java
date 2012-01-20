package org.openstreetmap.josm.plugins.housenumbertool;

import java.io.Serializable;

/**
 * @author Oliver Raupach 18.01.2012 <http://www.oliver-raupach.de>
 */
public class Dto implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = -4025800761473341695L;

   private boolean saveBuilding = true;
   private boolean saveCountry = true;
   private boolean saveState = true;
   private boolean saveCity = true;
   private boolean savePostcode = true;
   private boolean saveStreet = true;
   private boolean saveHousenumber = true;

   private String country;
   private String state;
   private String city;
   private String postcode;
   private String street;
   private String housenumber;

   public boolean isSaveBuilding()
   {
      return saveBuilding;
   }

   public void setSaveBuilding(boolean saveBuilding)
   {
      this.saveBuilding = saveBuilding;
   }

   public boolean isSaveCountry()
   {
      return saveCountry;
   }

   public void setSaveCountry(boolean saveCountry)
   {
      this.saveCountry = saveCountry;
   }

   public boolean isSaveCity()
   {
      return saveCity;
   }

   public void setSaveCity(boolean saveCity)
   {
      this.saveCity = saveCity;
   }

   public boolean isSavePostcode()
   {
      return savePostcode;
   }

   public void setSavePostcode(boolean savePostcode)
   {
      this.savePostcode = savePostcode;
   }

   public boolean isSaveStreet()
   {
      return saveStreet;
   }

   public void setSaveStreet(boolean saveStreet)
   {
      this.saveStreet = saveStreet;
   }

   public boolean isSaveHousenumber()
   {
      return saveHousenumber;
   }

   public void setSaveHousenumber(boolean saveHousenumber)
   {
      this.saveHousenumber = saveHousenumber;
   }

   public String getCountry()
   {
      return country;
   }

   public void setCountry(String country)
   {
      this.country = country;
   }

   public String getCity()
   {
      return city;
   }

   public void setCity(String city)
   {
      this.city = city;
   }

   public String getPostcode()
   {
      return postcode;
   }

   public void setPostcode(String postcode)
   {
      this.postcode = postcode;
   }

   public String getStreet()
   {
      return street;
   }

   public void setStreet(String street)
   {
      this.street = street;
   }

   public String getHousenumber()
   {
      return housenumber;
   }

   public void setHousenumber(String housenumber)
   {
      this.housenumber = housenumber;
   }

   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   public boolean isSaveState()
   {
      return saveState;
   }

   public void setSaveState(boolean saveState)
   {
      this.saveState = saveState;
   }
}
