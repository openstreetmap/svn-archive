![](http://www.kyralovi.cz/tmp/josm/pointinfo_beta4.png)

---

# PointInfo plugin

This plugin shows all available information for clicked point from external database.
Currently, only Czech RUIAN module is available.


##Author

 * Marián Kyral <mkyral@email.cz>


##Licence:

 * GPL v2 or later


##Used libraries:

* org.json (http://www.json.org/java/index.html) for parse json data.

##Notes:

- Plugin could be easy extend to show another data source.
- Input is position, output html string that is shown on message.
- Optionally you can define special links (file://...) that will be sent back to the module to the performAction method

###The interface:

```java

    /**
     * Get a information about given position from external database.
     * @param pos Position on the map
     */
    public void prepareData(LatLon pos) {
    }

    /**
     * Return Html text representation
     * @return String htmlText
     */
    public String getHtml() {
    }

    /**
     * Perform given action
     *  e.g.: copy tags to clipboard
     * @param act Action to be performed
     */
    public void performAction(String act) {
    }

```


