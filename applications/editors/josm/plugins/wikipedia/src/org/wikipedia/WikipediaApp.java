// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.Predicate;
import org.openstreetmap.josm.tools.Utils;
import org.openstreetmap.josm.tools.Utils.Function;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public final class WikipediaApp {

    private WikipediaApp() {
    }

    static List<WikipediaEntry> getEntriesFromCoordinates(String wikipediaLang, LatLon min, LatLon max) {
        try {
            final String bbox = min.lon() + "," + min.lat() + "," + max.lon() + "," + max.lat();
            // construct url
            final String url = "http://toolserver.org/~kolossos/geoworld/marks.php?"
                    + "bbox=" + bbox + "&LANG=" + wikipediaLang;
            System.out.println("Wikipedia: GET " + url);
            // parse XML document
            final XPathExpression xpathPlacemark = XPathFactory.newInstance().newXPath().compile("//Placemark");
            final XPathExpression xpathName = XPathFactory.newInstance().newXPath().compile("name/text()");
            final XPathExpression xpathCoord = XPathFactory.newInstance().newXPath().compile("Point/coordinates/text()");
            final XPathExpression xpathDescr = XPathFactory.newInstance().newXPath().compile("description");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new URL(url).openStream());
            NodeList nodes = (NodeList) xpathPlacemark.evaluate(doc, XPathConstants.NODESET);
            // construct WikipediaEntry for each XML element
            List<WikipediaEntry> entries = new ArrayList<WikipediaEntry>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                final String[] coord = xpathCoord.evaluate(nodes.item(i)).split(",");
                if (coord.length <= 2) {
                    continue;
                }
                final String name = xpathName.evaluate(nodes.item(i));
                final String descr = xpathDescr.evaluate(nodes.item(i));
                entries.add(new WikipediaEntry(name, descr,
                        new LatLon(Double.parseDouble(coord[1]), Double.parseDouble(coord[0]))));
            }
            return entries;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<WikipediaEntry> getEntriesFromCategory(String wikipediaLang, String category, int depth) {
        try {
            final String url = "http://toolserver.org/~simon04/cats.php"
                    + "?lang=" + wikipediaLang
                    + "&depth=" + depth
                    + "&cat=" + encodeURL(category);
            System.out.println("Wikipedia: GET " + url);
            final Scanner scanner = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\n");
            final List<WikipediaEntry> entries = new ArrayList<WikipediaEntry>();
            while (scanner.hasNext()) {
                final String article = scanner.next().trim().replace("_", " ");
                entries.add(new WikipediaEntry(article, wikipediaLang, article));
            }
            return entries;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<WikipediaEntry> getEntriesFromClipboard(final String wikipediaLang) {
        final List<String> clipboardLines = Arrays.asList(Utils.getClipboardContent().split("[\\n\\r]+"));
        return new ArrayList<WikipediaEntry>(Utils.transform(clipboardLines, new Function<String, WikipediaEntry>() {

            @Override
            public WikipediaEntry apply(String x) {
                return new WikipediaEntry(x, wikipediaLang, x);
            }
        }));
    }

    static void updateWIWOSMStatus(String wikipediaLang, Collection<WikipediaEntry> entries) {
        Collection<String> articleNames = new ArrayList<String>();
        for (WikipediaEntry i : entries) {
            articleNames.add(i.wikipediaArticle);
        }
        Map<String, Boolean> status = new HashMap<String, Boolean>();
        if (!articleNames.isEmpty()) {
            final String url = "http://toolserver.org/~master/osmjson/getGeoJSON.php?action=check"
                    + "&lang=" + wikipediaLang;
            System.out.println("Wikipedia: POST " + url);

            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setDoOutput(true);

                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                out.write("articles=" + encodeURL(Utils.join(",", articleNames)));
                out.close();


                final Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\n");
                while (scanner.hasNext()) {
                    //[article]\t[0|1]
                    final String line = scanner.next();
                    final String[] x = line.split("\t");
                    if (x.length == 2) {
                        status.put(x[0], "1".equals(x[1]));
                    } else {
                        System.err.println("Unknown element " + line);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        for (WikipediaEntry i : entries) {
            i.setWiwosmStatus(status.get(i.wikipediaArticle));
        }
    }

    static Collection<String> getWikipediaArticles(final String wikipediaLang, OsmPrimitive p) {
        final Map<String, String> tags = p.getKeys();
        return Utils.transform(Utils.filter(
                Arrays.asList(
                        WikipediaLangArticle.parseTag("wikipedia", tags.get("wikipedia")),
                        WikipediaLangArticle.parseTag("wikipedia:" + wikipediaLang, tags.get("wikipedia:" + wikipediaLang))
                ), new Predicate<WikipediaLangArticle>() {
            @Override
            public boolean evaluate(WikipediaLangArticle wp) {
                return wp != null && wikipediaLang.equals(wp.lang);
            }
        }), new Function<WikipediaLangArticle, String>() {
            @Override
            public String apply(WikipediaLangArticle wp) {
                return wp.article;
            }
        });
    }

    static Collection<WikipediaLangArticle> getInterwikiArticles(String wikipediaLang, String article) {
        try {
            Collection<WikipediaLangArticle> r = new ArrayList<WikipediaLangArticle>();
            final String url = "http://" + wikipediaLang + ".wikipedia.org/w/api.php" +
                    "?action=query" +
                    "&prop=langlinks" +
                    "&titles=" + URLEncoder.encode(article, "UTF-8") +
                    "&lllimit=500" +
                    "&format=xml";
            System.out.println("Wikipedia: GET " + url);
            final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new URL(url).openStream());
            final NodeList nodes = (NodeList) XPathFactory.newInstance().newXPath().compile("//ll").evaluate(xml, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                final String lang = nodes.item(i).getAttributes().getNamedItem("lang").getTextContent();
                final String name = nodes.item(i).getTextContent();
                r.add(new WikipediaLangArticle(lang, name));
            }
            return r;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static class WikipediaLangArticle {

        final String lang, article;

        public WikipediaLangArticle(String lang, String article) {
            this.lang = lang;
            this.article = article;
        }

        public static WikipediaLangArticle parseFromUrl(String url) {
            if (url == null) {
                return null;
            }
            // decode URL for nicer value
            url = decodeURL(url);
            // extract Wikipedia language and
            final Matcher m = Pattern.compile("https?://(\\w*)\\.wikipedia\\.org/wiki/(.*)").matcher(url);
            if (!m.matches()) {
                return null;
            }
            return new WikipediaLangArticle(m.group(1), m.group(2));
        }

        public static WikipediaLangArticle parseTag(String key, String value) {
            if (value == null) {
                return null;
            } else if (value.startsWith("http")) {
                //wikipedia=http...
                return parseFromUrl(value);
            } else if (value.contains(":")) {
                //wikipedia=[lang]:[article]
                //wikipedia:[lang]=[lang]:[article]
                final String[] item = decodeURL(value).split(":", 2);
                final String article = item[1].replace("_", " ");
                return new WikipediaLangArticle(item[0], article);
            } else if (key.startsWith("wikipedia:")) {
                //wikipedia:[lang]=[lang]:[article]
                //wikipedia:[lang]=[article]
                final String lang = key.split(":", 2)[1];
                final String[] item = decodeURL(value).split(":", 2);
                final String article = item[item.length == 2 ? 1 : 0].replace("_", " ");
                return new WikipediaLangArticle(lang, article);
            } else {
                return null;
            }
        }
    }

    static class WikipediaEntry implements Comparable<WikipediaEntry> {

        final String name, description;
        final String wikipediaLang, wikipediaArticle;
        final LatLon coordinate;
        private Boolean wiwosmStatus;

        public WikipediaEntry(String name, String description, LatLon coordinate) {
            this.name = name;
            this.description = description;
            this.coordinate = coordinate;

            final WikipediaLangArticle wp = WikipediaLangArticle.parseFromUrl(getHrefFromDescription());
            if (wp == null) {
                System.err.println("Could not extract Wikipedia tag from: " + getHrefFromDescription());
            }
            this.wikipediaLang = wp == null ? null : wp.lang;
            this.wikipediaArticle = wp == null ? null : wp.article;
        }

        public WikipediaEntry(String name, String wikipediaLang, String wikipediaArticle) {
            this.name = name;
            this.description = null;
            this.wikipediaLang = wikipediaLang;
            this.wikipediaArticle = wikipediaArticle;
            this.coordinate = null;
        }

        protected final String getHrefFromDescription() {
            if (description == null) {
                return null;
            }
            final Matcher m = Pattern.compile(".*href=\"(.+?)\".*").matcher(description);
            if (m.matches()) {
                return m.group(1);
            } else {
                System.err.println("Could not parse URL from: " + description);
                return null;
            }
        }

        protected final Tag createWikipediaTag() {
            return new Tag("wikipedia", wikipediaLang + ":" + wikipediaArticle);
        }

        private void updateWiwosmStatus() {
            try {
                final String url = "http://toolserver.org/~master/osmjson/getGeoJSON.php?action=check"
                        + "&lang=" + wikipediaLang
                        + "&article=" + encodeURL(wikipediaArticle);
                System.out.println("Wikipedia: GET " + url);
                final Scanner scanner = new Scanner(new URL(url).openStream(), "UTF-8");
                wiwosmStatus = scanner.hasNextInt() && scanner.nextInt() == 1;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void setWiwosmStatus(Boolean wiwosmStatus) {
            this.wiwosmStatus = wiwosmStatus;
        }

        public Boolean getWiwosmStatus() {
            return wiwosmStatus;
        }

        public String getBrowserUrl() {
            if (getHrefFromDescription() != null) {
                return getHrefFromDescription().replace(" ", "_");
            } else {
                return "http://" + wikipediaLang + ".wikipedia.org/wiki/"
                        + encodeURL(wikipediaArticle.replace(" ", "_"));
            }
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(WikipediaEntry o) {
            return name.compareTo(o.name);
        }
    }

    public static String decodeURL(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String encodeURL(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
