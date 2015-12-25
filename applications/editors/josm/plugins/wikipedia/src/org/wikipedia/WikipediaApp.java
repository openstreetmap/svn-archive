// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Predicate;
import org.openstreetmap.josm.tools.Utils;
import org.openstreetmap.josm.tools.Utils.Function;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class WikipediaApp {

    public static Pattern WIKIDATA_PATTERN = Pattern.compile("Q\\d+");

    private WikipediaApp() {
    }

    static List<WikipediaEntry> getEntriesFromCoordinates(String wikipediaLang, LatLon min, LatLon max) {
        try {
            final String bbox = min.lon() + "," + min.lat() + "," + max.lon() + "," + max.lat();
            // construct url
            final String url = "https://tools.wmflabs.org/wp-world/marks.php?"
                    + "bbox=" + bbox + "&LANG=" + wikipediaLang;
            Main.info("Wikipedia: GET " + url);
            // parse XML document
            final XPathExpression xpathPlacemark = XPathFactory.newInstance().newXPath().compile("//Placemark");
            final XPathExpression xpathName = XPathFactory.newInstance().newXPath().compile("name/text()");
            final XPathExpression xpathCoord = XPathFactory.newInstance().newXPath().compile("Point/coordinates/text()");
            final XPathExpression xpathDescr = XPathFactory.newInstance().newXPath().compile("description");
            try (final InputStream in = Utils.openURL(new URL(url))) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                NodeList nodes = (NodeList) xpathPlacemark.evaluate(doc, XPathConstants.NODESET);
                // construct WikipediaEntry for each XML element
                List<WikipediaEntry> entries = new ArrayList<>(nodes.getLength());
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
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<WikipediaEntry> getEntriesFromCategory(String wikipediaLang, String category, int depth) {
        try {
            final String url = "https://tools.wmflabs.org/cats-php/"
                    + "?lang=" + wikipediaLang
                    + "&depth=" + depth
                    + "&cat=" + Utils.encodeUrl(category);
            Main.info("Wikipedia: GET " + url);
            try (final InputStream in = Utils.openURL(new URL(url));
                 final Scanner scanner = new Scanner(in, "UTF-8").useDelimiter("\n")) {
                final List<WikipediaEntry> entries = new ArrayList<>();
                while (scanner.hasNext()) {
                    final String article = scanner.next().trim().replace("_", " ");
                    entries.add(new WikipediaEntry(article, wikipediaLang, article));
                }
                return entries;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static List<WikipediaEntry> getEntriesFromClipboard(final String wikipediaLang) {
        final List<String> clipboardLines = Arrays.asList(Utils.getClipboardContent().split("[\\n\\r]+"));
        return new ArrayList<>(Utils.transform(clipboardLines, new Function<String, WikipediaEntry>() {

            @Override
            public WikipediaEntry apply(String x) {
                return new WikipediaEntry(x, wikipediaLang, x);
            }
        }));
    }

    static void updateWIWOSMStatus(String wikipediaLang, Collection<WikipediaEntry> entries) {
        Collection<String> articleNames = new ArrayList<>();
        for (WikipediaEntry i : entries) {
            articleNames.add(i.wikipediaArticle);
        }
        Map<String, Boolean> status = new HashMap<>();
        if (!articleNames.isEmpty()) {
            final String url = "https://tools.wmflabs.org/wiwosm/osmjson/getGeoJSON.php?action=check"
                    + "&lang=" + wikipediaLang;
            Main.info("Wikipedia: POST " + url + " " + articleNames);

            try {
                final HttpURLConnection connection = Utils.openHttpConnection(new URL(url));
                connection.setDoOutput(true);

                try (final OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                    out.write("articles=" + Utils.encodeUrl(Utils.join(",", articleNames)));
                }

                try (final Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\n")) {
                    while (scanner.hasNext()) {
                        //[article]\t[0|1]
                        final String line = scanner.next();
                        final String[] x = line.split("\t");
                        if (x.length == 2) {
                            status.put(x[0], "1".equals(x[1]));
                        } else {
                            Main.error("Unknown element " + line);
                        }
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

    /**
     * Returns a map mapping wikipedia articles to wikidata ids.
     */
    static Map<String, String> getWikidataForArticles(String wikipediaLang, Collection<String> articles) {
        try {
            final String url = "https://www.wikidata.org/w/api.php" +
                    "?action=wbgetentities" +
                    "&props=sitelinks" +
                    "&sites=" + wikipediaLang + "wiki" +
                    "&sitefilter=" + wikipediaLang + "wiki" +
                    "&format=xml" +
                    "&titles=" + Utils.join("|", Utils.transform(articles, new Function<String, String>() {
                @Override
                public String apply(String x) {
                    return Utils.encodeUrl(x);
                }
            }));
            Main.info("Wikipedia: GET " + url);
            final Map<String, String> r = new TreeMap<>();
            try (final InputStream in = Utils.openURL(new URL(url))) {
                final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                final NodeList nodes = (NodeList) XPathFactory.newInstance().newXPath().compile("//entity").evaluate(xml, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    final String wikidata = (String) XPathFactory.newInstance().newXPath().compile("./@id").evaluate(node, XPathConstants.STRING);
                    final String wikipedia = (String) XPathFactory.newInstance().newXPath().compile("./sitelinks/sitelink/@title").evaluate(node, XPathConstants.STRING);
                    r.put(wikipedia, wikidata);
                }
            }
            return r;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static String getLabelForWikidata(String wikidataId, String preferredLanguage) {
        try {
            CheckParameterUtil.ensureThat(WIKIDATA_PATTERN.matcher(wikidataId).matches(), "Invalid Wikidata ID given");
            final String url = "https://www.wikidata.org/w/api.php" +
                    "?action=wbgetentities" +
                    "&props=labels" +
                    "&ids=" + wikidataId +
                    "&languages=" + preferredLanguage +
                    "&languagefallback=en" +
                    "&format=xml";
            Main.info("Wikipedia: GET " + url);
            try (final InputStream in = Utils.openURL(new URL(url))) {
                final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                final Node label = (Node) XPathFactory.newInstance().newXPath().compile("//label").evaluate(xml, XPathConstants.NODE);
                if (label == null) {
                    return null;
                } else {
                    return (String) XPathFactory.newInstance().newXPath().compile("./@value").evaluate(label, XPathConstants.STRING);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static Collection<WikipediaLangArticle> getInterwikiArticles(String wikipediaLang, String article) {
        try {
            Collection<WikipediaLangArticle> r = new ArrayList<>();
            final String url = "https://" + wikipediaLang + ".wikipedia.org/w/api.php" +
                    "?action=query" +
                    "&prop=langlinks" +
                    "&titles=" + Utils.encodeUrl(article) +
                    "&lllimit=500" +
                    "&format=xml";
            Main.info("Wikipedia: GET " + url);
            try (final InputStream in = Utils.openURL(new URL(url))) {
                final Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                final NodeList nodes = (NodeList) XPathFactory.newInstance().newXPath().compile("//ll").evaluate(xml, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    final String lang = nodes.item(i).getAttributes().getNamedItem("lang").getTextContent();
                    final String name = nodes.item(i).getTextContent();
                    r.add(new WikipediaLangArticle(lang, name));
                }
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
            url = Utils.decodeUrl(url);
            // extract Wikipedia language and
            final Matcher m = Pattern.compile("(https?:)?//(\\w*)\\.wikipedia\\.org/wiki/(.*)").matcher(url);
            if (!m.matches()) {
                return null;
            }
            return new WikipediaLangArticle(m.group(2), m.group(3));
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
                final String[] item = Utils.decodeUrl(value).split(":", 2);
                final String article = item[1].replace("_", " ");
                return new WikipediaLangArticle(item[0], article);
            } else if (key.startsWith("wikipedia:")) {
                //wikipedia:[lang]=[lang]:[article]
                //wikipedia:[lang]=[article]
                final String lang = key.split(":", 2)[1];
                final String[] item = Utils.decodeUrl(value).split(":", 2);
                final String article = item[item.length == 2 ? 1 : 0].replace("_", " ");
                return new WikipediaLangArticle(lang, article);
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return lang + ":" + article;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final WikipediaLangArticle that = (WikipediaLangArticle) o;
            return Objects.equals(lang, that.lang) &&
                    Objects.equals(article, that.article);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lang, article);
        }
    }

    static class WikipediaEntry implements Comparable<WikipediaEntry> {

        final String name;
        final String wikipediaLang, wikipediaArticle;
        final LatLon coordinate;
        private Boolean wiwosmStatus;

        public WikipediaEntry(String name, String description, LatLon coordinate) {
            this.name = name;
            this.coordinate = coordinate;

            final WikipediaLangArticle wp = WikipediaLangArticle.parseFromUrl(getHrefFromDescription(description));
            if (wp == null) {
                Main.warn("Could not extract Wikipedia tag from: " + getHrefFromDescription(description));
            }
            this.wikipediaLang = wp == null ? null : wp.lang;
            this.wikipediaArticle = wp == null ? null : wp.article;
        }

        public WikipediaEntry(String name, String wikipediaLang, String wikipediaArticle) {
            this.name = name;
            this.wikipediaLang = wikipediaLang;
            this.wikipediaArticle = wikipediaArticle;
            this.coordinate = null;
        }

        protected final String getHrefFromDescription(final String description) {
            if (description == null) {
                return null;
            }
            final Matcher m = Pattern.compile(".*href=\"(.+?)\".*").matcher(description);
            if (m.matches()) {
                return m.group(1);
            } else {
                Main.warn("Could not parse URL from: " + description);
                return null;
            }
        }

        protected final Tag createWikipediaTag() {
            return new Tag("wikipedia", wikipediaLang + ":" + wikipediaArticle);
        }

        private void updateWiwosmStatus() {
            try {
                final String url = "https://tools.wmflabs.org/wiwosm/osmjson/getGeoJSON.php?action=check"
                        + "&lang=" + wikipediaLang
                        + "&article=" + Utils.encodeUrl(wikipediaArticle);
                Main.info("Wikipedia: GET " + url);
                try (final InputStream in = Utils.openURL(new URL(url));
                     final Scanner scanner = new Scanner(in, "UTF-8")) {
                    wiwosmStatus = scanner.hasNextInt() && scanner.nextInt() == 1;
                }
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
            return "https://" + wikipediaLang + ".wikipedia.org/wiki/"
                    + Utils.encodeUrl(wikipediaArticle.replace(" ", "_"));
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

    public static <T> List<List<T>> partitionList(final List<T> list, final int size) {
        return new AbstractList<List<T>>() {
            @Override
            public List<T> get(int index) {
                final int fromIndex = index * size;
                final int toIndex = Math.min(fromIndex + size, list.size());
                return list.subList(fromIndex, toIndex);
            }

            @Override
            public int size() {
                return (int) Math.ceil(((float) list.size()) / size);
            }
        };
    }
}
