// License: GPL. See LICENSE file for details./*
package org.wikipedia;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.tools.AlphanumComparator;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Predicate;
import org.openstreetmap.josm.tools.Predicates;
import org.openstreetmap.josm.tools.Utils;
import org.openstreetmap.josm.tools.Utils.Function;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class WikipediaApp {

    public static Pattern WIKIDATA_PATTERN = Pattern.compile("Q\\d+");
    private static final DocumentBuilder DOCUMENT_BUILDER = newDocumentBuilder();
    private static final XPath X_PATH = XPathFactory.newInstance().newXPath();

    private WikipediaApp() {
    }

    static String getMediawikiLocale(Locale locale) {
        if (!locale.getCountry().isEmpty()) {
            return locale.getLanguage() + "-" + locale.getCountry().toLowerCase();
        } else {
            return locale.getLanguage();
        }
    }

    static String getSiteUrl(String wikipediaLang) {
        if ("wikidata".equals(wikipediaLang)) {
            return "https://www.wikidata.org";
        } else {
            return "https://" + wikipediaLang + ".wikipedia.org";
        }
    }

    static List<WikipediaEntry> getEntriesFromCoordinates(String wikipediaLang, LatLon min, LatLon max) {
        try {
            // construct url
            final String url = getSiteUrl(wikipediaLang) + "/w/api.php"
                    + "?action=query"
                    + "&list=geosearch"
                    + "&format=xml"
                    + "&gslimit=500"
                    + "&gsbbox=" + max.lat() + "|" + min.lon() + "|" + min.lat() + "|" + max.lon();
            // parse XML document
            final XPathExpression xpathPlacemark = X_PATH.compile("//gs");
            final XPathExpression xpathName = X_PATH.compile("@title");
            final XPathExpression xpathLat = X_PATH.compile("@lat");
            final XPathExpression xpathLon = X_PATH.compile("@lon");
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document doc = DOCUMENT_BUILDER.parse(in);
                final NodeList nodes = (NodeList) xpathPlacemark.evaluate(doc, XPathConstants.NODESET);
                final List<String> names = new ArrayList<>(nodes.getLength());
                final List<WikipediaEntry> entries = new ArrayList<>(nodes.getLength());
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    final String name = xpathName.evaluate(node);
                    names.add(name);
                    final LatLon latLon = new LatLon((
                            (double) xpathLat.evaluate(node, XPathConstants.NUMBER)),
                            (double) xpathLon.evaluate(node, XPathConstants.NUMBER));
                    if ("wikidata".equals(wikipediaLang)) {
                        entries.add(new WikidataEntry(name, latLon, null));
                    } else {
                        entries.add(new WikipediaEntry(name, wikipediaLang, name, latLon
                        ));
                    }
                }
                if ("wikidata".equals(wikipediaLang)) {
                    final Map<String, String> labels = getLabelForWikidata(names, Locale.getDefault());
                    final List<WikipediaEntry> entriesWithLabel = new ArrayList<>(nodes.getLength());
                    for (WikipediaEntry entry : entries) {
                        entriesWithLabel.add(new WikidataEntry(entry.wikipediaArticle, entry.coordinate, labels.get(entry.wikipediaArticle)));
                    }
                    return entriesWithLabel;
                } else {
                    return entries;
                }
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

            try (final Scanner scanner = new Scanner(
                    HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContentReader())
                    .useDelimiter("\n")) {
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

            try {
                final String requestBody = "articles=" + Utils.encodeUrl(Utils.join(",", articleNames));
                try (final Scanner scanner = new Scanner(
                        HttpClient.create(new URL(url), "POST").setReasonForRequest("Wikipedia")
                                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                                .setRequestBody(requestBody.getBytes(StandardCharsets.UTF_8))
                                .connect().getContentReader())
                        .useDelimiter("\n")) {
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
        if ("wikidata".equals(wikipediaLang)) {
            return Utils.filter(Collections.singleton(p.get("wikidata")), Predicates.not(Predicates.isNull()));
        }
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
            final Map<String, String> r = new TreeMap<>();
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                final NodeList nodes = (NodeList) X_PATH.compile("//entity").evaluate(xml, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    final String wikidata = (String) X_PATH.compile("./@id").evaluate(node, XPathConstants.STRING);
                    final String wikipedia = (String) X_PATH.compile("./sitelinks/sitelink/@title").evaluate(node, XPathConstants.STRING);
                    r.put(wikipedia, wikidata);
                }
            }
            return r;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static String getLabelForWikidata(String wikidataId, Locale locale, String ... preferredLanguage) {
        return getLabelForWikidata(Collections.singleton(wikidataId), locale, preferredLanguage).get(wikidataId);
    }

    static Map<String, String> getLabelForWikidata(Collection<String> wikidataIds, Locale locale, String ... preferredLanguage) {
        try {
            for (final String wikidataId : wikidataIds) {
                ensureValidWikidataId(wikidataId);
            }
            final String url = "https://www.wikidata.org/w/api.php" +
                    "?action=wbgetentities" +
                    "&props=labels" +
                    "&ids=" + Utils.join("|", wikidataIds) +
                    "&format=xml";
            final Collection<String> languages = new ArrayList<>();
            if (locale != null) {
                languages.add(getMediawikiLocale(locale));
                languages.add(getMediawikiLocale(new Locale(locale.getLanguage())));
            }
            languages.addAll(Arrays.asList(preferredLanguage));
            languages.add("en");
            languages.add(null);
            final Map<String, String> r = new HashMap<>();
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                for (final String wikidataId : wikidataIds) {
                    final Node entity = (Node) X_PATH.compile("//entity[@id='" + wikidataId + "']").evaluate(xml, XPathConstants.NODE);
                    for (String language : languages) {
                        final String label = (String) X_PATH.compile(language != null
                                ? "./labels/label[@language='" + language + "']/@value"
                                : "./labels/label/@value"
                        ).evaluate(entity, XPathConstants.STRING);
                        if (label != null && !label.isEmpty()) {
                            r.put(wikidataId, label);
                            break;
                        }
                    }
                }
            }
            return r;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static Collection<WikipediaLangArticle> getInterwikiArticles(String wikipediaLang, String article) {
        try {
            Collection<WikipediaLangArticle> r = new ArrayList<>();
            final String url = getSiteUrl(wikipediaLang) + "/w/api.php" +
                    "?action=query" +
                    "&prop=langlinks" +
                    "&titles=" + Utils.encodeUrl(article) +
                    "&lllimit=500" +
                    "&format=xml";
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                final NodeList nodes = (NodeList) X_PATH.compile("//ll").evaluate(xml, XPathConstants.NODESET);
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

    static LatLon getCoordinateForArticle(String wikipediaLang, String article) {
        try {
            final String url = getSiteUrl(wikipediaLang) + "/w/api.php" +
                    "?action=query" +
                    "&prop=coordinates" +
                    "&titles=" + Utils.encodeUrl(article) +
                    "&format=xml";
            try (final InputStream in = HttpClient.create(new URL(url)).setReasonForRequest("Wikipedia").connect().getContent()) {
                final Document xml = DOCUMENT_BUILDER.parse(in);
                final Node node = (Node) X_PATH.compile("//coordinates/co").evaluate(xml, XPathConstants.NODE);
                if (node == null) {
                    return null;
                } else {
                    final double lat = Double.parseDouble(node.getAttributes().getNamedItem("lat").getTextContent());
                    final double lon = Double.parseDouble(node.getAttributes().getNamedItem("lon").getTextContent());
                    return new LatLon(lat, lon);
                }
            }
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

        public WikipediaEntry(String name, String wikipediaLang, String wikipediaArticle) {
            this(name, wikipediaLang, wikipediaArticle, null);
        }

        public WikipediaEntry(String name, String wikipediaLang, String wikipediaArticle, LatLon coordinate) {
            this.name = name;
            this.wikipediaLang = wikipediaLang;
            this.wikipediaArticle = wikipediaArticle;
            this.coordinate = coordinate;
        }

        protected Tag createWikipediaTag() {
            return new Tag("wikipedia", wikipediaLang + ":" + wikipediaArticle);
        }

        public void setWiwosmStatus(Boolean wiwosmStatus) {
            this.wiwosmStatus = wiwosmStatus;
        }

        public Boolean getWiwosmStatus() {
            return wiwosmStatus;
        }

        public String getBrowserUrl() {
            return getSiteUrl(wikipediaLang) + "/wiki/" + Utils.encodeUrl(wikipediaArticle.replace(" ", "_"));
        }

        public String getLabelText() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(WikipediaEntry o) {
            return AlphanumComparator.getInstance().compare(name, o.name);
        }
    }

    static class WikidataEntry extends WikipediaEntry {

        WikidataEntry(String id, LatLon coordinate, String label) {
            super(label, "wikidata", id, coordinate);
            ensureValidWikidataId(id);
        }

        @Override
        protected Tag createWikipediaTag() {
            return new Tag("wikidata", wikipediaArticle);
        }

        @Override
        public String getLabelText() {
            return getLabelText(name, wikipediaArticle);
        }

        static String getLabelText(String bold, String gray) {
            return Utils.escapeReservedCharactersHTML(bold) + " <span color='gray'>" + Utils.escapeReservedCharactersHTML(gray) + "</span>";
        }
    }

    static void ensureValidWikidataId(String id) {
        CheckParameterUtil.ensureThat(WIKIDATA_PATTERN.matcher(id).matches(), "Invalid Wikidata ID given: " + id);
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

    private static DocumentBuilder newDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            Main.warn("Cannot create DocumentBuilder");
            Main.warn(e);
            throw new RuntimeException(e);
        }
    }
}
